-- Etapa 6: indices de lectura para reportes y dashboard.
-- No cambia datos ni agrega tablas; puede ejecutarse sobre una instalacion existente.

SET @crear_idx_solicitud = (
    SELECT IF(COUNT(*) = 0,
        'CREATE INDEX idx_visitas_fecha_solicitud ON visitas (fecha_solicitud)',
        'SELECT 1')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'visitas'
      AND index_name = 'idx_visitas_fecha_solicitud'
);
PREPARE sentencia FROM @crear_idx_solicitud;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @crear_idx_ingreso = (
    SELECT IF(COUNT(*) = 0,
        'CREATE INDEX idx_visitas_fecha_ingreso ON visitas (fecha_ingreso)',
        'SELECT 1')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'visitas'
      AND index_name = 'idx_visitas_fecha_ingreso'
);
PREPARE sentencia FROM @crear_idx_ingreso;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @crear_idx_salida = (
    SELECT IF(COUNT(*) = 0,
        'CREATE INDEX idx_visitas_fecha_salida ON visitas (fecha_salida)',
        'SELECT 1')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'visitas'
      AND index_name = 'idx_visitas_fecha_salida'
);
PREPARE sentencia FROM @crear_idx_salida;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @crear_idx_cierre = (
    SELECT IF(COUNT(*) = 0,
        'CREATE INDEX idx_visitas_fecha_cierre_sistema ON visitas (fecha_cierre_sistema)',
        'SELECT 1')
    FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'visitas'
      AND index_name = 'idx_visitas_fecha_cierre_sistema'
);
PREPARE sentencia FROM @crear_idx_cierre;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;
