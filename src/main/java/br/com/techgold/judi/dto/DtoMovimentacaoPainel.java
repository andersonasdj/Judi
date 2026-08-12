package br.com.techgold.judi.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.MovimentacaoProcesso;
import br.com.techgold.judi.model.enums.OrigemMovimentacao;

public record DtoMovimentacaoPainel(
		Long processoId,
		String numeroProcesso,
		String nomeCliente,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataMovimentacao,
		String descricao,
		OrigemMovimentacao origem) implements Serializable {

	public DtoMovimentacaoPainel(MovimentacaoProcesso m) {
		this(m.getProcesso().getId(),
				m.getProcesso().getNumeroProcesso(),
				m.getProcesso().getCliente() != null ? m.getProcesso().getCliente().getNomeCliente() : null,
				m.getDataMovimentacao(),
				m.getDescricao(),
				m.getOrigem());
	}

}
