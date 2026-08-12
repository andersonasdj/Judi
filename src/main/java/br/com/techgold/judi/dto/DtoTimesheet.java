package br.com.techgold.judi.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.Timesheet;
import br.com.techgold.judi.model.enums.OrigemTimesheet;

public record DtoTimesheet(
		Long id,
		Long tarefaId,
		Long funcionarioId,
		String nomeFuncionario,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataInicio,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataFim,
		Long duracaoMinutos,
		OrigemTimesheet origem,
		String observacoes) implements Serializable {

	public DtoTimesheet(Timesheet t) {
		this(t.getId(),
				t.getTarefa().getId(),
				t.getFuncionario().getId(),
				t.getFuncionario().getNomeFuncionario(),
				t.getDataInicio(),
				t.getDataFim(),
				t.getDataFim() != null ? ChronoUnit.MINUTES.between(t.getDataInicio(), t.getDataFim()) : null,
				t.getOrigem(),
				t.getObservacoes());
	}

}
