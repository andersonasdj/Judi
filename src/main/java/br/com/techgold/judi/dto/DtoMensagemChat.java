package br.com.techgold.judi.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.MensagemChat;

public record DtoMensagemChat(
		Long id,
		Long remetenteId,
		String nomeRemetente,
		Long destinatarioId,
		String nomeDestinatario,
		String conteudo,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataEnvio,
		boolean lida) implements Serializable {

	public DtoMensagemChat(MensagemChat m) {
		this(m.getId(),
				m.getRemetente().getId(),
				m.getRemetente().getNomeFuncionario(),
				m.getDestinatario().getId(),
				m.getDestinatario().getNomeFuncionario(),
				m.getConteudo(),
				m.getDataEnvio(),
				m.isLida());
	}

}
