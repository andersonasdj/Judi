package br.com.techgold.judi.datajud;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.model.Processo;
import br.com.techgold.judi.model.enums.SeveridadeAlerta;
import br.com.techgold.judi.model.enums.StatusConsultaDataJud;
import br.com.techgold.judi.model.enums.TipoAlerta;
import br.com.techgold.judi.repository.ProcessoRepository;
import br.com.techgold.judi.services.AlertaProcessoService;
import br.com.techgold.judi.services.ConfiguracaoDataJudService;
import br.com.techgold.judi.services.MovimentacaoProcessoService;

/**
 * Orquestra a sincronização diária dos processos monitorados com a API
 * Pública do DataJud: busca movimentações novas, grava-as e dispara alertas.
 *
 * Enquanto {@link ConfiguracaoDataJudService#isConfigurado()} for falso
 * (integração desativada ou sem api-key definida em Configurações →
 * Integração DataJud), a sincronização é ignorada — os processos ficam
 * marcados como {@code ERRO} de consulta com uma mensagem explicativa, sem
 * gerar exceção nem travar a aplicação.
 */
@Service
public class DataJudSyncService {

	private static final Logger log = LoggerFactory.getLogger(DataJudSyncService.class);

	private static final String MENSAGEM_INTEGRACAO_PENDENTE = "Integração com o DataJud ainda não configurada ou desativada.";

	private final ProcessoRepository processoRepository;
	private final DataJudClient client;
	private final ConfiguracaoDataJudService configuracaoService;
	private final MovimentacaoProcessoService movimentacaoService;
	private final AlertaProcessoService alertaService;

	DataJudSyncService(ProcessoRepository processoRepository, DataJudClient client, ConfiguracaoDataJudService configuracaoService,
			MovimentacaoProcessoService movimentacaoService, AlertaProcessoService alertaService) {
		this.processoRepository = processoRepository;
		this.client = client;
		this.configuracaoService = configuracaoService;
		this.movimentacaoService = movimentacaoService;
		this.alertaService = alertaService;
	}

	/**
	 * Dispara a sincronização de um único processo em background (usado logo
	 * após a importação em lote, para não travar a resposta HTTP esperando
	 * cada consulta ao DataJud).
	 */
	@Async("asyncExecutor")
	public void sincronizarAsync(Long processoId) {
		processoRepository.findById(processoId).ifPresent(processo -> {
			try {
				sincronizarProcesso(processo);
			} catch (Exception e) {
				log.error("Falha ao sincronizar em background o processo {}: {}", processo.getNumeroProcesso(), e.getMessage(), e);
			}
		});
	}

	public void sincronizarTodos() {
		List<Processo> processos = processoRepository.findByAtivoTrueAndMonitoradoTrue();
		log.info("Iniciando sincronização DataJud de {} processo(s) monitorado(s).", processos.size());

		for (Processo processo : processos) {
			try {
				sincronizarProcesso(processo);
			} catch (Exception e) {
				log.error("Falha inesperada ao sincronizar o processo {}: {}", processo.getNumeroProcesso(), e.getMessage(), e);
			}
		}
	}

	public void sincronizarProcesso(Processo processo) {
		if (!configuracaoService.isConfigurado()) {
			marcarErro(processo, MENSAGEM_INTEGRACAO_PENDENTE, false);
			return;
		}

		StatusConsultaDataJud statusAnterior = processo.getStatusUltimaConsulta();

		try {
			List<MovimentoDataJud> movimentos = client.buscarMovimentos(processo.getTribunal(), processo.getNumeroProcesso());

			int novos = 0;
			for (MovimentoDataJud movimento : movimentos) {
				if (movimento.dataHora() == null) {
					continue;
				}
				boolean inserido = movimentacaoService.registrarSeNovo(processo, movimento.dataHora(), movimento.codigo(),
						movimento.descricao(), movimento.complemento());
				if (inserido) {
					novos++;
				}
			}

			processo.setStatusUltimaConsulta(StatusConsultaDataJud.SUCESSO);
			processo.setMensagemErroUltimaConsulta(null);
			processo.setDataUltimaConsulta(LocalDateTime.now().withNano(0));
			processoRepository.save(processo);

			if (novos > 0) {
				alertaService.gerar(processo, TipoAlerta.NOVA_MOVIMENTACAO, SeveridadeAlerta.INFO,
						novos + " nova(s) movimentação(ões) encontrada(s) para o processo " + processo.getNumeroProcesso() + ".");
			}
		} catch (DataJudException e) {
			marcarErro(processo, e.getMessage(), statusAnterior != StatusConsultaDataJud.ERRO);
		}
	}

	private void marcarErro(Processo processo, String mensagem, boolean gerarAlerta) {
		processo.setStatusUltimaConsulta(StatusConsultaDataJud.ERRO);
		processo.setMensagemErroUltimaConsulta(mensagem);
		processo.setDataUltimaConsulta(LocalDateTime.now().withNano(0));
		processoRepository.save(processo);

		if (gerarAlerta) {
			alertaService.gerar(processo, TipoAlerta.ERRO_CONSULTA, SeveridadeAlerta.ATENCAO,
					"Falha ao consultar o DataJud para o processo " + processo.getNumeroProcesso() + ": " + mensagem);
		}
	}

}
