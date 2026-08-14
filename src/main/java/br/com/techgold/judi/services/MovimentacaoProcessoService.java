package br.com.techgold.judi.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoCadastroMovimentacaoManual;
import br.com.techgold.judi.dto.DtoMovimentacao;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.MovimentacaoProcesso;
import br.com.techgold.judi.model.Processo;
import br.com.techgold.judi.model.enums.OrigemMovimentacao;
import br.com.techgold.judi.repository.MovimentacaoProcessoRepository;
import br.com.techgold.judi.repository.ProcessoRepository;

@Service
public class MovimentacaoProcessoService {

	private final MovimentacaoProcessoRepository repository;
	private final ProcessoRepository processoRepository;

	MovimentacaoProcessoService(MovimentacaoProcessoRepository repository, ProcessoRepository processoRepository) {
		this.repository = repository;
		this.processoRepository = processoRepository;
	}

	public List<DtoMovimentacao> listarPorProcesso(Long processoId) {
		return repository.findByProcessoIdOrderByDataMovimentacaoDesc(processoId).stream().map(DtoMovimentacao::new).toList();
	}

	public MovimentacaoProcesso registrarManual(Long processoId, DtoCadastroMovimentacaoManual dados, Funcionario funcionarioLogado) {
		Processo processo = processoRepository.getReferenceById(processoId);

		MovimentacaoProcesso movimentacao = new MovimentacaoProcesso();
		movimentacao.setProcesso(processo);
		movimentacao.setDataMovimentacao(dados.dataMovimentacao());
		movimentacao.setCodigoMovimento(dados.codigoMovimento());
		movimentacao.setDescricao(dados.descricao());
		movimentacao.setComplemento(dados.complemento());
		movimentacao.setOrigem(OrigemMovimentacao.MANUAL);
		movimentacao.setDataRegistro(LocalDateTime.now().withNano(0));
		movimentacao.setRegistradoPor(funcionarioLogado);
		MovimentacaoProcesso salva = repository.save(movimentacao);

		atualizarDataUltimaMovimentacao(processo, dados.dataMovimentacao());

		return salva;
	}

	/**
	 * Usado pela sincronização com o DataJud: só grava se ainda não existir
	 * a mesma movimentação (mesmo código + mesma data) para o processo.
	 */
	public boolean registrarSeNovo(Processo processo, LocalDateTime dataMovimentacao, String codigoMovimento, String descricao, String complemento) {
		if (repository.existsByProcessoIdAndCodigoMovimentoAndDataMovimentacao(processo.getId(), codigoMovimento, dataMovimentacao)) {
			return false;
		}

		MovimentacaoProcesso movimentacao = new MovimentacaoProcesso();
		movimentacao.setProcesso(processo);
		movimentacao.setDataMovimentacao(dataMovimentacao);
		movimentacao.setCodigoMovimento(codigoMovimento);
		movimentacao.setDescricao(descricao);
		movimentacao.setComplemento(complemento);
		movimentacao.setOrigem(OrigemMovimentacao.DATAJUD);
		movimentacao.setDataRegistro(LocalDateTime.now().withNano(0));
		repository.save(movimentacao);

		atualizarDataUltimaMovimentacao(processo, dataMovimentacao);

		return true;
	}

	private void atualizarDataUltimaMovimentacao(Processo processo, LocalDateTime dataMovimentacao) {
		if (processo.getDataUltimaMovimentacao() == null || dataMovimentacao.isAfter(processo.getDataUltimaMovimentacao())) {
			processo.setDataUltimaMovimentacao(dataMovimentacao);
			processoRepository.save(processo);
		}
	}

}
