-- Esquema inicial de SICA para MySQL 8.4.
-- No elimina datos existentes. Para una reconstruccion completa use una base vacia.

CREATE DATABASE IF NOT EXISTS sica_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE sica_db;

CREATE TABLE IF NOT EXISTS roles (
    id TINYINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    requiere_empresa BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_roles_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS permisos (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(60) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    CONSTRAINT uk_permisos_codigo UNIQUE (codigo)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS rol_permisos (
    rol_id TINYINT UNSIGNED NOT NULL,
    permiso_id SMALLINT UNSIGNED NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rol_permisos_rol
        FOREIGN KEY (rol_id) REFERENCES roles (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_rol_permisos_permiso
        FOREIGN KEY (permiso_id) REFERENCES permisos (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS empresas (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nit VARCHAR(20) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    direccion VARCHAR(180),
    telefono VARCHAR(30),
    correo VARCHAR(120),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_empresas_nit UNIQUE (nit)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    rol_id TINYINT UNSIGNED NOT NULL,
    empresa_id BIGINT UNSIGNED,
    nombre_usuario VARCHAR(60) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    correo VARCHAR(120) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acceso DATETIME,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuarios_nombre_usuario UNIQUE (nombre_usuario),
    CONSTRAINT uk_usuarios_correo UNIQUE (correo),
    INDEX idx_usuarios_rol (rol_id),
    INDEX idx_usuarios_empresa (empresa_id),
    CONSTRAINT fk_usuarios_rol
        FOREIGN KEY (rol_id) REFERENCES roles (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_usuarios_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresas (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS persona_estados_acceso (
    id TINYINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255),
    CONSTRAINT uk_persona_estados_codigo UNIQUE (codigo)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS visita_estados (
    id TINYINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255),
    CONSTRAINT uk_visita_estados_codigo UNIQUE (codigo)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS personas (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT UNSIGNED,
    estado_acceso_id TINYINT UNSIGNED NOT NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(30) NOT NULL,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    telefono VARCHAR(30),
    correo VARCHAR(120),
    tipo_persona VARCHAR(20) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    foto_url VARCHAR(500),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_personas_documento UNIQUE (tipo_documento, numero_documento),
    INDEX idx_personas_empresa (empresa_id),
    INDEX idx_personas_estado_acceso (estado_acceso_id),
    INDEX idx_personas_apellidos_nombres (apellidos, nombres),
    CONSTRAINT chk_personas_tipo
        CHECK (tipo_persona IN ('VISITANTE', 'TRABAJADOR')),
    CONSTRAINT fk_personas_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresas (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_personas_estado_acceso
        FOREIGN KEY (estado_acceso_id) REFERENCES persona_estados_acceso (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS visitas (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    numero_solicitud VARCHAR(30) NOT NULL,
    visitante_persona_id BIGINT UNSIGNED NOT NULL,
    anfitrion_usuario_id BIGINT UNSIGNED NOT NULL,
    estado_visita_id TINYINT UNSIGNED NOT NULL,
    registrado_por_usuario_id BIGINT UNSIGNED NOT NULL,
    respondido_por_usuario_id BIGINT UNSIGNED,
    regularizada_por_usuario_id BIGINT UNSIGNED,
    tipo_solicitud VARCHAR(30) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    placa_vehiculo VARCHAR(20),
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_programada DATETIME,
    fecha_respuesta DATETIME,
    fecha_ingreso DATETIME,
    fecha_salida DATETIME,
    fecha_cierre_sistema DATETIME,
    motivo_regularizacion VARCHAR(255),
    codigo_pase VARCHAR(40),
    fecha_vencimiento_pase DATETIME,
    observaciones VARCHAR(500),
    CONSTRAINT uk_visitas_numero_solicitud UNIQUE (numero_solicitud),
    CONSTRAINT uk_visitas_codigo_pase UNIQUE (codigo_pase),
    INDEX idx_visitas_visitante (visitante_persona_id),
    INDEX idx_visitas_persona_estado (visitante_persona_id, estado_visita_id),
    INDEX idx_visitas_anfitrion (anfitrion_usuario_id),
    INDEX idx_visitas_estado_fecha (estado_visita_id, fecha_programada),
    INDEX idx_visitas_tipo_solicitud (tipo_solicitud),
    INDEX idx_visitas_fecha_solicitud (fecha_solicitud),
    INDEX idx_visitas_fecha_ingreso (fecha_ingreso),
    INDEX idx_visitas_fecha_salida (fecha_salida),
    INDEX idx_visitas_fecha_cierre_sistema (fecha_cierre_sistema),
    CONSTRAINT chk_visitas_tipo_solicitud
        CHECK (tipo_solicitud IN ('PRERREGISTRADA', 'NO_ANUNCIADA', 'PASE_TEMPORAL')),
    CONSTRAINT chk_visitas_respuesta
        CHECK (fecha_respuesta IS NULL OR fecha_respuesta >= fecha_solicitud),
    CONSTRAINT chk_visitas_salida
        CHECK (fecha_salida IS NULL OR fecha_ingreso IS NULL OR fecha_salida >= fecha_ingreso),
    CONSTRAINT chk_visitas_regularizacion
        CHECK (
            (fecha_cierre_sistema IS NULL
                AND regularizada_por_usuario_id IS NULL
                AND motivo_regularizacion IS NULL)
            OR
            (fecha_cierre_sistema IS NOT NULL
                AND regularizada_por_usuario_id IS NOT NULL
                AND motivo_regularizacion IS NOT NULL)
        ),
    CONSTRAINT chk_visitas_pase_temporal
        CHECK (
            tipo_solicitud <> 'PASE_TEMPORAL'
            OR (codigo_pase IS NOT NULL AND fecha_vencimiento_pase IS NOT NULL)
        ),
    CONSTRAINT fk_visitas_visitante
        FOREIGN KEY (visitante_persona_id) REFERENCES personas (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_visitas_anfitrion
        FOREIGN KEY (anfitrion_usuario_id) REFERENCES usuarios (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_visitas_estado
        FOREIGN KEY (estado_visita_id) REFERENCES visita_estados (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_visitas_registrado_por
        FOREIGN KEY (registrado_por_usuario_id) REFERENCES usuarios (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_visitas_respondido_por
        FOREIGN KEY (respondido_por_usuario_id) REFERENCES usuarios (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_visitas_regularizada_por
        FOREIGN KEY (regularizada_por_usuario_id) REFERENCES usuarios (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS incidentes (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    visita_id BIGINT UNSIGNED,
    persona_id BIGINT UNSIGNED,
    reportado_por_usuario_id BIGINT UNSIGNED NOT NULL,
    tipo VARCHAR(60) NOT NULL,
    gravedad VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
    descripcion VARCHAR(1000) NOT NULL,
    acciones_tomadas VARCHAR(1000),
    fecha_incidente DATETIME NOT NULL,
    fecha_reporte DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre DATETIME,
    INDEX idx_incidentes_fecha (fecha_incidente),
    INDEX idx_incidentes_estado_gravedad (estado, gravedad),
    INDEX idx_incidentes_visita_fecha (visita_id, fecha_incidente),
    INDEX idx_incidentes_persona_fecha (persona_id, fecha_incidente),
    INDEX idx_incidentes_reportado_fecha (reportado_por_usuario_id, fecha_incidente),
    CONSTRAINT chk_incidentes_gravedad
        CHECK (gravedad IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA')),
    CONSTRAINT chk_incidentes_estado
        CHECK (estado IN ('ABIERTO', 'EN_REVISION', 'CERRADO')),
    CONSTRAINT chk_incidentes_referencia
        CHECK (visita_id IS NOT NULL OR persona_id IS NOT NULL),
    CONSTRAINT fk_incidentes_visita
        FOREIGN KEY (visita_id) REFERENCES visitas (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_incidentes_persona
        FOREIGN KEY (persona_id) REFERENCES personas (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_incidentes_reportado_por
        FOREIGN KEY (reportado_por_usuario_id) REFERENCES usuarios (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS bitacora_auditoria (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT UNSIGNED,
    nombre_usuario_intentado VARCHAR(120),
    accion VARCHAR(80) NOT NULL,
    entidad VARCHAR(60),
    entidad_id VARCHAR(60),
    resultado VARCHAR(20) NOT NULL,
    descripcion VARCHAR(1000),
    ip_origen VARCHAR(45),
    fecha_evento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_bitacora_fecha (fecha_evento),
    INDEX idx_bitacora_usuario_fecha (usuario_id, fecha_evento),
    INDEX idx_bitacora_accion_resultado (accion, resultado),
    INDEX idx_bitacora_entidad_registro_fecha (entidad, entidad_id, fecha_evento),
    CONSTRAINT chk_bitacora_resultado
        CHECK (resultado IN ('EXITOSO', 'FALLIDO')),
    CONSTRAINT fk_bitacora_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE = InnoDB;
