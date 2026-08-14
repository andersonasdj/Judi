package br.com.techgold.judi.dto;

import br.com.techgold.judi.model.enums.NaturezaCaso;
import br.com.techgold.judi.model.enums.StatusCaso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DtoAtualizarCaso(
		@NotNull
		Long id,
		@NotBlank
		String titulo,
		String descricao,
		@NotNull
		Long clienteId,
		Long funcionarioResponsavelId,
		NaturezaCaso natureza,
		StatusCaso status,
		Boolean ativo) {
}
