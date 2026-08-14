package br.com.techgold.judi.dto;

/** Dados públicos da Receita Federal para um CNPJ, obtidos via BrasilAPI — usado para autopreencher formulários. */
public record DtoConsultaCnpj(
		String cnpj,
		String razaoSocial,
		String nomeFantasia,
		String situacaoCadastral,
		String logradouro,
		String numero,
		String bairro,
		String municipio,
		String uf,
		String cep,
		String telefone) {
}
