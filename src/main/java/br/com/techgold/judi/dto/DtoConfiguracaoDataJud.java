package br.com.techgold.judi.dto;

/** Nunca expõe a api-key em texto puro — só se ela está configurada e uma versão mascarada, para conferência visual. */
public record DtoConfiguracaoDataJud(
		boolean ativo,
		boolean apiKeyConfigurada,
		String apiKeyMascarada,
		String horarioExecucao) {
}
