package br.com.techgold.judi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DtoCadastroProcesso(
		@NotBlank
		String numeroProcesso,
		@NotNull
		Long clienteId,
		Long funcionarioResponsavelId,
		String tribunal,
		String classeProcessual,
		String assunto,
		String orgaoJulgador,
		String grau,
		LocalDate dataDistribuicao,
		BigDecimal valorCausa,
		String observacoes,
		Boolean monitorado) {
}
