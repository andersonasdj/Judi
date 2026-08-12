package br.com.techgold.judi.restcontroller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.dto.DtoAtualizarTimesheet;
import br.com.techgold.judi.dto.DtoCadastroTimesheetManual;
import br.com.techgold.judi.dto.DtoIniciarTimesheet;
import br.com.techgold.judi.dto.DtoTimesheet;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.services.FuncionarioService;
import br.com.techgold.judi.services.TimesheetService;

@RestController
@RequestMapping("timesheets")
public class TimesheetRestController {

	private final TimesheetService service;
	private final FuncionarioService funcionarioService;

	TimesheetRestController(TimesheetService service, FuncionarioService funcionarioService) {
		this.service = service;
		this.funcionarioService = funcionarioService;
	}

	/**
	 * O objeto guardado na sessão de login pode estar desatualizado/desanexado
	 * da transação atual (ver bug já corrigido de TransientPropertyValueException),
	 * por isso sempre buscamos o funcionário de novo — mesmo padrão já usado em
	 * UserAuthenticationFilter/AppController.
	 */
	private Funcionario funcionarioAutenticado() {
		Funcionario principal = (Funcionario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return funcionarioService.buscaPorNome(principal.getNomeFuncionario());
	}

	@GetMapping("/tarefa/{tarefaId}")
	public List<DtoTimesheet> listarPorTarefa(@PathVariable Long tarefaId) {
		return service.listarPorTarefa(tarefaId);
	}

	@GetMapping("/funcionario/{funcionarioId}")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Page<DtoTimesheet> listarPorFuncionario(@PathVariable Long funcionarioId,
			@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorFuncionario(funcionarioId, page);
	}

	@GetMapping("/meus")
	public Page<DtoTimesheet> meusTimesheets(@PageableDefault(size = 20, sort = { "id" }, direction = Direction.DESC) Pageable page) {
		return service.listarPorFuncionario(funcionarioAutenticado().getId(), page);
	}

	@GetMapping("/aberto")
	public DtoTimesheet meuAberto() {
		return service.buscarAbertoPorFuncionario(funcionarioAutenticado().getId());
	}

	@PostMapping("/iniciar/{tarefaId}")
	public void iniciar(@PathVariable Long tarefaId, @RequestBody(required = false) DtoIniciarTimesheet dados) {
		service.iniciar(tarefaId, funcionarioAutenticado(), dados != null ? dados.observacoes() : null);
	}

	@PutMapping("/{id}/finalizar")
	public void finalizar(@PathVariable Long id) {
		service.finalizar(id, funcionarioAutenticado());
	}

	@PostMapping("/manual")
	public void registrarManual(@RequestBody DtoCadastroTimesheetManual dados) {
		service.registrarManual(dados, funcionarioAutenticado());
	}

	@PutMapping("/{id}")
	public void atualizar(@PathVariable Long id, @RequestBody DtoAtualizarTimesheet dados) {
		service.atualizar(id, dados, funcionarioAutenticado());
	}

	@DeleteMapping("/{id}")
	public void excluir(@PathVariable Long id) {
		service.excluir(id, funcionarioAutenticado());
	}

}
