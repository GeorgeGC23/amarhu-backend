package com.amarhu.scheduler;

import com.amarhu.config.YoutubeConfig;
import com.amarhu.video.service.YouTubeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class YouTubeDataScheduler {

    @Autowired
    private YouTubeDataService youTubeDataService;

    @Autowired
    private YoutubeConfig youtubeConfig;  // 🔹 Inyectamos YoutubeConfig


    private boolean processCompleted = false; // Bandera para saber si el proceso se completó con éxito

    //@Scheduled(cron = "0 30 10 * * ?") // Cron para ejecutar a las 10:30 AM
    public void scheduleYouTubeDataProcessing() {
        processCompleted = false; // Resetea la bandera al inicio del día
        fetchYouTubeData();
    }

    //@Scheduled(fixedDelay = 300000) // Reintenta cada 5 minutos si no se completó
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
            // Verifica si hay credenciales guardadas antes de continuar
            if (youtubeConfig.getFlow().loadCredential("user") == null) {
                System.err.println("Intento de procesar datos sin autenticación. Se necesita login en /oauth2/login.");
                return false;
            }

            youTubeDataService.processYouTubeData();
            processCompleted = true;
            return true;
        } catch (Exception e) {
            System.err.println("Error durante la ejecución manual: " + e.getMessage());
            return false;
        }
    }


    public boolean isProcessCompleted() {
        return processCompleted;
    }
}
