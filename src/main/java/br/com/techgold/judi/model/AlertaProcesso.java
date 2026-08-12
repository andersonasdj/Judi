package br.com.techgold.judi.model;

import java.time.LocalDateTime;

import br.com.techgold.judi.model.enums.SeveridadeAlerta;
import br.com.techgold.judi.model.enums.TipoAlerta;
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

@Entity
@Table(name = "alertas_processo")
@Getter
@Setter
@ToString
public class AlertaProcesso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "processo_id", nullable = false)
	private Processo processo;

	@Enumerated(EnumType.STRING)
	private TipoAlerta tipo;

	@Enumerated(EnumType.STRING)
	private SeveridadeAlerta severidade;

	@Column(length = 500)
	private String mensagem;

	private LocalDateTime dataGeracao;

	private Boolean lido = false;

	private LocalDateTime dataLeitura;

}
