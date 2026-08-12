package br.com.techgold.judi.dto;

import br.com.techgold.judi.model.UserRole;
import jakarta.validation.constraints.NotBlank;

public record DtoCadastroFuncionario(
		
		@NotBlank
		String nomeFuncionario,
		@NotBlank
		String username,
		@NotBlank
		String password,
		UserRole role) {

}
