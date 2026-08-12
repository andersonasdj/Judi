package br.com.techgold.judi.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.Funcionario;

public record DtoFuncionarioEdit(
		Long id,
		String nomeFuncionario,
		String username,
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

	public DtoFuncionarioEdit(Funcionario f) {
		this(
				f.getId(),
				f.getNomeFuncionario(),
				f.getUsername(),
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
