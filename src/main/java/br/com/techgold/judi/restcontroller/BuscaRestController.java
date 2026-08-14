package br.com.techgold.judi.restcontroller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.dto.DtoBuscaUniversal;
import br.com.techgold.judi.services.BuscaUniversalService;

@RestController
@RequestMapping("busca")
public class BuscaRestController {

	private final BuscaUniversalService service;

	BuscaRestController(BuscaUniversalService service) {
		this.service = service;
	}

	@GetMapping
	public DtoBuscaUniversal buscar(@RequestParam String q) {
		if (q == null || q.trim().length() < 2) {
			return new DtoBuscaUniversal(List.of(), List.of(), List.of(), List.of());
		}
		return service.buscar(q.trim());
	}

}
