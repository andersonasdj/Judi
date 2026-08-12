package br.com.techgold.judi.restcontroller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.multipart.MultipartFile;

import br.com.techgold.judi.datajud.DataJudSyncService;
import br.com.techgold.judi.dto.DtoAtualizarProcesso;
import br.com.techgold.judi.dto.DtoCadastroMovimentacaoManual;
import br.com.techgold.judi.dto.DtoCadastroProcesso;
import br.com.techgold.judi.dto.DtoMovimentacao;
import br.com.techgold.judi.dto.DtoProcessoDetalhe;
import br.com.techgold.judi.dto.DtoProcessoList;
import br.com.techgold.judi.dto.DtoResultadoImportacaoProcessos;
import br.com.techgold.judi.services.MovimentacaoProcessoService;
import br.com.techgold.judi.services.ProcessoService;

@RestController
@RequestMapping("processos")
public class ProcessoRestController {

	private final ProcessoService service;
	private final MovimentacaoProcessoService movimentacaoService;
	private final DataJudSyncService dataJudSyncService;

	ProcessoRestController(ProcessoService service, MovimentacaoProcessoService movimentacaoService, DataJudSyncService dataJudSyncService) {
		this.service = service;
		this.movimentacaoService = movimentacaoService;
		this.dataJudSyncService = dataJudSyncService;
	}

	@GetMapping
	public ResponseEntity<Page<DtoProcessoList>> listar(@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return ResponseEntity.ok(service.listar(page));
	}

	@GetMapping("/nome/{conteudo}")
	public Page<DtoProcessoList> buscarPorPalavra(@PathVariable String conteudo,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.buscarPorPalavra(page, conteudo);
	}

	@GetMapping("/cliente/{clienteId}")
	public Page<DtoProcessoList> listarPorCliente(@PathVariable Long clienteId,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorCliente(clienteId, page);
	}

	@GetMapping("/funcionario/{funcionarioId}")
	public Page<DtoProcessoList> listarPorFuncionario(@PathVariable Long funcionarioId,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorFuncionario(funcionarioId, page);
	}

	@GetMapping("/{id}")
	public DtoProcessoDetalhe buscar(@PathVariable Long id) {
		return service.buscarDetalhe(id);
	}

	@PostMapping
	public void cadastrar(@RequestBody DtoCadastroProcesso dados) {
		service.cadastrar(dados);
	}

	@PostMapping(value = "/importar/{clienteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DtoResultadoImportacaoProcessos importar(@PathVariable Long clienteId, @RequestParam("arquivo") MultipartFile arquivo) {
		return service.importarCsv(clienteId, arquivo);
	}

	@PutMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void atualizar(@RequestBody DtoAtualizarProcesso dados) {
		service.atualizar(dados);
	}

	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void inativar(@PathVariable Long id) {
		service.inativar(id);
	}

	@GetMapping("/{id}/movimentacoes")
	public List<DtoMovimentacao> listarMovimentacoes(@PathVariable Long id) {
		return movimentacaoService.listarPorProcesso(id);
	}

	@PostMapping("/{id}/movimentacoes")
	public void registrarMovimentacao(@PathVariable Long id, @RequestBody DtoCadastroMovimentacaoManual dados) {
		movimentacaoService.registrarManual(id, dados);
	}

	@PostMapping("/{id}/sincronizar")
	public void sincronizar(@PathVariable Long id) {
		dataJudSyncService.sincronizarProcesso(service.buscarEntidade(id));
	}

	@PostMapping("/sincronizar-todos")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void sincronizarTodos() {
		dataJudSyncService.sincronizarTodos();
	}

}
