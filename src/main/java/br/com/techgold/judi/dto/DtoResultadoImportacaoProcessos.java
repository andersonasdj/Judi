package br.com.techgold.judi.dto;

import java.util.List;

public record DtoResultadoImportacaoProcessos(
		int totalLinhas,
		int importados,
		int duplicados,
		int invalidos,
		List<DtoErroImportacaoProcesso> erros) {
}
