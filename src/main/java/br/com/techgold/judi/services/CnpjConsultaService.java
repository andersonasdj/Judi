package br.com.techgold.judi.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.techgold.judi.dto.DtoConsultaCnpj;

/**
 * Consulta dados públicos de CNPJ na Receita Federal via BrasilAPI
 * (https://brasilapi.com.br/api/cnpj/v1/{cnpj}) — gratuita, sem autenticação.
 * Usada para autopreencher formulários (cliente, parte adversa) a partir do
 * CNPJ digitado. Não existe consulta equivalente para CPF: dados de pessoa
 * física não são expostos publicamente (LGPD), então para CPF só é possível
 * validar o dígito verificador, feito no próprio front-end.
 */
@Service
public class CnpjConsultaService {

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	CnpjConsultaService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.restClient = RestClient.builder().baseUrl("https://brasilapi.com.br/api/cnpj/v1").build();
	}

	public DtoConsultaCnpj consultar(String cnpj) {
		String numeroLimpo = cnpj == null ? "" : cnpj.replaceAll("\\D", "");
		if (numeroLimpo.length() != 14) {
			throw new IllegalStateException("Informe um CNPJ válido, com 14 dígitos.");
		}

		String resposta;
		try {
			resposta = restClient.get().uri("/{cnpj}", numeroLimpo).retrieve().body(String.class);
		} catch (RestClientResponseException e) {
			if (e.getStatusCode().value() == 404) {
				throw new IllegalStateException("CNPJ não encontrado na Receita Federal.");
			}
			throw new IllegalStateException("Falha ao consultar o CNPJ: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new IllegalStateException("Falha ao consultar o CNPJ: " + e.getMessage(), e);
		}

		try {
			JsonNode raiz = objectMapper.readTree(resposta);
			return new DtoConsultaCnpj(
					numeroLimpo,
					texto(raiz, "razao_social"),
					texto(raiz, "nome_fantasia"),
					texto(raiz, "descricao_situacao_cadastral"),
					texto(raiz, "logradouro"),
					texto(raiz, "numero"),
					texto(raiz, "bairro"),
					texto(raiz, "municipio"),
					texto(raiz, "uf"),
					texto(raiz, "cep"),
					texto(raiz, "ddd_telefone_1"));
		} catch (Exception e) {
			throw new IllegalStateException("Não foi possível interpretar a resposta da consulta de CNPJ.", e);
		}
	}

	private String texto(JsonNode raiz, String campo) {
		JsonNode valor = raiz.path(campo);
		return valor.isMissingNode() || valor.isNull() ? null : valor.asText(null);
	}

}
