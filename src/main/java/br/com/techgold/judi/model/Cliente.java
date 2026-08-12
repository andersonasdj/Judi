package br.com.techgold.judi.model;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.techgold.judi.dto.DtoAtualizarCliente;
import br.com.techgold.judi.dto.DtoCadastroCliente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clientes")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Cliente extends Usuario {

	@Column(length = 100)
	private String nomeCliente;
	@Column(length = 200)
	private String endereco;
	@Column(length = 20)
	private String telefone;
	@Column(length = 20)
	private String cnpj;
	@Column(length = 20)
	private String bairro;
	private String caminhoFoto;

	public Cliente(DtoCadastroCliente dados) {
		this.nomeCliente = dados.nomeCliente();
		this.setUsername(dados.username());
		this.setPassword(new BCryptPasswordEncoder().encode(dados.password().toString()));
		this.setAtivo(true);
		this.setMfa(false);
		this.setDataAtualizacao(LocalDateTime.now().withNano(0));
		this.endereco = dados.endereco();
		this.telefone = dados.telefone();
		this.cnpj = dados.cnpj();
		this.bairro = dados.bairro();
	}

public Cliente(DtoAtualizarCliente dados) {
		this.setId(dados.id());
		this.nomeCliente = dados.nomeCliente();
		this.setUsername(dados.username());
		this.setPassword(new BCryptPasswordEncoder().encode(dados.password().toString()));
		this.setAtivo(dados.ativo());
		this.setMfa(false);
		this.setDataAtualizacao(LocalDateTime.now().withNano(0));
		this.endereco = dados.endereco();
		this.telefone = dados.telefone();
		this.cnpj = dados.cnpj();
		this.bairro = dados.bairro();
	}
}
