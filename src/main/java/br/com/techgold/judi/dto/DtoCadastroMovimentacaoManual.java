package br.com.techgold.judi.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DtoCadastroMovimentacaoManual(
		@NotNull
		LocalDateTime dataMovimentacao,
		String codigoMovimento,
		@NotBlank
		String descricao,
		String complemento) {
}
