-- Etapa 5: permisos e indices para incidentes y consultas de auditoria.
USE sica_db;

SET @indice_existe = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'incidentes'
      AND INDEX_NAME = 'idx_incidentes_visita_fecha'
);
SET @sql = IF(@indice_existe = 0,
    'CREATE INDEX idx_incidentes_visita_fecha ON incidentes (visita_id, fecha_incidente)',
    'SELECT 1');
PREPARE sentencia FROM @sql; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @indice_existe = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'incidentes'
      AND INDEX_NAME = 'idx_incidentes_persona_fecha'
);
SET @sql = IF(@indice_existe = 0,
    'CREATE INDEX idx_incidentes_persona_fecha ON incidentes (persona_id, fecha_incidente)',
    'SELECT 1');
PREPARE sentencia FROM @sql; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @indice_existe = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'incidentes'
      AND INDEX_NAME = 'idx_incidentes_reportado_fecha'
);
SET @sql = IF(@indice_existe = 0,
    'CREATE INDEX idx_incidentes_reportado_fecha ON incidentes (reportado_por_usuario_id, fecha_incidente)',
    'SELECT 1');
PREPARE sentencia FROM @sql; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

SET @indice_existe = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bitacora_auditoria'
      AND INDEX_NAME = 'idx_bitacora_entidad_registro_fecha'
);
SET @sql = IF(@indice_existe = 0,
    'CREATE INDEX idx_bitacora_entidad_registro_fecha ON bitacora_auditoria (entidad, entidad_id, fecha_evento)',
    'SELECT 1');
PREPARE sentencia FROM @sql; EXECUTE sentencia; DEALLOCATE PREPARE sentencia;

INSERT INTO permisos (id, codigo, nombre, descripcion) VALUES
    (34, 'REGISTRAR_INCIDENTE', 'Registrar incidente', 'Registrar acontecimientos de seguridad.'),
    (35, 'CONSULTAR_INCIDENTE', 'Consultar incidente', 'Consultar incidentes autorizados.'),
    (36, 'ACTUALIZAR_INCIDENTE', 'Actualizar incidente', 'Dar seguimiento justificado a incidentes.')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), descripcion = VALUES(descripcion);

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 1, id FROM permisos;

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 2, id FROM permisos WHERE codigo IN (
    'REGISTRAR_INCIDENTE', 'CONSULTAR_INCIDENTE', 'ACTUALIZAR_INCIDENTE',
    'CONSULTAR_AUDITORIA'
);

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 3, id FROM permisos WHERE codigo IN (
    'REGISTRAR_INCIDENTE', 'CONSULTAR_INCIDENTE'
);
