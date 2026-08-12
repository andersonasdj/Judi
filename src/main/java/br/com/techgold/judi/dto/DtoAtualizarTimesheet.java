package br.com.techgold.judi.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record DtoAtualizarTimesheet(
		@NotNull
		LocalDateTime dataInicio,
		LocalDateTime dataFim,
		String observacoes) {
}
