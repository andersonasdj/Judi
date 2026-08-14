package br.com.techgold.judi.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoFiltroFinanceiro;
import br.com.techgold.judi.dto.DtoHonorario;
import br.com.techgold.judi.dto.DtoHonorarioAgrupado;
import br.com.techgold.judi.dto.DtoHonorarioMensal;
import br.com.techgold.judi.dto.DtoHonorarioResumo;
import br.com.techgold.judi.dto.DtoValorHoraEfetivo;
import br.com.techgold.judi.model.Caso;
import br.com.techgold.judi.model.Cliente;
import br.com.techgold.judi.model.Processo;
import br.com.techgold.judi.model.Tarefa;
import br.com.techgold.judi.model.Timesheet;
import br.com.techgold.judi.repository.TimesheetRepository;
import jakarta.persistence.criteria.Predicate;

/**
 * Calcula os honorários a partir dos lançamentos de timesheet já concluídos:
 * horas trabalhadas × valor/hora efetivo (personalizado do cliente, com
 * fallback para o padrão do funcionário — ver {@link ValorHoraService}).
 */
@Service
public class HonorarioService {

	private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MM/yyyy");

	private final TimesheetRepository repository;
	private final ValorHoraService valorHoraService;

	HonorarioService(TimesheetRepository repository, ValorHoraService valorHoraService) {
		this.repository = repository;
		this.valorHoraService = valorHoraService;
	}

	public DtoHonorarioResumo relatorio(DtoFiltroFinanceiro filtro) {
		List<DtoHonorario> honorarios = repository.findAll(especificacao(filtro)).stream().map(this::calcular).toList();

		BigDecimal totalHoras = BigDecimal.ZERO;
		BigDecimal totalHonorarios = BigDecimal.ZERO;
		long semValorConfigurado = 0;
		for (DtoHonorario h : honorarios) {
			totalHoras = totalHoras.add(h.horas());
			if (h.honorario() != null) {
				totalHonorarios = totalHonorarios.add(h.honorario());
			} else {
				semValorConfigurado++;
			}
		}

		return new DtoHonorarioResumo(
				totalHonorarios,
				totalHoras,
				honorarios.size(),
				semValorConfigurado,
				agrupar(honorarios, DtoHonorario::clienteId, DtoHonorario::nomeCliente),
				agrupar(honorarios, DtoHonorario::funcionarioId, DtoHonorario::nomeFuncionario),
				agrupar(honorarios, DtoHonorario::processoId, DtoHonorario::numeroProcesso),
				agrupar(honorarios, DtoHonorario::casoId, DtoHonorario::tituloCaso),
				agruparPorMes(honorarios));
	}

	public Page<DtoHonorario> listar(DtoFiltroFinanceiro filtro, Pageable page) {
		return repository.findAll(especificacao(filtro), page).map(this::calcular);
	}

	public List<DtoHonorario> listarTudo(DtoFiltroFinanceiro filtro) {
		return repository.findAll(especificacao(filtro), Sort.by(Sort.Direction.DESC, "dataInicio")).stream()
				.map(this::calcular).toList();
	}

	private DtoHonorario calcular(Timesheet t) {
		Tarefa tarefa = t.getTarefa();
		Cliente cliente = tarefa.getCliente();
		Processo processo = tarefa.getProcesso();
		Caso caso = tarefa.getCaso();
		Long funcionarioId = t.getFuncionario().getId();
		Long clienteId = cliente != null ? cliente.getId() : null;

		BigDecimal horas = BigDecimal.valueOf(Duration.between(t.getDataInicio(), t.getDataFim()).toMinutes())
				.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

		DtoValorHoraEfetivo efetivo = clienteId != null ? valorHoraService.efetivoComOrigem(clienteId, funcionarioId) : null;
		BigDecimal valorHora = efetivo != null ? efetivo.valor() : null;
		BigDecimal honorario = valorHora != null ? horas.multiply(valorHora).setScale(2, RoundingMode.HALF_UP) : null;

		return new DtoHonorario(
				t.getId(),
				tarefa.getId(), tarefa.getTitulo(),
				clienteId, cliente != null ? cliente.getNomeCliente() : null,
				processo != null ? processo.getId() : null, processo != null ? processo.getNumeroProcesso() : null,
				caso != null ? caso.getId() : null, caso != null ? caso.getTitulo() : null,
				funcionarioId, t.getFuncionario().getNomeFuncionario(),
				t.getDataInicio(), t.getDataFim(),
				horas, valorHora, efetivo != null && efetivo.personalizado(), honorario);
	}

	private List<DtoHonorarioAgrupado> agrupar(List<DtoHonorario> honorarios, Function<DtoHonorario, Long> idExtrator, Function<DtoHonorario, String> nomeExtrator) {
		Map<Long, Acumulador> mapa = new LinkedHashMap<>();
		for (DtoHonorario h : honorarios) {
			Long id = idExtrator.apply(h);
			if (id == null) {
				continue;
			}
			Acumulador acumulador = mapa.computeIfAbsent(id, chave -> new Acumulador());
			acumulador.nome = nomeExtrator.apply(h);
			acumulador.quantidade++;
			acumulador.horas = acumulador.horas.add(h.horas());
			if (h.honorario() != null) {
				acumulador.total = acumulador.total.add(h.honorario());
			}
		}

		List<DtoHonorarioAgrupado> resultado = new ArrayList<>();
		mapa.forEach((id, acumulador) -> resultado.add(new DtoHonorarioAgrupado(id, acumulador.nome, acumulador.quantidade, acumulador.horas, acumulador.total)));
		resultado.sort(Comparator.comparing(DtoHonorarioAgrupado::total).reversed());
		return resultado;
	}

	private List<DtoHonorarioMensal> agruparPorMes(List<DtoHonorario> honorarios) {
		Map<YearMonth, BigDecimal[]> mapa = new TreeMap<>();
		for (DtoHonorario h : honorarios) {
			YearMonth mes = YearMonth.from(h.dataInicio());
			BigDecimal[] acumulado = mapa.computeIfAbsent(mes, chave -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
			acumulado[0] = acumulado[0].add(h.horas());
			if (h.honorario() != null) {
				acumulado[1] = acumulado[1].add(h.honorario());
			}
		}

		List<DtoHonorarioMensal> resultado = new ArrayList<>();
		mapa.forEach((mes, acumulado) -> resultado.add(new DtoHonorarioMensal(mes.format(FORMATO_MES), acumulado[0], acumulado[1])));
		return resultado;
	}

	private Specification<Timesheet> especificacao(DtoFiltroFinanceiro filtro) {
		return (root, query, cb) -> {
			var tarefa = root.join("tarefa");
			List<Predicate> predicados = new ArrayList<>();
			predicados.add(cb.isNotNull(root.get("dataFim")));

			if (filtro.clienteId() != null) {
				predicados.add(cb.equal(tarefa.get("cliente").get("id"), filtro.clienteId()));
			}
			if (filtro.funcionarioId() != null) {
				predicados.add(cb.equal(root.get("funcionario").get("id"), filtro.funcionarioId()));
			}
			if (filtro.processoId() != null) {
				predicados.add(cb.equal(tarefa.get("processo").get("id"), filtro.processoId()));
			}
			if (filtro.casoId() != null) {
				predicados.add(cb.equal(tarefa.get("caso").get("id"), filtro.casoId()));
			}
			if (filtro.dataInicio() != null) {
				predicados.add(cb.greaterThanOrEqualTo(root.get("dataInicio"), filtro.dataInicio().atStartOfDay()));
			}
			if (filtro.dataFim() != null) {
				predicados.add(cb.lessThanOrEqualTo(root.get("dataInicio"), filtro.dataFim().atTime(23, 59, 59)));
			}
			return cb.and(predicados.toArray(new Predicate[0]));
		};
	}

	private static final class Acumulador {
		String nome;
		long quantidade;
		BigDecimal horas = BigDecimal.ZERO;
		BigDecimal total = BigDecimal.ZERO;
	}

}
