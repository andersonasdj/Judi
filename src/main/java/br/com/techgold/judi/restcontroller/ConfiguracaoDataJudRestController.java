package br.com.techgold.judi.restcontroller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.datajud.DataJudSyncScheduler;
import br.com.techgold.judi.dto.DtoConfiguracaoDataJud;
import br.com.techgold.judi.dto.DtoSalvarConfiguracaoDataJud;
import br.com.techgold.judi.services.ConfiguracaoDataJudService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("sistema/configuracao/datajud")
public class ConfiguracaoDataJudRestController {

	private final ConfiguracaoDataJudService service;
	private final DataJudSyncScheduler scheduler;

	ConfiguracaoDataJudRestController(ConfiguracaoDataJudService service, DataJudSyncScheduler scheduler) {
		this.service = service;
		this.scheduler = scheduler;
	}

	@PreAuthorize("hasRole('ROLE_SADMIN')")
	@GetMapping
	public DtoConfiguracaoDataJud buscar() {
		return service.buscar();
	}

	@PreAuthorize("hasRole('ROLE_SADMIN')")
	@PutMapping
	public DtoConfiguracaoDataJud salvar(@RequestBody @Valid DtoSalvarConfiguracaoDataJud dados) {
		service.salvar(dados);
		scheduler.reagendar();
		return service.buscar();
	}

}
