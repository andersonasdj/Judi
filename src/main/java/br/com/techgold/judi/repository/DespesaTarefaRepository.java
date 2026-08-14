package br.com.techgold.judi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.techgold.judi.model.DespesaTarefa;

public interface DespesaTarefaRepository extends JpaRepository<DespesaTarefa, Long>, JpaSpecificationExecutor<DespesaTarefa> {

	public List<DespesaTarefa> findByTarefaIdOrderByDataDesc(Long tarefaId);

	public Page<DespesaTarefa> findByFuncionarioIdOrderByDataDesc(Long funcionarioId, Pageable page);

	public Page<DespesaTarefa> findByReembolsadaFalseOrderByDataDesc(Pageable page);

}
