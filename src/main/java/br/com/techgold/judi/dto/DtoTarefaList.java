package br.com.techgold.judi.dto;

import java.io.Serializable;

import br.com.techgold.judi.model.Tarefa;
import br.com.techgold.judi.model.enums.StatusTarefa;

public record DtoTarefaList(
		Long id,
		String titulo,
		String nomeCliente,
		String numeroProcesso,
		String nomeFuncionarioResponsavel,
		StatusTarefa status,
		boolean ativo) implements Serializable {

	public DtoTarefaList(Tarefa t) {
		this(t.getId(),
				t.getTitulo(),
				t.getCliente() != null ? t.getCliente().getNomeCliente() : null,
				t.getProcesso() != null ? t.getProcesso().getNumeroProcesso() : null,
				t.getFuncionarioResponsavel() != null ? t.getFuncionarioResponsavel().getNomeFuncionario() : null,
				t.getStatus(),
				t.getAtivo());
	}

}
