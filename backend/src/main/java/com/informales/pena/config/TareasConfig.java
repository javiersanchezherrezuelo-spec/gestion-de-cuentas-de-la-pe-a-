package com.informales.pena.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Activa las tareas en segundo plano (@Async) y las programadas (@Scheduled), como el resumen mensual. */
@Configuration
@EnableAsync
@EnableScheduling
public class TareasConfig {
}
