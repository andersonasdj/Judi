package br.com.techgold.judi.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("processo")
public class ProcessoController {

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/list")
	public String listar() {
		return "processoList.html";
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/form")
	public String formulario() {
		return "processoForm.html";
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/detalhe")
	public String detalhe() {
		return "processoDetalhe.html";
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/alertas")
	public String alertas() {
		return "alertasList.html";
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/importar")
	public String importar() {
		return "processoImportar.html";
	}

}
