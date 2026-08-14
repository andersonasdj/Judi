package br.com.techgold.judi.dto;

import br.com.techgold.judi.model.enums.NaturezaCaso;
import br.com.techgold.judi.model.enums.StatusCaso;

public record DtoFiltroCaso(
		String texto,
		Long clienteId,
		Long funcionarioId,
		NaturezaCaso natureza,
		StatusCaso status) {
}
