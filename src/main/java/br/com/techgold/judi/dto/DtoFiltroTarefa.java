package br.com.techgold.judi.dto;

import br.com.techgold.judi.model.enums.StatusTarefa;

public record DtoFiltroTarefa(
		String texto,
		Long clienteId,
		Long processoId,
		Long funcionarioId,
		StatusTarefa status) {
}
