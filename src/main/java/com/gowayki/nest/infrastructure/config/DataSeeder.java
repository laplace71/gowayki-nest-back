package com.gowayki.nest.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DataSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM linea", Integer.class);
        if (count != null && count == 0) {
            System.out.println(">>> [DataSeeder] Sembrando rutas de prueba de Arequipa...");
            
            jdbcTemplate.execute(
                "INSERT INTO linea (id, nombre, origen, destino, corredor, color_hex) VALUES " +
                "(1, 'Línea C2', 'Pte. Grau', 'Paucarpata', 'Transcayma', '#E53935'), " +
                "(2, 'Línea C3', 'Cerro Colorado', 'JLByR', 'C3', '#1E88E5') " +
                "ON CONFLICT (id) DO NOTHING;"
            );
            
            jdbcTemplate.execute("SELECT setval('linea_id_seq', (SELECT COALESCE(MAX(id), 1) FROM linea));");

            jdbcTemplate.execute(
                "INSERT INTO punto_control (linea_id, nombre, orden, ubicacion, radio_tolerancia_metros, tolerancia_minutos) VALUES " +
                "(1, 'Paradero Pte. Grau', 1, ST_SetSRID(ST_MakePoint(-71.560, -16.432), 4326), 50, 2), " +
                "(1, 'Paradero Av. Ejército', 2, ST_SetSRID(ST_MakePoint(-71.548, -16.420), 4326), 50, 2), " +
                "(1, 'Plaza de Armas Arequipa', 3, ST_SetSRID(ST_MakePoint(-71.537, -16.409), 4326), 50, 2), " +
                "(1, 'Av. Mariscal Castilla', 4, ST_SetSRID(ST_MakePoint(-71.525, -16.395), 4326), 50, 2), " +
                "(1, 'Terminal Paucarpata', 5, ST_SetSRID(ST_MakePoint(-71.510, -16.380), 4326), 50, 2), " +
                "(2, 'Terminal Cerro Colorado', 1, ST_SetSRID(ST_MakePoint(-71.520, -16.440), 4326), 50, 2), " +
                "(2, 'Av. Aviación', 2, ST_SetSRID(ST_MakePoint(-71.530, -16.425), 4326), 50, 2), " +
                "(2, 'Plaza de Armas Arequipa', 3, ST_SetSRID(ST_MakePoint(-71.537, -16.409), 4326), 50, 2), " +
                "(2, 'Av. Dolores', 4, ST_SetSRID(ST_MakePoint(-71.545, -16.390), 4326), 50, 2), " +
                "(2, 'JLByR Retorno', 5, ST_SetSRID(ST_MakePoint(-71.555, -16.370), 4326), 50, 2) " +
                "ON CONFLICT DO NOTHING;"
            );

            System.out.println(">>> [DataSeeder] ¡Rutas de prueba sembradas con éxito!");
        }
    }
}

