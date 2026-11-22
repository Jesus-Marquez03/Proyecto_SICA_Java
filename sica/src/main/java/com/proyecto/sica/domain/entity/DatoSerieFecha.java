package com.proyecto.sica.domain.entity;

import java.time.LocalDate;

/** Punto diario utilizado por graficas y reportes. */
public record DatoSerieFecha(LocalDate fecha, long valor) { }
