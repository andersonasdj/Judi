package br.com.techgold.judi.datajud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Endereço base da API Pública do DataJud (CNJ) — deployment estável, por
 * isso continua em application.properties. Ativação e api-key ficam em
 * {@code ConfiguracaoDataJudService}, editáveis em Configurações →
 * Integração DataJud.
 */
@Component
public class DataJudProperties {

	@Value("${datajud.base-url:https://api-publica.datajud.cnj.jus.br}")
	private String baseUrl;

	public String getBaseUrl() {
		return baseUrl;
	}

}
