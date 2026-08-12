package br.com.techgold.judi.restcontroller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.dto.DtoDashboardPainel;
import br.com.techgold.judi.dto.DtoDashboardResumo;
import br.com.techgold.judi.services.DashboardService;

@RestController
@RequestMapping("dashboard")
public class DashboardRestController {

	private final DashboardService service;

	DashboardRestController(DashboardService service) {
		this.service = service;
	}

	@GetMapping("/resumo")
	public DtoDashboardResumo resumo() {
		return service.montarResumo();
	}

	@GetMapping("/painel")
	public DtoDashboardPainel painel() {
		return service.montarPainel();
	}

}
