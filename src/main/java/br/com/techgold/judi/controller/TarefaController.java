package br.com.techgold.judi.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("tarefa")
public class TarefaController {

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/list")
	public String listar() {
		return "tarefaList.html";
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/form")
	public String formulario() {
		return "tarefaForm.html";
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/detalhe")
	public String detalhe() {
		return "tarefaDetalhe.html";
	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@GetMapping("/despesas")
	public String despesas() {
		return "despesasList.html";
	}

}
