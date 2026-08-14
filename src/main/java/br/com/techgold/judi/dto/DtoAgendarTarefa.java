package br.com.techgold.judi.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record DtoAgendarTarefa(
		@NotNull
		LocalDateTime dataAgendamento) {
}
