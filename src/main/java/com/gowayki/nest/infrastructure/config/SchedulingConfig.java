package com.gowayki.nest.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita las tareas programadas del backend. Hoy solo la purga de grupos
 * cuya eliminación definitiva ya venció.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
