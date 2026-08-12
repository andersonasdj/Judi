package br.com.techgold.judi.restcontroller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.dto.DtoEmails;
import br.com.techgold.judi.dto.DtoLogAcesso;

import br.com.techgold.judi.dto.DtoPaises;
import br.com.techgold.judi.model.ConfiguracaoEmail;
import br.com.techgold.judi.model.ConfiguracaoPaises;
import br.com.techgold.judi.services.ConfiguracaoEmailService;
import br.com.techgold.judi.services.ConfiguracaoPaisesService;
import br.com.techgold.judi.services.LogLoginService;

@RestController
@RequestMapping("sistema")
public class AppRestController {
	
	private final LogLoginService loginService;
	private final ConfiguracaoPaisesService paisesService;
	private final ConfiguracaoEmailService emailService;

	AppRestController(LogLoginService loginService, ConfiguracaoPaisesService paisesService, ConfiguracaoEmailService emailService) {
		this.loginService = loginService;
		this.paisesService = paisesService;
		this.emailService = emailService;
	}
	
	@GetMapping("/logs")
	public List<DtoLogAcesso> logar() {
		return loginService.listarLogs();
	}
		
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/configuracao/paises")
	public ResponseEntity<List<ConfiguracaoPaises>> configuracaoPaises(@RequestBody DtoPaises dados) {
		return ResponseEntity.ok().body(paisesService.checarPaises(dados));
	}
	
	@GetMapping("/configuracao/paises")
	public ResponseEntity<List<ConfiguracaoPaises>> buscaConfiguracaoPaises() {
		return ResponseEntity.ok().body(paisesService.listarPaises());
	}
	
	@GetMapping("/configuracao/email")
	public ResponseEntity<List<ConfiguracaoEmail>> buscaConfiguracaoEmails() {
		return ResponseEntity.ok().body(emailService.listarEmails());
	}
	
	@PutMapping("/configuracao/email")
	public void configuracaoEmails(@RequestBody List<DtoEmails> dados) {
		emailService.atualiza(dados);
		System.out.println(dados);
	}
	
}
