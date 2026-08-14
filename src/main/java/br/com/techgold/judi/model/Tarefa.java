package br.com.techgold.judi.model;

import java.time.LocalDateTime;

import br.com.techgold.judi.model.enums.StatusTarefa;
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

@Entity
@Table(name = "tarefas")
@Getter
@Setter
@ToString
public class Tarefa {

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

	/** Opcional: o caso ao qual esta tarefa pertence — trabalho consultivo/preventivo pode não ter processo algum. */
	@ManyToOne
	@JoinColumn(name = "caso_id")
	private Caso caso;

	@ManyToOne
	@JoinColumn(name = "processo_id")
	private Processo processo;

	/** Funcionário designado agora — quem tem o timesheet aberto, se houver. */
	@ManyToOne
	@JoinColumn(name = "funcionario_responsavel_id")
	private Funcionario funcionarioResponsavel;

	@Enumerated(EnumType.STRING)
	private StatusTarefa status = StatusTarefa.ABERTA;

	/** Preenchida quando status = AGENDADA — usada futuramente para gerar alertas de tarefas agendadas. */
	private LocalDateTime dataAgendamento;

	private Boolean ativo = true;

	private LocalDateTime dataCadastro;

	private LocalDateTime dataAtualizacao;

	/** Último funcionário a alterar esta tarefa (cadastro, edição, mudança de status ou agendamento). */
	@ManyToOne
	@JoinColumn(name = "atualizado_por_id")
	private Funcionario atualizadoPor;

}
