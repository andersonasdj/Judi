package br.com.techgold.judi.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Configuração (linha única) da integração com a API Pública do DataJud
 * (CNJ), editável em Configurações → Integração DataJud (restrito a SADMIN).
 * A api-key fica criptografada em repouso — ver {@code ConfiguracaoDataJudService}.
 */
@Entity
@Table(name = "configuracao_datajud")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ConfiguracaoDataJud {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private boolean ativo;

	@ToString.Exclude
	private String apiKeyCriptografada;

	private LocalTime horarioExecucao;

	private LocalDateTime dataAtualizacao;

}
