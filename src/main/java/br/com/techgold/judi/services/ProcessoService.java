package br.com.techgold.judi.services;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.techgold.judi.datajud.DataJudSyncService;
import br.com.techgold.judi.dto.DtoAlerta;
import br.com.techgold.judi.dto.DtoAtualizarProcesso;
import br.com.techgold.judi.dto.DtoCadastroProcesso;
import br.com.techgold.judi.dto.DtoErroImportacaoProcesso;
import br.com.techgold.judi.dto.DtoFiltroProcesso;
import br.com.techgold.judi.dto.DtoMovimentacao;
import br.com.techgold.judi.dto.DtoProcessoDetalhe;
import br.com.techgold.judi.dto.DtoProcessoList;
import br.com.techgold.judi.dto.DtoResultadoImportacaoProcessos;
import br.com.techgold.judi.model.Caso;
import br.com.techgold.judi.model.Cliente;
import br.com.techgold.judi.model.Processo;
import br.com.techgold.judi.model.enums.StatusConsultaDataJud;
import br.com.techgold.judi.model.enums.StatusProcesso;
import br.com.techgold.judi.repository.AlertaProcessoRepository;
import br.com.techgold.judi.repository.CasoRepository;
import br.com.techgold.judi.repository.ClienteRepository;
import br.com.techgold.judi.repository.FuncionarioRepository;
import br.com.techgold.judi.repository.MovimentacaoProcessoRepository;
import br.com.techgold.judi.repository.ProcessoRepository;
import jakarta.persistence.criteria.Predicate;

@Service
public class ProcessoService {

	private static final String REGEX_NUMERO_CNJ = "\\d{7}-\\d{2}\\.\\d{4}\\.\\d\\.\\d{2}\\.\\d{4}";

	private final ProcessoRepository repository;
	private final ClienteRepository clienteRepository;
	private final FuncionarioRepository funcionarioRepository;
	private final CasoRepository casoRepository;
	private final MovimentacaoProcessoRepository movimentacaoRepository;
	private final AlertaProcessoRepository alertaRepository;
	private final DataJudSyncService dataJudSyncService;

	ProcessoService(ProcessoRepository repository, ClienteRepository clienteRepository,
			FuncionarioRepository funcionarioRepository, CasoRepository casoRepository, MovimentacaoProcessoRepository movimentacaoRepository,
			AlertaProcessoRepository alertaRepository, DataJudSyncService dataJudSyncService) {
		this.repository = repository;
		this.clienteRepository = clienteRepository;
		this.funcionarioRepository = funcionarioRepository;
		this.casoRepository = casoRepository;
		this.movimentacaoRepository = movimentacaoRepository;
		this.alertaRepository = alertaRepository;
		this.dataJudSyncService = dataJudSyncService;
	}

	/** Se um caso for informado, ele precisa pertencer ao mesmo cliente do processo. */
	private Caso resolverCaso(Long casoId, Long clienteId) {
		if (casoId == null) {
			return null;
		}
		Caso caso = casoRepository.getReferenceById(casoId);
		if (!caso.getCliente().getId().equals(clienteId)) {
			throw new IllegalStateException("O caso selecionado pertence a outro cliente.");
		}
		return caso;
	}

	public Page<DtoProcessoList> listar(Pageable page) {
		return repository.findByAtivoTrue(page).map(DtoProcessoList::new);
	}

	public Page<DtoProcessoList> listarPorCliente(Long clienteId, Pageable page) {
		return repository.findByClienteIdAndAtivoTrue(clienteId, page).map(DtoProcessoList::new);
	}

	public Page<DtoProcessoList> listarPorFuncionario(Long funcionarioId, Pageable page) {
		return repository.findByFuncionarioResponsavelIdAndAtivoTrue(funcionarioId, page).map(DtoProcessoList::new);
	}

	public Page<DtoProcessoList> listarPorCaso(Long casoId, Pageable page) {
		return repository.findByCasoIdAndAtivoTrue(casoId, page).map(DtoProcessoList::new);
	}

	/** Para o card "Últimos processos criados" da home. */
	public List<DtoProcessoList> listarUltimosCriados(int limite) {
		return repository.findByAtivoTrueOrderByDataCadastroDesc(PageRequest.of(0, limite))
				.map(DtoProcessoList::new).getContent();
	}

	public Page<DtoProcessoList> buscarPorPalavra(Pageable page, String conteudo) {
		return repository.buscarPorPalavra(page, conteudo).map(DtoProcessoList::new);
	}

	/** Filtro rápido combinável da listagem: qualquer combinação de texto (nº processo/cliente), cliente, funcionário, caso e status. */
	public Page<DtoProcessoList> listarFiltrado(DtoFiltroProcesso filtro, Pageable page) {
		return repository.findAll(especificacao(filtro), page).map(DtoProcessoList::new);
	}

	private Specification<Processo> especificacao(DtoFiltroProcesso filtro) {
		return (root, query, cb) -> {
			List<Predicate> predicados = new ArrayList<>();
			predicados.add(cb.isTrue(root.get("ativo")));

			if (filtro.texto() != null && !filtro.texto().isBlank()) {
				String termo = "%" + filtro.texto().toLowerCase() + "%";
				predicados.add(cb.or(
						cb.like(cb.lower(root.get("numeroProcesso")), termo),
						cb.like(cb.lower(root.get("cliente").get("nomeCliente")), termo)));
			}
			if (filtro.clienteId() != null) {
				predicados.add(cb.equal(root.get("cliente").get("id"), filtro.clienteId()));
			}
			if (filtro.funcionarioId() != null) {
				predicados.add(cb.equal(root.get("funcionarioResponsavel").get("id"), filtro.funcionarioId()));
			}
			if (filtro.casoId() != null) {
				predicados.add(cb.equal(root.get("caso").get("id"), filtro.casoId()));
			}
			if (filtro.status() != null) {
				predicados.add(cb.equal(root.get("status"), filtro.status()));
			}
			return cb.and(predicados.toArray(new Predicate[0]));
		};
	}

	public DtoProcessoDetalhe buscarDetalhe(Long id) {
		Processo processo = repository.getReferenceById(id);
		List<DtoMovimentacao> movimentacoes = movimentacaoRepository.findByProcessoIdOrderByDataMovimentacaoDesc(id)
				.stream().map(DtoMovimentacao::new).toList();
		List<DtoAlerta> alertas = alertaRepository.findByProcessoIdOrderByDataGeracaoDesc(id, Pageable.unpaged())
				.map(DtoAlerta::new).toList();
		return new DtoProcessoDetalhe(processo, movimentacoes, alertas);
	}

	public Processo buscarEntidade(Long id) {
		return repository.getReferenceById(id);
	}

	public Processo cadastrar(DtoCadastroProcesso dados) {
		if (repository.existsByNumeroProcesso(dados.numeroProcesso())) {
			throw new IllegalStateException("Já existe um processo cadastrado com este número.");
		}

		Processo processo = new Processo();
		processo.setNumeroProcesso(dados.numeroProcesso());
		processo.setCliente(clienteRepository.getReferenceById(dados.clienteId()));
		processo.setCaso(resolverCaso(dados.casoId(), dados.clienteId()));
		if (dados.funcionarioResponsavelId() != null) {
			processo.setFuncionarioResponsavel(funcionarioRepository.getReferenceById(dados.funcionarioResponsavelId()));
		}
		processo.setTribunal(dados.tribunal());
		processo.setClasseProcessual(dados.classeProcessual());
		processo.setAssunto(dados.assunto());
		processo.setOrgaoJulgador(dados.orgaoJulgador());
		processo.setGrau(dados.grau());
		processo.setParteAdversa(dados.parteAdversa());
		processo.setDocumentoParteAdversa(dados.documentoParteAdversa());
		processo.setPoloCliente(dados.poloCliente());
		processo.setDataDistribuicao(dados.dataDistribuicao());
		processo.setValorCausa(dados.valorCausa());
		processo.setObservacoes(dados.observacoes());
		processo.setMonitorado(dados.monitorado() == null || dados.monitorado());
		processo.setStatus(StatusProcesso.ATIVO);
		processo.setAtivo(true);
		processo.setDataCadastro(LocalDateTime.now().withNano(0));
		processo.setDataAtualizacao(LocalDateTime.now().withNano(0));
		return repository.save(processo);
	}

	public Processo atualizar(DtoAtualizarProcesso dados) {
		Processo processo = repository.getReferenceById(dados.id());

		if (!processo.getNumeroProcesso().equals(dados.numeroProcesso()) && repository.existsByNumeroProcesso(dados.numeroProcesso())) {
			throw new IllegalStateException("Já existe um processo cadastrado com este número.");
		}

		processo.setNumeroProcesso(dados.numeroProcesso());
		processo.setCliente(clienteRepository.getReferenceById(dados.clienteId()));
		processo.setCaso(resolverCaso(dados.casoId(), dados.clienteId()));
		processo.setFuncionarioResponsavel(dados.funcionarioResponsavelId() != null
				? funcionarioRepository.getReferenceById(dados.funcionarioResponsavelId())
				: null);
		processo.setTribunal(dados.tribunal());
		processo.setClasseProcessual(dados.classeProcessual());
		processo.setAssunto(dados.assunto());
		processo.setOrgaoJulgador(dados.orgaoJulgador());
		processo.setGrau(dados.grau());
		processo.setParteAdversa(dados.parteAdversa());
		processo.setDocumentoParteAdversa(dados.documentoParteAdversa());
		processo.setPoloCliente(dados.poloCliente());
		processo.setStatus(dados.status() != null ? dados.status() : processo.getStatus());
		processo.setDataDistribuicao(dados.dataDistribuicao());
		processo.setValorCausa(dados.valorCausa());
		processo.setObservacoes(dados.observacoes());
		processo.setMonitorado(dados.monitorado() != null ? dados.monitorado() : processo.getMonitorado());
		processo.setAtivo(dados.ativo() != null ? dados.ativo() : processo.getAtivo());
		processo.setDataAtualizacao(LocalDateTime.now().withNano(0));
		return repository.save(processo);
	}

	public void inativar(Long id) {
		Processo processo = repository.getReferenceById(id);
		processo.setAtivo(false);
		processo.setMonitorado(false);
		repository.save(processo);
	}

	/**
	 * Importa processos em lote para um cliente a partir de um CSV.
	 * Colunas aceitas (cabeçalho, só {@code numeroProcesso} é obrigatória):
	 * numeroProcesso, tribunal, classeProcessual, assunto, orgaoJulgador, grau,
	 * dataDistribuicao (ISO yyyy-MM-dd), valorCausa, observacoes, monitorado (true/false).
	 * Linhas com número já cadastrado são ignoradas (contadas como duplicadas);
	 * linhas malformadas não interrompem a importação das demais. Ao final,
	 * dispara a sincronização com o DataJud em background para cada processo novo.
	 */
	public DtoResultadoImportacaoProcessos importarCsv(Long clienteId, MultipartFile arquivo) {
		Cliente cliente = clienteRepository.getReferenceById(clienteId);

		String conteudo;
		try {
			conteudo = new String(arquivo.getBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Não foi possível ler o arquivo enviado: " + e.getMessage(), e);
		}

		CSVFormat formato = CSVFormat.DEFAULT
				.withHeader()
				.withSkipHeaderRecord(true)
				.withIgnoreHeaderCase(true)
				.withTrim(true)
				.withDelimiter(detectarDelimitador(conteudo));

		List<DtoErroImportacaoProcesso> erros = new ArrayList<>();
		List<Long> idsNovosProcessos = new ArrayList<>();
		int totalLinhas = 0;
		int duplicados = 0;

		try (CSVParser parser = new CSVParser(new StringReader(conteudo), formato)) {
			for (CSVRecord registro : parser) {
				totalLinhas++;
				int linha = (int) registro.getRecordNumber() + 1; // +1 por causa do cabeçalho

				String numeroProcesso = valor(registro, "numeroProcesso");
				if (numeroProcesso == null || numeroProcesso.isBlank()) {
					erros.add(new DtoErroImportacaoProcesso(linha, numeroProcesso, "Número do processo vazio"));
					continue;
				}
				if (!numeroProcesso.matches(REGEX_NUMERO_CNJ)) {
					erros.add(new DtoErroImportacaoProcesso(linha, numeroProcesso, "Formato CNJ inválido (esperado NNNNNNN-DD.AAAA.J.TR.OOOO)"));
					continue;
				}
				if (repository.existsByNumeroProcesso(numeroProcesso)) {
					duplicados++;
					continue;
				}

				try {
					idsNovosProcessos.add(importarLinha(cliente, registro, numeroProcesso).getId());
				} catch (Exception e) {
					erros.add(new DtoErroImportacaoProcesso(linha, numeroProcesso, "Erro ao importar: " + e.getMessage()));
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Não foi possível interpretar o arquivo CSV: " + e.getMessage(), e);
		}

		idsNovosProcessos.forEach(dataJudSyncService::sincronizarAsync);

		return new DtoResultadoImportacaoProcessos(totalLinhas, idsNovosProcessos.size(), duplicados, erros.size(), erros);
	}

	private Processo importarLinha(Cliente cliente, CSVRecord registro, String numeroProcesso) {
		Processo processo = new Processo();
		processo.setNumeroProcesso(numeroProcesso);
		processo.setCliente(cliente);
		processo.setTribunal(valor(registro, "tribunal"));
		processo.setClasseProcessual(valor(registro, "classeProcessual"));
		processo.setAssunto(valor(registro, "assunto"));
		processo.setOrgaoJulgador(valor(registro, "orgaoJulgador"));
		processo.setGrau(valor(registro, "grau"));
		processo.setObservacoes(valor(registro, "observacoes"));

		String dataDistribuicao = valor(registro, "dataDistribuicao");
		if (dataDistribuicao != null && !dataDistribuicao.isBlank()) {
			processo.setDataDistribuicao(LocalDate.parse(dataDistribuicao));
		}

		String valorCausa = valor(registro, "valorCausa");
		if (valorCausa != null && !valorCausa.isBlank()) {
			processo.setValorCausa(new BigDecimal(valorCausa.replace(",", ".")));
		}

		String monitorado = valor(registro, "monitorado");
		processo.setMonitorado(monitorado == null || monitorado.isBlank() || Boolean.parseBoolean(monitorado));

		processo.setStatus(StatusProcesso.ATIVO);
		processo.setStatusUltimaConsulta(StatusConsultaDataJud.NUNCA_CONSULTADO);
		processo.setAtivo(true);
		processo.setDataCadastro(LocalDateTime.now().withNano(0));
		processo.setDataAtualizacao(LocalDateTime.now().withNano(0));
		return repository.save(processo);
	}

	private String valor(CSVRecord registro, String coluna) {
		String bruto = registro.isMapped(coluna) ? registro.get(coluna) : null;
		return bruto != null ? bruto.trim() : null;
	}

	private char detectarDelimitador(String conteudo) {
		int quebraLinha = conteudo.indexOf('\n');
		String primeiraLinha = quebraLinha > 0 ? conteudo.substring(0, quebraLinha) : conteudo;
		long virgulas = primeiraLinha.chars().filter(c -> c == ',').count();
		long pontoEVirgulas = primeiraLinha.chars().filter(c -> c == ';').count();
		return pontoEVirgulas > virgulas ? ';' : ',';
	}

}
