package br.com.techgold.judi.dto;

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
		Long processoId,
		Long funcionarioResponsavelId,
		StatusTarefa status,
		Boolean ativo) {
}
