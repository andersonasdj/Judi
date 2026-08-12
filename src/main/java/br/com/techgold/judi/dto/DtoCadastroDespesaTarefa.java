package br.com.techgold.judi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DtoCadastroDespesaTarefa(
		@NotNull
		Long tarefaId,
		@NotBlank
		String descricao,
		@NotNull
		BigDecimal valor,
		@NotNull
		LocalDate data) {
}
