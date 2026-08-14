package br.com.techgold.judi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Valor/hora personalizado que um cliente paga por um funcionário específico,
 * substituindo o valor/hora padrão do funcionário ({@link Funcionario#getValorHora()})
 * no cálculo dos honorários pelo timesheet. A ausência de registro aqui para o
 * par (cliente, funcionário) significa "usar o valor padrão do funcionário".
 */
@Entity
@Table(name = "cliente_funcionario_valor_hora", uniqueConstraints = @UniqueConstraint(columnNames = { "cliente_id", "funcionario_id" }))
@Getter
@Setter
@ToString
public class ClienteFuncionarioValorHora {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	@ManyToOne
	@JoinColumn(name = "funcionario_id", nullable = false)
	private Funcionario funcionario;

	@Column(nullable = false)
	private BigDecimal valorHora;

	private LocalDateTime dataAtualizacao;

}
