package br.com.techgold.judi.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.techgold.judi.model.Timesheet;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

	public List<Timesheet> findByTarefaIdOrderByDataInicioDesc(Long tarefaId);

	public Optional<Timesheet> findByTarefaIdAndDataFimIsNull(Long tarefaId);

	public Optional<Timesheet> findByFuncionarioIdAndDataFimIsNull(Long funcionarioId);

	public Page<Timesheet> findByFuncionarioIdOrderByDataInicioDesc(Long funcionarioId, Pageable page);

	/**
	 * Verifica se já existe algum timesheet da tarefa cujo intervalo
	 * [dataInicio, dataFim) colide com o intervalo informado — usado para
	 * garantir "um funcionário por vez" também nos lançamentos manuais
	 * (dataFim nula é tratada como "em aberto", ou seja, colide com tudo
	 * que vier depois do início dele).
	 */
	@Query("SELECT COUNT(t) > 0 FROM Timesheet t "
			+ "WHERE t.tarefa.id = :tarefaId "
			+ "AND (:idExcluir IS NULL OR t.id <> :idExcluir) "
			+ "AND t.dataInicio < :dataFim "
			+ "AND (t.dataFim IS NULL OR t.dataFim > :dataInicio)")
	public boolean existeSobreposicao(Long tarefaId, LocalDateTime dataInicio, LocalDateTime dataFim, Long idExcluir);

}
