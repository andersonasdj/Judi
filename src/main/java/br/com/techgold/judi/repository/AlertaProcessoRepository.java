package br.com.techgold.judi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.techgold.judi.model.AlertaProcesso;

public interface AlertaProcessoRepository extends JpaRepository<AlertaProcesso, Long> {

	public Page<AlertaProcesso> findByLidoFalseOrderByDataGeracaoDesc(Pageable page);

	public Page<AlertaProcesso> findAllByOrderByDataGeracaoDesc(Pageable page);

	public long countByLidoFalse();

	@Query("SELECT MAX(a.id) FROM AlertaProcesso a")
	public Long maxId();

	public Page<AlertaProcesso> findByProcessoIdOrderByDataGeracaoDesc(Long processoId, Pageable page);

	@Query(value = "SELECT a FROM AlertaProcesso a JOIN a.processo p LEFT JOIN p.cliente c "
			+ "WHERE a.mensagem LIKE CONCAT('%', :conteudo, '%') "
			+ "OR p.numeroProcesso LIKE CONCAT('%', :conteudo, '%') "
			+ "OR c.nomeCliente LIKE CONCAT('%', :conteudo, '%') "
			+ "ORDER BY a.dataGeracao DESC")
	public Page<AlertaProcesso> buscarPorPalavra(Pageable page, String conteudo);

}
