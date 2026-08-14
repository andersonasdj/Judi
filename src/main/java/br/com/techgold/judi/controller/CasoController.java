package br.com.techgold.judi.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("caso")
public class CasoController {

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/list")
	public String listar() {
		return "casoList.html";
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/form")
	public String formulario() {
		return "casoForm.html";
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/detalhe")
	public String detalhe() {
		return "casoDetalhe.html";
	}

}
