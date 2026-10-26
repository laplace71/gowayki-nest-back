# Wayki Nest - Backend

Backend oficial y API REST para la plataforma de transporte urbano **Wayki Nest**. Provee servicios de telemetría GPS en tiempo real para unidades de transporte, control de frecuencias y tiempos en ruta, gestión de emergencias con botón físico SOS (BLE) y chat/comunicación entre conductores.

---

## Stack

* **Lenguaje:** Java 21 (LTS)
* **Framework:** Spring Boot 3 / Gradle
* **Base de Datos:** PostgreSQL 15 con extensión geoespacial **PostGIS 3.3**
* **ORM:** Spring Data JPA + Hibernate Spatial
* **Contenedores:** Docker & Docker Compose

---

## Requisitos Previos

Antes de empezar, asegúrate de tener instalado en tu computadora:
1. **Java 21 (JDK 21)** o superior.
2. **Docker Desktop** (debe estar abierto y corriendo).
3. **Git**.

---

## Guía de Instalación Rápida (Para el equipo)

Sigue estos 4 pasos para tener el backend corriendo en tu máquina:

### 1. Clonar el repositorio
```bash
git clone <URL_DEL_REPOSITORIO>
cd gowayki-nest-back
```

### 2. Configurar las variables de entorno
Crea tu archivo local `.env` a partir de la plantilla:

* **En Windows (PowerShell):**
  ```powershell
  Copy-Item .env.example .env
  ```
* **En Mac / Linux:**
  ```bash
  cp .env.example .env
  ```
*(Los valores por defecto en `.env.example` ya están listos para desarrollo local en el puerto `5433`).*

### 3. Levantar la Base de Datos con Docker
Asegúrate de tener Docker Desktop abierto y ejecuta:
```bash
docker compose up -d
```
> **Nota:** La base de datos corre en el puerto `5433` de tu máquina para evitar conflictos si ya tienes otro PostgreSQL instalado en el puerto 5432.

### 4. Ejecutar el Backend

* **En Windows:**
  ```powershell
  .\gradlew bootRun
  ```
* **En Mac / Linux:**
  ```bash
  ./gradlew bootRun
  ```

El servidor iniciará en `http://localhost:8080`.

---

## Estructura de la Base de Datos (PostGIS)

La base de datos cuenta con 11 tablas organizadas por módulos en `src/main/resources/db/migration/`:

1. **Flota y GPS (`V1__flota_y_gps.sql`):**
   * `linea`: Rutas y corredores de combis.
   * `unidad`: Vehículos físicos y números de padrón.
   * `unidad_posicion`: Telemetría viva con coordenadas PostGIS (`Point, 4326`) para calcular combis cercanas.

2. **Usuarios y Turnos (`V2__usuarios_y_turnos.sql`):**
   * `usuario`: Choferes, administradores y jefes con roles y PIN cifrado.
   * `asignacion_turno`: Registro del conductor asignado a una combi y su porcentaje de cumplimiento diario.

3. **Frecuencias y Puntos de Control (`V3__frecuencias_y_control.sql`):**
   * `punto_control`: Geocercas geográficas con radio de tolerancia para cada parada.
   * `registro_cumplimiento`: Tiempos programados vs reales (`A_TIEMPO`, `ATRASADO`, `ADELANTADO`).

4. **Seguridad y SOS (`V4__seguridad_y_sos.sql`):**
   * `alerta_incidencia`: Disparos del botón físico Bluetooth (BLE) y mapas de calor (Heatmaps).

5. **Comunicación y Chat (`V5__chat_y_comunicacion.sql`):**
   * `conversacion`, `conversacion_participante`, `mensaje`: Mensajes de texto y notas de voz con estados (`ENVIADO`, `ENTREGADO`, `LEIDO`).
