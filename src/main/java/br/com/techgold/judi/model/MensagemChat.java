package br.com.techgold.judi.model;

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
@Table(name = "mensagens_chat")
@Getter
@Setter
@ToString
public class MensagemChat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "remetente_id", nullable = false)
	private Funcionario remetente;

	@ManyToOne
	@JoinColumn(name = "destinatario_id", nullable = false)
	private Funcionario destinatario;

	@NotBlank
	@Column(length = 2000, nullable = false)
	private String conteudo;

	private LocalDateTime dataEnvio;

	private boolean lida = false;

}
