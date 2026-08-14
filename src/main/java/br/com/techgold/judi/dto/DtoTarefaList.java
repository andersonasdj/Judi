package br.com.techgold.judi.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.Tarefa;
import br.com.techgold.judi.model.enums.StatusTarefa;

public record DtoTarefaList(
		Long id,
		String titulo,
		String nomeCliente,
		Long casoId,
		String tituloCaso,
		String numeroProcesso,
		String nomeFuncionarioResponsavel,
		StatusTarefa status,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataAgendamento,
		boolean ativo,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataAtualizacao,
		String nomeAtualizadoPor) implements Serializable {

	public DtoTarefaList(Tarefa t) {
		this(t.getId(),
				t.getTitulo(),
				t.getCliente() != null ? t.getCliente().getNomeCliente() : null,
				t.getCaso() != null ? t.getCaso().getId() : null,
				t.getCaso() != null ? t.getCaso().getTitulo() : null,
				t.getProcesso() != null ? t.getProcesso().getNumeroProcesso() : null,
				t.getFuncionarioResponsavel() != null ? t.getFuncionarioResponsavel().getNomeFuncionario() : null,
				t.getStatus(),
				t.getDataAgendamento(),
				t.getAtivo(),
				t.getDataAtualizacao(),
				t.getAtualizadoPor() != null ? t.getAtualizadoPor().getNomeFuncionario() : null);
	}

}
