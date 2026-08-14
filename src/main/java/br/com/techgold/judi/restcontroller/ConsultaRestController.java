package br.com.techgold.judi.restcontroller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.dto.DtoConsultaCnpj;
import br.com.techgold.judi.services.CnpjConsultaService;

@RestController
@RequestMapping("consulta")
public class ConsultaRestController {

	private final CnpjConsultaService service;

	ConsultaRestController(CnpjConsultaService service) {
		this.service = service;
	}

	@GetMapping("/cnpj/{cnpj}")
	public DtoConsultaCnpj consultarCnpj(@PathVariable String cnpj) {
		return service.consultar(cnpj);
	}

}
