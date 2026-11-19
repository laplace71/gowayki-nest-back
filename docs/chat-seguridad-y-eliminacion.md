# Chat: identidad, ocultar conversaciones y eliminación definitiva de grupos

Este documento describe los cambios del chat para corregir el borrado de grupos y separar dos acciones que antes se mezclaban:

- **Ocultar chat / ocultar grupo**: acción personal. Solo quita la conversación de la lista del usuario que lo pide.
- **Eliminar grupo definitivamente**: acción separada y autorizada. Pide la purga real del grupo con 72 horas de espera.

## 1. Identidad y seguridad

- `/api/chat/**` requiere autenticación con JWT (`Authorization: Bearer <token>`).
- La identidad del usuario **siempre** se toma del principal autenticado (JWT). El backend ya no confía en `usuarioId`/`emisorId` enviados por el cliente en URL o cuerpo.
- Respuestas de seguridad:
  - `401`: sin token, token inválido o token vencido.
  - `403`: autenticado pero sin permiso (no participa, o no puede pedir/cancelar la eliminación definitiva).
  - `409`: conflicto de estado (por ejemplo, intentar unirse a un grupo con eliminación definitiva pendiente).
- Login y registro devuelven `token` (JWT HS256). La app debe guardarlo y enviarlo en todas las peticiones del chat y en el handshake del WebSocket.
- WebSocket (`/ws/chat`): el handshake exige `Authorization: Bearer <token>`. Sin token válido se rechaza. Los eventos personales (ocultar/mostrar, avisos de eliminación) se envían solo a las sesiones del usuario autenticado.
- Configuración:
  - `app.jwt.secret` / env `JWT_SECRET`: secreto HS256. Debe tener al menos 32 bytes. **No usar el valor por defecto en producción.**
  - `app.jwt.expiration-hours`: horas de vigencia del token (por defecto 168 = 7 días).

### Endpoints que siguen abiertos (pendiente)

Por alcance, el JWT se aplicó solo al chat. Siguen sin autenticación JWT:

- `/api/sos/**`
- `/api/turnos/**`
- `/api/unidades/**`
- `/api/lineas/**`
- `/api/usuarios/**` (listado/registro/login según configuración actual)

Estos deben protegerse en un paso siguiente con la misma estrategia de identidad.

## 2. Ocultar conversación (personal)

- `DELETE /api/chat/conversaciones/{id}` **oculta** la conversación solo para el usuario autenticado.
- `POST /api/chat/conversaciones/{id}/mostrar` la vuelve a mostrar.
- Efectos de ocultar:
  - La conversación deja de aparecer en la lista de ese usuario.
  - **No** borra mensajes, audios, archivos ni la conversación.
  - **No** hay espera de 72 horas.
  - **No** cambia membresía ni roles.
  - Los demás participantes no reciben ningún efecto visible.
- La conversación permanece oculta aunque lleguen mensajes nuevos; solo reaparece si el usuario la muestra explícitamente.
- Eventos WebSocket personales: `CONVERSACION_OCULTA`, `CONVERSACION_MOSTRADA` (solo al usuario que realizó la acción).

## 3. Eliminación definitiva de grupo (72 horas)

Es una operación distinta, nunca equivalente a "ocultar".

- `POST /api/chat/conversaciones/{id}/eliminacion`: solicita la eliminación definitiva.
  - Solo para grupos.
  - Autorización: creador del grupo (`created_by`) o participante con rol `ADMIN`.
  - Responde `202 Accepted` con `solicitudId`, `estado`, `solicitadoEn`, `seEliminaraEn`, `horasDeEspera` (72).
  - No borra nada de inmediato.
- `DELETE /api/chat/conversaciones/{id}/eliminacion`: cancela la solicitud antes del plazo (misma autorización).
- Estado pendiente:
  - La conversación expone `eliminacionPendiente` y `eliminacionProgramadaPara` en el listado.
  - No se pueden agregar nuevos participantes a un grupo pendiente (`409`).
  - Se notifica a los participantes con `GRUPO_ELIMINACION_PENDIENTE` / `GRUPO_ELIMINACION_CANCELADA`.
- Purga (a las 72 horas):
  - La solicitud se persiste en base de datos: sobrevive reinicios del backend (no hay timers en memoria).
  - Un scheduler consulta periódicamente las solicitudes vencidas en BD.
  - Antes de borrar se **revalida**: que el grupo exista, siga siendo grupo y que el solicitante siga autorizado (creador o admin y participante). Si ya no procede, la solicitud se cancela y no se borra nada.
  - Al ejecutarse: se eliminan mensajes y la conversación (en cascada según FKs) y se avisa a los participantes con `CONVERSACION_ELIMINADA`.
  - Los archivos/audios **no** se borran dentro de la transacción: se encolan y un proceso diferido los elimina después del commit (ver sección 5).

## 4. Membresía, concurrencia y grupos de ruta

- Las mutaciones de membresía (salir, entrar, cambiar rol, promover admin) bloquean la conversación con `SELECT ... FOR UPDATE` para evitar carreras.
- Si un administrador sale y el grupo queda sin administradores, se promueve automáticamente al participante más antiguo (`agregado_en`, desempate por id) y la promoción queda registrada.
- No se puede entrar a un grupo con eliminación definitiva pendiente.
- Grupos de subruta: el tipo se identifica por campos estructurados `tipo_conversacion` (`GRUPO_RUTA`) y `clave_ruta` (subruta concreta), con índice único parcial. El prefijo en el título (`GRUPO · ...`) se mantiene solo como compatibilidad para datos/clientes antiguos.
- Si el último participante sale o se cambia de subruta y el grupo queda vacío, el grupo se elimina (comportamiento existente). En este caso los archivos también se encolan para borrado diferido.

## 5. Borrado diferido de archivos

- Tabla `archivo_pendiente_borrado`: cola persistente de rutas a eliminar.
- Los audios/archivos se encolan al purgar un grupo o al vaciar un grupo de ruta; **nunca** se borran dentro de la transacción de base de datos.
- `LimpiarArchivosPendientesUseCase` / `ArchivosPendientesScheduler`:
  - Procesa la cola periódicamente (idempotente y reintentable).
  - Antes de borrar un archivo verifica que no exista otro mensaje que lo referencie (`existeAudioUrl`); si aún se usa, se descarta sin borrarlo.
  - Si el borrado falla, registra el error y el intento y lo reintenta más tarde.
  - Configuración: `app.archivos.intervalo-ms`, `app.archivos.retraso-inicial-ms`, `app.archivos.limite-por-vuelta`.

## 6. Migraciones (aplicar manualmente)

El backend **no** usa Flyway/Liquibase (`spring.jpa.hibernate.ddl-auto=validate`). Las migraciones se entregan como SQL y deben aplicarse manualmente, en orden, contra la base de datos:

1. `V7__ocultar_conversaciones_por_usuario.sql`
2. `V8__eliminacion_definitiva_grupos.sql`
3. `V9__archivos_pendientes_de_borrado.sql`
4. `V10__antiguedad_participantes.sql`
5. `V11__tipo_conversacion_estructurado.sql`

Ubicación: `src/main/resources/db/migration/`.

- Son incrementales y no destructivas (crean tablas/columnas/índices y hacen backfill con `IF NOT EXISTS`/`ADD COLUMN IF NOT EXISTS`).
- Aplicarlas antes de desplegar el backend que usa estas entidades (con `ddl-auto=validate`, el arranque fallará si el esquema no coincide).
- En producción actual (contenedor `gowayki_db_container`, BD `gowayki_db`, usuario `admin` según variables del contenedor) estas tablas/columnas **no** estaban presentes al momento de la verificación.

## 7. API de chat (resumen)

- `GET /api/chat/conversaciones` → conversaciones visibles del usuario autenticado (sin las ocultas). Incluye `tipoConversacion`, `claveRuta`, `miRol`, `eliminacionPendiente`, `eliminacionProgramadaPara`.
- `POST /api/chat/conversaciones` → crear conversación (acepta `tipoConversacion`/`claveRuta`).
- `DELETE /api/chat/conversaciones/{id}` → ocultar para el usuario autenticado.
- `POST /api/chat/conversaciones/{id}/mostrar` → restaurar (mostrar).
- `POST /api/chat/conversaciones/{id}/eliminacion` → solicitar eliminación definitiva del grupo (72h).
- `DELETE /api/chat/conversaciones/{id}/eliminacion` → cancelar eliminación definitiva.
- `POST /api/chat/conversaciones/{id}/salir` → salir del grupo.
- Mensajes: el emisor se toma del JWT (el cliente no envía `emisorId`).
- Identidad siempre desde JWT; sin `usuarioId` del cliente.

## 8. Pruebas

Pruebas unitarias (sin BD, fakes en memoria) en `src/test/java/com/gowayki/nest/chat` y `src/test/java/com/gowayki/nest/security` cubren:

- JWT válido/inválido/alterado/otra clave/vencido/secreto corto.
- Ocultar: requiere identidad, requiere ser participante, no borra ni cambia membresía/roles, solo afecta al solicitante; mostrar restaura.
- Eliminación definitiva: 72h, no duplica, solo creador/admin, privado no aplica, no borra antes del plazo, cancelación, revalidación al purgar (permiso perdido/cancelada, conversación inexistente), purga lee vencidas de BD, notifica solo participantes, audios encolados (no borrados en transacción).
- Archivos diferidos: no borra si otro mensaje lo usa, fallo de disco reintenta, idempotencia.
- Salir: bloqueo, herencia de admin al más antiguo, grupo vacío encola audios.
- Crear: tipo/claveRuta estructurados, compatibilidad con título legado, bloqueado si pendiente de eliminación.
- Códigos HTTP: 401 vs 403, 409, 400, 500 sin filtrar detalles.

Ejecutar: `./gradlew test --tests 'com.gowayki.nest.chat.*' --tests 'com.gowayki.nest.security.*'`

Nota: `GowaykiNestApplicationTests` es `@SpringBootTest` y puede requerir una base de datos disponible.
