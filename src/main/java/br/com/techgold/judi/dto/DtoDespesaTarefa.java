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
		Long clienteId,
		String nomeCliente,
		Long funcionarioId,
		String nomeFuncionario,
		String descricao,
		BigDecimal valor,
		@JsonFormat(pattern = "dd/MM/yyyy")
		LocalDate data,
		boolean reembolsada,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataReembolso,
		String comprovanteNomeOriginal,
		String comprovanteMimeType,
		Long comprovanteTamanho) implements Serializable {

	public DtoDespesaTarefa(DespesaTarefa d) {
		this(d.getId(),
				d.getTarefa().getId(),
				d.getTarefa().getTitulo(),
				d.getTarefa().getCliente() != null ? d.getTarefa().getCliente().getId() : null,
				d.getTarefa().getCliente() != null ? d.getTarefa().getCliente().getNomeCliente() : null,
				d.getFuncionario().getId(),
				d.getFuncionario().getNomeFuncionario(),
				d.getDescricao(),
				d.getValor(),
				d.getData(),
				d.getReembolsada(),
				d.getDataReembolso(),
				d.getComprovanteNomeOriginal(),
				d.getComprovanteMimeType(),
				d.getComprovanteTamanho());
	}

}
