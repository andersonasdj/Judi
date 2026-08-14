package br.com.techgold.judi.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoValorHoraEfetivo;
import br.com.techgold.judi.dto.DtoValorHoraFuncionario;
import br.com.techgold.judi.model.Cliente;
import br.com.techgold.judi.model.ClienteFuncionarioValorHora;
import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.repository.ClienteFuncionarioValorHoraRepository;
import br.com.techgold.judi.repository.ClienteRepository;
import br.com.techgold.judi.repository.FuncionarioRepository;
import jakarta.transaction.Transactional;

/**
 * Regra de negócio do valor/hora usado no cálculo de honorários pelo timesheet:
 * cada funcionário tem um valor/hora padrão ({@link Funcionario#getValorHora()});
 * cada cliente pode, opcionalmente, personalizar esse valor por funcionário
 * ({@link ClienteFuncionarioValorHora}); na ausência de personalização, vale o
 * padrão do funcionário.
 */
@Service
public class ValorHoraService {

	private final ClienteFuncionarioValorHoraRepository repository;
	private final ClienteRepository clienteRepository;
	private final FuncionarioRepository funcionarioRepository;

	ValorHoraService(ClienteFuncionarioValorHoraRepository repository, ClienteRepository clienteRepository, FuncionarioRepository funcionarioRepository) {
		this.repository = repository;
		this.clienteRepository = clienteRepository;
		this.funcionarioRepository = funcionarioRepository;
	}

	public List<DtoValorHoraFuncionario> listarPorCliente(Long clienteId) {
		Map<Long, BigDecimal> personalizados = new HashMap<>();
		repository.findByClienteId(clienteId).forEach(v -> personalizados.put(v.getFuncionario().getId(), v.getValorHora()));

		return funcionarioRepository.listarFuncionarios().stream()
				.sorted(Comparator.comparing(Funcionario::getNomeFuncionario))
				.map(f -> {
					BigDecimal personalizado = personalizados.get(f.getId());
					BigDecimal efetivo = personalizado != null ? personalizado : f.getValorHora();
					return new DtoValorHoraFuncionario(f.getId(), f.getNomeFuncionario(), f.getValorHora(), personalizado, efetivo);
				})
				.toList();
	}

	@Transactional
	public void salvar(Long clienteId, Long funcionarioId, BigDecimal valorHora) {
		if (valorHora == null) {
			repository.deleteByClienteIdAndFuncionarioId(clienteId, funcionarioId);
			return;
		}

		ClienteFuncionarioValorHora registro = repository.findByClienteIdAndFuncionarioId(clienteId, funcionarioId).orElseGet(() -> {
			Cliente cliente = clienteRepository.getReferenceById(clienteId);
			Funcionario funcionario = funcionarioRepository.getReferenceById(funcionarioId);
			ClienteFuncionarioValorHora novo = new ClienteFuncionarioValorHora();
			novo.setCliente(cliente);
			novo.setFuncionario(funcionario);
			return novo;
		});
		registro.setValorHora(valorHora);
		registro.setDataAtualizacao(LocalDateTime.now().withNano(0));
		repository.save(registro);
	}

	/** Valor/hora a usar no cálculo dos honorários: personalizado do cliente, com fallback para o padrão do funcionário. */
	public BigDecimal valorHoraEfetivo(Long clienteId, Long funcionarioId) {
		return efetivoComOrigem(clienteId, funcionarioId).valor();
	}

	/** Igual a {@link #valorHoraEfetivo}, mas informando também se o valor veio da personalização do cliente. */
	public DtoValorHoraEfetivo efetivoComOrigem(Long clienteId, Long funcionarioId) {
		return repository.findByClienteIdAndFuncionarioId(clienteId, funcionarioId)
				.map(v -> new DtoValorHoraEfetivo(v.getValorHora(), true))
				.orElseGet(() -> new DtoValorHoraEfetivo(funcionarioRepository.getReferenceById(funcionarioId).getValorHora(), false));
	}

}
