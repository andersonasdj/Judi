package br.com.techgold.judi.dto;

import br.com.techgold.judi.model.enums.NaturezaCaso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DtoCadastroCaso(
		@NotBlank
		String titulo,
		String descricao,
		@NotNull
		Long clienteId,
		Long funcionarioResponsavelId,
		@NotNull
		NaturezaCaso natureza) {
}
