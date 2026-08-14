package br.com.techgold.judi.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoAtualizarCaso;
import br.com.techgold.judi.dto.DtoCadastroCaso;
import br.com.techgold.judi.dto.DtoCasoDetalhe;
import br.com.techgold.judi.dto.DtoCasoList;
import br.com.techgold.judi.dto.DtoFiltroCaso;
import br.com.techgold.judi.dto.DtoProcessoList;
import br.com.techgold.judi.dto.DtoTarefaList;
import br.com.techgold.judi.model.Caso;
import br.com.techgold.judi.model.enums.StatusCaso;
import br.com.techgold.judi.repository.CasoRepository;
import br.com.techgold.judi.repository.ClienteRepository;
import br.com.techgold.judi.repository.FuncionarioRepository;
import br.com.techgold.judi.repository.ProcessoRepository;
import br.com.techgold.judi.repository.TarefaRepository;
import jakarta.persistence.criteria.Predicate;

@Service
public class CasoService {

	private final CasoRepository repository;
	private final ClienteRepository clienteRepository;
	private final FuncionarioRepository funcionarioRepository;
	private final ProcessoRepository processoRepository;
	private final TarefaRepository tarefaRepository;

	CasoService(CasoRepository repository, ClienteRepository clienteRepository, FuncionarioRepository funcionarioRepository,
			ProcessoRepository processoRepository, TarefaRepository tarefaRepository) {
		this.repository = repository;
		this.clienteRepository = clienteRepository;
		this.funcionarioRepository = funcionarioRepository;
		this.processoRepository = processoRepository;
		this.tarefaRepository = tarefaRepository;
	}

	public Page<DtoCasoList> listar(Pageable page) {
		return repository.findByAtivoTrue(page).map(this::comTotalProcessos);
	}

	public Page<DtoCasoList> listarPorCliente(Long clienteId, Pageable page) {
		return repository.findByClienteIdAndAtivoTrue(clienteId, page).map(this::comTotalProcessos);
	}

	public Page<DtoCasoList> listarPorFuncionario(Long funcionarioId, Pageable page) {
		return repository.findByFuncionarioResponsavelIdAndAtivoTrue(funcionarioId, page).map(this::comTotalProcessos);
	}

	public Page<DtoCasoList> buscarPorPalavra(Pageable page, String conteudo) {
		return repository.buscarPorPalavra(page, conteudo).map(this::comTotalProcessos);
	}

	/** Filtro rápido combinável da listagem: qualquer combinação de texto (título/cliente), cliente, funcionário, natureza e status. */
	public Page<DtoCasoList> listarFiltrado(DtoFiltroCaso filtro, Pageable page) {
		return repository.findAll(especificacao(filtro), page).map(this::comTotalProcessos);
	}

	private Specification<Caso> especificacao(DtoFiltroCaso filtro) {
		return (root, query, cb) -> {
			List<Predicate> predicados = new ArrayList<>();
			predicados.add(cb.isTrue(root.get("ativo")));

			if (filtro.texto() != null && !filtro.texto().isBlank()) {
				String termo = "%" + filtro.texto().toLowerCase() + "%";
				predicados.add(cb.or(
						cb.like(cb.lower(root.get("titulo")), termo),
						cb.like(cb.lower(root.get("cliente").get("nomeCliente")), termo)));
			}
			if (filtro.clienteId() != null) {
				predicados.add(cb.equal(root.get("cliente").get("id"), filtro.clienteId()));
			}
			if (filtro.funcionarioId() != null) {
				predicados.add(cb.equal(root.get("funcionarioResponsavel").get("id"), filtro.funcionarioId()));
			}
			if (filtro.natureza() != null) {
				predicados.add(cb.equal(root.get("natureza"), filtro.natureza()));
			}
			if (filtro.status() != null) {
				predicados.add(cb.equal(root.get("status"), filtro.status()));
			}
			return cb.and(predicados.toArray(new Predicate[0]));
		};
	}

	/** Para o card "Últimos casos criados" da home. */
	public List<DtoCasoList> listarUltimosCriados(int limite) {
		return repository.findByAtivoTrueOrderByDataCadastroDesc(PageRequest.of(0, limite))
				.map(this::comTotalProcessos).getContent();
	}

	private DtoCasoList comTotalProcessos(Caso caso) {
		return new DtoCasoList(caso, processoRepository.countByCasoIdAndAtivoTrue(caso.getId()));
	}

	public DtoCasoDetalhe buscarDetalhe(Long id) {
		Caso caso = repository.getReferenceById(id);
		List<DtoProcessoList> processos = processoRepository.findByCasoIdAndAtivoTrue(id, Pageable.unpaged())
				.map(DtoProcessoList::new).toList();
		List<DtoTarefaList> tarefas = tarefaRepository.findByCasoIdAndAtivoTrue(id, Pageable.unpaged())
				.map(DtoTarefaList::new).toList();
		return new DtoCasoDetalhe(caso, processos, tarefas);
	}

	public Caso buscarEntidade(Long id) {
		return repository.getReferenceById(id);
	}

	public Caso cadastrar(DtoCadastroCaso dados) {
		Caso caso = new Caso();
		caso.setTitulo(dados.titulo());
		caso.setDescricao(dados.descricao());
		caso.setCliente(clienteRepository.getReferenceById(dados.clienteId()));
		caso.setFuncionarioResponsavel(
				dados.funcionarioResponsavelId() != null ? funcionarioRepository.getReferenceById(dados.funcionarioResponsavelId()) : null);
		caso.setNatureza(dados.natureza());
		caso.setStatus(StatusCaso.ABERTO);
		caso.setAtivo(true);
		caso.setDataCadastro(LocalDateTime.now().withNano(0));
		caso.setDataAtualizacao(LocalDateTime.now().withNano(0));
		return repository.save(caso);
	}

	public Caso atualizar(DtoAtualizarCaso dados) {
		Caso caso = repository.getReferenceById(dados.id());
		caso.setTitulo(dados.titulo());
		caso.setDescricao(dados.descricao());
		caso.setCliente(clienteRepository.getReferenceById(dados.clienteId()));
		caso.setFuncionarioResponsavel(
				dados.funcionarioResponsavelId() != null ? funcionarioRepository.getReferenceById(dados.funcionarioResponsavelId()) : null);
		if (dados.natureza() != null) {
			caso.setNatureza(dados.natureza());
		}
		if (dados.status() != null) {
			caso.setStatus(dados.status());
		}
		if (dados.ativo() != null) {
			caso.setAtivo(dados.ativo());
		}
		caso.setDataAtualizacao(LocalDateTime.now().withNano(0));
		return repository.save(caso);
	}

	public void alterarStatus(Long id, StatusCaso status) {
		Caso caso = repository.getReferenceById(id);
		caso.setStatus(status);
		caso.setDataAtualizacao(LocalDateTime.now().withNano(0));
		repository.save(caso);
	}

	public void inativar(Long id) {
		Caso caso = repository.getReferenceById(id);
		caso.setAtivo(false);
		repository.save(caso);
	}

}
