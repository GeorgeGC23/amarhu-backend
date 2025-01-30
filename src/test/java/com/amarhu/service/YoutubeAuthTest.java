package com.amarhu.service;

import com.amarhu.config.YoutubeConfig;

public class YoutubeAuthTest {

    public static void main(String[] args) {
        try {
            YoutubeConfig youtubeConfig = new YoutubeConfig();
            System.out.println("Iniciando el flujo de autenticación...");

            // Llama al método authorize() para abrir el navegador y autenticar
            youtubeConfig.authorize();

            System.out.println("Autenticación completada. Credenciales generadas y guardadas.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error durante el flujo de autenticación: " + e.getMessage());
        }
    }
}
