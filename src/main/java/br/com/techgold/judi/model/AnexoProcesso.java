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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Um arquivo anexado a um processo (petição, procuração, decisão etc.).
 * O arquivo em si fica em disco sob {@code upload.dir}/processos/{processoId}/{nomeArmazenado};
 * esta entidade só guarda os metadados — ver {@code AnexoProcessoService}.
 */
@Entity
@Table(name = "anexos_processo")
@Getter
@Setter
@ToString
public class AnexoProcesso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "processo_id", nullable = false)
	private Processo processo;

	@ManyToOne
	@JoinColumn(name = "funcionario_id", nullable = false)
	private Funcionario funcionario;

	@Column(length = 255, nullable = false)
	private String nomeOriginal;

	@Column(length = 255, nullable = false)
	private String nomeArmazenado;

	@Column(length = 100)
	private String mimeType;

	private Long tamanho;

	private LocalDateTime dataUpload;

}
