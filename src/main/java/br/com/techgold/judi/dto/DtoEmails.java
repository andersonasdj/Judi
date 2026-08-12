package br.com.techgold.judi.dto;

import br.com.techgold.judi.model.enums.Agendamentos;

public record DtoEmails(
		Long id,
		Agendamentos agendamento,
		String email,
		boolean status
		) {

}
