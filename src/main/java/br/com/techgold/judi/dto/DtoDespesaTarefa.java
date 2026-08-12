package br.com.techgold.judi.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.DespesaTarefa;

public record DtoDespesaTarefa(
		Long id,
		Long tarefaId,
		String tituloTarefa,
		Long funcionarioId,
		String nomeFuncionario,
		String descricao,
		BigDecimal valor,
		@JsonFormat(pattern = "dd/MM/yyyy")
		LocalDate data,
		boolean reembolsada,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataReembolso) implements Serializable {

	public DtoDespesaTarefa(DespesaTarefa d) {
		this(d.getId(),
				d.getTarefa().getId(),
				d.getTarefa().getTitulo(),
				d.getFuncionario().getId(),
				d.getFuncionario().getNomeFuncionario(),
				d.getDescricao(),
				d.getValor(),
				d.getData(),
				d.getReembolsada(),
				d.getDataReembolso());
	}

}
