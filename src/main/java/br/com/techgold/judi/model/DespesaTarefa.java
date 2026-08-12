package br.com.techgold.judi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "despesas_tarefa")
@Getter
@Setter
@ToString
public class DespesaTarefa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "tarefa_id", nullable = false)
	private Tarefa tarefa;

	@ManyToOne
	@JoinColumn(name = "funcionario_id", nullable = false)
	private Funcionario funcionario;

	@NotBlank
	@Column(length = 255, nullable = false)
	private String descricao;

	private BigDecimal valor;

	private LocalDate data;

	private Boolean reembolsada = false;

	private LocalDateTime dataReembolso;

	private LocalDateTime dataRegistro;

}
