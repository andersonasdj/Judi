package br.com.techgold.judi.restcontroller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.com.techgold.judi.dto.DtoCadastroDespesaTarefa;
import br.com.techgold.judi.dto.DtoDespesaTarefa;
import br.com.techgold.judi.model.DespesaTarefa;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.services.DespesaTarefaService;
import br.com.techgold.judi.services.FuncionarioService;

@RestController
@RequestMapping("despesas")
public class DespesaTarefaRestController {

	private final DespesaTarefaService service;
	private final FuncionarioService funcionarioService;

	DespesaTarefaRestController(DespesaTarefaService service, FuncionarioService funcionarioService) {
		this.service = service;
		this.funcionarioService = funcionarioService;
	}

	/**
	 * O objeto guardado na sessão de login pode estar desatualizado/desanexado
	 * da transação atual, por isso sempre buscamos o funcionário de novo —
	 * mesmo padrão já usado em UserAuthenticationFilter/AppController.
	 */
	private Funcionario funcionarioAutenticado() {
		Funcionario principal = (Funcionario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return funcionarioService.buscaPorNome(principal.getNomeFuncionario());
	}

	@GetMapping("/tarefa/{tarefaId}")
	public List<DtoDespesaTarefa> listarPorTarefa(@PathVariable Long tarefaId) {
		return service.listarPorTarefa(tarefaId);
	}

	@GetMapping("/funcionario/{funcionarioId}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Page<DtoDespesaTarefa> listarPorFuncionario(@PathVariable Long funcionarioId,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorFuncionario(funcionarioId, page);
	}

	@GetMapping("/minhas")
	public Page<DtoDespesaTarefa> minhasDespesas(@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorFuncionario(funcionarioAutenticado().getId(), page);
	}

	@GetMapping("/pendentes")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Page<DtoDespesaTarefa> pendentes(@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPendentes(page);
	}

	@PostMapping
	public void cadastrar(@RequestBody DtoCadastroDespesaTarefa dados) {
		service.cadastrar(dados, funcionarioAutenticado());
	}

	@PutMapping("/{id}/reembolsar")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void reembolsar(@PathVariable Long id) {
		service.marcarReembolsada(id);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void excluir(@PathVariable Long id) {
		service.excluir(id);
	}

	@PostMapping("/{id}/comprovante")
	public void anexarComprovante(@PathVariable Long id, @RequestParam("arquivo") MultipartFile arquivo) {
		service.anexarComprovante(id, arquivo, funcionarioAutenticado());
	}

	@GetMapping("/{id}/comprovante")
	public ResponseEntity<Resource> baixarComprovante(@PathVariable Long id) throws MalformedURLException {
		DespesaTarefa despesa = service.buscarEntidade(id);
		if (despesa.getComprovanteNomeArmazenado() == null) {
			return ResponseEntity.notFound().build();
		}

		Path caminho = service.caminhoComprovante(despesa);
		Resource recurso = new UrlResource(caminho.toUri());
		if (!recurso.exists() || !recurso.isReadable()) {
			return ResponseEntity.notFound().build();
		}

		MediaType tipo;
		try {
			tipo = despesa.getComprovanteMimeType() != null ? MediaType.parseMediaType(despesa.getComprovanteMimeType()) : MediaType.APPLICATION_OCTET_STREAM;
		} catch (Exception e) {
			tipo = MediaType.APPLICATION_OCTET_STREAM;
		}

		return ResponseEntity.ok()
				.contentType(tipo)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + despesa.getComprovanteNomeOriginal().replace("\"", "") + "\"")
				.body(recurso);
	}

	@DeleteMapping("/{id}/comprovante")
	public void removerComprovante(@PathVariable Long id) {
		service.removerComprovante(id, funcionarioAutenticado());
	}

}
