package br.com.techgold.judi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DtoCadastroTarefa(
		@NotBlank
		String titulo,
		String descricao,
		@NotNull
		Long clienteId,
		Long casoId,
		Long processoId,
		Long funcionarioResponsavelId) {
}
