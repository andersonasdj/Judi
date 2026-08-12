package br.com.techgold.judi.dto;

import java.io.Serializable;

import br.com.techgold.judi.model.Cliente;

public record DtoClienteList(
		Long id,
		boolean ativo,
		String nomeCliente

		) implements Serializable {

	public DtoClienteList(Cliente c){
		this(c.getId(), c.getAtivo(), c.getNomeCliente());


	}

}
