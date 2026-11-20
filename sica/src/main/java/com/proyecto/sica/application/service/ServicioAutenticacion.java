package com.proyecto.sica.application.service;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.AuditoriaRepository;
import com.proyecto.sica.domain.repository.UsuarioRepository;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Autentica usuarios sin mezclar la verificacion de permisos.
 */
public final class ServicioAutenticacion {

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final ProtectorPassword protectorPassword;
    private final SesionUsuario sesionUsuario;

    public ServicioAutenticacion(UsuarioRepository usuarioRepository,
            AuditoriaRepository auditoriaRepository,
            ProtectorPassword protectorPassword,
            SesionUsuario sesionUsuario) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.protectorPassword = protectorPassword;
        this.sesionUsuario = sesionUsuario;
    }

    public Usuario iniciarSesion(String correo, char[] password) {
        String correoNormalizado = normalizarCorreo(correo);
        try {
            Optional<Usuario> usuarioEncontrado = usuarioRepository.buscarPorCorreo(correoNormalizado);
            if (usuarioEncontrado.isEmpty()) {
                registrarFallo(correoNormalizado, "Usuario inexistente");
                throw new AutenticacionException("Correo o contrasena incorrectos");
            }

            Usuario usuario = usuarioEncontrado.get();
            if (!usuario.isActivo()) {
                registrarFallo(correoNormalizado, "Usuario inactivo");
                throw new AutenticacionException("El usuario se encuentra inactivo");
            }

            if (!protectorPassword.verificar(password, usuario.getPasswordHash())) {
                registrarFallo(correoNormalizado, "Credenciales incorrectas");
                throw new AutenticacionException("Correo o contrasena incorrectos");
            }

            EventoAuditoria evento = new EventoAuditoria(
                    usuario.getId(), null, "LOGIN", "usuarios",
                    String.valueOf(usuario.getId()), "EXITOSO", "Inicio de sesion exitoso");
            usuarioRepository.registrarLoginExitoso(usuario.getId(), evento);
            sesionUsuario.iniciar(usuario);
            return usuario;
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    public void cerrarSesion() {
        Optional<Usuario> usuario = sesionUsuario.obtenerUsuario();
        try {
            usuario.ifPresent(valor -> auditoriaRepository.registrar(new EventoAuditoria(
                    valor.getId(), null, "LOGOUT", "usuarios",
                    String.valueOf(valor.getId()), "EXITOSO", "Cierre de sesion")));
        } finally {
            sesionUsuario.cerrar();
        }
    }

    private void registrarFallo(String correo, String descripcion) {
        auditoriaRepository.registrar(new EventoAuditoria(
                null, correo, "LOGIN", "usuarios", null,
                "FALLIDO", descripcion));
    }

    private String normalizarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return "";
        }
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
