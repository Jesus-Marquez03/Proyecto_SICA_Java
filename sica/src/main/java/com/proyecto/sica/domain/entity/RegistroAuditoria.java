package com.proyecto.sica.domain.entity;

import java.time.LocalDateTime;

/** Lectura inmutable de una entrada historica de la bitacora. */
public record RegistroAuditoria(long id, Long usuarioId, String nombreUsuario,
        String identificadorIntentado, String accion, String entidad, String entidadId,
        String resultado, String descripcion, String ipOrigen, LocalDateTime fechaEvento) {
}
