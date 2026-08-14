package br.com.techgold.judi.restcontroller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import br.com.techgold.judi.dto.DtoEnviarMensagemChat;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.services.ChatService;
import br.com.techgold.judi.services.FuncionarioService;

/**
 * Recebe o envio de mensagens via STOMP (/app/chat.enviar). A entrega ao
 * destinatário e a persistência acontecem em {@link ChatService#enviar}.
 */
@Controller
public class ChatWsController {

	private final ChatService service;
	private final FuncionarioService funcionarioService;

	ChatWsController(ChatService service, FuncionarioService funcionarioService) {
		this.service = service;
		this.funcionarioService = funcionarioService;
	}

	@MessageMapping("/chat.enviar")
	public void enviar(DtoEnviarMensagemChat dados, Principal principal) {
		Funcionario principalLogado = (Funcionario) ((Authentication) principal).getPrincipal();
		Funcionario remetente = funcionarioService.buscaPorNome(principalLogado.getNomeFuncionario());
		service.enviar(dados, remetente);
	}

}
