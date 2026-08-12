package br.com.techgold.judi.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoAtualizarTimesheet;
import br.com.techgold.judi.dto.DtoCadastroTimesheetManual;
import br.com.techgold.judi.dto.DtoTimesheet;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.Tarefa;
import br.com.techgold.judi.model.Timesheet;
import br.com.techgold.judi.model.UserRole;
import br.com.techgold.judi.model.enums.OrigemTimesheet;
import br.com.techgold.judi.model.enums.StatusTarefa;
import br.com.techgold.judi.repository.FuncionarioRepository;
import br.com.techgold.judi.repository.TarefaRepository;
import br.com.techgold.judi.repository.TimesheetRepository;

/**
 * Garante a regra de "um funcionário por vez" numa tarefa: só pode existir
 * um timesheet em aberto (sem data fim) por tarefa, e nenhum lançamento
 * (nem manual) pode se sobrepor a outro já registrado para a mesma tarefa.
 */
@Service
public class TimesheetService {

	private final TimesheetRepository repository;
	private final TarefaRepository tarefaRepository;
	private final FuncionarioRepository funcionarioRepository;

	TimesheetService(TimesheetRepository repository, TarefaRepository tarefaRepository, FuncionarioRepository funcionarioRepository) {
		this.repository = repository;
		this.tarefaRepository = tarefaRepository;
		this.funcionarioRepository = funcionarioRepository;
	}

	public List<DtoTimesheet> listarPorTarefa(Long tarefaId) {
		return repository.findByTarefaIdOrderByDataInicioDesc(tarefaId).stream().map(DtoTimesheet::new).toList();
	}

	public Page<DtoTimesheet> listarPorFuncionario(Long funcionarioId, Pageable page) {
		return repository.findByFuncionarioIdOrderByDataInicioDesc(funcionarioId, page).map(DtoTimesheet::new);
	}

	public DtoTimesheet buscarAbertoPorFuncionario(Long funcionarioId) {
		return repository.findByFuncionarioIdAndDataFimIsNull(funcionarioId).map(DtoTimesheet::new).orElse(null);
	}

	public Timesheet iniciar(Long tarefaId, Funcionario funcionarioLogado, String observacoes) {
		Tarefa tarefa = tarefaRepository.getReferenceById(tarefaId);
		Funcionario funcionario = funcionarioRepository.getReferenceById(funcionarioLogado.getId());

		validarNaoFinalizada(tarefa);

		if (repository.findByTarefaIdAndDataFimIsNull(tarefaId).isPresent()) {
			throw new IllegalStateException("Esta tarefa já está sendo trabalhada por outro funcionário no momento.");
		}

		LocalDateTime agora = LocalDateTime.now().withNano(0);

		Timesheet timesheet = new Timesheet();
		timesheet.setTarefa(tarefa);
		timesheet.setFuncionario(funcionario);
		timesheet.setDataInicio(agora);
		timesheet.setOrigem(OrigemTimesheet.TIMER);
		timesheet.setObservacoes(observacoes);
		timesheet.setDataRegistro(agora);
		Timesheet salvo = repository.save(timesheet);

		tarefa.setFuncionarioResponsavel(funcionario);
		tarefa.setStatus(StatusTarefa.EM_ANDAMENTO);
		tarefa.setDataAtualizacao(agora);
		tarefaRepository.save(tarefa);

		return salvo;
	}

	public Timesheet finalizar(Long timesheetId, Funcionario funcionario) {
		Timesheet timesheet = repository.getReferenceById(timesheetId);

		if (!timesheet.getFuncionario().getId().equals(funcionario.getId())) {
			throw new IllegalStateException("Somente quem iniciou o timesheet pode finalizá-lo.");
		}
		if (timesheet.getDataFim() != null) {
			throw new IllegalStateException("Este timesheet já foi finalizado.");
		}

		LocalDateTime agora = LocalDateTime.now().withNano(0);
		timesheet.setDataFim(agora);
		Timesheet salvo = repository.save(timesheet);

		Tarefa tarefa = timesheet.getTarefa();
		if (tarefa.getStatus() == StatusTarefa.EM_ANDAMENTO) {
			tarefa.setStatus(StatusTarefa.ABERTA);
		}
		tarefa.setDataAtualizacao(agora);
		tarefaRepository.save(tarefa);

		return salvo;
	}

	public Timesheet registrarManual(DtoCadastroTimesheetManual dados, Funcionario funcionarioLogado) {
		if (!dados.dataFim().isAfter(dados.dataInicio())) {
			throw new IllegalStateException("A data de fim deve ser posterior à data de início.");
		}
		if (repository.existeSobreposicao(dados.tarefaId(), dados.dataInicio(), dados.dataFim(), null)) {
			throw new IllegalStateException("Já existe um lançamento de outro funcionário nesse período para esta tarefa.");
		}

		Tarefa tarefa = tarefaRepository.getReferenceById(dados.tarefaId());
		Funcionario funcionario = funcionarioRepository.getReferenceById(funcionarioLogado.getId());

		validarNaoFinalizada(tarefa);

		Timesheet timesheet = new Timesheet();
		timesheet.setTarefa(tarefa);
		timesheet.setFuncionario(funcionario);
		timesheet.setDataInicio(dados.dataInicio());
		timesheet.setDataFim(dados.dataFim());
		timesheet.setOrigem(OrigemTimesheet.MANUAL);
		timesheet.setObservacoes(dados.observacoes());
		timesheet.setDataRegistro(LocalDateTime.now().withNano(0));
		return repository.save(timesheet);
	}

	/**
	 * Corrige um lançamento já existente (início, fim, observações). Não é
	 * possível, por aqui, transformar um timesheet aberto em fechado nem o
	 * contrário — isso continua sendo função de {@link #iniciar} e
	 * {@link #finalizar}, então o estado aberto/fechado original é preservado.
	 */
	public Timesheet atualizar(Long id, DtoAtualizarTimesheet dados, Funcionario funcionarioLogado) {
		Timesheet timesheet = repository.getReferenceById(id);
		validarPermissao(timesheet, funcionarioLogado);
		validarNaoFinalizada(timesheet.getTarefa());

		boolean estavaAberto = timesheet.getDataFim() == null;
		LocalDateTime novoFim = estavaAberto ? null : dados.dataFim();

		if (!estavaAberto) {
			if (novoFim == null || !novoFim.isAfter(dados.dataInicio())) {
				throw new IllegalStateException("A data de fim deve ser posterior à data de início.");
			}
			if (repository.existeSobreposicao(timesheet.getTarefa().getId(), dados.dataInicio(), novoFim, id)) {
				throw new IllegalStateException("Esse período se sobrepõe a outro lançamento já existente da tarefa.");
			}
		}

		timesheet.setDataInicio(dados.dataInicio());
		timesheet.setDataFim(novoFim);
		timesheet.setObservacoes(dados.observacoes());
		return repository.save(timesheet);
	}

	public void excluir(Long id, Funcionario funcionarioLogado) {
		Timesheet timesheet = repository.getReferenceById(id);
		validarPermissao(timesheet, funcionarioLogado);
		validarNaoFinalizada(timesheet.getTarefa());

		boolean estavaAberto = timesheet.getDataFim() == null;
		Tarefa tarefa = timesheet.getTarefa();
		repository.delete(timesheet);

		if (estavaAberto && tarefa.getStatus() == StatusTarefa.EM_ANDAMENTO) {
			tarefa.setStatus(StatusTarefa.ABERTA);
			tarefa.setDataAtualizacao(LocalDateTime.now().withNano(0));
			tarefaRepository.save(tarefa);
		}
	}

	private void validarNaoFinalizada(Tarefa tarefa) {
		if (tarefa.getStatus().isFinal()) {
			throw new IllegalStateException("Esta tarefa já está " + (tarefa.getStatus() == StatusTarefa.CONCLUIDA ? "concluída" : "cancelada")
					+ " e não aceita mais lançamentos de horas.");
		}
	}

	private void validarPermissao(Timesheet timesheet, Funcionario funcionarioLogado) {
		boolean admin = funcionarioLogado.getRole() == UserRole.ADMIN || funcionarioLogado.getRole() == UserRole.SADMIN;
		if (!admin && !timesheet.getFuncionario().getId().equals(funcionarioLogado.getId())) {
			throw new IllegalStateException("Você só pode editar ou excluir seus próprios lançamentos.");
		}
	}

}
