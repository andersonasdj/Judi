package br.com.techgold.judi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.techgold.judi.model.enums.StatusProcesso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DtoAtualizarProcesso(
		@NotNull
		Long id,
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
		StatusProcesso status,
		LocalDate dataDistribuicao,
		BigDecimal valorCausa,
		String observacoes,
		Boolean monitorado,
		Boolean ativo) {
}
