package com.amarhu.scheduler.controller;

import com.amarhu.scheduler.YouTubeDataScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    @Autowired
    private YouTubeDataScheduler youTubeDataScheduler;

    /**
     * Permite ejecutar manualmente el proceso de obtención de datos desde el scheduler.
     */
    @PostMapping("/run")
    public ResponseEntity<String> runSchedulerManually() {
        try {
            boolean isCompleted = youTubeDataScheduler.fetchYouTubeData();
            if (isCompleted) {
                return ResponseEntity.ok("Tarea ejecutada manualmente y completada exitosamente.");
            } else {
                return ResponseEntity.ok("Tarea ejecutada manualmente, pero no se completó. Reintentos en proceso.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al ejecutar manualmente: " + e.getMessage());
        }
    }

    /**
     * Consulta el estado actual del proceso.
     */
    @PostMapping("/status")
    public ResponseEntity<String> getSchedulerStatus() {
        boolean isCompleted = youTubeDataScheduler.isProcessCompleted();
        if (isCompleted) {
            return ResponseEntity.ok("El proceso ya se completó con éxito hoy.");
        } else {
            return ResponseEntity.ok("El proceso aún no se ha completado o está en reintento.");
        }
    }
}
