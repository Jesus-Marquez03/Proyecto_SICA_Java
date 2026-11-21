-- Migracion idempotente para control de visitas y concurrencia.
USE sica_db;

SET @existe_placa = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'visitas'
      AND column_name = 'placa_vehiculo'
);
SET @agregar_placa = IF(
    @existe_placa = 0,
    'ALTER TABLE visitas ADD COLUMN placa_vehiculo VARCHAR(20) NULL AFTER motivo',
    'SELECT 1'
);
PREPARE sentencia FROM @agregar_placa;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @existe_indice_persona_estado = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'visitas'
      AND index_name = 'idx_visitas_persona_estado'
);
SET @agregar_indice_persona_estado = IF(
    @existe_indice_persona_estado = 0,
    'CREATE INDEX idx_visitas_persona_estado ON visitas (visitante_persona_id, estado_visita_id)',
    'SELECT 1'
);
PREPARE sentencia FROM @agregar_indice_persona_estado;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

INSERT INTO visita_estados (id, codigo, nombre, descripcion) VALUES
    (9, 'APROBADA', 'Aprobada', 'La visita esta autorizada pero aun no ha ingresado.'),
    (10, 'PENDIENTE_APROBACION', 'Pendiente de aprobacion', 'Solicitud no anunciada pendiente del anfitrion.'),
    (11, 'PENDIENTE_APROBACION_OLVIDO', 'Pendiente por carnet olvidado', 'Pase temporal pendiente de aprobacion.'),
    (12, 'DENTRO', 'Dentro', 'La persona se encuentra fisicamente dentro.'),
    (13, 'FUERA', 'Fuera', 'La visita finalizo con salida registrada.'),
    (14, 'EXPIRADA', 'Expirada', 'La autorizacion o pase ya no puede utilizarse.'),
    (15, 'CERRADA_POR_SISTEMA', 'Cerrada por sistema', 'Salida olvidada regularizada por el sistema.')
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre), descripcion = VALUES(descripcion);

INSERT INTO permisos (id, codigo, nombre, descripcion) VALUES
    (25, 'PRERREGISTRAR_VISITA', 'Prerregistrar visita', 'Crear una visita programada aprobada.'),
    (26, 'SOLICITAR_INGRESO', 'Solicitar ingreso', 'Crear solicitudes no anunciadas o pases temporales.'),
    (27, 'CONSULTAR_VISITA', 'Consultar visita', 'Consultar solicitudes y visitas autorizadas.'),
    (28, 'APROBAR_SOLICITUD', 'Aprobar solicitud', 'Aprobar solicitudes dirigidas al funcionario.'),
    (29, 'RECHAZAR_SOLICITUD', 'Rechazar solicitud', 'Rechazar solicitudes dirigidas al funcionario.'),
    (30, 'REGISTRAR_CHECK_IN', 'Registrar check-in', 'Confirmar el ingreso fisico autorizado.'),
    (31, 'REGISTRAR_CHECK_OUT', 'Registrar check-out', 'Confirmar la salida fisica.'),
    (32, 'REGULARIZAR_SALIDA_OLVIDADA', 'Regularizar salida olvidada', 'Cerrar visitas abiertas con justificacion.'),
    (33, 'CONSULTAR_HISTORIAL_VISITAS', 'Consultar historial de visitas', 'Consultar entradas, salidas y regularizaciones.')
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre), descripcion = VALUES(descripcion);

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 1, id FROM permisos;

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 2, id FROM permisos WHERE codigo IN (
    'PRERREGISTRAR_VISITA', 'SOLICITAR_INGRESO', 'CONSULTAR_VISITA',
    'APROBAR_SOLICITUD', 'RECHAZAR_SOLICITUD', 'REGISTRAR_CHECK_IN',
    'REGISTRAR_CHECK_OUT', 'REGULARIZAR_SALIDA_OLVIDADA',
    'CONSULTAR_HISTORIAL_VISITAS'
);

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 3, id FROM permisos WHERE codigo IN (
    'SOLICITAR_INGRESO', 'CONSULTAR_VISITA', 'REGISTRAR_CHECK_IN',
    'REGISTRAR_CHECK_OUT', 'REGULARIZAR_SALIDA_OLVIDADA',
    'CONSULTAR_HISTORIAL_VISITAS'
);

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 4, id FROM permisos WHERE codigo IN (
    'PRERREGISTRAR_VISITA', 'CONSULTAR_VISITA', 'APROBAR_SOLICITUD',
    'RECHAZAR_SOLICITUD', 'CONSULTAR_HISTORIAL_VISITAS', 'CONSULTAR_PERSONA'
);
