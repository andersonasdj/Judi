package br.com.techgold.judi.dto;

import jakarta.validation.constraints.NotNull;

/** {@code apiKey} em branco/nulo mantém a chave já salva — só troca quando vier preenchida. */
public record DtoSalvarConfiguracaoDataJud(
		@NotNull
		Boolean ativo,
		String apiKey,
		@NotNull
		String horarioExecucao) {
}
