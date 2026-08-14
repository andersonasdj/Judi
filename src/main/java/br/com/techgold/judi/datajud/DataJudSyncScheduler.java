package br.com.techgold.judi.datajud;

import java.time.LocalTime;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import br.com.techgold.judi.services.ConfiguracaoDataJudService;
import jakarta.annotation.PostConstruct;

/**
 * Agenda a sincronização diária com o DataJud no horário configurado em
 * Configurações → Integração DataJud. Ao contrário de um {@code @Scheduled}
 * fixo, {@link #reagendar()} permite trocar o horário em runtime sem
 * reiniciar a aplicação — chamado ao iniciar e sempre que a configuração é
 * salva (ver {@code ConfiguracaoDataJudRestController}).
 */
@Component
public class DataJudSyncScheduler {

	private final DataJudSyncService syncService;
	private final ConfiguracaoDataJudService configuracaoService;
	private final TaskScheduler taskScheduler;

	private ScheduledFuture<?> execucaoAgendada;

	DataJudSyncScheduler(DataJudSyncService syncService, ConfiguracaoDataJudService configuracaoService, TaskScheduler taskScheduler) {
		this.syncService = syncService;
		this.configuracaoService = configuracaoService;
		this.taskScheduler = taskScheduler;
	}

	@PostConstruct
	void iniciar() {
		reagendar();
	}

	public synchronized void reagendar() {
		if (execucaoAgendada != null) {
			execucaoAgendada.cancel(false);
		}
		LocalTime horario = configuracaoService.getHorarioExecucao();
		String cron = "0 " + horario.getMinute() + " " + horario.getHour() + " * * *";
		execucaoAgendada = taskScheduler.schedule(syncService::sincronizarTodos, new CronTrigger(cron));
	}

}
