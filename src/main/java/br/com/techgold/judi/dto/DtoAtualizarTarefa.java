package br.com.techgold.judi.dto;

import java.time.LocalDateTime;

import br.com.techgold.judi.model.enums.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DtoAtualizarTarefa(
		@NotNull
		Long id,
		@NotBlank
		String titulo,
		String descricao,
		@NotNull
		Long clienteId,
		Long casoId,
		Long processoId,
		Long funcionarioResponsavelId,
		StatusTarefa status,
		LocalDateTime dataAgendamento,
		Boolean ativo) {
}
