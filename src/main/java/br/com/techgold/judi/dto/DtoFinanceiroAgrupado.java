package br.com.techgold.judi.dto;

import java.math.BigDecimal;

public record DtoFinanceiroAgrupado(
		Long id,
		String nome,
		long quantidade,
		BigDecimal total,
		BigDecimal pendente,
		BigDecimal reembolsado) {
}
