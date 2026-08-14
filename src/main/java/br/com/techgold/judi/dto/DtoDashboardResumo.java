package br.com.techgold.judi.dto;

import java.util.List;

public record DtoDashboardResumo(
		long totalProcessosAtivos,
		List<DtoContagem> processosPorStatus,
		long alertasNaoLidos,
		long processosComErroConsulta,
		long movimentacoesUltimos7Dias,
		long totalCasosAbertos,
		long totalTarefasAbertas) {
}
