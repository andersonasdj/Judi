package br.com.techgold.judi.datajud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuração da integração com a API Pública do DataJud (CNJ).
 * Enquanto {@code datajud.api-key} não estiver preenchida (ver .env), a
 * sincronização fica desabilitada e nenhuma chamada HTTP é realizada —
 * ver {@link DataJudSyncService}.
 */
@Component
public class DataJudProperties {

	@Value("${datajud.enabled:false}")
	private boolean enabled;

	@Value("${datajud.api-key:}")
	private String apiKey;

	@Value("${datajud.base-url:https://api-publica.datajud.cnj.jus.br}")
	private String baseUrl;

	public boolean isConfigurado() {
		return enabled && apiKey != null && !apiKey.isBlank();
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

}
