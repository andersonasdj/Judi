package br.com.techgold.judi.dto;

public record DtoFuncionarioHome(
		String nomeFuncionario,
		String dataUltimoLogin,
		Boolean trocaSenha,
		Long id
		) {
}
