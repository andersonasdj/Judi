package br.com.techgold.judi.dto;

import java.math.BigDecimal;
import java.util.List;

public record DtoHonorarioResumo(
		BigDecimal totalHonorarios,
		BigDecimal totalHoras,
		long quantidade,
		long quantidadeSemValorConfigurado,
		List<DtoHonorarioAgrupado> porCliente,
		List<DtoHonorarioAgrupado> porFuncionario,
		List<DtoHonorarioAgrupado> porProcesso,
		List<DtoHonorarioAgrupado> porCaso,
		List<DtoHonorarioMensal> porMes) {
}
