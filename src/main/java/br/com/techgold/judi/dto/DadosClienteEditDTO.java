package br.com.techgold.judi.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.Cliente;

public record DadosClienteEditDTO(
		Long id,
		Boolean ativo,
		String nomeCliente,
		String endereco,
		String telefone,
		String username,
		String password,
		String cnpj,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataUltimoLogin,
		String nomeFantasia,
		String tipoPessoa,
		String inscricaoEstadual,
		String inscricaoMunicipal
		) {

	public DadosClienteEditDTO(Cliente c) {
		this(c.getId(),
				c.getAtivo(),
				c.getNomeCliente(),
				c.getEndereco(),
				c.getTelefone(),
				c.getUsername(),
				c.getPassword(),
				c.getCnpj(),
				c.getDataUltimoLogin(),
				c.getNomeFantasia(),
				c.getTipoPessoa(),
				c.getInscricaoEstadual(),
				c.getInscricaoMunicipal()
		);
	}
}
