package br.com.techgold.judi.datajud;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Cliente HTTP para a API Pública do DataJud (CNJ).
 *
 * Documentação: https://datajud-wiki.cnj.jus.br/api-publica/
 *
 * A resposta é um retorno padrão do Elasticsearch (hits.hits[]._source), por
 * isso o parsing aqui é feito de forma defensiva via {@link JsonNode} — o
 * layout exato pode variar entre tribunais e ainda não foi validado com uma
 * chave de API real neste projeto.
 */
@Component
public class DataJudClient {

	private final DataJudProperties properties;
	private final ObjectMapper objectMapper;
	private final RestClient restClient;

	DataJudClient(DataJudProperties properties, ObjectMapper objectMapper) {
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.restClient = RestClient.builder().build();
	}

	public List<MovimentoDataJud> buscarMovimentos(String siglaTribunal, String numeroProcesso) {
		String alias = TribunalDataJud.alias(siglaTribunal);
		if (alias == null) {
			throw new DataJudException("Tribunal '" + siglaTribunal + "' não mapeado para a API do DataJud.");
		}

		String numeroLimpo = numeroProcesso == null ? "" : numeroProcesso.replaceAll("\\D", "");
		String url = properties.getBaseUrl() + "/api_publica_" + alias + "/_search";

		ObjectNode corpo = objectMapper.createObjectNode();
		corpo.putObject("query").putObject("match").put("numeroProcesso", numeroLimpo);

		String resposta;
		try {
			resposta = restClient.post()
					.uri(url)
					.header("Authorization", "APIKey " + properties.getApiKey())
					.header("Content-Type", "application/json")
					.body(corpo.toString())
					.retrieve()
					.body(String.class);
		} catch (Exception e) {
			throw new DataJudException("Falha ao consultar o DataJud para o processo " + numeroProcesso + ": " + e.getMessage(), e);
		}

		return extrairMovimentos(resposta);
	}

	private List<MovimentoDataJud> extrairMovimentos(String respostaJson) {
		List<MovimentoDataJud> movimentos = new ArrayList<>();
		try {
			JsonNode raiz = objectMapper.readTree(respostaJson);
			JsonNode hits = raiz.path("hits").path("hits");
			for (JsonNode hit : hits) {
				JsonNode fonte = hit.path("_source");
				for (JsonNode movimento : fonte.path("movimentos")) {
					movimentos.add(converter(movimento));
				}
			}
		} catch (Exception e) {
			throw new DataJudException("Não foi possível interpretar a resposta do DataJud: " + e.getMessage(), e);
		}
		return movimentos;
	}

	private MovimentoDataJud converter(JsonNode movimento) {
		String codigo = movimento.path("codigo").asText(null);
		String nome = movimento.path("nome").asText(null);
		String dataHoraTexto = movimento.path("dataHora").asText(null);

		java.time.LocalDateTime dataHora = null;
		if (dataHoraTexto != null && !dataHoraTexto.isBlank()) {
			try {
				dataHora = Instant.parse(dataHoraTexto).atZone(ZoneId.systemDefault()).toLocalDateTime();
			} catch (DateTimeParseException e) {
				dataHora = null;
			}
		}

		StringJoiner complementos = new StringJoiner("; ");
		for (JsonNode complemento : movimento.path("complementosTabelados")) {
			String descricaoComplemento = complemento.path("nome").asText(null);
			if (descricaoComplemento != null && !descricaoComplemento.isBlank()) {
				complementos.add(descricaoComplemento);
			}
		}

		return new MovimentoDataJud(codigo, nome, dataHora, complementos.length() > 0 ? complementos.toString() : null);
	}

}
