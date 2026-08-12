package br.com.techgold.judi.datajud;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataJudSyncScheduler {

	private final DataJudSyncService syncService;

	DataJudSyncScheduler(DataJudSyncService syncService) {
		this.syncService = syncService;
	}

	@Async("asyncExecutor")
	@Scheduled(cron = "${datajud.sync-cron:0 0 6 * * *}")
	public void sincronizacaoDiaria() {
		syncService.sincronizarTodos();
	}

}
