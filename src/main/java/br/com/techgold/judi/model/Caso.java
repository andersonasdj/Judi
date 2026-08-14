package br.com.techgold.judi.model;

import java.time.LocalDateTime;

import br.com.techgold.judi.model.enums.NaturezaCaso;
import br.com.techgold.judi.model.enums.StatusCaso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * O problema/assunto jurídico do cliente — agrupa um ou mais {@link Processo}
 * e/ou {@link Tarefa}. Ex: "Disputa contratual com Fornecedor XYZ" (natureza
 * CONTENCIOSO) pode reunir uma ação de cobrança, uma de indenização e um
 * processo administrativo, todos tratando da mesma questão.
 */
@Entity
@Table(name = "casos")
@Getter
@Setter
@ToString
public class Caso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(length = 200, nullable = false)
	private String titulo;

	@Column(length = 2000)
	private String descricao;

	@ManyToOne
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	@ManyToOne
	@JoinColumn(name = "funcionario_responsavel_id")
	private Funcionario funcionarioResponsavel;

	@Enumerated(EnumType.STRING)
	private NaturezaCaso natureza;

	@Enumerated(EnumType.STRING)
	private StatusCaso status = StatusCaso.ABERTO;

	private Boolean ativo = true;

	private LocalDateTime dataCadastro;

	private LocalDateTime dataAtualizacao;

}
