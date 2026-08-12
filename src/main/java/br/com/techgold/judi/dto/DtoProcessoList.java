package br.com.techgold.judi.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.Processo;
import br.com.techgold.judi.model.enums.StatusConsultaDataJud;
import br.com.techgold.judi.model.enums.StatusProcesso;

public record DtoProcessoList(
		Long id,
		String numeroProcesso,
		String nomeCliente,
		String nomeFuncionarioResponsavel,
		String tribunal,
		StatusProcesso status,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataUltimaMovimentacao,
		StatusConsultaDataJud statusUltimaConsulta,
		boolean ativo) implements Serializable {

	public DtoProcessoList(Processo p) {
		this(p.getId(),
				p.getNumeroProcesso(),
				p.getCliente() != null ? p.getCliente().getNomeCliente() : null,
				p.getFuncionarioResponsavel() != null ? p.getFuncionarioResponsavel().getNomeFuncionario() : null,
				p.getTribunal(),
				p.getStatus(),
				p.getDataUltimaMovimentacao(),
				p.getStatusUltimaConsulta(),
				p.getAtivo());
	}

}
