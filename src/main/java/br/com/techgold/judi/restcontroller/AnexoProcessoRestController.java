package br.com.techgold.judi.restcontroller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.techgold.judi.dto.DtoAnexoProcesso;
import br.com.techgold.judi.model.AnexoProcesso;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.services.AnexoProcessoService;
import br.com.techgold.judi.services.FuncionarioService;

@RestController
public class AnexoProcessoRestController {

	private final AnexoProcessoService service;
	private final FuncionarioService funcionarioService;

	AnexoProcessoRestController(AnexoProcessoService service, FuncionarioService funcionarioService) {
		this.service = service;
		this.funcionarioService = funcionarioService;
	}

	private Funcionario funcionarioAutenticado() {
		Funcionario principal = (Funcionario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return funcionarioService.buscaPorNome(principal.getNomeFuncionario());
	}

	@GetMapping("processos/{processoId}/anexos")
	public List<DtoAnexoProcesso> listar(@PathVariable Long processoId) {
		return service.listarPorProcesso(processoId);
	}

	@PostMapping("processos/{processoId}/anexos")
	public void enviar(@PathVariable Long processoId, @RequestParam("arquivo") MultipartFile arquivo) {
		service.cadastrar(processoId, arquivo, funcionarioAutenticado());
	}

	@GetMapping("anexos/{id}/download")
	public ResponseEntity<Resource> download(@PathVariable Long id) throws MalformedURLException {
		AnexoProcesso anexo = service.buscar(id);
		Path caminho = service.caminhoFisico(anexo);
		Resource recurso = new UrlResource(caminho.toUri());

		if (!recurso.exists() || !recurso.isReadable()) {
			return ResponseEntity.notFound().build();
		}

		MediaType tipo;
		try {
			tipo = anexo.getMimeType() != null ? MediaType.parseMediaType(anexo.getMimeType()) : MediaType.APPLICATION_OCTET_STREAM;
		} catch (Exception e) {
			tipo = MediaType.APPLICATION_OCTET_STREAM;
		}

		return ResponseEntity.ok()
				.contentType(tipo)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + anexo.getNomeOriginal().replace("\"", "") + "\"")
				.body(recurso);
	}

	@DeleteMapping("anexos/{id}")
	public void excluir(@PathVariable Long id) {
		service.excluir(id, funcionarioAutenticado());
	}

}
