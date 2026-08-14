package br.com.techgold.judi.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoAtualizarTarefa;
import br.com.techgold.judi.dto.DtoCadastroTarefa;
import br.com.techgold.judi.dto.DtoDespesaTarefa;
import br.com.techgold.judi.dto.DtoFiltroTarefa;
import br.com.techgold.judi.dto.DtoTarefaDetalhe;
import br.com.techgold.judi.dto.DtoTarefaList;
import br.com.techgold.judi.dto.DtoTimesheet;
import br.com.techgold.judi.model.Caso;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.Tarefa;
import br.com.techgold.judi.model.enums.StatusTarefa;
import br.com.techgold.judi.repository.CasoRepository;
import br.com.techgold.judi.repository.ClienteRepository;
import br.com.techgold.judi.repository.DespesaTarefaRepository;
import br.com.techgold.judi.repository.FuncionarioRepository;
import br.com.techgold.judi.repository.ProcessoRepository;
import br.com.techgold.judi.repository.TarefaRepository;
import br.com.techgold.judi.repository.TimesheetRepository;
import jakarta.persistence.criteria.Predicate;

@Service
public class TarefaService {

	private final TarefaRepository repository;
	private final ClienteRepository clienteRepository;
	private final ProcessoRepository processoRepository;
	private final CasoRepository casoRepository;
	private final FuncionarioRepository funcionarioRepository;
	private final TimesheetRepository timesheetRepository;
	private final DespesaTarefaRepository despesaRepository;

	TarefaService(TarefaRepository repository, ClienteRepository clienteRepository, ProcessoRepository processoRepository,
			CasoRepository casoRepository, FuncionarioRepository funcionarioRepository, TimesheetRepository timesheetRepository,
			DespesaTarefaRepository despesaRepository) {
		this.repository = repository;
		this.clienteRepository = clienteRepository;
		this.processoRepository = processoRepository;
		this.casoRepository = casoRepository;
		this.funcionarioRepository = funcionarioRepository;
		this.timesheetRepository = timesheetRepository;
		this.despesaRepository = despesaRepository;
	}

	/** Se um caso for informado, ele precisa pertencer ao mesmo cliente da tarefa. */
	private Caso resolverCaso(Long casoId, Long clienteId) {
		if (casoId == null) {
			return null;
		}
		Caso caso = casoRepository.getReferenceById(casoId);
		if (!caso.getCliente().getId().equals(clienteId)) {
			throw new IllegalStateException("O caso selecionado pertence a outro cliente.");
		}
		return caso;
	}

	public Page<DtoTarefaList> listar(Pageable page) {
		return repository.findByAtivoTrue(page).map(DtoTarefaList::new);
	}

	public Page<DtoTarefaList> listarPorCliente(Long clienteId, Pageable page) {
		return repository.findByClienteIdAndAtivoTrue(clienteId, page).map(DtoTarefaList::new);
	}

	public Page<DtoTarefaList> listarPorProcesso(Long processoId, Pageable page) {
		return repository.findByProcessoIdAndAtivoTrue(processoId, page).map(DtoTarefaList::new);
	}

	public Page<DtoTarefaList> listarPorFuncionario(Long funcionarioId, Pageable page) {
		return repository.findByFuncionarioResponsavelIdAndAtivoTrue(funcionarioId, page).map(DtoTarefaList::new);
	}

	public Page<DtoTarefaList> listarPorCaso(Long casoId, Pageable page) {
		return repository.findByCasoIdAndAtivoTrue(casoId, page).map(DtoTarefaList::new);
	}

	/** Para o card "Últimas tarefas atualizadas" da home. */
	public List<DtoTarefaList> listarUltimasAtualizadas(int limite) {
		return repository.findByAtivoTrueOrderByDataAtualizacaoDesc(PageRequest.of(0, limite))
				.map(DtoTarefaList::new).getContent();
	}

	/** Para o card "Próximos agendamentos" da home — as mais próximas primeiro. */
	public List<DtoTarefaList> listarProximosAgendamentos(int limite) {
		return repository.findByStatusAndAtivoTrueOrderByDataAgendamentoAsc(StatusTarefa.AGENDADA, PageRequest.of(0, limite))
				.map(DtoTarefaList::new).getContent();
	}

	public Page<DtoTarefaList> buscarPorPalavra(Pageable page, String conteudo) {
		return repository.buscarPorPalavra(page, conteudo).map(DtoTarefaList::new);
	}

	/** Filtro rápido combinável da listagem: qualquer combinação de texto (título/cliente), cliente, processo, funcionário e status. */
	public Page<DtoTarefaList> listarFiltrado(DtoFiltroTarefa filtro, Pageable page) {
		return repository.findAll(especificacao(filtro), page).map(DtoTarefaList::new);
	}

	private Specification<Tarefa> especificacao(DtoFiltroTarefa filtro) {
		return (root, query, cb) -> {
			List<Predicate> predicados = new ArrayList<>();
			predicados.add(cb.isTrue(root.get("ativo")));

			if (filtro.texto() != null && !filtro.texto().isBlank()) {
				String termo = "%" + filtro.texto().toLowerCase() + "%";
				predicados.add(cb.or(
						cb.like(cb.lower(root.get("titulo")), termo),
						cb.like(cb.lower(root.get("cliente").get("nomeCliente")), termo)));
			}
			if (filtro.clienteId() != null) {
				predicados.add(cb.equal(root.get("cliente").get("id"), filtro.clienteId()));
			}
			if (filtro.processoId() != null) {
				predicados.add(cb.equal(root.get("processo").get("id"), filtro.processoId()));
			}
			if (filtro.funcionarioId() != null) {
				predicados.add(cb.equal(root.get("funcionarioResponsavel").get("id"), filtro.funcionarioId()));
			}
			if (filtro.status() != null) {
				predicados.add(cb.equal(root.get("status"), filtro.status()));
			}
			return cb.and(predicados.toArray(new Predicate[0]));
		};
	}

	public DtoTarefaDetalhe buscarDetalhe(Long id) {
		Tarefa tarefa = repository.getReferenceById(id);
		List<DtoTimesheet> timesheets = timesheetRepository.findByTarefaIdOrderByDataInicioDesc(id)
				.stream().map(DtoTimesheet::new).toList();
		List<DtoDespesaTarefa> despesas = despesaRepository.findByTarefaIdOrderByDataDesc(id)
				.stream().map(DtoDespesaTarefa::new).toList();
		return new DtoTarefaDetalhe(tarefa, timesheets, despesas);
	}

	public Tarefa buscarEntidade(Long id) {
		return repository.getReferenceById(id);
	}

	public Tarefa cadastrar(DtoCadastroTarefa dados, Funcionario funcionarioLogado) {
		Tarefa tarefa = new Tarefa();
		tarefa.setTitulo(dados.titulo());
		tarefa.setDescricao(dados.descricao());
		tarefa.setCliente(clienteRepository.getReferenceById(dados.clienteId()));
		tarefa.setCaso(resolverCaso(dados.casoId(), dados.clienteId()));
		tarefa.setProcesso(dados.processoId() != null ? processoRepository.getReferenceById(dados.processoId()) : null);
		tarefa.setFuncionarioResponsavel(
				dados.funcionarioResponsavelId() != null ? funcionarioRepository.getReferenceById(dados.funcionarioResponsavelId()) : null);
		tarefa.setStatus(StatusTarefa.ABERTA);
		tarefa.setAtivo(true);
		tarefa.setDataCadastro(LocalDateTime.now().withNano(0));
		tarefa.setDataAtualizacao(LocalDateTime.now().withNano(0));
		tarefa.setAtualizadoPor(funcionarioLogado);
		return repository.save(tarefa);
	}

	public Tarefa atualizar(DtoAtualizarTarefa dados, Funcionario funcionarioLogado) {
		Tarefa tarefa = repository.getReferenceById(dados.id());
		tarefa.setTitulo(dados.titulo());
		tarefa.setDescricao(dados.descricao());
		tarefa.setCliente(clienteRepository.getReferenceById(dados.clienteId()));
		tarefa.setCaso(resolverCaso(dados.casoId(), dados.clienteId()));
		tarefa.setProcesso(dados.processoId() != null ? processoRepository.getReferenceById(dados.processoId()) : null);
		tarefa.setFuncionarioResponsavel(
				dados.funcionarioResponsavelId() != null ? funcionarioRepository.getReferenceById(dados.funcionarioResponsavelId()) : null);
		if (dados.status() != null) {
			if (dados.status() == StatusTarefa.AGENDADA && dados.dataAgendamento() == null) {
				throw new IllegalStateException("Informe a data/hora do agendamento.");
			}
			tarefa.setStatus(dados.status());
		}
		if (dados.dataAgendamento() != null) {
			tarefa.setDataAgendamento(dados.dataAgendamento());
		}
		if (dados.ativo() != null) {
			tarefa.setAtivo(dados.ativo());
		}
		tarefa.setDataAtualizacao(LocalDateTime.now().withNano(0));
		tarefa.setAtualizadoPor(funcionarioLogado);
		return repository.save(tarefa);
	}

	/**
	 * CONCLUIDA/CANCELADA são estados finais: uma vez lá, a tarefa para de
	 * aceitar novos lançamentos de trabalho (ver TimesheetService/
	 * DespesaTarefaService) e só um ROLE_ADMIN pode tirá-la de lá (reabrir).
	 * Também não deixa concluir/cancelar com um timesheet ainda aberto —
	 * finalize o trabalho em andamento antes.
	 */
	public void alterarStatus(Long id, StatusTarefa novoStatus, boolean admin, Funcionario funcionarioLogado) {
		if (novoStatus == StatusTarefa.AGENDADA) {
			throw new IllegalStateException("Para agendar a tarefa, informe a data/hora — use a ação \"Agendar\".");
		}

		Tarefa tarefa = repository.getReferenceById(id);

		if (tarefa.getStatus().isFinal() && novoStatus != tarefa.getStatus() && !admin) {
			throw new IllegalStateException("Esta tarefa já está " + rotulo(tarefa.getStatus()) + ". Apenas um administrador pode reabri-la.");
		}
		if (novoStatus.isFinal() && timesheetRepository.findByTarefaIdAndDataFimIsNull(id).isPresent()) {
			throw new IllegalStateException("Finalize o trabalho em andamento (timer aberto) antes de concluir ou cancelar a tarefa.");
		}

		tarefa.setStatus(novoStatus);
		tarefa.setDataAtualizacao(LocalDateTime.now().withNano(0));
		tarefa.setAtualizadoPor(funcionarioLogado);
		repository.save(tarefa);
	}

	/**
	 * Agenda a tarefa para uma data/hora futura — status vira AGENDADA.
	 * Pensado para, futuramente, alimentar um job que gera alertas de
	 * tarefas agendadas (ainda não implementado).
	 */
	public void agendar(Long id, LocalDateTime dataAgendamento, Funcionario funcionarioLogado) {
		Tarefa tarefa = repository.getReferenceById(id);

		if (tarefa.getStatus().isFinal()) {
			throw new IllegalStateException("Esta tarefa já está " + rotulo(tarefa.getStatus()) + " e não pode ser agendada.");
		}

		tarefa.setStatus(StatusTarefa.AGENDADA);
		tarefa.setDataAgendamento(dataAgendamento);
		tarefa.setDataAtualizacao(LocalDateTime.now().withNano(0));
		tarefa.setAtualizadoPor(funcionarioLogado);
		repository.save(tarefa);
	}

	private String rotulo(StatusTarefa status) {
		return status == StatusTarefa.CONCLUIDA ? "concluída" : "cancelada";
	}

	public void inativar(Long id) {
		Tarefa tarefa = repository.getReferenceById(id);
		tarefa.setAtivo(false);
		repository.save(tarefa);
	}

}
