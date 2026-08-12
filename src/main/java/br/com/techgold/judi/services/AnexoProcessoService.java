package br.com.techgold.judi.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.techgold.judi.dto.DtoAnexoProcesso;
import br.com.techgold.judi.model.AnexoProcesso;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.Processo;
import br.com.techgold.judi.model.UserRole;
import br.com.techgold.judi.repository.AnexoProcessoRepository;
import br.com.techgold.judi.repository.FuncionarioRepository;
import br.com.techgold.judi.repository.ProcessoRepository;

/**
 * Guarda o arquivo em disco sob {@code upload.dir}/processos/{processoId}/{nomeArmazenado}
 * — nomeArmazenado é sempre um UUID gerado pelo servidor (nunca o nome enviado
 * pelo usuário), o que evita colisão de nomes e travessia de diretório.
 */
@Service
public class AnexoProcessoService {

	@Value("${upload.dir}")
	private String uploadDir;

	private final AnexoProcessoRepository repository;
	private final ProcessoRepository processoRepository;
	private final FuncionarioRepository funcionarioRepository;

	AnexoProcessoService(AnexoProcessoRepository repository, ProcessoRepository processoRepository, FuncionarioRepository funcionarioRepository) {
		this.repository = repository;
		this.processoRepository = processoRepository;
		this.funcionarioRepository = funcionarioRepository;
	}

	public List<DtoAnexoProcesso> listarPorProcesso(Long processoId) {
		return repository.findByProcessoIdOrderByDataUploadDesc(processoId).stream().map(DtoAnexoProcesso::new).toList();
	}

	public AnexoProcesso cadastrar(Long processoId, MultipartFile arquivo, Funcionario funcionarioLogado) {
		if (arquivo == null || arquivo.isEmpty()) {
			throw new IllegalStateException("Selecione um arquivo para enviar.");
		}

		Processo processo = processoRepository.getReferenceById(processoId);
		Funcionario funcionario = funcionarioRepository.getReferenceById(funcionarioLogado.getId());

		String nomeOriginal = sanitizarNomeOriginal(arquivo.getOriginalFilename());
		String extensao = extrairExtensao(nomeOriginal);
		String nomeArmazenado = UUID.randomUUID() + (extensao.isEmpty() ? "" : "." + extensao);

		Path pasta = diretorioDoProcesso(processoId);
		try {
			Files.createDirectories(pasta);
			arquivo.transferTo(pasta.resolve(nomeArmazenado));
		} catch (IOException e) {
			throw new IllegalStateException("Falha ao salvar o arquivo: " + e.getMessage(), e);
		}

		AnexoProcesso anexo = new AnexoProcesso();
		anexo.setProcesso(processo);
		anexo.setFuncionario(funcionario);
		anexo.setNomeOriginal(nomeOriginal);
		anexo.setNomeArmazenado(nomeArmazenado);
		anexo.setMimeType(arquivo.getContentType());
		anexo.setTamanho(arquivo.getSize());
		anexo.setDataUpload(LocalDateTime.now().withNano(0));
		return repository.save(anexo);
	}

	public AnexoProcesso buscar(Long id) {
		return repository.getReferenceById(id);
	}

	public Path caminhoFisico(AnexoProcesso anexo) {
		return diretorioDoProcesso(anexo.getProcesso().getId()).resolve(anexo.getNomeArmazenado());
	}

	public void excluir(Long id, Funcionario funcionarioLogado) {
		AnexoProcesso anexo = repository.getReferenceById(id);

		boolean admin = funcionarioLogado.getRole() == UserRole.ADMIN || funcionarioLogado.getRole() == UserRole.SADMIN;
		if (!admin && !anexo.getFuncionario().getId().equals(funcionarioLogado.getId())) {
			throw new IllegalStateException("Você só pode excluir anexos enviados por você.");
		}

		Path caminho = caminhoFisico(anexo);
		repository.delete(anexo);
		try {
			Files.deleteIfExists(caminho);
		} catch (IOException e) {
			// registro já removido do banco — um arquivo órfão em disco não deve travar a exclusão
		}
	}

	private Path diretorioDoProcesso(Long processoId) {
		return Paths.get(uploadDir, "processos", String.valueOf(processoId));
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
