package br.com.techgold.judi.model;

import java.time.LocalDateTime;

import br.com.techgold.judi.model.enums.OrigemMovimentacao;
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
@Table(name = "movimentacoes_processo")
@Getter
@Setter
@ToString
public class MovimentacaoProcesso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "processo_id", nullable = false)
	private Processo processo;

	private LocalDateTime dataMovimentacao;

	@Column(length = 20)
	private String codigoMovimento;

	@Column(length = 500)
	private String descricao;

	@Column(length = 1000)
	private String complemento;

	@Enumerated(EnumType.STRING)
	private OrigemMovimentacao origem = OrigemMovimentacao.MANUAL;

	private LocalDateTime dataRegistro;

}
