package br.com.techgold.judi.model;

import java.time.LocalDateTime;

import br.com.techgold.judi.model.enums.OrigemTimesheet;
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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Um intervalo de trabalho de um funcionário numa tarefa. É o próprio
 * "trabalho realizado" — enquanto {@code dataFim} for nula, está em
 * andamento. A regra de "um funcionário por vez" é garantida no
 * {@code TimesheetService}: não pode haver dois timesheets sobrepostos
 * (nem dois abertos ao mesmo tempo) para a mesma tarefa.
 */
@Entity
@Table(name = "timesheets")
@Getter
@Setter
@ToString
public class Timesheet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "tarefa_id", nullable = false)
	private Tarefa tarefa;

	@ManyToOne
	@JoinColumn(name = "funcionario_id", nullable = false)
	private Funcionario funcionario;

	private LocalDateTime dataInicio;

	private LocalDateTime dataFim;

	@Enumerated(EnumType.STRING)
	private OrigemTimesheet origem = OrigemTimesheet.TIMER;

	@Column(length = 1000)
	private String observacoes;

	private LocalDateTime dataRegistro;

}
