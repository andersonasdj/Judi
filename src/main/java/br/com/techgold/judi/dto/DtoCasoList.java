package br.com.techgold.judi.dto;

import java.io.Serializable;

import br.com.techgold.judi.model.Caso;
import br.com.techgold.judi.model.enums.NaturezaCaso;
import br.com.techgold.judi.model.enums.StatusCaso;

public record DtoCasoList(
		Long id,
		String titulo,
		String nomeCliente,
		NaturezaCaso natureza,
		StatusCaso status,
		String nomeFuncionarioResponsavel,
		boolean ativo,
		long totalProcessos) implements Serializable {

	public DtoCasoList(Caso c, long totalProcessos) {
		this(c.getId(),
				c.getTitulo(),
				c.getCliente() != null ? c.getCliente().getNomeCliente() : null,
				c.getNatureza(),
				c.getStatus(),
				c.getFuncionarioResponsavel() != null ? c.getFuncionarioResponsavel().getNomeFuncionario() : null,
				c.getAtivo(),
				totalProcessos);
	}

}
