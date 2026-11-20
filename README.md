# SICA - Sistema Integrado de Control de Acceso

Proyecto academico en Java para controlar el acceso a Zona Acme. La etapa actual implementa seguridad, empresas, trabajadores, invitados, estados de acceso, auditoria y pantallas JavaFX. Aun no incluye los modulos completos de visitas, incidentes ni reportes.

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

- `domain/entity`: usuarios, roles, permisos, empresas, personas y estados de acceso.
- `domain/repository`: puertos de persistencia independientes de JDBC.
- `application/service`: autenticacion, autorizacion, sesion, hash y validadores.
- `application/usecase`: operaciones autorizadas de usuarios, empresas y personas.
- `infrastructure/database`: fabrica y comprobacion de conexiones.
- `infrastructure/persistence`: adaptadores JDBC y transacciones.
- `infrastructure/ui`: vistas JavaFX, controladores MVC y tareas asincronas.
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

## 2. Iniciar MySQL y aplicar las migraciones

```powershell
docker compose up -d
docker compose ps
```

Un volumen nuevo ejecuta automaticamente esquema, datos y migracion. Para una base creada en la etapa anterior, ejecute una vez:

```powershell
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /docker-entrypoint-initdb.d/03-usuarios-seguridad.sql'
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /docker-entrypoint-initdb.d/04-personas-empresas.sql'
```

Ambas migraciones son repetibles. La tercera etapa agrega `personas.foto_url`, los estados administrativos, permisos granulares y datos ficticios solo cuando faltan. No borra ni reemplaza registros existentes.

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
mvn "-Dtest=SeguridadMysqlIT,PersonasEmpresasMysqlIT" test
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

Tambien se puede iniciar `Main` con `--javafx`. Tras el login, el menu lateral abre Empresas, Personas y Estados de acceso. Los botones se deshabilitan segun RBAC y cada caso de uso vuelve a comprobar el permiso. Las consultas JDBC se ejecutan mediante `Task`, fuera del hilo grafico.

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

Los permisos se consultan desde `roles`, `permisos` y `rol_permisos`. `ServicioAutorizacion` comprueba usuario activo, rol activo y codigo de permiso. La migracion conserva los 13 codigos originales por compatibilidad y agrega 11 codigos granulares que se usan realmente:

- Usuarios: `CREAR_USUARIO`, `CONSULTAR_USUARIO`, `ACTUALIZAR_USUARIO` y `CAMBIAR_ESTADO_USUARIO`.
- Empresas: `CREAR_EMPRESA`, `CONSULTAR_EMPRESA` y `ACTUALIZAR_EMPRESA`.
- Personas: `CREAR_PERSONA`, `CONSULTAR_PERSONA`, `ACTUALIZAR_PERSONA` y `CAMBIAR_ESTADO_ACCESO`.

Superusuario y Supervisor gestionan este modulo. El Guarda puede consultar empresas y crear, consultar o actualizar personas, pero no cambiar restricciones administrativas. El Funcionario solo consulta empresas. Esta distribucion evita conceder permisos administrativos a todos.

## Gestion de usuarios y auditoria

Los casos de uso validan nombres obligatorios, correo valido y unico, rol activo, empresa activa cuando corresponda y contrasena obligatoria al crear. Los usuarios no se eliminan: se activan o desactivan para conservar claves foraneas y trazabilidad.

Crear, actualizar, activar/desactivar y confirmar un login comparten transaccion con su evento de auditoria. Ante un error se ejecuta `rollback`.

## Empresas, personas y estados de acceso

Una `Empresa` tiene NIT unico, nombre unico a nivel de aplicacion, contacto opcional y estado activo. No se elimina fisicamente porque puede estar relacionada con usuarios, personas y visitas.

Una `Persona` representa un `TRABAJADOR` o un `VISITANTE` (nombre visible: Invitado). El documento compuesto por tipo y numero es unico. Un trabajador requiere una empresa activa; para un invitado es opcional y puede identificar la empresa que visitara. La fotografia es una URL opcional `http` o `https`.

El estado inicial es `ACTIVO`. `PROHIBICION_INGRESO` expresa una restriccion administrativa sin borrar la persona ni su historial. Los estados fisicos anteriores (`FUERA`, `DENTRO` y `SALIDA_PENDIENTE`) se conservan para los futuros flujos de visita. Aplicar el mismo estado no escribe ni audita un cambio innecesario.

Datos ficticios incluidos de forma idempotente:

- Andrea Trabajadora Demo, activa y vinculada a Empresa Demo S.A.S.
- Bruno Restringido Demo, con prohibicion de ingreso.
- Carla Invitada Demo, activa y vinculada a Zona Acme Administracion.

Las creaciones, actualizaciones y cambios de estado registran usuario, accion, tabla, ID y una descripcion no sensible. `EjecutorTransaccionalJdbc` confirma el cambio y la auditoria juntos o revierte ambos.

## Principios y patrones aplicados

- Responsabilidad unica: hash, autenticacion, autorizacion, validacion, UI y JDBC estan separados.
- Inversion de dependencias: aplicacion depende de interfaces de repositorio, no de JDBC.
- Abierto/cerrado: puede agregarse otro adaptador de persistencia sin cambiar casos de uso.
- Repository: interfaces del dominio y adaptadores JDBC o en memoria.
- Factory simple: `ConnectionFactory` entrega una conexion nueva por operacion.
- Inyeccion por constructor: las dependencias se ensamblan en `ConfiguracionAplicacion`.
- MVC: las vistas JavaFX capturan/muestran datos y los controladores llaman casos de uso.
- Ejecutor transaccional: reutiliza el limite de transaccion y evita duplicar `commit`, `rollback` y auditoria.

No se agregaron patrones cuyo uso no fuera necesario.

## Base de datos

Se conservan las 11 tablas originales. Los cambios de seguridad son:

- `roles.requiere_empresa` para expresar la regla desde datos.
- `usuarios.correo NOT NULL` porque el login se realiza por correo.
- `bitacora_auditoria.nombre_usuario_intentado VARCHAR(120)` para aceptar correos completos.
- `personas.foto_url VARCHAR(500)` para una referencia opcional, sin almacenar archivos binarios.
- Estados `ACTIVO` y `PROHIBICION_INGRESO` sin eliminar los estados fisicos previos.
- Once permisos granulares y su distribucion por rol.
- Coherencia entre esquema y migraciones: `usuarios.correo` obligatorio y `empresas.correo` opcional.

Los detalles se encuentran en `database/decisiones-bd.md`.

## Flujo Git

- `main`: entrega estable; no se modifica durante esta etapa.
- `develop`: integracion.
- `feature/personas-empresas`: desarrollo de esta etapa.
