package com.gowayki.nest.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
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

        Integer unidadCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM unidad", Integer.class);
        if (unidadCount != null && unidadCount == 0) {
            System.out.println(">>> [DataSeeder] Sembrando combis/unidades y telemetría GPS...");

            jdbcTemplate.execute(
                "INSERT INTO unidad (id, numero_padron, placa, linea_id, estado) VALUES " +
                "(1, '08', 'V1A-954', 1, 'OPERATIVA'), " +
                "(2, '03', 'A3B-112', 1, 'OPERATIVA'), " +
                "(3, '15', 'V4C-889', 2, 'OPERATIVA') " +
                "ON CONFLICT (id) DO NOTHING;"
            );

            jdbcTemplate.execute("SELECT setval('unidad_id_seq', (SELECT COALESCE(MAX(id), 1) FROM unidad));");

            jdbcTemplate.execute(
                "INSERT INTO unidad_posicion (unidad_id, posicion, velocidad_kmh, rumbo_grados, updated_at) VALUES " +
                "(1, ST_SetSRID(ST_MakePoint(-71.548, -16.420), 4326), 25.5, 135, NOW()), " +
                "(2, ST_SetSRID(ST_MakePoint(-71.537, -16.409), 4326), 18.0, 140, NOW()), " +
                "(3, ST_SetSRID(ST_MakePoint(-71.545, -16.390), 4326), 30.0, 90, NOW()) " +
                "ON CONFLICT (unidad_id) DO NOTHING;"
            );

            System.out.println(">>> [DataSeeder] ¡Unidades y GPS sembrados con éxito!");
        }

        Integer usuarioCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuario", Integer.class);
        if (usuarioCount != null && usuarioCount == 0) {
            System.out.println(">>> [DataSeeder] Sembrando usuario chofer de prueba...");
            String pinHash = passwordEncoder.encode("1234");
            UUID userId = UUID.randomUUID();

            jdbcTemplate.update(
                "INSERT INTO usuario (id, nombre, dni, telefono, email, pin_hash, rol, is_online, intentos_fallidos, created_at) VALUES " +
                "(?, 'Carlos Chofer Arequipa', '71234567', '958123456', 'chofer@gowayki.pe', ?, 'CHOFER', false, 0, NOW()) " +
                "ON CONFLICT (dni) DO NOTHING;",
                userId, pinHash
            );

            System.out.println(">>> [DataSeeder] ¡Usuario chofer (DNI: 71234567, PIN: 1234) sembrado con éxito!");
        }
    }
}
