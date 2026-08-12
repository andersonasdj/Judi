package br.com.techgold.judi.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.techgold.judi.dto.DtoLogin;
import br.com.techgold.judi.model.LogLogin;

public interface LogLoginRepository extends JpaRepository<br.com.techgold.judi.model.LogLogin, Long>{
	
	@Query(nativeQuery = true, value = "SELECT * FROM logLogin s ORDER BY s.id DESC LIMIT 200")
	public List<LogLogin> lstarTodos();

	@Query(nativeQuery = true,
			value = "SELECT l.dataLogin "
			+ "FROM logLogin l "
			+ "WHERE l.usuario=:usuario "
			+ "AND l.dataLogin >= :inicio "
			+ "AND l.dataLogin <= :fim "
			+ "ORDER BY l.id "
			+ "LIMIT 1")
	public DtoLogin buscarPrimeiroLogin(String usuario, LocalDateTime inicio, LocalDateTime fim);

}
