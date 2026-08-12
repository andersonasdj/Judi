package br.com.techgold.judi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.techgold.judi.model.AlertaProcesso;

public interface AlertaProcessoRepository extends JpaRepository<AlertaProcesso, Long> {

	public Page<AlertaProcesso> findByLidoFalseOrderByDataGeracaoDesc(Pageable page);

	public Page<AlertaProcesso> findAllByOrderByDataGeracaoDesc(Pageable page);

	public long countByLidoFalse();

	public Page<AlertaProcesso> findByProcessoIdOrderByDataGeracaoDesc(Long processoId, Pageable page);

}
