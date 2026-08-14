package br.com.techgold.judi.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.DespesaTarefa;

public record DtoDespesaFinanceiro(
		Long id,
		Long tarefaId,
		String tituloTarefa,
		Long clienteId,
		String nomeCliente,
		Long processoId,
		String numeroProcesso,
		Long casoId,
		String tituloCaso,
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
		String comprovanteMimeType) implements Serializable {

	public DtoDespesaFinanceiro(DespesaTarefa d) {
		this(d.getId(),
				d.getTarefa().getId(),
				d.getTarefa().getTitulo(),
				d.getTarefa().getCliente() != null ? d.getTarefa().getCliente().getId() : null,
				d.getTarefa().getCliente() != null ? d.getTarefa().getCliente().getNomeCliente() : null,
				d.getTarefa().getProcesso() != null ? d.getTarefa().getProcesso().getId() : null,
				d.getTarefa().getProcesso() != null ? d.getTarefa().getProcesso().getNumeroProcesso() : null,
				d.getTarefa().getCaso() != null ? d.getTarefa().getCaso().getId() : null,
				d.getTarefa().getCaso() != null ? d.getTarefa().getCaso().getTitulo() : null,
				d.getFuncionario().getId(),
				d.getFuncionario().getNomeFuncionario(),
				d.getDescricao(),
				d.getValor(),
				d.getData(),
				d.getReembolsada(),
				d.getDataReembolso(),
				d.getComprovanteNomeOriginal(),
				d.getComprovanteMimeType());
	}

}
