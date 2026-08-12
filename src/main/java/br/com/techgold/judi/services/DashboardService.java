package br.com.techgold.judi.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoAlerta;
import br.com.techgold.judi.dto.DtoContagem;
import br.com.techgold.judi.dto.DtoDashboardPainel;
import br.com.techgold.judi.dto.DtoDashboardResumo;
import br.com.techgold.judi.dto.DtoMovimentacaoPainel;
import br.com.techgold.judi.model.enums.StatusConsultaDataJud;
import br.com.techgold.judi.model.enums.StatusProcesso;
import br.com.techgold.judi.repository.AlertaProcessoRepository;
import br.com.techgold.judi.repository.MovimentacaoProcessoRepository;
import br.com.techgold.judi.repository.ProcessoRepository;

@Service
public class DashboardService {

	private final ProcessoRepository processoRepository;
	private final AlertaProcessoRepository alertaRepository;
	private final MovimentacaoProcessoRepository movimentacaoRepository;

	DashboardService(ProcessoRepository processoRepository, AlertaProcessoRepository alertaRepository,
			MovimentacaoProcessoRepository movimentacaoRepository) {
		this.processoRepository = processoRepository;
		this.alertaRepository = alertaRepository;
		this.movimentacaoRepository = movimentacaoRepository;
	}

	public DtoDashboardResumo montarResumo() {
		List<DtoContagem> processosPorStatus = Arrays.stream(StatusProcesso.values())
				.map(status -> new DtoContagem(status.name(), processoRepository.countByStatusAndAtivoTrue(status)))
				.toList();

		long processosComErroConsulta = processoRepository.countByStatusUltimaConsultaAndAtivoTrue(StatusConsultaDataJud.ERRO);
		long movimentacoesUltimos7Dias = movimentacaoRepository.countByDataRegistroAfter(LocalDateTime.now().minusDays(7));

		return new DtoDashboardResumo(
				processoRepository.countByAtivoTrue(),
				processosPorStatus,
				alertaRepository.countByLidoFalse(),
				processosComErroConsulta,
				movimentacoesUltimos7Dias);
	}

	/**
	 * Dados para o painel de acompanhamento (pensado para ficar aberto numa
	 * TV do escritório): o resumo de KPIs + as últimas movimentações e
	 * alertas de qualquer processo, para dar sensação de painel "ao vivo".
	 */
	public DtoDashboardPainel montarPainel() {
		List<DtoMovimentacaoPainel> ultimasMovimentacoes = movimentacaoRepository.findTop10ByOrderByDataRegistroDesc()
				.stream().map(DtoMovimentacaoPainel::new).toList();

		List<DtoAlerta> ultimosAlertas = alertaRepository.findByLidoFalseOrderByDataGeracaoDesc(PageRequest.of(0, 10))
				.map(DtoAlerta::new).getContent();

		return new DtoDashboardPainel(montarResumo(), ultimasMovimentacoes, ultimosAlertas);
	}

}
