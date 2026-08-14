package br.com.techgold.judi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import br.com.techgold.judi.model.Caso;
import br.com.techgold.judi.model.enums.StatusCaso;

public interface CasoRepository extends JpaRepository<Caso, Long>, JpaSpecificationExecutor<Caso> {

	public Page<Caso> findByAtivoTrue(Pageable page);

	public long countByStatusAndAtivoTrue(StatusCaso status);

	public Page<Caso> findByClienteIdAndAtivoTrue(Long clienteId, Pageable page);

	public Page<Caso> findByFuncionarioResponsavelIdAndAtivoTrue(Long funcionarioId, Pageable page);

	public Page<Caso> findByAtivoTrueOrderByDataCadastroDesc(Pageable page);

	@Query(value = "SELECT c FROM Caso c JOIN c.cliente cl "
			+ "WHERE c.ativo = true AND (c.titulo LIKE CONCAT('%', :conteudo, '%') "
			+ "OR cl.nomeCliente LIKE CONCAT('%', :conteudo, '%')) "
			+ "ORDER BY c.id DESC")
	public Page<Caso> buscarPorPalavra(Pageable page, String conteudo);

}
