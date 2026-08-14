package br.com.techgold.judi.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Honorário calculado de um lançamento de timesheet: horas trabalhadas
 * (dataFim - dataInicio) multiplicadas pelo valor/hora efetivo do par
 * cliente/funcionário (personalizado do cliente, ou padrão do funcionário).
 * Lançamentos em aberto (sem dataFim) não geram honorário ainda.
 */
public record DtoHonorario(
		Long timesheetId,
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
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataInicio,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataFim,
		BigDecimal horas,
		BigDecimal valorHora,
		boolean valorPersonalizado,
		BigDecimal honorario) implements Serializable {
}
