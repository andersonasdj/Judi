package br.com.techgold.judi.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.techgold.judi.dto.DtoCadastroDespesaTarefa;
import br.com.techgold.judi.dto.DtoDespesaTarefa;
import br.com.techgold.judi.model.DespesaTarefa;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.Tarefa;
import br.com.techgold.judi.model.UserRole;
import br.com.techgold.judi.model.enums.StatusTarefa;
import br.com.techgold.judi.repository.DespesaTarefaRepository;
import br.com.techgold.judi.repository.FuncionarioRepository;
import br.com.techgold.judi.repository.TarefaRepository;

@Service
public class DespesaTarefaService {

	private static final Set<String> EXTENSOES_COMPROVANTE_PERMITIDAS = Set.of("pdf", "jpg", "jpeg", "png", "gif", "webp");

	@Value("${upload.dir}")
	private String uploadDir;

	private final DespesaTarefaRepository repository;
	private final TarefaRepository tarefaRepository;
	private final FuncionarioRepository funcionarioRepository;

	DespesaTarefaService(DespesaTarefaRepository repository, TarefaRepository tarefaRepository, FuncionarioRepository funcionarioRepository) {
		this.repository = repository;
		this.tarefaRepository = tarefaRepository;
		this.funcionarioRepository = funcionarioRepository;
	}

	public List<DtoDespesaTarefa> listarPorTarefa(Long tarefaId) {
		return repository.findByTarefaIdOrderByDataDesc(tarefaId).stream().map(DtoDespesaTarefa::new).toList();
	}

	public Page<DtoDespesaTarefa> listarPorFuncionario(Long funcionarioId, Pageable page) {
		return repository.findByFuncionarioIdOrderByDataDesc(funcionarioId, page).map(DtoDespesaTarefa::new);
	}

	public Page<DtoDespesaTarefa> listarPendentes(Pageable page) {
		return repository.findByReembolsadaFalseOrderByDataDesc(page).map(DtoDespesaTarefa::new);
	}

	public DespesaTarefa cadastrar(DtoCadastroDespesaTarefa dados, Funcionario funcionarioLogado) {
		Tarefa tarefa = tarefaRepository.getReferenceById(dados.tarefaId());
		if (tarefa.getStatus().isFinal()) {
			throw new IllegalStateException("Esta tarefa já está " + (tarefa.getStatus() == StatusTarefa.CONCLUIDA ? "concluída" : "cancelada")
					+ " e não aceita mais despesas.");
		}

		DespesaTarefa despesa = new DespesaTarefa();
		despesa.setTarefa(tarefa);
		despesa.setFuncionario(funcionarioRepository.getReferenceById(funcionarioLogado.getId()));
		despesa.setDescricao(dados.descricao());
		despesa.setValor(dados.valor());
		despesa.setData(dados.data());
		despesa.setReembolsada(false);
		despesa.setDataRegistro(LocalDateTime.now().withNano(0));
		return repository.save(despesa);
	}

	public void marcarReembolsada(Long id) {
		DespesaTarefa despesa = repository.getReferenceById(id);
		despesa.setReembolsada(true);
		despesa.setDataReembolso(LocalDateTime.now().withNano(0));
		repository.save(despesa);
	}

	public void excluir(Long id) {
		repository.deleteById(id);
	}

	/**
	 * Substitui o comprovante da despesa (se já houver um, o arquivo antigo é
	 * removido do disco). Só o funcionário que registrou a despesa ou um
	 * ROLE_ADMIN pode anexar/trocar o comprovante.
	 */
	public DespesaTarefa anexarComprovante(Long despesaId, MultipartFile arquivo, Funcionario funcionarioLogado) {
		if (arquivo == null || arquivo.isEmpty()) {
			throw new IllegalStateException("Selecione um arquivo para enviar.");
		}

		DespesaTarefa despesa = repository.getReferenceById(despesaId);
		validarPermissao(despesa, funcionarioLogado);

		String nomeOriginal = sanitizarNomeOriginal(arquivo.getOriginalFilename());
		String extensao = extrairExtensao(nomeOriginal).toLowerCase();
		if (!EXTENSOES_COMPROVANTE_PERMITIDAS.contains(extensao)) {
			throw new IllegalStateException("Formato não suportado. Envie um PDF ou uma imagem (jpg, png, gif, webp).");
		}

		String nomeArmazenadoAntigo = despesa.getComprovanteNomeArmazenado();
		String nomeArmazenado = UUID.randomUUID() + "." + extensao;

		Path pasta = diretorioDaDespesa(despesaId);
		try {
			Files.createDirectories(pasta);
			arquivo.transferTo(pasta.resolve(nomeArmazenado));
		} catch (IOException e) {
			throw new IllegalStateException("Falha ao salvar o arquivo: " + e.getMessage(), e);
		}

		if (nomeArmazenadoAntigo != null) {
			try {
				Files.deleteIfExists(pasta.resolve(nomeArmazenadoAntigo));
			} catch (IOException e) {
				// comprovante novo já salvo — um arquivo antigo órfão em disco não deve travar a troca
			}
		}

		despesa.setComprovanteNomeOriginal(nomeOriginal);
		despesa.setComprovanteNomeArmazenado(nomeArmazenado);
		despesa.setComprovanteMimeType(arquivo.getContentType());
		despesa.setComprovanteTamanho(arquivo.getSize());
		return repository.save(despesa);
	}

	public DespesaTarefa buscarEntidade(Long id) {
		return repository.getReferenceById(id);
	}

	public Path caminhoComprovante(DespesaTarefa despesa) {
		return diretorioDaDespesa(despesa.getId()).resolve(despesa.getComprovanteNomeArmazenado());
	}

	public void removerComprovante(Long despesaId, Funcionario funcionarioLogado) {
		DespesaTarefa despesa = repository.getReferenceById(despesaId);
		validarPermissao(despesa, funcionarioLogado);

		if (despesa.getComprovanteNomeArmazenado() == null) {
			return;
		}

		Path caminho = diretorioDaDespesa(despesaId).resolve(despesa.getComprovanteNomeArmazenado());
		despesa.setComprovanteNomeOriginal(null);
		despesa.setComprovanteNomeArmazenado(null);
		despesa.setComprovanteMimeType(null);
		despesa.setComprovanteTamanho(null);
		repository.save(despesa);
		try {
			Files.deleteIfExists(caminho);
		} catch (IOException e) {
			// registro já atualizado — um arquivo órfão em disco não deve travar a remoção
		}
	}

	private void validarPermissao(DespesaTarefa despesa, Funcionario funcionarioLogado) {
		boolean admin = funcionarioLogado.getRole() == UserRole.ADMIN || funcionarioLogado.getRole() == UserRole.SADMIN;
		if (!admin && !despesa.getFuncionario().getId().equals(funcionarioLogado.getId())) {
			throw new IllegalStateException("Você só pode anexar ou remover comprovante das suas próprias despesas.");
		}
	}

	private Path diretorioDaDespesa(Long despesaId) {
		return Paths.get(uploadDir, "despesas", String.valueOf(despesaId));
	}

	private String sanitizarNomeOriginal(String nome) {
		if (nome == null || nome.isBlank()) return "arquivo";
		String semCaminho = nome.replace("\\", "/");
		semCaminho = semCaminho.substring(semCaminho.lastIndexOf('/') + 1);
		return semCaminho.length() > 255 ? semCaminho.substring(0, 255) : semCaminho;
	}

	private String extrairExtensao(String nomeOriginal) {
		int ponto = nomeOriginal.lastIndexOf('.');
		if (ponto < 0 || ponto == nomeOriginal.length() - 1) return "";
		String ext = nomeOriginal.substring(ponto + 1).replaceAll("[^a-zA-Z0-9]", "");
		return ext.length() > 10 ? ext.substring(0, 10) : ext;
	}

}
