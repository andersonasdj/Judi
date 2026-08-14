package br.com.techgold.judi.restcontroller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
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

import br.com.techgold.judi.dto.DtoAgendarTarefa;
import br.com.techgold.judi.dto.DtoAtualizarTarefa;
import br.com.techgold.judi.dto.DtoCadastroTarefa;
import br.com.techgold.judi.dto.DtoFiltroTarefa;
import br.com.techgold.judi.dto.DtoTarefaDetalhe;
import br.com.techgold.judi.dto.DtoTarefaList;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.model.UserRole;
import br.com.techgold.judi.model.enums.StatusTarefa;
import br.com.techgold.judi.services.FuncionarioService;
import br.com.techgold.judi.services.TarefaService;

@RestController
@RequestMapping("tarefas")
public class TarefaRestController {

	private final TarefaService service;
	private final FuncionarioService funcionarioService;

	TarefaRestController(TarefaService service, FuncionarioService funcionarioService) {
		this.service = service;
		this.funcionarioService = funcionarioService;
	}

	/**
	 * Sempre busca de novo no banco em vez de confiar no objeto da sessão de
	 * login (ver bug de TransientPropertyValueException/id nulo já corrigido
	 * em TimesheetRestController/DespesaTarefaRestController).
	 */
	private Funcionario funcionarioAutenticado() {
		Funcionario principal = (Funcionario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return funcionarioService.buscaPorNome(principal.getNomeFuncionario());
	}

	private boolean admin() {
		Funcionario funcionario = funcionarioAutenticado();
		return funcionario.getRole() == UserRole.ADMIN || funcionario.getRole() == UserRole.SADMIN;
	}

	@GetMapping
	public Page<DtoTarefaList> listar(@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listar(page);
	}

	@GetMapping("/nome/{conteudo}")
	public Page<DtoTarefaList> buscarPorPalavra(@PathVariable String conteudo,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.buscarPorPalavra(page, conteudo);
	}

	@GetMapping("/filtro")
	public Page<DtoTarefaList> filtrar(
			@RequestParam(required = false) String texto,
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long processoId,
			@RequestParam(required = false) Long funcionarioId,
			@RequestParam(required = false) StatusTarefa status,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarFiltrado(new DtoFiltroTarefa(texto, clienteId, processoId, funcionarioId, status), page);
	}

	@GetMapping("/cliente/{clienteId}")
	public Page<DtoTarefaList> listarPorCliente(@PathVariable Long clienteId,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorCliente(clienteId, page);
	}

	@GetMapping("/processo/{processoId}")
	public Page<DtoTarefaList> listarPorProcesso(@PathVariable Long processoId,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorProcesso(processoId, page);
	}

	@GetMapping("/funcionario/{funcionarioId}")
	public Page<DtoTarefaList> listarPorFuncionario(@PathVariable Long funcionarioId,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorFuncionario(funcionarioId, page);
	}

	@GetMapping("/caso/{casoId}")
	public Page<DtoTarefaList> listarPorCaso(@PathVariable Long casoId,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorCaso(casoId, page);
	}

	@GetMapping("/{id}")
	public DtoTarefaDetalhe buscar(@PathVariable Long id) {
		return service.buscarDetalhe(id);
	}

	@PostMapping
	public void cadastrar(@RequestBody DtoCadastroTarefa dados) {
		service.cadastrar(dados, funcionarioAutenticado());
	}

	@PutMapping
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void atualizar(@RequestBody DtoAtualizarTarefa dados) {
		service.atualizar(dados, funcionarioAutenticado());
	}

	@PutMapping("/{id}/status/{status}")
	public void alterarStatus(@PathVariable Long id, @PathVariable StatusTarefa status) {
		service.alterarStatus(id, status, admin(), funcionarioAutenticado());
	}

	@PutMapping("/{id}/agendar")
	public void agendar(@PathVariable Long id, @RequestBody DtoAgendarTarefa dados) {
		service.agendar(id, dados.dataAgendamento(), funcionarioAutenticado());
	}

	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void inativar(@PathVariable Long id) {
		service.inativar(id);
	}

}
