package br.com.techgold.judi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.techgold.judi.model.enums.PoloCliente;
import br.com.techgold.judi.model.enums.StatusConsultaDataJud;
import br.com.techgold.judi.model.enums.StatusProcesso;
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
@Table(name = "processos")
@Getter
@Setter
@ToString
public class Processo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(length = 25, unique = true, nullable = false)
	private String numeroProcesso;

	@ManyToOne
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	/** Opcional: o caso (assunto jurídico) ao qual este processo pertence. */
	@ManyToOne
	@JoinColumn(name = "caso_id")
	private Caso caso;

	@ManyToOne
	@JoinColumn(name = "funcionario_responsavel_id")
	private Funcionario funcionarioResponsavel;

	@Column(length = 10)
	private String tribunal;

	@Column(length = 150)
	private String classeProcessual;

	@Column(length = 255)
	private String assunto;

	@Column(length = 150)
	private String orgaoJulgador;

	@Column(length = 20)
	private String grau;

	/** Nome/razão social da contraparte no processo — o cliente é sempre o outro lado. */
	@Column(length = 200)
	private String parteAdversa;

	@Column(length = 20)
	private String documentoParteAdversa;

	/** Em qual polo o cliente está: ATIVO (autor/exequente/reclamante) ou PASSIVO (réu/executado/reclamado). */
	@Enumerated(EnumType.STRING)
	private PoloCliente poloCliente;

	@Enumerated(EnumType.STRING)
	private StatusProcesso status = StatusProcesso.ATIVO;

	private LocalDate dataDistribuicao;

	private BigDecimal valorCausa;

	private Boolean monitorado = true;

	private LocalDateTime dataUltimaConsulta;

	private LocalDateTime dataUltimaMovimentacao;

	@Enumerated(EnumType.STRING)
	private StatusConsultaDataJud statusUltimaConsulta = StatusConsultaDataJud.NUNCA_CONSULTADO;

	@Column(length = 500)
	private String mensagemErroUltimaConsulta;

	@Column(length = 2000)
	private String observacoes;

	private Boolean ativo = true;

	private LocalDateTime dataCadastro;

	private LocalDateTime dataAtualizacao;

}
