package com.redsolidaria.enjambre.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración del pool de threads para métodos @Async (como envío de correos)
 */
@Configuration
public class AsyncConfig {

    /**
     * Define un bean Executor para manejar tareas asincrónicas
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);           // Hilos mínimos
        executor.setMaxPoolSize(5);            // Hilos máximos
        executor.setQueueCapacity(100);        // Tareas en cola
        executor.setThreadNamePrefix("async-"); // Prefijo del nombre del hilo
        executor.initialize();
        return executor;
    }
}
