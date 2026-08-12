package br.com.techgold.judi.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.Tarefa;
import br.com.techgold.judi.model.enums.StatusTarefa;

public record DtoTarefaDetalhe(
		Long id,
		String titulo,
		String descricao,
		Long clienteId,
		String nomeCliente,
		Long processoId,
		String numeroProcesso,
		Long funcionarioResponsavelId,
		String nomeFuncionarioResponsavel,
		StatusTarefa status,
		boolean ativo,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataCadastro,
		List<DtoTimesheet> timesheets,
		List<DtoDespesaTarefa> despesas) {

	public DtoTarefaDetalhe(Tarefa t, List<DtoTimesheet> timesheets, List<DtoDespesaTarefa> despesas) {
		this(t.getId(),
				t.getTitulo(),
				t.getDescricao(),
				t.getCliente() != null ? t.getCliente().getId() : null,
				t.getCliente() != null ? t.getCliente().getNomeCliente() : null,
				t.getProcesso() != null ? t.getProcesso().getId() : null,
				t.getProcesso() != null ? t.getProcesso().getNumeroProcesso() : null,
				t.getFuncionarioResponsavel() != null ? t.getFuncionarioResponsavel().getId() : null,
				t.getFuncionarioResponsavel() != null ? t.getFuncionarioResponsavel().getNomeFuncionario() : null,
				t.getStatus(),
				t.getAtivo(),
				t.getDataCadastro(),
				timesheets,
				despesas);
	}

}
