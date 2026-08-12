package br.com.techgold.judi.services;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoAlerta;
import br.com.techgold.judi.model.AlertaProcesso;
import br.com.techgold.judi.model.Processo;
import br.com.techgold.judi.model.enums.SeveridadeAlerta;
import br.com.techgold.judi.model.enums.TipoAlerta;
import br.com.techgold.judi.repository.AlertaProcessoRepository;

@Service
public class AlertaProcessoService {

	private final AlertaProcessoRepository repository;

	AlertaProcessoService(AlertaProcessoRepository repository) {
		this.repository = repository;
	}

	public Page<DtoAlerta> listar(Pageable page, boolean apenasNaoLidos) {
		Page<AlertaProcesso> alertas = apenasNaoLidos ? repository.findByLidoFalseOrderByDataGeracaoDesc(page)
				: repository.findAllByOrderByDataGeracaoDesc(page);
		return alertas.map(DtoAlerta::new);
	}

	public Page<DtoAlerta> listarPorProcesso(Long processoId, Pageable page) {
		return repository.findByProcessoIdOrderByDataGeracaoDesc(processoId, page).map(DtoAlerta::new);
	}

	public long contarNaoLidos() {
		return repository.countByLidoFalse();
	}

	public void marcarComoLido(Long id) {
		AlertaProcesso alerta = repository.getReferenceById(id);
		alerta.setLido(true);
		alerta.setDataLeitura(LocalDateTime.now().withNano(0));
		repository.save(alerta);
	}

	public void gerar(Processo processo, TipoAlerta tipo, SeveridadeAlerta severidade, String mensagem) {
		AlertaProcesso alerta = new AlertaProcesso();
		alerta.setProcesso(processo);
		alerta.setTipo(tipo);
		alerta.setSeveridade(severidade);
		alerta.setMensagem(mensagem);
		alerta.setDataGeracao(LocalDateTime.now().withNano(0));
		alerta.setLido(false);
		repository.save(alerta);
	}

}
