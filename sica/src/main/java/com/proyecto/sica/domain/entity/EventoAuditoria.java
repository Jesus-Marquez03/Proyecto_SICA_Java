package com.proyecto.sica.domain.entity;

/**
 * Informacion no sensible que debe conservarse en la bitacora.
 */
public final class EventoAuditoria {

    private final Long usuarioId;
    private final String identificadorIntentado;
    private final String accion;
    private final String entidad;
    private final String entidadId;
    private final String resultado;
    private final String descripcion;

    public EventoAuditoria(Long usuarioId, String identificadorIntentado, String accion,
            String entidad, String entidadId, String resultado, String descripcion) {
        this.usuarioId = usuarioId;
        this.identificadorIntentado = identificadorIntentado;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.resultado = resultado;
        this.descripcion = descripcion;
    }

    public EventoAuditoria conEntidadId(String nuevoEntidadId) {
        return new EventoAuditoria(usuarioId, identificadorIntentado, accion,
                entidad, nuevoEntidadId, resultado, descripcion);
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getIdentificadorIntentado() {
        return identificadorIntentado;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public String getEntidadId() {
        return entidadId;
    }

    public String getResultado() {
        return resultado;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
