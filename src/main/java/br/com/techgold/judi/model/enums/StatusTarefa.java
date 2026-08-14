package br.com.techgold.judi.model.enums;

public enum StatusTarefa {
	ABERTA, AGENDADA, EM_ANDAMENTO, CONCLUIDA, CANCELADA;

	/**
	 * Estado final: a tarefa para de aceitar novos lançamentos de trabalho
	 * (timer, horas manuais, edição/exclusão de timesheets, novas despesas).
	 * Só um ROLE_ADMIN pode tirar a tarefa de um estado final (reabrir).
	 */
	public boolean isFinal() {
		return this == CONCLUIDA || this == CANCELADA;
	}
}
