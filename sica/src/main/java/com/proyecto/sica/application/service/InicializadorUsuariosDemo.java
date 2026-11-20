package com.proyecto.sica.application.service;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.EmpresaRepository;
import com.proyecto.sica.domain.repository.RolRepository;
import com.proyecto.sica.domain.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Crea datos academicos solamente cuando se invoca de forma explicita.
 */
public final class InicializadorUsuariosDemo {

    private static final List<DefinicionDemo> USUARIOS = List.of(
            new DefinicionDemo("superadmin", "SicaSuper2026!", "Sofia", "Superusuario",
                    "superusuario@sica.example", 1, null),
            new DefinicionDemo("supervisor", "SicaSupervisor2026!", "Samuel", "Supervisor",
                    "supervisor@sica.example", 2, null),
            new DefinicionDemo("guarda", "SicaGuarda2026!", "Gabriela", "Guarda",
                    "guarda@sica.example", 3, null),
            new DefinicionDemo("funcionario", "SicaFuncionario2026!", "Fernanda", "Funcionaria",
                    "funcionario@sica.example", 4, 2L));

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EmpresaRepository empresaRepository;
    private final ProtectorPassword protectorPassword;

    public InicializadorUsuariosDemo(UsuarioRepository usuarioRepository,
            RolRepository rolRepository, EmpresaRepository empresaRepository,
            ProtectorPassword protectorPassword) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.empresaRepository = empresaRepository;
        this.protectorPassword = protectorPassword;
    }

    public List<String> ejecutar() {
        List<String> resultados = new ArrayList<>();
        for (DefinicionDemo demo : USUARIOS) {
            if (usuarioRepository.buscarPorCorreo(demo.correo()).isPresent()) {
                resultados.add("Omitido (ya existe): " + demo.correo());
                continue;
            }
            crear(demo);
            resultados.add("Creado: " + demo.correo());
        }
        return resultados;
    }

    private void crear(DefinicionDemo demo) {
        Rol rol = rolRepository.buscarPorId(demo.rolId())
                .orElseThrow(() -> new IllegalStateException("No existe el rol demo " + demo.rolId()));
        String empresaNombre = demo.empresaId() == null ? null
                : empresaRepository.buscarNombre(demo.empresaId())
                        .orElseThrow(() -> new IllegalStateException("No existe la empresa demo"));
        char[] password = demo.password().toCharArray();
        try {
            Usuario usuario = new Usuario(null, rol, demo.empresaId(), empresaNombre,
                    demo.nombreUsuario(), protectorPassword.generarHash(password),
                    demo.nombres(), demo.apellidos(), demo.correo(), true, null);
            EventoAuditoria evento = new EventoAuditoria(null, null,
                    "INICIALIZAR_USUARIO_DEMO", "usuarios", null, "EXITOSO",
                    "Inicializacion controlada del usuario " + demo.correo());
            usuarioRepository.crearConAuditoria(usuario, evento);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private record DefinicionDemo(String nombreUsuario, String password, String nombres,
            String apellidos, String correo, int rolId, Long empresaId) {
    }
}
