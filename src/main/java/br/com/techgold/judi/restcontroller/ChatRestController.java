package br.com.techgold.judi.restcontroller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.dto.DtoContagemNaoLidas;
import br.com.techgold.judi.dto.DtoMensagemChat;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.services.ChatService;
import br.com.techgold.judi.services.FuncionarioService;

@RestController
@RequestMapping("chat")
public class ChatRestController {

	private final ChatService service;
	private final FuncionarioService funcionarioService;

	ChatRestController(ChatService service, FuncionarioService funcionarioService) {
		this.service = service;
		this.funcionarioService = funcionarioService;
	}

	private Funcionario funcionarioAutenticado() {
		Funcionario principal = (Funcionario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return funcionarioService.buscaPorNome(principal.getNomeFuncionario());
	}

	@GetMapping("/conversas/{outroId}")
	public Page<DtoMensagemChat> listarConversa(@PathVariable Long outroId,
			@RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "30") int tamanho) {
		return service.listarConversa(outroId, funcionarioAutenticado(), PageRequest.of(pagina, tamanho, Direction.DESC, "dataEnvio"));
	}

	@PostMapping("/conversas/{outroId}/marcar-lida")
	public void marcarComoLida(@PathVariable Long outroId) {
		service.marcarComoLida(outroId, funcionarioAutenticado());
	}

	@GetMapping("/nao-lidas")
	public List<DtoContagemNaoLidas> contarNaoLidas() {
		return service.contarNaoLidasPorRemetente(funcionarioAutenticado());
	}

}
