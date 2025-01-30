package com.amarhu.scheduler;

import com.amarhu.video.service.YouTubeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class YouTubeDataScheduler {

    @Autowired
    private YouTubeDataService youTubeDataService;

    private boolean processCompleted = false; // Bandera para saber si el proceso se completó con éxito

    @Scheduled(cron = "0 30 10 * * ?") // Cron para ejecutar a las 10:30 AM
    public void scheduleYouTubeDataProcessing() {
        processCompleted = false; // Resetea la bandera al inicio del día
        fetchYouTubeData();
    }

    @Scheduled(fixedDelay = 300000) // Reintenta cada 5 minutos si no se completó
    public void retryYouTubeDataProcessing() {
        if (!processCompleted) {
            fetchYouTubeData();
        }
    }

    /**
     * Método para ejecutar manualmente o programáticamente la tarea de obtención de datos.
     *
     * @return `true` si el proceso se completó con éxito, `false` si no.
     */
    public boolean fetchYouTubeData() {
        try {
            youTubeDataService.processYouTubeData();
            processCompleted = true; // Marca como completado
            return true;
        } catch (Exception e) {
            System.err.println("Error durante la ejecución manual: " + e.getMessage());
            return false; // Indica que falló
        }
    }

    public boolean isProcessCompleted() {
        return processCompleted;
    }
}
