package br.com.techgold.judi.dto;

import java.math.BigDecimal;
import java.util.List;

public record DtoFinanceiroResumo(
		BigDecimal totalGeral,
		BigDecimal totalPendente,
		BigDecimal totalReembolsado,
		long quantidade,
		List<DtoFinanceiroAgrupado> porCliente,
		List<DtoFinanceiroAgrupado> porFuncionario,
		List<DtoFinanceiroAgrupado> porProcesso,
		List<DtoFinanceiroAgrupado> porCaso,
		List<DtoFinanceiroMensal> porMes) {
}
