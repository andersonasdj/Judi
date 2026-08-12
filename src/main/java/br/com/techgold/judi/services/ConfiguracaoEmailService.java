package br.com.techgold.judi.services;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoEmails;
import br.com.techgold.judi.model.ConfiguracaoEmail;
import br.com.techgold.judi.repository.ConfiguracaoEmailRepository;
import jakarta.transaction.Transactional;

@Service
public class ConfiguracaoEmailService {

	final ConfiguracaoEmailRepository repository;

	ConfiguracaoEmailService(ConfiguracaoEmailRepository repository) {
		this.repository = repository;
	}
	
	public void cadastra(ConfiguracaoEmail email) {
		repository.save(email);
	}
	
	public int existeEmail() {
		return repository.existsConfigEmails();
	}

	public List<ConfiguracaoEmail> listarEmails() {
		
		return repository.listarEmails();
	}

	@Transactional
	public void atualiza(List<DtoEmails> dados) {
		
		dados.forEach(d -> {
			repository.save(new ConfiguracaoEmail(d));
		});
		
	}
	
	public ConfiguracaoEmail buscaConfiguracao(String agendamentos) {
		return repository.buscaPorConfiguracao(agendamentos);
	}
	
}
