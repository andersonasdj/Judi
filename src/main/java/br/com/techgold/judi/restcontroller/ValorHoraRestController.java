package br.com.techgold.judi.restcontroller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.techgold.judi.dto.DtoSalvarValorHora;
import br.com.techgold.judi.dto.DtoValorHoraFuncionario;
import br.com.techgold.judi.services.ValorHoraService;

@RestController
@RequestMapping("clientes/{clienteId}/valores-hora")
public class ValorHoraRestController {

	private final ValorHoraService service;

	ValorHoraRestController(ValorHoraService service) {
		this.service = service;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
	public List<DtoValorHoraFuncionario> listar(@PathVariable Long clienteId) {
		return service.listarPorCliente(clienteId);
	}

	@PutMapping("/{funcionarioId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCEIRO')")
	public void salvar(@PathVariable Long clienteId, @PathVariable Long funcionarioId, @RequestBody DtoSalvarValorHora dados) {
		service.salvar(clienteId, funcionarioId, dados.valorHora());
	}

}
