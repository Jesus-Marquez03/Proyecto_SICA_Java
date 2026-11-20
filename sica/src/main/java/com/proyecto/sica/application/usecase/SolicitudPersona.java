package com.proyecto.sica.application.usecase;

import com.proyecto.sica.domain.entity.TipoPersona;

/** Datos editables para crear o actualizar una persona. */
public final class SolicitudPersona {
    private final Long empresaId;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final String nombres;
    private final String apellidos;
    private final String telefono;
    private final String correo;
    private final TipoPersona tipoPersona;
    private final String fotoUrl;

    public SolicitudPersona(Long empresaId, String tipoDocumento, String numeroDocumento,
            String nombres, String apellidos, String telefono, String correo,
            TipoPersona tipoPersona, String fotoUrl) {
        this.empresaId = empresaId;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.correo = correo;
        this.tipoPersona = tipoPersona;
        this.fotoUrl = fotoUrl;
    }

    public Long getEmpresaId() { return empresaId; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public TipoPersona getTipoPersona() { return tipoPersona; }
    public String getFotoUrl() { return fotoUrl; }
}
