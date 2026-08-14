package br.com.techgold.judi.services;

import java.math.BigDecimal;
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

import br.com.techgold.judi.dto.DtoDespesaFinanceiro;
import br.com.techgold.judi.dto.DtoFiltroFinanceiro;
import br.com.techgold.judi.dto.DtoFinanceiroAgrupado;
import br.com.techgold.judi.dto.DtoFinanceiroMensal;
import br.com.techgold.judi.dto.DtoFinanceiroResumo;
import br.com.techgold.judi.model.DespesaTarefa;
import br.com.techgold.judi.repository.DespesaTarefaRepository;
import jakarta.persistence.criteria.Predicate;

/**
 * Consolida as despesas de tarefa (único dado monetário do sistema hoje) em
 * uma visão financeira: totais, agrupamentos por cliente/funcionário/processo/
 * caso e série mensal, todos sujeitos aos mesmos filtros da tela de Financeiro.
 */
@Service
public class FinanceiroService {

	private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MM/yyyy");

	private final DespesaTarefaRepository repository;

	FinanceiroService(DespesaTarefaRepository repository) {
		this.repository = repository;
	}

	public DtoFinanceiroResumo relatorio(DtoFiltroFinanceiro filtro) {
		List<DespesaTarefa> despesas = repository.findAll(especificacao(filtro));

		BigDecimal totalGeral = BigDecimal.ZERO;
		BigDecimal totalReembolsado = BigDecimal.ZERO;
		for (DespesaTarefa d : despesas) {
			BigDecimal valor = d.getValor() != null ? d.getValor() : BigDecimal.ZERO;
			totalGeral = totalGeral.add(valor);
			if (Boolean.TRUE.equals(d.getReembolsada())) {
				totalReembolsado = totalReembolsado.add(valor);
			}
		}
		BigDecimal totalPendente = totalGeral.subtract(totalReembolsado);

		return new DtoFinanceiroResumo(
				totalGeral,
				totalPendente,
				totalReembolsado,
				despesas.size(),
				agrupar(despesas,
						d -> d.getTarefa().getCliente() != null ? d.getTarefa().getCliente().getId() : null,
						d -> d.getTarefa().getCliente() != null ? d.getTarefa().getCliente().getNomeCliente() : null),
				agrupar(despesas,
						d -> d.getFuncionario().getId(),
						d -> d.getFuncionario().getNomeFuncionario()),
				agrupar(despesas,
						d -> d.getTarefa().getProcesso() != null ? d.getTarefa().getProcesso().getId() : null,
						d -> d.getTarefa().getProcesso() != null ? d.getTarefa().getProcesso().getNumeroProcesso() : null),
				agrupar(despesas,
						d -> d.getTarefa().getCaso() != null ? d.getTarefa().getCaso().getId() : null,
						d -> d.getTarefa().getCaso() != null ? d.getTarefa().getCaso().getTitulo() : null),
				agruparPorMes(despesas));
	}

	public Page<DtoDespesaFinanceiro> listar(DtoFiltroFinanceiro filtro, Pageable page) {
		return repository.findAll(especificacao(filtro), page).map(DtoDespesaFinanceiro::new);
	}

	public List<DtoDespesaFinanceiro> listarTudo(DtoFiltroFinanceiro filtro) {
		return repository.findAll(especificacao(filtro), Sort.by(Sort.Direction.DESC, "data")).stream()
				.map(DtoDespesaFinanceiro::new).toList();
	}

	private List<DtoFinanceiroAgrupado> agrupar(List<DespesaTarefa> despesas, Function<DespesaTarefa, Long> idExtrator,
			Function<DespesaTarefa, String> nomeExtrator) {
		Map<Long, Acumulador> mapa = new LinkedHashMap<>();
		for (DespesaTarefa d : despesas) {
			Long id = idExtrator.apply(d);
			if (id == null) {
				continue;
			}
			Acumulador acumulador = mapa.computeIfAbsent(id, chave -> new Acumulador());
			acumulador.nome = nomeExtrator.apply(d);
			acumulador.quantidade++;
			BigDecimal valor = d.getValor() != null ? d.getValor() : BigDecimal.ZERO;
			acumulador.total = acumulador.total.add(valor);
			if (Boolean.TRUE.equals(d.getReembolsada())) {
				acumulador.reembolsado = acumulador.reembolsado.add(valor);
			} else {
				acumulador.pendente = acumulador.pendente.add(valor);
			}
		}

		List<DtoFinanceiroAgrupado> resultado = new ArrayList<>();
		mapa.forEach((id, acumulador) -> resultado.add(new DtoFinanceiroAgrupado(
				id, acumulador.nome, acumulador.quantidade, acumulador.total, acumulador.pendente, acumulador.reembolsado)));
		resultado.sort(Comparator.comparing(DtoFinanceiroAgrupado::total).reversed());
		return resultado;
	}

	private List<DtoFinanceiroMensal> agruparPorMes(List<DespesaTarefa> despesas) {
		Map<YearMonth, BigDecimal> mapa = new TreeMap<>();
		for (DespesaTarefa d : despesas) {
			if (d.getData() == null) {
				continue;
			}
			BigDecimal valor = d.getValor() != null ? d.getValor() : BigDecimal.ZERO;
			mapa.merge(YearMonth.from(d.getData()), valor, BigDecimal::add);
		}

		List<DtoFinanceiroMensal> resultado = new ArrayList<>();
		mapa.forEach((mes, total) -> resultado.add(new DtoFinanceiroMensal(mes.format(FORMATO_MES), total)));
		return resultado;
	}

	private Specification<DespesaTarefa> especificacao(DtoFiltroFinanceiro filtro) {
		return (root, query, cb) -> {
			var tarefa = root.join("tarefa");
			List<Predicate> predicados = new ArrayList<>();

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
				predicados.add(cb.greaterThanOrEqualTo(root.get("data"), filtro.dataInicio()));
			}
			if (filtro.dataFim() != null) {
				predicados.add(cb.lessThanOrEqualTo(root.get("data"), filtro.dataFim()));
			}
			if (filtro.reembolsada() != null) {
				predicados.add(cb.equal(root.get("reembolsada"), filtro.reembolsada()));
			}
			return cb.and(predicados.toArray(new Predicate[0]));
		};
	}

	private static final class Acumulador {
		String nome;
		long quantidade;
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal pendente = BigDecimal.ZERO;
		BigDecimal reembolsado = BigDecimal.ZERO;
	}

}
