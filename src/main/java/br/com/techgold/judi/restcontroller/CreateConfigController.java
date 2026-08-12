package br.com.techgold.judi.restcontroller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.dto.DtoCadastroFuncionario;
import br.com.techgold.judi.model.ConfiguracaoEmail;
import br.com.techgold.judi.model.ConfiguracaoPaises;

import br.com.techgold.judi.model.enums.Agendamentos;

import br.com.techgold.judi.services.ConfiguracaoEmailService;
import br.com.techgold.judi.services.ConfiguracaoPaisesService;
import br.com.techgold.judi.services.FuncionarioService;

@RestController
@RequestMapping()
public class CreateConfigController {
	
	private final FuncionarioService service;
	private final ConfiguracaoPaisesService paisesService;
	private final ConfiguracaoEmailService emailService;

	CreateConfigController(FuncionarioService service, ConfiguracaoPaisesService paisesService, ConfiguracaoEmailService emailService) {
		this.service = service;
		this.paisesService = paisesService;
		this.emailService = emailService;
	}
	
	@PostMapping("/create")
	public String register(@RequestBody DtoCadastroFuncionario dados ) {
		
		if(paisesService.existeConfig() == 0) {
			paisesService.salvar(new ConfiguracaoPaises("BR",true));
			paisesService.salvar(new ConfiguracaoPaises("US",true));
			paisesService.salvar(new ConfiguracaoPaises("PT",true));
			paisesService.salvar(new ConfiguracaoPaises("CA",true));
			paisesService.salvar(new ConfiguracaoPaises("FR",true));
			paisesService.salvar(new ConfiguracaoPaises("CL",true));
		}
		
		if(emailService.existeEmail() == 0) {
			emailService.cadastra(new ConfiguracaoEmail( " ", false, Agendamentos.AGENDAMENTO));
			emailService.cadastra(new ConfiguracaoEmail( " ", false, Agendamentos.ABERTURA));
		}
		
		if(service.existeFuncionarios() == 0) {
			service.salvar(dados);
			return "Usuário cadastrado com sucesso!";
		} else {
			return "Erro!";
		}
	}	

}
