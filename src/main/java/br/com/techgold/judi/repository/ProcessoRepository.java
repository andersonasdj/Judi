package br.com.techgold.judi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import br.com.techgold.judi.model.Processo;
import br.com.techgold.judi.model.enums.StatusConsultaDataJud;
import br.com.techgold.judi.model.enums.StatusProcesso;

public interface ProcessoRepository extends JpaRepository<Processo, Long>, JpaSpecificationExecutor<Processo> {

	public boolean existsByNumeroProcesso(String numeroProcesso);

	public Page<Processo> findByAtivoTrue(Pageable page);

	public Page<Processo> findByClienteIdAndAtivoTrue(Long clienteId, Pageable page);

	public Page<Processo> findByFuncionarioResponsavelIdAndAtivoTrue(Long funcionarioId, Pageable page);

	public Page<Processo> findByCasoIdAndAtivoTrue(Long casoId, Pageable page);

	public long countByCasoIdAndAtivoTrue(Long casoId);

	public List<Processo> findByAtivoTrueAndMonitoradoTrue();

	public long countByAtivoTrue();

	public Page<Processo> findByAtivoTrueOrderByDataCadastroDesc(Pageable page);

	public long countByStatusAndAtivoTrue(StatusProcesso status);

	public long countByStatusUltimaConsultaAndAtivoTrue(StatusConsultaDataJud status);

	@Query(value = "SELECT p FROM Processo p JOIN p.cliente c "
			+ "WHERE p.ativo = true AND (p.numeroProcesso LIKE CONCAT('%', :conteudo, '%') "
			+ "OR c.nomeCliente LIKE CONCAT('%', :conteudo, '%')) "
			+ "ORDER BY p.id DESC")
	public Page<Processo> buscarPorPalavra(Pageable page, String conteudo);

}
