package br.com.techgold.judi.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.UserRole;

public record DtoFuncionarioAdvancedList(
		Long id,
		String nomeFuncionario,
		String username,
		UserRole role,
		Boolean ativo,
		Boolean mfa,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataAtualizacao,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataAtualizacaoSenha,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataUltimoLogin,
		Boolean trocaSenha,
		String email
		) {

	public DtoFuncionarioAdvancedList(Funcionario f) {
		this(
				f.getId(),
				f.getNomeFuncionario(),
				f.getUsername(),
				f.getRole(),
				f.getAtivo(),
				f.getMfa(),
				f.getDataAtualizacao(),
				f.getDataAtualizacaoSenha(),
				f.getDataUltimoLogin(),
				(f.getTrocaSenha()) != null? f.getTrocaSenha(): false,
				f.getEmail()
				);
	}

}
