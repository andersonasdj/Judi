package br.com.techgold.judi.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.techgold.judi.model.MovimentacaoProcesso;

public interface MovimentacaoProcessoRepository extends JpaRepository<MovimentacaoProcesso, Long> {

	public List<MovimentacaoProcesso> findByProcessoIdOrderByDataMovimentacaoDesc(Long processoId);

	public boolean existsByProcessoIdAndCodigoMovimentoAndDataMovimentacao(Long processoId, String codigoMovimento, LocalDateTime dataMovimentacao);

	public long countByProcessoId(Long processoId);

	public long countByDataRegistroAfter(LocalDateTime data);

	public List<MovimentacaoProcesso> findTop10ByOrderByDataRegistroDesc();

}
