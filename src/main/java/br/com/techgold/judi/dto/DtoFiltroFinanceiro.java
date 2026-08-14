package br.com.techgold.judi.dto;

import java.time.LocalDate;

public record DtoFiltroFinanceiro(
		Long clienteId,
		Long funcionarioId,
		Long processoId,
		Long casoId,
		LocalDate dataInicio,
		LocalDate dataFim,
		Boolean reembolsada) {
}
