package br.com.techgold.judi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

	@Bean
	OpenAPI judiOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Judi API")
						.description("API do sistema de acompanhamento automatizado de processos jurídicos: "
								+ "cadastro de clientes/funcionários, processos monitorados, movimentações e "
								+ "alertas gerados a partir da API Pública do DataJud (CNJ).")
						.version("v1")
						.contact(new Contact().name("TechGold")));
	}

}
