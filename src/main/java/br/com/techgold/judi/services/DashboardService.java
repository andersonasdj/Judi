package br.com.techgold.judi.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoAlerta;
import br.com.techgold.judi.dto.DtoContagem;
import br.com.techgold.judi.dto.DtoDashboardPainel;
import br.com.techgold.judi.dto.DtoDashboardRecentes;
import br.com.techgold.judi.dto.DtoDashboardResumo;
import br.com.techgold.judi.dto.DtoMovimentacaoPainel;
import br.com.techgold.judi.dto.DtoPainelVersao;
import br.com.techgold.judi.dto.DtoUsuarioOnline;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.enums.StatusCaso;
import br.com.techgold.judi.model.enums.StatusConsultaDataJud;
import br.com.techgold.judi.model.enums.StatusProcesso;
import br.com.techgold.judi.model.enums.StatusTarefa;
import br.com.techgold.judi.repository.AlertaProcessoRepository;
import br.com.techgold.judi.repository.CasoRepository;
import br.com.techgold.judi.repository.MovimentacaoProcessoRepository;
import br.com.techgold.judi.repository.ProcessoRepository;
import br.com.techgold.judi.repository.TarefaRepository;

@Service
public class DashboardService {

	private final ProcessoRepository processoRepository;
	private final AlertaProcessoRepository alertaRepository;
	private final MovimentacaoProcessoRepository movimentacaoRepository;
	private final CasoRepository casoRepository;
	private final TarefaRepository tarefaRepository;
	private final TarefaService tarefaService;
	private final ProcessoService processoService;
	private final CasoService casoService;
	private final SessionRegistry sessionRegistry;

	DashboardService(ProcessoRepository processoRepository, AlertaProcessoRepository alertaRepository,
			MovimentacaoProcessoRepository movimentacaoRepository, CasoRepository casoRepository, TarefaRepository tarefaRepository,
			TarefaService tarefaService, ProcessoService processoService, CasoService casoService, SessionRegistry sessionRegistry) {
		this.processoRepository = processoRepository;
		this.alertaRepository = alertaRepository;
		this.movimentacaoRepository = movimentacaoRepository;
		this.casoRepository = casoRepository;
		this.tarefaRepository = tarefaRepository;
		this.tarefaService = tarefaService;
		this.processoService = processoService;
		this.casoService = casoService;
		this.sessionRegistry = sessionRegistry;
	}

	public DtoDashboardResumo montarResumo() {
		List<DtoContagem> processosPorStatus = Arrays.stream(StatusProcesso.values())
				.map(status -> new DtoContagem(status.name(), processoRepository.countByStatusAndAtivoTrue(status)))
				.toList();

		long processosComErroConsulta = processoRepository.countByStatusUltimaConsultaAndAtivoTrue(StatusConsultaDataJud.ERRO);
		long movimentacoesUltimos7Dias = movimentacaoRepository.countByDataRegistroAfter(LocalDateTime.now().minusDays(7));

		long totalCasosAbertos = casoRepository.countByStatusAndAtivoTrue(StatusCaso.ABERTO)
				+ casoRepository.countByStatusAndAtivoTrue(StatusCaso.EM_ANDAMENTO);

		long totalTarefasAbertas = Arrays.stream(StatusTarefa.values())
				.filter(status -> !status.isFinal())
				.mapToLong(status -> tarefaRepository.countByStatusAndAtivoTrue(status))
				.sum();

		return new DtoDashboardResumo(
				processoRepository.countByAtivoTrue(),
				processosPorStatus,
				alertaRepository.countByLidoFalse(),
				processosComErroConsulta,
				movimentacoesUltimos7Dias,
				totalCasosAbertos,
				totalTarefasAbertas);
	}

	/**
	 * Dados para o painel de acompanhamento (pensado para ficar aberto numa
	 * TV do escritório): o resumo de KPIs + as últimas movimentações, alertas
	 * e próximos agendamentos, para dar sensação de painel "ao vivo".
	 */
	public DtoDashboardPainel montarPainel() {
		List<DtoMovimentacaoPainel> ultimasMovimentacoes = movimentacaoRepository.findTop10ByOrderByDataRegistroDesc()
				.stream().map(DtoMovimentacaoPainel::new).toList();

		List<DtoAlerta> ultimosAlertas = alertaRepository.findByLidoFalseOrderByDataGeracaoDesc(PageRequest.of(0, 10))
				.map(DtoAlerta::new).getContent();

		return new DtoDashboardPainel(montarResumo(), ultimasMovimentacoes, ultimosAlertas,
				tarefaService.listarProximosAgendamentos(8), tarefaService.listarUltimasAtualizadas(8));
	}

	/**
	 * Versão "barata" do painel: só COUNT/MAX (sem joins nem listas), pensada para
	 * ser consultada a cada poucos segundos. O painel só busca {@link #montarPainel()}
	 * (bem mais caro) quando esta versão muda — mesmo padrão do lastupdateid do
	 * dashboard.html de referência.
	 */
	public DtoPainelVersao montarVersaoPainel() {
		return new DtoPainelVersao(
				montarResumo(),
				alertaRepository.maxId(),
				movimentacaoRepository.maxId(),
				tarefaRepository.maxDataAtualizacao());
	}

	/**
	 * Usuários com sessão ativa no momento, via SessionRegistry do Spring Security
	 * (mesmo mecanismo que já impõe "1 sessão por vez" no login). Depende do
	 * HttpSessionEventPublisher para não mostrar sessões já expiradas por timeout.
	 */
	public List<DtoUsuarioOnline> listarUsuariosOnline() {
		return sessionRegistry.getAllPrincipals().stream()
				.filter(Funcionario.class::isInstance)
				.map(Funcionario.class::cast)
				.map(funcionario -> {
					List<SessionInformation> sessoes = sessionRegistry.getAllSessions(funcionario, false);
					if (sessoes.isEmpty()) {
						return null;
					}
					Date ultimaAtividade = sessoes.stream().map(SessionInformation::getLastRequest).max(Date::compareTo).orElse(null);
					return new DtoUsuarioOnline(
							funcionario.getId(),
							funcionario.getNomeFuncionario(),
							ultimaAtividade != null ? LocalDateTime.ofInstant(ultimaAtividade.toInstant(), ZoneId.systemDefault()) : null);
				})
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(DtoUsuarioOnline::nome, Comparator.nullsLast(String::compareToIgnoreCase)))
				.toList();
	}

	/**
	 * Dados para o card "Atividade recente" da home: últimas tarefas
	 * atualizadas, últimos processos e casos criados, e as próximas tarefas
	 * agendadas (mais próximas primeiro).
	 */
	public DtoDashboardRecentes montarRecentes() {
		return new DtoDashboardRecentes(
				tarefaService.listarUltimasAtualizadas(5),
				processoService.listarUltimosCriados(5),
				casoService.listarUltimosCriados(5),
				tarefaService.listarProximosAgendamentos(5));
	}

}
