package br.com.techgold.judi.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.AnexoProcesso;

public record DtoAnexoProcesso(
		Long id,
		Long processoId,
		String nomeOriginal,
		String mimeType,
		Long tamanho,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataUpload,
		Long funcionarioId,
		String nomeFuncionario) implements Serializable {

	public DtoAnexoProcesso(AnexoProcesso a) {
		this(a.getId(),
				a.getProcesso().getId(),
				a.getNomeOriginal(),
				a.getMimeType(),
				a.getTamanho(),
				a.getDataUpload(),
				a.getFuncionario().getId(),
				a.getFuncionario().getNomeFuncionario());
	}

}
