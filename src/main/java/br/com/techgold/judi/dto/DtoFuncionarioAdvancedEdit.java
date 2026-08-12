package br.com.techgold.judi.dto;

import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.UserRole;

public record DtoFuncionarioAdvancedEdit(
		Long id,
		UserRole role
		) {

	public DtoFuncionarioAdvancedEdit(Funcionario f) {
		this(
				f.getId(),
				f.getRole()
				);
	}

}
