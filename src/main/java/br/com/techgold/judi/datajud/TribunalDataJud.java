package br.com.techgold.judi.datajud;

import java.util.Locale;
import java.util.Map;

/**
 * Mapeia a sigla do tribunal (cadastrada em {@code Processo.tribunal}) para o
 * alias do endpoint público do DataJud, no formato
 * {@code https://api-publica.datajud.cnj.jus.br/api_publica_<alias>/_search}.
 *
 * Referência: painel de metadados públicos do DataJud/CNJ. Como a integração
 * ainda não foi testada com uma chave de API real (ver {@link DataJudProperties}),
 * revise esta lista ao habilitar a sincronização em produção.
 */
public final class TribunalDataJud {

	private static final Map<String, String> ALIAS_POR_SIGLA = Map.ofEntries(
			// Tribunais Superiores
			Map.entry("STF", "stf"),
			Map.entry("STJ", "stj"),
			Map.entry("TST", "tst"),
			Map.entry("TSE", "tse"),
			Map.entry("STM", "stm"),
			// Justiça Federal
			Map.entry("TRF1", "trf1"),
			Map.entry("TRF2", "trf2"),
			Map.entry("TRF3", "trf3"),
			Map.entry("TRF4", "trf4"),
			Map.entry("TRF5", "trf5"),
			Map.entry("TRF6", "trf6"),
			// Justiça Estadual
			Map.entry("TJAC", "tjac"), Map.entry("TJAL", "tjal"), Map.entry("TJAP", "tjap"),
			Map.entry("TJAM", "tjam"), Map.entry("TJBA", "tjba"), Map.entry("TJCE", "tjce"),
			Map.entry("TJDFT", "tjdft"), Map.entry("TJES", "tjes"), Map.entry("TJGO", "tjgo"),
			Map.entry("TJMA", "tjma"), Map.entry("TJMT", "tjmt"), Map.entry("TJMS", "tjms"),
			Map.entry("TJMG", "tjmg"), Map.entry("TJPA", "tjpa"), Map.entry("TJPB", "tjpb"),
			Map.entry("TJPR", "tjpr"), Map.entry("TJPE", "tjpe"), Map.entry("TJPI", "tjpi"),
			Map.entry("TJRJ", "tjrj"), Map.entry("TJRN", "tjrn"), Map.entry("TJRS", "tjrs"),
			Map.entry("TJRO", "tjro"), Map.entry("TJRR", "tjrr"), Map.entry("TJSC", "tjsc"),
			Map.entry("TJSP", "tjsp"), Map.entry("TJSE", "tjse"), Map.entry("TJTO", "tjto"));

	private TribunalDataJud() {
	}

	public static String alias(String siglaTribunal) {
		if (siglaTribunal == null) {
			return null;
		}
		return ALIAS_POR_SIGLA.get(siglaTribunal.trim().toUpperCase(Locale.ROOT));
	}

}
