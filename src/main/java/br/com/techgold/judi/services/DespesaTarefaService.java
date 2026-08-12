package br.com.techgold.judi.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoCadastroDespesaTarefa;
import br.com.techgold.judi.dto.DtoDespesaTarefa;
import br.com.techgold.judi.model.DespesaTarefa;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.Tarefa;
import br.com.techgold.judi.model.enums.StatusTarefa;
import br.com.techgold.judi.repository.DespesaTarefaRepository;
import br.com.techgold.judi.repository.FuncionarioRepository;
import br.com.techgold.judi.repository.TarefaRepository;

@Service
public class DespesaTarefaService {

	private final DespesaTarefaRepository repository;
	private final TarefaRepository tarefaRepository;
	private final FuncionarioRepository funcionarioRepository;

	DespesaTarefaService(DespesaTarefaRepository repository, TarefaRepository tarefaRepository, FuncionarioRepository funcionarioRepository) {
		this.repository = repository;
		this.tarefaRepository = tarefaRepository;
		this.funcionarioRepository = funcionarioRepository;
	}

	public List<DtoDespesaTarefa> listarPorTarefa(Long tarefaId) {
		return repository.findByTarefaIdOrderByDataDesc(tarefaId).stream().map(DtoDespesaTarefa::new).toList();
	}

	public Page<DtoDespesaTarefa> listarPorFuncionario(Long funcionarioId, Pageable page) {
		return repository.findByFuncionarioIdOrderByDataDesc(funcionarioId, page).map(DtoDespesaTarefa::new);
	}

	public Page<DtoDespesaTarefa> listarPendentes(Pageable page) {
		return repository.findByReembolsadaFalseOrderByDataDesc(page).map(DtoDespesaTarefa::new);
	}

	public DespesaTarefa cadastrar(DtoCadastroDespesaTarefa dados, Funcionario funcionarioLogado) {
		Tarefa tarefa = tarefaRepository.getReferenceById(dados.tarefaId());
		if (tarefa.getStatus().isFinal()) {
			throw new IllegalStateException("Esta tarefa já está " + (tarefa.getStatus() == StatusTarefa.CONCLUIDA ? "concluída" : "cancelada")
					+ " e não aceita mais despesas.");
		}

		DespesaTarefa despesa = new DespesaTarefa();
		despesa.setTarefa(tarefa);
		despesa.setFuncionario(funcionarioRepository.getReferenceById(funcionarioLogado.getId()));
		despesa.setDescricao(dados.descricao());
		despesa.setValor(dados.valor());
		despesa.setData(dados.data());
		despesa.setReembolsada(false);
		despesa.setDataRegistro(LocalDateTime.now().withNano(0));
		return repository.save(despesa);
	}

	public void marcarReembolsada(Long id) {
		DespesaTarefa despesa = repository.getReferenceById(id);
		despesa.setReembolsada(true);
		despesa.setDataReembolso(LocalDateTime.now().withNano(0));
		repository.save(despesa);
	}

	public void excluir(Long id) {
		repository.deleteById(id);
	}

}
