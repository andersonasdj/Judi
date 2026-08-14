package br.com.techgold.judi.dto;

import java.util.List;

public record DtoDashboardRecentes(
		List<DtoTarefaList> tarefasAtualizadas,
		List<DtoProcessoList> processosCriados,
		List<DtoCasoList> casosCriados,
		List<DtoTarefaList> tarefasAgendadas) {
}
