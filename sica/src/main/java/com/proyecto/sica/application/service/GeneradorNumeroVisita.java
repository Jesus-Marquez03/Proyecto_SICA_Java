package com.proyecto.sica.application.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** Genera identificadores legibles sin depender de la base de datos. */
public final class GeneradorNumeroVisita {
    public String generarSolicitud() {
        return "VIS-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public String generarPase() {
        return "PASE-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
