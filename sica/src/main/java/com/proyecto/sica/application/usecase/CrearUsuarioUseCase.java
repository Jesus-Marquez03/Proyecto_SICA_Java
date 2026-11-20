package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ProtectorPassword;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorUsuario;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.UsuarioRepository;
import java.util.Arrays;

/**
 * Caso de uso para crear usuarios con autorizacion y auditoria atomica.
 */
public final class CrearUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorUsuario validador;
    private final ProtectorPassword protectorPassword;

    public CrearUsuarioUseCase(UsuarioRepository usuarioRepository,
            ServicioAutorizacion autorizacion, ValidadorUsuario validador,
            ProtectorPassword protectorPassword) {
        this.usuarioRepository = usuarioRepository;
        this.autorizacion = autorizacion;
        this.validador = validador;
        this.protectorPassword = protectorPassword;
    }

    public Usuario ejecutar(Usuario actor, SolicitudCrearUsuario solicitud) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CREAR_USUARIO);
        char[] password = solicitud.copiarPassword();
        try {
            if (password == null || password.length == 0) {
                throw new ValidacionException("La contrasena es obligatoria");
            }
            validador.validarTextos(solicitud.getNombreUsuario(), solicitud.getNombres(),
                    solicitud.getApellidos(), solicitud.getCorreo());
            validador.validarCorreoUnico(solicitud.getCorreo(), null);
            Rol rol = validador.obtenerRolValido(solicitud.getRolId());
            String empresaNombre = validador.validarYObtenerEmpresa(rol, solicitud.getEmpresaId());
            String correo = validador.normalizarCorreo(solicitud.getCorreo());

            Usuario nuevo = new Usuario(null, rol, solicitud.getEmpresaId(), empresaNombre,
                    solicitud.getNombreUsuario().trim(), protectorPassword.generarHash(password),
                    solicitud.getNombres().trim(), solicitud.getApellidos().trim(),
                    correo, true, null);
            EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                    "CREAR_USUARIO", "usuarios", null, "EXITOSO",
                    "Creacion del usuario " + correo);
            return usuarioRepository.crearConAuditoria(nuevo, evento);
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
            solicitud.limpiarPassword();
        }
    }
}
