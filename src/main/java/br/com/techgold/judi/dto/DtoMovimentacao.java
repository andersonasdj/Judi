package br.com.techgold.judi.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.MovimentacaoProcesso;
import br.com.techgold.judi.model.enums.OrigemMovimentacao;

public record DtoMovimentacao(
		Long id,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataMovimentacao,
		String codigoMovimento,
		String descricao,
		String complemento,
		OrigemMovimentacao origem) implements Serializable {

	public DtoMovimentacao(MovimentacaoProcesso m) {
		this(m.getId(), m.getDataMovimentacao(), m.getCodigoMovimento(), m.getDescricao(), m.getComplemento(), m.getOrigem());
	}

}
