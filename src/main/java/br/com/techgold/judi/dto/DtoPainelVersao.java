package br.com.techgold.judi.dto;

import java.time.LocalDateTime;

/**
 * "Fingerprint" barato de checar (poucas queries de COUNT/MAX, sem joins) usado
 * pelo painel de TV para saber se algo mudou antes de buscar o payload completo
 * de {@link DtoDashboardPainel} — mesmo padrão do lastupdateid do dashboard.html.
 */
public record DtoPainelVersao(
		DtoDashboardResumo resumo,
		Long ultimoAlertaId,
		Long ultimaMovimentacaoId,
		LocalDateTime ultimaTarefaAtualizacao) {
}
