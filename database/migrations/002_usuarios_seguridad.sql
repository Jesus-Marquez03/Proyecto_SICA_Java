-- Migracion de la etapa de usuarios y seguridad para bases ya inicializadas.
USE sica_db;

SET @existe_requiere_empresa = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'roles'
      AND column_name = 'requiere_empresa'
);
SET @agregar_requiere_empresa = IF(
    @existe_requiere_empresa = 0,
    'ALTER TABLE roles ADD COLUMN requiere_empresa BOOLEAN NOT NULL DEFAULT FALSE AFTER activo',
    'SELECT 1'
);
PREPARE sentencia FROM @agregar_requiere_empresa;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

UPDATE roles SET requiere_empresa = (id = 4);

-- Protege datos heredados antes de exigir correo obligatorio.
UPDATE usuarios
SET correo = CONCAT(nombre_usuario, '@pendiente.local')
WHERE correo IS NULL OR TRIM(correo) = '';

ALTER TABLE usuarios MODIFY correo VARCHAR(120) NOT NULL;
ALTER TABLE bitacora_auditoria MODIFY nombre_usuario_intentado VARCHAR(120);
