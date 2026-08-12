package br.com.techgold.judi.restcontroller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.dto.DtoAlerta;
import br.com.techgold.judi.services.AlertaProcessoService;

@RestController
@RequestMapping("alertas")
public class AlertaRestController {

	private final AlertaProcessoService service;

	AlertaRestController(AlertaProcessoService service) {
		this.service = service;
	}

	@GetMapping
	public Page<DtoAlerta> listar(
			@RequestParam(defaultValue = "false") boolean apenasNaoLidos,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listar(page, apenasNaoLidos);
	}

	@GetMapping("/contagem-nao-lidos")
	public long contarNaoLidos() {
		return service.contarNaoLidos();
	}

	@PutMapping("/{id}/lido")
	public void marcarComoLido(@PathVariable Long id) {
		service.marcarComoLido(id);
	}

}
