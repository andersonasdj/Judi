package br.com.techgold.judi.dto;

import java.math.BigDecimal;

import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.UserRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DtoFuncionarioAdvancedEdit(
		Long id,
		UserRole role,
		@NotNull(message = "Informe o valor/hora padrão do funcionário.")
		@Positive(message = "O valor/hora deve ser maior que zero.")
		BigDecimal valorHora
		) {

	public DtoFuncionarioAdvancedEdit(Funcionario f) {
		this(
				f.getId(),
				f.getRole(),
				f.getValorHora()
				);
	}

}
