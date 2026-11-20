package com.proyecto.sica.application.usecase;

/** Datos editables para crear o actualizar una empresa. */
public final class SolicitudEmpresa {
    private final String nit;
    private final String nombre;
    private final String direccion;
    private final String telefono;
    private final String correo;

    public SolicitudEmpresa(String nit, String nombre, String direccion,
            String telefono, String correo) {
        this.nit = nit;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
    }

    public String getNit() { return nit; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
}
