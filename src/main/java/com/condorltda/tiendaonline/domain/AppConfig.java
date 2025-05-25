package com.condorltda.tiendaonline.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling // Habilita la capacidad de programación de tareas
public class AppConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // Número de hilos para tareas programadas
        scheduler.setThreadNamePrefix("pago-scheduler-"); // Prefijo para los nombres de los hilos
        scheduler.initialize();
        return scheduler;
    }
}
