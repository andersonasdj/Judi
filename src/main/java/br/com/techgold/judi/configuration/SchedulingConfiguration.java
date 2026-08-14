package br.com.techgold.judi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * TaskScheduler nomeado, usado pelo DataJudSyncScheduler para poder
 * cancelar/reagendar o job de sincronização em runtime (o horário é
 * configurável em Configurações → Integração DataJud, sem precisar
 * reiniciar a aplicação).
 */
@Configuration
public class SchedulingConfiguration {

	@Bean
	TaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(1);
		scheduler.setThreadNamePrefix("scheduled-task-");
		scheduler.initialize();
		return scheduler;
	}

}
