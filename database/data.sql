-- Datos iniciales idempotentes para SICA.
USE sica_db;

INSERT INTO roles (id, nombre, descripcion, activo) VALUES
    (1, 'Superusuario', 'Administra la configuracion completa de SICA.', TRUE),
    (2, 'Supervisor de Seguridad', 'Supervisa accesos, incidentes, auditoria y reportes.', TRUE),
    (3, 'Guarda de Seguridad', 'Registra y controla ingresos, salidas e incidentes.', TRUE),
    (4, 'Funcionario de Empresa', 'Solicita visitas y consulta sus solicitudes.', TRUE)
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    descripcion = VALUES(descripcion),
    activo = VALUES(activo);

INSERT INTO permisos (id, codigo, nombre, descripcion) VALUES
    (1, 'GESTIONAR_ROLES_PERMISOS', 'Gestionar roles y permisos', 'Configurar la autorizacion RBAC.'),
    (2, 'GESTIONAR_USUARIOS', 'Gestionar usuarios', 'Crear, consultar y actualizar usuarios.'),
    (3, 'GESTIONAR_EMPRESAS', 'Gestionar empresas', 'Administrar empresas vinculadas.'),
    (4, 'GESTIONAR_PERSONAS', 'Gestionar personas', 'Administrar visitantes y trabajadores.'),
    (5, 'SOLICITAR_VISITA', 'Solicitar visita', 'Crear solicitudes de visita.'),
    (6, 'CONSULTAR_SOLICITUDES', 'Consultar solicitudes', 'Consultar solicitudes segun el alcance del rol.'),
    (7, 'AUTORIZAR_VISITA', 'Autorizar visita', 'Aprobar o rechazar solicitudes.'),
    (8, 'REGISTRAR_INGRESO', 'Registrar ingreso', 'Registrar entradas autorizadas.'),
    (9, 'REGISTRAR_SALIDA', 'Registrar salida', 'Registrar salidas de personas.'),
    (10, 'REGULARIZAR_SALIDA', 'Regularizar salida', 'Cerrar una salida olvidada con justificacion.'),
    (11, 'GESTIONAR_INCIDENTES', 'Gestionar incidentes', 'Registrar y dar seguimiento a incidentes.'),
    (12, 'CONSULTAR_AUDITORIA', 'Consultar auditoria', 'Consultar la trazabilidad del sistema.'),
    (13, 'GENERAR_REPORTES', 'Generar reportes', 'Consultar y exportar reportes autorizados.')
ON DUPLICATE KEY UPDATE
    codigo = VALUES(codigo),
    nombre = VALUES(nombre),
    descripcion = VALUES(descripcion);

-- El superusuario posee todos los permisos.
INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 1, id FROM permisos;

-- El supervisor opera y supervisa seguridad, sin administrar RBAC.
INSERT IGNORE INTO rol_permisos (rol_id, permiso_id) VALUES
    (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7),
    (2, 8), (2, 9), (2, 10), (2, 11), (2, 12), (2, 13);

-- El guarda atiende el control diario de acceso.
INSERT IGNORE INTO rol_permisos (rol_id, permiso_id) VALUES
    (3, 4), (3, 5), (3, 6), (3, 8), (3, 9), (3, 11);

-- El funcionario gestiona y consulta las solicitudes de su empresa.
INSERT IGNORE INTO rol_permisos (rol_id, permiso_id) VALUES
    (4, 5), (4, 6);

INSERT INTO persona_estados_acceso (id, codigo, nombre, descripcion) VALUES
    (1, 'FUERA', 'Fuera de las instalaciones', 'La persona no registra un ingreso activo.'),
    (2, 'DENTRO', 'Dentro de las instalaciones', 'La persona tiene un ingreso activo.'),
    (3, 'SALIDA_PENDIENTE', 'Salida pendiente de regularizacion', 'Existe una salida olvidada por revisar.'),
    (4, 'BLOQUEADO', 'Acceso bloqueado', 'La persona no puede ingresar hasta nueva autorizacion.')
ON DUPLICATE KEY UPDATE
    codigo = VALUES(codigo),
    nombre = VALUES(nombre),
    descripcion = VALUES(descripcion);

INSERT INTO visita_estados (id, codigo, nombre, descripcion) VALUES
    (1, 'PENDIENTE', 'Pendiente', 'La solicitud espera respuesta.'),
    (2, 'AUTORIZADA', 'Autorizada', 'La solicitud fue aprobada.'),
    (3, 'RECHAZADA', 'Rechazada', 'La solicitud fue rechazada.'),
    (4, 'EN_CURSO', 'En curso', 'La persona se encuentra dentro.'),
    (5, 'PENDIENTE_REGULARIZACION', 'Pendiente de regularizacion', 'No se registro la salida oportunamente.'),
    (6, 'FINALIZADA', 'Finalizada', 'La visita o pase fue cerrado.'),
    (7, 'CANCELADA', 'Cancelada', 'La solicitud fue cancelada.'),
    (8, 'VENCIDA', 'Vencida', 'La autorizacion no se utilizo dentro del plazo.')
ON DUPLICATE KEY UPDATE
    codigo = VALUES(codigo),
    nombre = VALUES(nombre),
    descripcion = VALUES(descripcion);

INSERT INTO empresas (id, nit, nombre, direccion, telefono, correo, activa) VALUES
    (1, '900123456-7', 'Zona Acme Administracion', 'Zona Acme', '6015550100', 'contacto@zona-acme.example', TRUE),
    (2, '901234567-8', 'Empresa Demo S.A.S.', 'Zona Acme, Edificio Demo', '6015550200', 'contacto@empresa-demo.example', TRUE)
ON DUPLICATE KEY UPDATE
    nit = VALUES(nit),
    nombre = VALUES(nombre),
    direccion = VALUES(direccion),
    telefono = VALUES(telefono),
    correo = VALUES(correo),
    activa = VALUES(activa);

-- No se incluyen usuarios de demostracion para evitar distribuir credenciales.
