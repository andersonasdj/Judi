package br.com.techgold.judi.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record DtoCadastroTimesheetManual(
		@NotNull
		Long tarefaId,
		@NotNull
		LocalDateTime dataInicio,
		@NotNull
		LocalDateTime dataFim,
		String observacoes) {
}
