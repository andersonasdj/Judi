package br.com.techgold.judi.dto;

import java.math.BigDecimal;

public record DtoHonorarioAgrupado(
		Long id,
		String nome,
		long quantidade,
		BigDecimal horas,
		BigDecimal total) {
}
