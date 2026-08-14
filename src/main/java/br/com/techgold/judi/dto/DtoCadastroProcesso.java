package br.com.techgold.judi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.techgold.judi.model.enums.PoloCliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DtoCadastroProcesso(
		@NotBlank
		String numeroProcesso,
		@NotNull
		Long clienteId,
		Long casoId,
		Long funcionarioResponsavelId,
		String tribunal,
		String classeProcessual,
		String assunto,
		String orgaoJulgador,
		String grau,
		String parteAdversa,
		String documentoParteAdversa,
		PoloCliente poloCliente,
		LocalDate dataDistribuicao,
		BigDecimal valorCausa,
		String observacoes,
		Boolean monitorado) {
}
