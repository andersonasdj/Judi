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

import br.com.techgold.judi.dto.DtoFiltroFinanceiro;
import br.com.techgold.judi.dto.DtoHonorario;
import br.com.techgold.judi.dto.DtoHonorarioResumo;
import br.com.techgold.judi.services.HonorarioService;

@RestController
@RequestMapping("financeiro/honorarios")
public class HonorarioRestController {

	private final HonorarioService service;

	HonorarioRestController(HonorarioService service) {
		this.service = service;
	}

	@GetMapping("/relatorio")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
	public DtoHonorarioResumo relatorio(
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long funcionarioId,
			@RequestParam(required = false) Long processoId,
			@RequestParam(required = false) Long casoId,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataFim) {
		return service.relatorio(new DtoFiltroFinanceiro(clienteId, funcionarioId, processoId, casoId, dataInicio, dataFim, null));
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
	public Page<DtoHonorario> listar(
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long funcionarioId,
			@RequestParam(required = false) Long processoId,
			@RequestParam(required = false) Long casoId,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataFim,
			@PageableDefault(size = 20, sort = { "dataInicio" }, direction = Direction.DESC) Pageable page) {
		return service.listar(new DtoFiltroFinanceiro(clienteId, funcionarioId, processoId, casoId, dataInicio, dataFim, null), page);
	}

	@GetMapping("/exportar")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
	public List<DtoHonorario> exportar(
			@RequestParam(required = false) Long clienteId,
			@RequestParam(required = false) Long funcionarioId,
			@RequestParam(required = false) Long processoId,
			@RequestParam(required = false) Long casoId,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataInicio,
			@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dataFim) {
		return service.listarTudo(new DtoFiltroFinanceiro(clienteId, funcionarioId, processoId, casoId, dataInicio, dataFim, null));
	}

}
