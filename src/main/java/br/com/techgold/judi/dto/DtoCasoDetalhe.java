package br.com.techgold.judi.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.Caso;
import br.com.techgold.judi.model.enums.NaturezaCaso;
import br.com.techgold.judi.model.enums.StatusCaso;

public record DtoCasoDetalhe(
		Long id,
		String titulo,
		String descricao,
		Long clienteId,
		String nomeCliente,
		Long funcionarioResponsavelId,
		String nomeFuncionarioResponsavel,
		NaturezaCaso natureza,
		StatusCaso status,
		boolean ativo,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataCadastro,
		List<DtoProcessoList> processos,
		List<DtoTarefaList> tarefas) {

	public DtoCasoDetalhe(Caso c, List<DtoProcessoList> processos, List<DtoTarefaList> tarefas) {
		this(c.getId(),
				c.getTitulo(),
				c.getDescricao(),
				c.getCliente() != null ? c.getCliente().getId() : null,
				c.getCliente() != null ? c.getCliente().getNomeCliente() : null,
				c.getFuncionarioResponsavel() != null ? c.getFuncionarioResponsavel().getId() : null,
				c.getFuncionarioResponsavel() != null ? c.getFuncionarioResponsavel().getNomeFuncionario() : null,
				c.getNatureza(),
				c.getStatus(),
				c.getAtivo(),
				c.getDataCadastro(),
				processos,
				tarefas);
	}

}
