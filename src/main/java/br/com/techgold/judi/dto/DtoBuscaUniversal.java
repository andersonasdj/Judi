package br.com.techgold.judi.dto;

import java.util.List;

public record DtoBuscaUniversal(
		List<DtoCasoList> casos,
		List<DtoProcessoList> processos,
		List<DtoTarefaList> tarefas,
		List<DtoAlerta> alertas) {
}
