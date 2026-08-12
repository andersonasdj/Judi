package br.com.techgold.judi.dto;

import java.util.List;

public record DtoDashboardPainel(
		DtoDashboardResumo resumo,
		List<DtoMovimentacaoPainel> ultimasMovimentacoes,
		List<DtoAlerta> ultimosAlertas) {
}
