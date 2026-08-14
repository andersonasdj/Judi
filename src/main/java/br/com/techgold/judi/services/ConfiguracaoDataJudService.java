package br.com.techgold.judi.services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import br.com.techgold.judi.dto.DtoConfiguracaoDataJud;
import br.com.techgold.judi.dto.DtoSalvarConfiguracaoDataJud;
import br.com.techgold.judi.model.ConfiguracaoDataJud;
import br.com.techgold.judi.repository.ConfiguracaoDataJudRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

/**
 * Configuração (linha única) da integração com o DataJud: ativação, api-key
 * (criptografada em repouso) e horário da sincronização diária, editáveis em
 * Configurações → Integração DataJud. Antes disso, esses dados viviam só no
 * {@code .env} (DATAJUD_ENABLED/DATAJUD_API_KEY) — na primeira inicialização
 * sem registro no banco, {@link #seedInicial()} migra o que estiver lá para
 * cá, então o .env pode ficar em branco depois.
 */
@Service
public class ConfiguracaoDataJudService {

	private static final LocalTime HORARIO_PADRAO = LocalTime.of(6, 0);
	private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

	private final ConfiguracaoDataJudRepository repository;
	private final TextEncryptor encryptor;

	@Value("${datajud.enabled:false}")
	private boolean enabledSemente;

	@Value("${datajud.api-key:}")
	private String apiKeySemente;

	ConfiguracaoDataJudService(ConfiguracaoDataJudRepository repository,
			@Value("${app.security.config-secret}") String segredo,
			@Value("${app.security.config-salt}") String salt) {
		this.repository = repository;
		this.encryptor = Encryptors.text(segredo, salt);
	}

	@PostConstruct
	@Transactional
	void seedInicial() {
		if (!repository.findAll().isEmpty()) {
			return;
		}
		ConfiguracaoDataJud configuracao = new ConfiguracaoDataJud();
		configuracao.setAtivo(enabledSemente);
		configuracao.setApiKeyCriptografada(apiKeySemente != null && !apiKeySemente.isBlank() ? encryptor.encrypt(apiKeySemente) : null);
		configuracao.setHorarioExecucao(HORARIO_PADRAO);
		configuracao.setDataAtualizacao(LocalDateTime.now().withNano(0));
		repository.save(configuracao);
	}

	private ConfiguracaoDataJud buscarEntidade() {
		return repository.findAll().stream().findFirst().orElseGet(() -> {
			seedInicial();
			return repository.findAll().stream().findFirst().orElseThrow();
		});
	}

	public DtoConfiguracaoDataJud buscar() {
		ConfiguracaoDataJud configuracao = buscarEntidade();
		boolean configurada = configuracao.getApiKeyCriptografada() != null;
		return new DtoConfiguracaoDataJud(
				configuracao.isAtivo(),
				configurada,
				configurada ? mascarar(descriptografar(configuracao)) : null,
				(configuracao.getHorarioExecucao() != null ? configuracao.getHorarioExecucao() : HORARIO_PADRAO).format(FORMATO_HORA));
	}

	@Transactional
	public void salvar(DtoSalvarConfiguracaoDataJud dados) {
		ConfiguracaoDataJud configuracao = buscarEntidade();
		configuracao.setAtivo(Boolean.TRUE.equals(dados.ativo()));
		if (dados.apiKey() != null && !dados.apiKey().isBlank()) {
			configuracao.setApiKeyCriptografada(encryptor.encrypt(dados.apiKey().trim()));
		}
		configuracao.setHorarioExecucao(LocalTime.parse(dados.horarioExecucao(), FORMATO_HORA));
		configuracao.setDataAtualizacao(LocalDateTime.now().withNano(0));
		repository.save(configuracao);
	}

	/** Usado pelo DataJudClient para montar o header de autenticação — nunca exposto via API. */
	public String getApiKeyDescriptografada() {
		return descriptografar(buscarEntidade());
	}

	public boolean isConfigurado() {
		ConfiguracaoDataJud configuracao = buscarEntidade();
		return configuracao.isAtivo() && configuracao.getApiKeyCriptografada() != null;
	}

	public LocalTime getHorarioExecucao() {
		LocalTime horario = buscarEntidade().getHorarioExecucao();
		return horario != null ? horario : HORARIO_PADRAO;
	}

	private String descriptografar(ConfiguracaoDataJud configuracao) {
		return configuracao.getApiKeyCriptografada() != null ? encryptor.decrypt(configuracao.getApiKeyCriptografada()) : null;
	}

	private String mascarar(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		int visiveis = Math.min(4, valor.length());
		return "••••••••" + valor.substring(valor.length() - visiveis);
	}

}
