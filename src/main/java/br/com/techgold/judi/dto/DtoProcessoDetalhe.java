package br.com.techgold.judi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.techgold.judi.model.Processo;
import br.com.techgold.judi.model.enums.StatusConsultaDataJud;
import br.com.techgold.judi.model.enums.StatusProcesso;

public record DtoProcessoDetalhe(
		Long id,
		String numeroProcesso,
		Long clienteId,
		String nomeCliente,
		Long funcionarioResponsavelId,
		String nomeFuncionarioResponsavel,
		String tribunal,
		String classeProcessual,
		String assunto,
		String orgaoJulgador,
		String grau,
		StatusProcesso status,
		LocalDate dataDistribuicao,
		BigDecimal valorCausa,
		Boolean monitorado,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataUltimaConsulta,
		@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
		LocalDateTime dataUltimaMovimentacao,
		StatusConsultaDataJud statusUltimaConsulta,
		String mensagemErroUltimaConsulta,
		String observacoes,
		boolean ativo,
		List<DtoMovimentacao> movimentacoes,
		List<DtoAlerta> alertas) {

	public DtoProcessoDetalhe(Processo p, List<DtoMovimentacao> movimentacoes, List<DtoAlerta> alertas) {
		this(p.getId(),
				p.getNumeroProcesso(),
				p.getCliente() != null ? p.getCliente().getId() : null,
				p.getCliente() != null ? p.getCliente().getNomeCliente() : null,
				p.getFuncionarioResponsavel() != null ? p.getFuncionarioResponsavel().getId() : null,
				p.getFuncionarioResponsavel() != null ? p.getFuncionarioResponsavel().getNomeFuncionario() : null,
				p.getTribunal(),
				p.getClasseProcessual(),
				p.getAssunto(),
				p.getOrgaoJulgador(),
				p.getGrau(),
				p.getStatus(),
				p.getDataDistribuicao(),
				p.getValorCausa(),
				p.getMonitorado(),
				p.getDataUltimaConsulta(),
				p.getDataUltimaMovimentacao(),
				p.getStatusUltimaConsulta(),
				p.getMensagemErroUltimaConsulta(),
				p.getObservacoes(),
				p.getAtivo(),
				movimentacoes,
				alertas);
	}

}
