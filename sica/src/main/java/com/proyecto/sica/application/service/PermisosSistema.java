package com.proyecto.sica.application.service;

/**
 * Codigos usados para preguntar al RBAC; las asignaciones viven en MySQL.
 */
public final class PermisosSistema {

    /** Conservado para compatibilidad; los casos de uso usan permisos granulares. */
    @Deprecated
    public static final String GESTIONAR_USUARIOS = "GESTIONAR_USUARIOS";
    public static final String CREAR_USUARIO = "CREAR_USUARIO";
    public static final String CONSULTAR_USUARIO = "CONSULTAR_USUARIO";
    public static final String ACTUALIZAR_USUARIO = "ACTUALIZAR_USUARIO";
    public static final String CAMBIAR_ESTADO_USUARIO = "CAMBIAR_ESTADO_USUARIO";
    public static final String CREAR_EMPRESA = "CREAR_EMPRESA";
    public static final String CONSULTAR_EMPRESA = "CONSULTAR_EMPRESA";
    public static final String ACTUALIZAR_EMPRESA = "ACTUALIZAR_EMPRESA";
    public static final String CREAR_PERSONA = "CREAR_PERSONA";
    public static final String CONSULTAR_PERSONA = "CONSULTAR_PERSONA";
    public static final String ACTUALIZAR_PERSONA = "ACTUALIZAR_PERSONA";
    public static final String CAMBIAR_ESTADO_ACCESO = "CAMBIAR_ESTADO_ACCESO";

    private PermisosSistema() {
    }
}
