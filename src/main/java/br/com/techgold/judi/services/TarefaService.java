package br.com.techgold.judi.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoAtualizarTarefa;
import br.com.techgold.judi.dto.DtoCadastroTarefa;
import br.com.techgold.judi.dto.DtoDespesaTarefa;
import br.com.techgold.judi.dto.DtoTarefaDetalhe;
import br.com.techgold.judi.dto.DtoTarefaList;
import br.com.techgold.judi.dto.DtoTimesheet;
import br.com.techgold.judi.model.Tarefa;
import br.com.techgold.judi.model.enums.StatusTarefa;
import br.com.techgold.judi.repository.ClienteRepository;
import br.com.techgold.judi.repository.DespesaTarefaRepository;
import br.com.techgold.judi.repository.FuncionarioRepository;
import br.com.techgold.judi.repository.ProcessoRepository;
import br.com.techgold.judi.repository.TarefaRepository;
import br.com.techgold.judi.repository.TimesheetRepository;

@Service
public class TarefaService {

	private final TarefaRepository repository;
	private final ClienteRepository clienteRepository;
	private final ProcessoRepository processoRepository;
	private final FuncionarioRepository funcionarioRepository;
	private final TimesheetRepository timesheetRepository;
	private final DespesaTarefaRepository despesaRepository;

	TarefaService(TarefaRepository repository, ClienteRepository clienteRepository, ProcessoRepository processoRepository,
			FuncionarioRepository funcionarioRepository, TimesheetRepository timesheetRepository,
			DespesaTarefaRepository despesaRepository) {
		this.repository = repository;
		this.clienteRepository = clienteRepository;
		this.processoRepository = processoRepository;
		this.funcionarioRepository = funcionarioRepository;
		this.timesheetRepository = timesheetRepository;
		this.despesaRepository = despesaRepository;
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

	public Page<DtoTarefaList> buscarPorPalavra(Pageable page, String conteudo) {
		return repository.buscarPorPalavra(page, conteudo).map(DtoTarefaList::new);
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

	public Tarefa cadastrar(DtoCadastroTarefa dados) {
		Tarefa tarefa = new Tarefa();
		tarefa.setTitulo(dados.titulo());
		tarefa.setDescricao(dados.descricao());
		tarefa.setCliente(clienteRepository.getReferenceById(dados.clienteId()));
		tarefa.setProcesso(dados.processoId() != null ? processoRepository.getReferenceById(dados.processoId()) : null);
		tarefa.setFuncionarioResponsavel(
				dados.funcionarioResponsavelId() != null ? funcionarioRepository.getReferenceById(dados.funcionarioResponsavelId()) : null);
		tarefa.setStatus(StatusTarefa.ABERTA);
		tarefa.setAtivo(true);
		tarefa.setDataCadastro(LocalDateTime.now().withNano(0));
		tarefa.setDataAtualizacao(LocalDateTime.now().withNano(0));
		return repository.save(tarefa);
	}

	public Tarefa atualizar(DtoAtualizarTarefa dados) {
		Tarefa tarefa = repository.getReferenceById(dados.id());
		tarefa.setTitulo(dados.titulo());
		tarefa.setDescricao(dados.descricao());
		tarefa.setCliente(clienteRepository.getReferenceById(dados.clienteId()));
		tarefa.setProcesso(dados.processoId() != null ? processoRepository.getReferenceById(dados.processoId()) : null);
		tarefa.setFuncionarioResponsavel(
				dados.funcionarioResponsavelId() != null ? funcionarioRepository.getReferenceById(dados.funcionarioResponsavelId()) : null);
		if (dados.status() != null) {
			tarefa.setStatus(dados.status());
		}
		if (dados.ativo() != null) {
			tarefa.setAtivo(dados.ativo());
		}
		tarefa.setDataAtualizacao(LocalDateTime.now().withNano(0));
		return repository.save(tarefa);
	}

	/**
	 * CONCLUIDA/CANCELADA são estados finais: uma vez lá, a tarefa para de
	 * aceitar novos lançamentos de trabalho (ver TimesheetService/
	 * DespesaTarefaService) e só um ROLE_ADMIN pode tirá-la de lá (reabrir).
	 * Também não deixa concluir/cancelar com um timesheet ainda aberto —
	 * finalize o trabalho em andamento antes.
	 */
	public void alterarStatus(Long id, StatusTarefa novoStatus, boolean admin) {
		Tarefa tarefa = repository.getReferenceById(id);

		if (tarefa.getStatus().isFinal() && novoStatus != tarefa.getStatus() && !admin) {
			throw new IllegalStateException("Esta tarefa já está " + rotulo(tarefa.getStatus()) + ". Apenas um administrador pode reabri-la.");
		}
		if (novoStatus.isFinal() && timesheetRepository.findByTarefaIdAndDataFimIsNull(id).isPresent()) {
			throw new IllegalStateException("Finalize o trabalho em andamento (timer aberto) antes de concluir ou cancelar a tarefa.");
		}

		tarefa.setStatus(novoStatus);
		tarefa.setDataAtualizacao(LocalDateTime.now().withNano(0));
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
