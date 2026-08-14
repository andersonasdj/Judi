package br.com.techgold.judi.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record DtoUsuarioOnline(
		Long id,
		String nome,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime ultimaAtividade) {
}
