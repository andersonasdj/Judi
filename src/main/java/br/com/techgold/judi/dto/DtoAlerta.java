package br.com.techgold.judi.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.AlertaProcesso;
import br.com.techgold.judi.model.enums.SeveridadeAlerta;
import br.com.techgold.judi.model.enums.TipoAlerta;

public record DtoAlerta(
		Long id,
		Long processoId,
		String numeroProcesso,
		String nomeCliente,
		TipoAlerta tipo,
		SeveridadeAlerta severidade,
		String mensagem,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataGeracao,
		boolean lido) implements Serializable {

	public DtoAlerta(AlertaProcesso a) {
		this(a.getId(),
				a.getProcesso().getId(),
				a.getProcesso().getNumeroProcesso(),
				a.getProcesso().getCliente() != null ? a.getProcesso().getCliente().getNomeCliente() : null,
				a.getTipo(),
				a.getSeveridade(),
				a.getMensagem(),
				a.getDataGeracao(),
				a.getLido());
	}

}
