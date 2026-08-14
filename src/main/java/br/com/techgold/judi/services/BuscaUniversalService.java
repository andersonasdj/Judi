package br.com.techgold.judi.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoBuscaUniversal;

/**
 * Agrega a busca por palavra já existente em Caso/Processo/Tarefa/Alerta
 * numa única chamada, para alimentar a busca universal da home.
 */
@Service
public class BuscaUniversalService {

	private static final int LIMITE_POR_CATEGORIA = 5;

	private final CasoService casoService;
	private final ProcessoService processoService;
	private final TarefaService tarefaService;
	private final AlertaProcessoService alertaService;

	BuscaUniversalService(CasoService casoService, ProcessoService processoService, TarefaService tarefaService,
			AlertaProcessoService alertaService) {
		this.casoService = casoService;
		this.processoService = processoService;
		this.tarefaService = tarefaService;
		this.alertaService = alertaService;
	}

	public DtoBuscaUniversal buscar(String conteudo) {
		PageRequest page = PageRequest.of(0, LIMITE_POR_CATEGORIA);
		return new DtoBuscaUniversal(
				casoService.buscarPorPalavra(page, conteudo).getContent(),
				processoService.buscarPorPalavra(page, conteudo).getContent(),
				tarefaService.buscarPorPalavra(page, conteudo).getContent(),
				alertaService.buscarPorPalavra(page, conteudo).getContent());
	}

}
