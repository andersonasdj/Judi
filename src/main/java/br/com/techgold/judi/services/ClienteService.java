package br.com.techgold.judi.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoAtualizarCliente;
import br.com.techgold.judi.dto.DtoCadastroCliente;
import br.com.techgold.judi.dto.DtoClienteList;
import br.com.techgold.judi.model.Cliente;
import br.com.techgold.judi.repository.ClienteRepository;

@Service
public class ClienteService {

	final ClienteRepository repository;

	ClienteService(ClienteRepository repository) {
		this.repository = repository;
	}

	public List<DtoClienteList> listarAtivos() {
		return repository.listarClientes().stream().map(DtoClienteList::new).toList();
	}

	public Page<DtoClienteList> listarClientePorPalavra(Pageable page, String conteudo) {
		return repository.listarClientesPorPalavra(page, conteudo).map(DtoClienteList::new);
	}

	@Cacheable(value="nomesClientesAtivos")
	public List<String> listarNomesClienteAtivos() {
		return repository.listarNomesClienteAtivos();
	}

	@Cacheable(value="idClientesAtivos")
	public List<String> listarIdClienteAtivos() {
		return repository.listarIdClienteAtivos();
	}

	@Cacheable(value="todosOsClientes")
	public Page<DtoClienteList> listarTodos(Pageable page) {
		return repository.findAll(page).map(DtoClienteList::new);
	}

	@Cacheable(value="bairrosClientes")
	public List<String> listarBairrosClientes(){
		List<String> listaDeBairros = new ArrayList<>();
		List<String> listaCompletaBairros = repository.listarBairrosClientes();

		listaCompletaBairros.forEach( b -> {
			if(b != null && !b.isBlank() && !listaDeBairros.contains(b)) {
				listaDeBairros.add(b);
			}
		});
		return listaDeBairros;
	}

	@CacheEvict(value = {"todosOsClientes","nomesClientesAtivos","idClientesAtivos","bairrosClientes"}, allEntries = true)
	public void cadastrarNovoCliente(DtoCadastroCliente dados) {
		repository.save(new Cliente(dados));
	}

	@CacheEvict(value = {"todosOsClientes","nomesClientesAtivos","idClientesAtivos","bairrosClientes"}, allEntries = true)
	public Cliente atualizarCliente(DtoAtualizarCliente dados) {
		return repository.save(new Cliente(dados));
	}

	public Cliente buscaClientePorNome(Long dados) {
		return  repository.getReferenceById(dados);
	}

}
