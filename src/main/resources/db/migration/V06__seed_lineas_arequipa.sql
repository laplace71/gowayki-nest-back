-- 1. Insertar lineas de prueba de Arequipa (coincidentes con el frontend)
INSERT INTO linea (id, nombre, origen, destino, corredor, color_hex)
VALUES 
    (1, 'Línea C2', 'Pte. Grau', 'Paucarpata', 'Transcayma', '#E53935'),
    (2, 'Línea C3', 'Cerro Colorado', 'JLByR', 'C3', '#1E88E5')
ON CONFLICT (id) DO NOTHING;

-- Sincronizar secuencia de ID
SELECT setval('linea_id_seq', (SELECT MAX(id) FROM linea));

-- 2. Insertar paraderos / puntos de control para Línea C2
INSERT INTO punto_control (linea_id, nombre, orden, ubicacion, radio_tolerancia_metros, tolerancia_minutos)
VALUES
    (1, 'Paradero Pte. Grau', 1, ST_SetSRID(ST_MakePoint(-71.560, -16.432), 4326), 50, 2),
    (1, 'Paradero Av. Ejército', 2, ST_SetSRID(ST_MakePoint(-71.548, -16.420), 4326), 50, 2),
    (1, 'Plaza de Armas Arequipa', 3, ST_SetSRID(ST_MakePoint(-71.537, -16.409), 4326), 50, 2),
    (1, 'Av. Mariscal Castilla', 4, ST_SetSRID(ST_MakePoint(-71.525, -16.395), 4326), 50, 2),
    (1, 'Terminal Paucarpata', 5, ST_SetSRID(ST_MakePoint(-71.510, -16.380), 4326), 50, 2)
ON CONFLICT DO NOTHING;

-- 3. Insertar paraderos para Línea C3
INSERT INTO punto_control (linea_id, nombre, orden, ubicacion, radio_tolerancia_metros, tolerancia_minutos)
VALUES
    (2, 'Terminal Cerro Colorado', 1, ST_SetSRID(ST_MakePoint(-71.520, -16.440), 4326), 50, 2),
    (2, 'Av. Aviación', 2, ST_SetSRID(ST_MakePoint(-71.530, -16.425), 4326), 50, 2),
    (2, 'Plaza de Armas Arequipa', 3, ST_SetSRID(ST_MakePoint(-71.537, -16.409), 4326), 50, 2),
    (2, 'Av. Dolores', 4, ST_SetSRID(ST_MakePoint(-71.545, -16.390), 4326), 50, 2),
    (2, 'JLByR Retorno', 5, ST_SetSRID(ST_MakePoint(-71.555, -16.370), 4326), 50, 2)
ON CONFLICT DO NOTHING;

