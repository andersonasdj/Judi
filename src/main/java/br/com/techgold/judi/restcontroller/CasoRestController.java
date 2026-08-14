package br.com.techgold.judi.restcontroller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.dto.DtoAtualizarCaso;
import br.com.techgold.judi.dto.DtoCadastroCaso;
import br.com.techgold.judi.dto.DtoCasoDetalhe;
import br.com.techgold.judi.dto.DtoCasoList;
import br.com.techgold.judi.dto.DtoFiltroCaso;
import br.com.techgold.judi.model.enums.NaturezaCaso;
import br.com.techgold.judi.model.enums.StatusCaso;
import br.com.techgold.judi.services.CasoService;

@RestController
@RequestMapping("casos")
public class CasoRestController {

	private final CasoService service;

	CasoRestController(CasoService service) {
		this.service = service;
	}

	@GetMapping
	public Page<DtoCasoList> listar(@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listar(page);
	}

	@GetMapping("/nome/{conteudo}")
	public Page<DtoCasoList> buscarPorPalavra(@PathVariable String conteudo,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.buscarPorPalavra(page, conteudo);
	}

	@GetMapping("/filtro")
	public Page<DtoCasoList> filtrar(
			@RequestParam(required = false) String texto,
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long funcionarioId,
			@RequestParam(required = false) NaturezaCaso natureza,
			@RequestParam(required = false) StatusCaso status,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarFiltrado(new DtoFiltroCaso(texto, clienteId, funcionarioId, natureza, status), page);
	}

	@GetMapping("/cliente/{clienteId}")
	public Page<DtoCasoList> listarPorCliente(@PathVariable Long clienteId,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorCliente(clienteId, page);
	}

	@GetMapping("/funcionario/{funcionarioId}")
	public Page<DtoCasoList> listarPorFuncionario(@PathVariable Long funcionarioId,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorFuncionario(funcionarioId, page);
	}

	@GetMapping("/{id}")
	public DtoCasoDetalhe buscar(@PathVariable Long id) {
		return service.buscarDetalhe(id);
	}

	@PostMapping
	public void cadastrar(@RequestBody DtoCadastroCaso dados) {
		service.cadastrar(dados);
	}

	@PutMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void atualizar(@RequestBody DtoAtualizarCaso dados) {
		service.atualizar(dados);
	}

	@PutMapping("/{id}/status/{status}")
	public void alterarStatus(@PathVariable Long id, @PathVariable StatusCaso status) {
		service.alterarStatus(id, status);
	}

	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void inativar(@PathVariable Long id) {
		service.inativar(id);
	}

}
