-- Migracion de personas, empresas y permisos granulares.
USE sica_db;

-- Corrige la coherencia entre el esquema inicial y la migracion de seguridad.
ALTER TABLE empresas MODIFY correo VARCHAR(120) NULL;
UPDATE usuarios
SET correo = CONCAT(nombre_usuario, '@pendiente.local')
WHERE correo IS NULL OR TRIM(correo) = '';
ALTER TABLE usuarios MODIFY correo VARCHAR(120) NOT NULL;

SET @existe_foto_url = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'personas'
      AND column_name = 'foto_url'
);
SET @agregar_foto_url = IF(
    @existe_foto_url = 0,
    'ALTER TABLE personas ADD COLUMN foto_url VARCHAR(500) NULL AFTER activa',
    'SELECT 1'
);
PREPARE sentencia FROM @agregar_foto_url;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

INSERT INTO persona_estados_acceso (id, codigo, nombre, descripcion) VALUES
    (5, 'ACTIVO', 'Activo', 'La persona esta habilitada para ingresar.'),
    (6, 'PROHIBICION_INGRESO', 'Con prohibicion de ingreso', 'La persona no puede ingresar hasta levantar la restriccion.')
ON DUPLICATE KEY UPDATE
    codigo = VALUES(codigo), nombre = VALUES(nombre), descripcion = VALUES(descripcion);

INSERT INTO permisos (id, codigo, nombre, descripcion) VALUES
    (14, 'CREAR_USUARIO', 'Crear usuario', 'Registrar nuevos usuarios.'),
    (15, 'CONSULTAR_USUARIO', 'Consultar usuario', 'Listar y consultar usuarios.'),
    (16, 'ACTUALIZAR_USUARIO', 'Actualizar usuario', 'Modificar datos basicos de usuarios.'),
    (17, 'CAMBIAR_ESTADO_USUARIO', 'Cambiar estado de usuario', 'Activar o desactivar usuarios.'),
    (18, 'CREAR_EMPRESA', 'Crear empresa', 'Registrar empresas.'),
    (19, 'CONSULTAR_EMPRESA', 'Consultar empresa', 'Consultar empresas registradas.'),
    (20, 'ACTUALIZAR_EMPRESA', 'Actualizar empresa', 'Modificar empresas registradas.'),
    (21, 'CREAR_PERSONA', 'Crear persona', 'Registrar trabajadores e invitados.'),
    (22, 'CONSULTAR_PERSONA', 'Consultar persona', 'Consultar trabajadores e invitados.'),
    (23, 'ACTUALIZAR_PERSONA', 'Actualizar persona', 'Modificar datos basicos de personas.'),
    (24, 'CAMBIAR_ESTADO_ACCESO', 'Cambiar estado de acceso', 'Aplicar o retirar restricciones de acceso.')
ON DUPLICATE KEY UPDATE
    codigo = VALUES(codigo), nombre = VALUES(nombre), descripcion = VALUES(descripcion);

-- El superusuario recibe todos los permisos existentes.
INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 1, id FROM permisos;

-- El supervisor administra usuarios, empresas, personas y restricciones.
INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 2, id FROM permisos WHERE codigo IN (
    'CREAR_USUARIO', 'CONSULTAR_USUARIO', 'ACTUALIZAR_USUARIO', 'CAMBIAR_ESTADO_USUARIO',
    'CREAR_EMPRESA', 'CONSULTAR_EMPRESA', 'ACTUALIZAR_EMPRESA',
    'CREAR_PERSONA', 'CONSULTAR_PERSONA', 'ACTUALIZAR_PERSONA', 'CAMBIAR_ESTADO_ACCESO'
);

-- El guarda consulta empresas y gestiona personas, pero no impone prohibiciones.
INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 3, id FROM permisos WHERE codigo IN (
    'CONSULTAR_EMPRESA', 'CREAR_PERSONA', 'CONSULTAR_PERSONA', 'ACTUALIZAR_PERSONA'
);

-- El funcionario solo consulta empresas para sus flujos de solicitudes.
INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 4, id FROM permisos WHERE codigo = 'CONSULTAR_EMPRESA';

-- Datos totalmente ficticios, insertados solo si el documento no existe.
INSERT INTO personas (empresa_id, estado_acceso_id, tipo_documento, numero_documento,
        nombres, apellidos, telefono, correo, tipo_persona, activa, foto_url)
SELECT 2, 5, 'CC', '10000001', 'Andrea', 'Trabajadora Demo', '3000000001',
       'andrea.trabajadora@example.com', 'TRABAJADOR', TRUE, NULL
WHERE NOT EXISTS (SELECT 1 FROM personas WHERE tipo_documento = 'CC' AND numero_documento = '10000001');

INSERT INTO personas (empresa_id, estado_acceso_id, tipo_documento, numero_documento,
        nombres, apellidos, telefono, correo, tipo_persona, activa, foto_url)
SELECT 2, 6, 'CC', '10000002', 'Bruno', 'Restringido Demo', '3000000002',
       'bruno.restringido@example.com', 'TRABAJADOR', TRUE, NULL
WHERE NOT EXISTS (SELECT 1 FROM personas WHERE tipo_documento = 'CC' AND numero_documento = '10000002');

INSERT INTO personas (empresa_id, estado_acceso_id, tipo_documento, numero_documento,
        nombres, apellidos, telefono, correo, tipo_persona, activa, foto_url)
SELECT 1, 5, 'CE', '20000001', 'Carla', 'Invitada Demo', '3000000003',
       'carla.invitada@example.com', 'VISITANTE', TRUE, NULL
WHERE NOT EXISTS (SELECT 1 FROM personas WHERE tipo_documento = 'CE' AND numero_documento = '20000001');
