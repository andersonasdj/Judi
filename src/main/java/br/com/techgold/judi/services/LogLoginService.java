package br.com.techgold.judi.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.techgold.judi.dto.DtoLogAcesso;
import br.com.techgold.judi.dto.DtoLogin;
import br.com.techgold.judi.model.LogLogin;
import br.com.techgold.judi.repository.LogLoginRepository;

@Component
public class LogLoginService {
	
	final LogLoginRepository repository;
	final FuncionarioService funcionarioService;

	LogLoginService(LogLoginRepository repository, FuncionarioService funcionarioService) {
		this.repository = repository;
		this.funcionarioService = funcionarioService;
	}

	public void salvaLog(LogLogin login) {
		repository.save(login);
	}
	
	public List<DtoLogAcesso> listarLogs(){
		return repository.lstarTodos().stream().map(DtoLogAcesso::new).toList();
	}

	public DtoLogin buscarPrimeiroLogin(Long id, LocalDate ini, LocalDate termino) {
		
		LocalDateTime inicio, fim;
		
		if(ini != null  && termino != null ) {
			inicio = ini.atTime(00, 00, 00);
			fim = termino.atTime(23, 59, 59);
			return repository.buscarPrimeiroLogin(funcionarioService.buscaNomeFuncionarioPorId(id), inicio, fim);
		}else {
			inicio = LocalDateTime.now().withNano(0);
			fim = LocalDateTime.now().withNano(0);
			return repository.buscarPrimeiroLogin(funcionarioService.buscaNomeFuncionarioPorId(id), inicio, fim);
		}
		
	}
	
}
