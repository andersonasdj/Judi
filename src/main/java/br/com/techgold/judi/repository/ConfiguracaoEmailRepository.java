package br.com.techgold.judi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.techgold.judi.model.ConfiguracaoEmail;

public interface ConfiguracaoEmailRepository extends JpaRepository<ConfiguracaoEmail, Long> {
	
	@Query(value = "SELECT COUNT(*) FROM configEmail", nativeQuery = true)
	public int existsConfigEmails();
	
	@Query(value = "SELECT * FROM configEmail e ORDER BY e.agendamento", nativeQuery = true)
	public List<ConfiguracaoEmail> listarEmails();
	
	@Query(value = "SELECT * FROM configEmail e WHERE e.agendamento=:agendamentos", nativeQuery = true)
	public ConfiguracaoEmail buscaPorConfiguracao(String agendamentos);

}
