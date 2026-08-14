package br.com.techgold.judi.restcontroller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.dto.DtoDespesaFinanceiro;
import br.com.techgold.judi.dto.DtoFiltroFinanceiro;
import br.com.techgold.judi.dto.DtoFinanceiroResumo;
import br.com.techgold.judi.services.FinanceiroService;

@RestController
@RequestMapping("financeiro")
public class FinanceiroRestController {

	private final FinanceiroService service;

	FinanceiroRestController(FinanceiroService service) {
		this.service = service;
	}

	@GetMapping("/relatorio")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
	public DtoFinanceiroResumo relatorio(
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long funcionarioId,
			@RequestParam(required = false) Long processoId,
			@RequestParam(required = false) Long casoId,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataFim,
			@RequestParam(required = false) Boolean reembolsada) {
		return service.relatorio(new DtoFiltroFinanceiro(clienteId, funcionarioId, processoId, casoId, dataInicio, dataFim, reembolsada));
	}

	@GetMapping("/despesas")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
	public Page<DtoDespesaFinanceiro> despesas(
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long funcionarioId,
			@RequestParam(required = false) Long processoId,
			@RequestParam(required = false) Long casoId,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataFim,
			@RequestParam(required = false) Boolean reembolsada,
			@PageableDefault(size = 20, sort = { "data" }, direction = Direction.DESC) Pageable page) {
		return service.listar(new DtoFiltroFinanceiro(clienteId, funcionarioId, processoId, casoId, dataInicio, dataFim, reembolsada), page);
	}

	@GetMapping("/despesas/exportar")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
	public List<DtoDespesaFinanceiro> exportar(
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long funcionarioId,
			@RequestParam(required = false) Long processoId,
			@RequestParam(required = false) Long casoId,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataFim,
			@RequestParam(required = false) Boolean reembolsada) {
		return service.listarTudo(new DtoFiltroFinanceiro(clienteId, funcionarioId, processoId, casoId, dataInicio, dataFim, reembolsada));
	}

}
