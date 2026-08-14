package br.com.techgold.judi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.techgold.judi.model.ClienteFuncionarioValorHora;

public interface ClienteFuncionarioValorHoraRepository extends JpaRepository<ClienteFuncionarioValorHora, Long> {

	List<ClienteFuncionarioValorHora> findByClienteId(Long clienteId);

	Optional<ClienteFuncionarioValorHora> findByClienteIdAndFuncionarioId(Long clienteId, Long funcionarioId);

	void deleteByClienteIdAndFuncionarioId(Long clienteId, Long funcionarioId);

}
