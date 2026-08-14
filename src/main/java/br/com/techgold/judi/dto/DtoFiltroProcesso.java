package br.com.techgold.judi.dto;

import br.com.techgold.judi.model.enums.StatusProcesso;

public record DtoFiltroProcesso(
		String texto,
		Long clienteId,
		Long funcionarioId,
		Long casoId,
		StatusProcesso status) {
}
