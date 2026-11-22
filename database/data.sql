-- Datos iniciales idempotentes para SICA.
USE sica_db;

INSERT INTO roles (id, nombre, descripcion, activo, requiere_empresa) VALUES
    (1, 'Superusuario', 'Administra la configuracion completa de SICA.', TRUE, FALSE),
    (2, 'Supervisor de Seguridad', 'Supervisa accesos, incidentes, auditoria y reportes.', TRUE, FALSE),
    (3, 'Guarda de Seguridad', 'Registra y controla ingresos, salidas e incidentes.', TRUE, FALSE),
    (4, 'Funcionario de Empresa', 'Solicita visitas y consulta sus solicitudes.', TRUE, TRUE)
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    descripcion = VALUES(descripcion),
    activo = VALUES(activo),
    requiere_empresa = VALUES(requiere_empresa);

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
    (13, 'GENERAR_REPORTES', 'Generar reportes', 'Consultar y exportar reportes autorizados.'),
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
    (24, 'CAMBIAR_ESTADO_ACCESO', 'Cambiar estado de acceso', 'Aplicar o retirar restricciones de acceso.'),
    (25, 'PRERREGISTRAR_VISITA', 'Prerregistrar visita', 'Crear una visita programada aprobada.'),
    (26, 'SOLICITAR_INGRESO', 'Solicitar ingreso', 'Crear solicitudes no anunciadas o pases temporales.'),
    (27, 'CONSULTAR_VISITA', 'Consultar visita', 'Consultar solicitudes y visitas autorizadas.'),
    (28, 'APROBAR_SOLICITUD', 'Aprobar solicitud', 'Aprobar solicitudes dirigidas al funcionario.'),
    (29, 'RECHAZAR_SOLICITUD', 'Rechazar solicitud', 'Rechazar solicitudes dirigidas al funcionario.'),
    (30, 'REGISTRAR_CHECK_IN', 'Registrar check-in', 'Confirmar el ingreso fisico autorizado.'),
    (31, 'REGISTRAR_CHECK_OUT', 'Registrar check-out', 'Confirmar la salida fisica.'),
    (32, 'REGULARIZAR_SALIDA_OLVIDADA', 'Regularizar salida olvidada', 'Cerrar visitas abiertas con justificacion.'),
    (33, 'CONSULTAR_HISTORIAL_VISITAS', 'Consultar historial de visitas', 'Consultar entradas, salidas y regularizaciones.'),
    (34, 'REGISTRAR_INCIDENTE', 'Registrar incidente', 'Registrar acontecimientos de seguridad.'),
    (35, 'CONSULTAR_INCIDENTE', 'Consultar incidente', 'Consultar incidentes autorizados.'),
    (36, 'ACTUALIZAR_INCIDENTE', 'Actualizar incidente', 'Dar seguimiento justificado a incidentes.')
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
    (2, 8), (2, 9), (2, 10), (2, 11), (2, 12), (2, 13),
    (2, 14), (2, 15), (2, 16), (2, 17), (2, 18), (2, 19),
    (2, 20), (2, 21), (2, 22), (2, 23), (2, 24);

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 2, id FROM permisos WHERE codigo IN (
    'PRERREGISTRAR_VISITA', 'SOLICITAR_INGRESO', 'CONSULTAR_VISITA',
    'APROBAR_SOLICITUD', 'RECHAZAR_SOLICITUD', 'REGISTRAR_CHECK_IN',
    'REGISTRAR_CHECK_OUT', 'REGULARIZAR_SALIDA_OLVIDADA',
    'CONSULTAR_HISTORIAL_VISITAS'
);

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 2, id FROM permisos WHERE codigo IN (
    'REGISTRAR_INCIDENTE', 'CONSULTAR_INCIDENTE', 'ACTUALIZAR_INCIDENTE',
    'CONSULTAR_AUDITORIA'
);

-- El guarda atiende el control diario de acceso.
INSERT IGNORE INTO rol_permisos (rol_id, permiso_id) VALUES
    (3, 4), (3, 5), (3, 6), (3, 8), (3, 9), (3, 11),
    (3, 19), (3, 21), (3, 22), (3, 23);

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 3, id FROM permisos WHERE codigo IN (
    'SOLICITAR_INGRESO', 'CONSULTAR_VISITA', 'REGISTRAR_CHECK_IN',
    'REGISTRAR_CHECK_OUT', 'REGULARIZAR_SALIDA_OLVIDADA',
    'CONSULTAR_HISTORIAL_VISITAS'
);

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 3, id FROM permisos WHERE codigo IN (
    'REGISTRAR_INCIDENTE', 'CONSULTAR_INCIDENTE'
);

-- El funcionario gestiona y consulta las solicitudes de su empresa.
INSERT IGNORE INTO rol_permisos (rol_id, permiso_id) VALUES
    (4, 5), (4, 6), (4, 19);

INSERT IGNORE INTO rol_permisos (rol_id, permiso_id)
SELECT 4, id FROM permisos WHERE codigo IN (
    'PRERREGISTRAR_VISITA', 'CONSULTAR_VISITA', 'APROBAR_SOLICITUD',
    'RECHAZAR_SOLICITUD', 'CONSULTAR_HISTORIAL_VISITAS', 'CONSULTAR_PERSONA'
);

INSERT INTO persona_estados_acceso (id, codigo, nombre, descripcion) VALUES
    (1, 'FUERA', 'Fuera de las instalaciones', 'La persona no registra un ingreso activo.'),
    (2, 'DENTRO', 'Dentro de las instalaciones', 'La persona tiene un ingreso activo.'),
    (3, 'SALIDA_PENDIENTE', 'Salida pendiente de regularizacion', 'Existe una salida olvidada por revisar.'),
    (4, 'BLOQUEADO', 'Acceso bloqueado', 'Estado historico de bloqueo de acceso.'),
    (5, 'ACTIVO', 'Activo', 'La persona esta habilitada para ingresar.'),
    (6, 'PROHIBICION_INGRESO', 'Con prohibicion de ingreso', 'La persona no puede ingresar hasta levantar la restriccion.')
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
    (8, 'VENCIDA', 'Vencida', 'La autorizacion no se utilizo dentro del plazo.'),
    (9, 'APROBADA', 'Aprobada', 'La visita esta autorizada pero aun no ha ingresado.'),
    (10, 'PENDIENTE_APROBACION', 'Pendiente de aprobacion', 'Solicitud no anunciada pendiente del anfitrion.'),
    (11, 'PENDIENTE_APROBACION_OLVIDO', 'Pendiente por carnet olvidado', 'Pase temporal pendiente de aprobacion.'),
    (12, 'DENTRO', 'Dentro', 'La persona se encuentra fisicamente dentro.'),
    (13, 'FUERA', 'Fuera', 'La visita finalizo con salida registrada.'),
    (14, 'EXPIRADA', 'Expirada', 'La autorizacion o pase ya no puede utilizarse.'),
    (15, 'CERRADA_POR_SISTEMA', 'Cerrada por sistema', 'Salida olvidada regularizada por el sistema.')
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

-- Personas ficticias de demostracion; no se sobrescriben documentos existentes.
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
