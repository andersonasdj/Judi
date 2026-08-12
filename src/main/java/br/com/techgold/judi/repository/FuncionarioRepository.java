package br.com.techgold.judi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.techgold.judi.model.Funcionario;
import br.com.techgold.judi.orm.DtoFuncionarioEditSimplificado;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
	
	@Query(value = "SELECT f.nomeFuncionario FROM funcionarios f  WHERE f.ativo=true ORDER BY f.nomeFuncionario", nativeQuery = true)
	public List<String> listarNomesFuncionarios();
	
	@Query(value = "SELECT f.id FROM funcionarios f WHERE f.ativo=true ORDER BY f.nomeFuncionario", nativeQuery = true)
	public List<String> listarIdFuncionarios();
	
	@Query(value = "SELECT f.id FROM funcionarios f WHERE f.ativo=true ORDER BY f.nomeFuncionario", nativeQuery = true)
	public List<Long> listarIdFuncionariosLong();
	
	@Query(value = "SELECT f.nomeFuncionario FROM funcionarios f WHERE f.id=:id", nativeQuery = true)
	public String buscarNomePorId(Long id);
	
	public Funcionario findBynomeFuncionario(String nomeFuncionario);
	
	@Query(value = "SELECT * FROM funcionarios f  WHERE f.username=:username", nativeQuery = true)
	public Funcionario buscarPorUsername(String username);
	
	@Query(value = "SELECT * FROM funcionarios f  WHERE f.nomeFuncionario=:nome", nativeQuery = true)
	public Funcionario buscarPorNome(String nome);
	
	public UserDetails findByUsername(String username);

	public Boolean existsByUsername(String username);
	
	public Boolean existsByNomeFuncionario(String nomeFuncionario);
	
	@Query(value = "SELECT f.trocaSenha FROM funcionarios f WHERE f.id=:id", nativeQuery = true)
	public Boolean exigeTrocaDeSenha(Long id);

	@Query(value = "SELECT COUNT(*) FROM funcionarios", nativeQuery = true)
	public int existsFuncionarios();

	@Query(value = "SELECT f.id, f.nomeFuncionario, f.userName, f.dataAtualizacao FROM funcionarios f WHERE f.id = :id", nativeQuery = true)
	public DtoFuncionarioEditSimplificado buscaFuncionarioSimplificadoPorId(Long id);

	@Query(value = "SELECT * FROM funcionarios f WHERE f.ativo=true", nativeQuery = true)
	public List<Funcionario> listarFuncionarios();

	@Query(value = "SELECT * FROM funcionarios f", nativeQuery = true)
	public List<Funcionario> listarTodosFuncionarios();
}
