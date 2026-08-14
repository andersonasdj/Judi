package br.com.techgold.judi.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import br.com.techgold.judi.model.Tarefa;
import br.com.techgold.judi.model.enums.StatusTarefa;

public interface TarefaRepository extends JpaRepository<Tarefa, Long>, JpaSpecificationExecutor<Tarefa> {

	public Page<Tarefa> findByAtivoTrue(Pageable page);

	public Page<Tarefa> findByClienteIdAndAtivoTrue(Long clienteId, Pageable page);

	public Page<Tarefa> findByProcessoIdAndAtivoTrue(Long processoId, Pageable page);

	public Page<Tarefa> findByCasoIdAndAtivoTrue(Long casoId, Pageable page);

	public Page<Tarefa> findByFuncionarioResponsavelIdAndAtivoTrue(Long funcionarioId, Pageable page);

	public long countByStatusAndAtivoTrue(StatusTarefa status);

	public Page<Tarefa> findByAtivoTrueOrderByDataAtualizacaoDesc(Pageable page);

	public Page<Tarefa> findByStatusAndAtivoTrueOrderByDataAgendamentoAsc(StatusTarefa status, Pageable page);

	@Query("SELECT MAX(t.dataAtualizacao) FROM Tarefa t WHERE t.ativo = true")
	public LocalDateTime maxDataAtualizacao();

	@Query(value = "SELECT t FROM Tarefa t JOIN t.cliente c "
			+ "WHERE t.ativo = true AND (t.titulo LIKE CONCAT('%', :conteudo, '%') "
			+ "OR c.nomeCliente LIKE CONCAT('%', :conteudo, '%')) "
			+ "ORDER BY t.id DESC")
	public Page<Tarefa> buscarPorPalavra(Pageable page, String conteudo);

}
