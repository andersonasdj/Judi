package br.com.techgold.judi.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.techgold.judi.dto.DtoContagemNaoLidas;
import br.com.techgold.judi.dto.DtoEnviarMensagemChat;
import br.com.techgold.judi.dto.DtoMensagemChat;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.MensagemChat;
import br.com.techgold.judi.repository.FuncionarioRepository;
import br.com.techgold.judi.repository.MensagemChatRepository;

@Service
public class ChatService {

	private final MensagemChatRepository repository;
	private final FuncionarioRepository funcionarioRepository;
	private final SimpMessagingTemplate messagingTemplate;

	ChatService(MensagemChatRepository repository, FuncionarioRepository funcionarioRepository, SimpMessagingTemplate messagingTemplate) {
		this.repository = repository;
		this.funcionarioRepository = funcionarioRepository;
		this.messagingTemplate = messagingTemplate;
	}

	/**
	 * Persiste a mensagem e entrega em tempo real via WebSocket — tanto para quem
	 * recebeu (fila privada dele) quanto para o próprio remetente, para sincronizar
	 * outras abas/dispositivos logados com o mesmo usuário.
	 *
	 * @Transactional é necessário aqui: remetente/destinatario vêm de
	 * getReferenceById (proxy preguiçoso) e o DTO/publish no fim do método
	 * acessam nome/username deles — sem uma sessão Hibernate aberta durante
	 * todo o método, isso dispara LazyInitializationException.
	 */
	@Transactional
	public DtoMensagemChat enviar(DtoEnviarMensagemChat dados, Funcionario remetenteLogado) {
		Funcionario remetente = funcionarioRepository.getReferenceById(remetenteLogado.getId());
		Funcionario destinatario = funcionarioRepository.getReferenceById(dados.destinatarioId());

		MensagemChat mensagem = new MensagemChat();
		mensagem.setRemetente(remetente);
		mensagem.setDestinatario(destinatario);
		mensagem.setConteudo(dados.conteudo());
		mensagem.setDataEnvio(LocalDateTime.now().withNano(0));
		mensagem.setLida(false);
		MensagemChat salva = repository.save(mensagem);

		DtoMensagemChat dto = new DtoMensagemChat(salva);

		messagingTemplate.convertAndSendToUser(destinatario.getUsername(), "/queue/mensagens", dto);
		if (!destinatario.getId().equals(remetente.getId())) {
			messagingTemplate.convertAndSendToUser(remetente.getUsername(), "/queue/mensagens", dto);
		}

		return dto;
	}

	public Page<DtoMensagemChat> listarConversa(Long outroUsuarioId, Funcionario usuarioLogado, Pageable page) {
		return repository.buscarConversa(usuarioLogado.getId(), outroUsuarioId, page).map(DtoMensagemChat::new);
	}

	public void marcarComoLida(Long outroUsuarioId, Funcionario usuarioLogado) {
		List<MensagemChat> naoLidas = repository.findByRemetenteIdAndDestinatarioIdAndLidaFalse(outroUsuarioId, usuarioLogado.getId());
		naoLidas.forEach(m -> m.setLida(true));
		repository.saveAll(naoLidas);
	}

	public List<DtoContagemNaoLidas> contarNaoLidasPorRemetente(Funcionario usuarioLogado) {
		return repository.contarNaoLidasPorRemetente(usuarioLogado.getId()).stream()
				.map(row -> new DtoContagemNaoLidas((Long) row[0], (Long) row[1]))
				.toList();
	}

}
