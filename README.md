# SICA - Sistema Integrado de Control de Acceso

Proyecto academico en Java para controlar el acceso a Zona Acme. La etapa actual implementa usuarios, autenticacion segura, autorizacion RBAC, auditoria y una primera interfaz JavaFX. Aun no incluye los modulos completos de visitas, incidentes ni reportes.

## Tecnologias

- JDK 21, compilando con `--release 17`.
- Maven.
- MySQL 8.4 y JDBC con MySQL Connector/J 9.2.0.
- JavaFX 17.0.17 y su plugin Maven oficial.
- JUnit 5.
- Docker Desktop y Docker Compose.

No se utiliza Spring Boot, Hibernate, JPA, Lombok, FXML ni librerias visuales externas.

## Arquitectura

El paquete base es `com.proyecto.sica`:

- `domain/entity`: `Usuario`, `Rol`, `Permiso` y `EventoAuditoria`.
- `domain/repository`: puertos de usuarios, roles, empresas y auditoria.
- `application/service`: autenticacion, autorizacion, sesion, hash y validaciones.
- `application/usecase`: crear, consultar, actualizar y activar o desactivar usuarios.
- `infrastructure/database`: fabrica y comprobacion de conexiones.
- `infrastructure/persistence`: adaptadores JDBC y transacciones.
- `infrastructure/ui`: login y dashboard provisional con JavaFX.
- `config`: propiedades y ensamblado manual de dependencias.
- `Main.java`: unico punto de entrada.

La interfaz llama servicios y casos de uso; nunca ejecuta SQL directamente.

## 1. Configurar el entorno

Desde la raiz del repositorio:

```powershell
Copy-Item .env.example .env
code .env
```

Cambie las claves del archivo local. `.env` esta ignorado por Git. Para ejecutar Java, defina en la terminal la misma configuracion:

```powershell
$env:SICA_DB_HOST = "localhost"
$env:SICA_DB_PORT = "3307"
$env:SICA_DB_NAME = "sica_db"
$env:SICA_DB_USER = "sica_app"
$env:SICA_DB_PASSWORD = "su_clave_local"
```

## 2. Iniciar MySQL y aplicar la migracion

```powershell
docker compose up -d
docker compose ps
```

Un volumen nuevo ejecuta automaticamente esquema, datos y migracion. Para una base creada en la etapa anterior, ejecute una vez:

```powershell
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /docker-entrypoint-initdb.d/03-usuarios-seguridad.sql'
```

La migracion es repetible: agrega `roles.requiere_empresa` solo si falta, hace obligatorio `usuarios.correo` y amplia a 120 caracteres el identificador de login fallido.

Para detener MySQL conservando los datos:

```powershell
docker compose stop
```

No use `docker compose down -v` si desea conservar la base.

## 3. Compilar y probar

Ejecute Maven desde `sica`, donde se encuentra `pom.xml`:

```powershell
cd sica
mvn clean compile
mvn test
```

La prueba de integracion con MySQL se ejecuta de forma explicita y requiere el contenedor y las variables anteriores:

```powershell
mvn -Dtest=SeguridadMysqlIT test
```

## 4. Inicializar usuarios academicos

La inicializacion genera hashes seguros y no reemplaza usuarios existentes:

```powershell
mvn exec:java "-Dexec.args=--inicializar-demo"
```

Credenciales exclusivas para desarrollo academico local:

| Rol | Correo | Contrasena |
| --- | --- | --- |
| Superusuario | `superusuario@sica.example` | `SicaSuper2026!` |
| Supervisor de Seguridad | `supervisor@sica.example` | `SicaSupervisor2026!` |
| Guarda de Seguridad | `guarda@sica.example` | `SicaGuarda2026!` |
| Funcionario de Empresa | `funcionario@sica.example` | `SicaFuncionario2026!` |

Estas contrasenas son publicas y deliberadamente de demostracion. No deben reutilizarse ni sustituir credenciales reales.

## 5. Abrir JavaFX

```powershell
mvn javafx:run
```

Tambien se puede iniciar `Main` con `--javafx`. La primera pantalla solicita correo y contrasena. Un login correcto muestra nombre, rol, empresa cuando corresponda y el boton para cerrar sesion.

La comprobacion JDBC sigue disponible:

```powershell
mvn exec:java "-Dexec.args=--verificar-conexion"
```

## Autenticacion y contrasenas

1. El correo se normaliza y se busca con `PreparedStatement`.
2. Se comprueba que el usuario este activo.
3. `ProtectorPassword` verifica PBKDF2-HMAC-SHA256.
4. Un login exitoso actualiza `ultimo_acceso`, registra auditoria e inicia la sesion en memoria.
5. Un intento fallido registra el correo intentado, nunca la contrasena.
6. El cierre de sesion registra auditoria y elimina el usuario de la sesion.

Cada hash contiene algoritmo, 600 000 iteraciones, sal aleatoria de 16 bytes y resultado de 256 bits. No se almacenan contrasenas en texto plano ni se utiliza SHA-256 simple.

## Autorizacion RBAC

Los permisos se consultan desde `roles`, `permisos` y `rol_permisos`. `ServicioAutorizacion` comprueba usuario activo, rol activo y codigo de permiso. Los casos de uso vuelven a validar el permiso, aunque la interfaz oculte una opcion en el futuro.

Los 13 permisos existentes cubren este modulo; `GESTIONAR_USUARIOS` protege crear, listar, consultar, actualizar, activar y desactivar. No fue necesario agregar permisos.

- Superusuario: los 13 permisos.
- Supervisor de Seguridad: gestion operativa, usuarios, auditoria y reportes; no modifica RBAC.
- Guarda de Seguridad: control diario de acceso e incidentes.
- Funcionario de Empresa: solicitudes propias y su consulta.

## Gestion de usuarios y auditoria

Los casos de uso validan nombres obligatorios, correo valido y unico, rol activo, empresa activa cuando corresponda y contrasena obligatoria al crear. Los usuarios no se eliminan: se activan o desactivan para conservar claves foraneas y trazabilidad.

Crear, actualizar, activar/desactivar y confirmar un login comparten transaccion con su evento de auditoria. Ante un error se ejecuta `rollback`.

## Principios y patrones aplicados

- Responsabilidad unica: hash, autenticacion, autorizacion, validacion, UI y JDBC estan separados.
- Inversion de dependencias: aplicacion depende de interfaces de repositorio, no de JDBC.
- Abierto/cerrado: puede agregarse otro adaptador de persistencia sin cambiar casos de uso.
- Repository: interfaces del dominio y adaptadores JDBC o en memoria.
- Factory simple: `ConnectionFactory` entrega una conexion nueva por operacion.
- Inyeccion por constructor: las dependencias se ensamblan en `ConfiguracionAplicacion`.

No se agregaron patrones cuyo uso no fuera necesario.

## Base de datos

Se conservan las 11 tablas originales. Los cambios de seguridad son:

- `roles.requiere_empresa` para expresar la regla desde datos.
- `usuarios.correo NOT NULL` porque el login se realiza por correo.
- `bitacora_auditoria.nombre_usuario_intentado VARCHAR(120)` para aceptar correos completos.

Los detalles se encuentran en `database/decisiones-bd.md`.

## Flujo Git

- `main`: entrega estable; no se modifica durante esta etapa.
- `develop`: integracion.
- `feature/usuarios-seguridad`: desarrollo de este modulo.
