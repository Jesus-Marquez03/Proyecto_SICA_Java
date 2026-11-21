# SICA - Sistema Integrado de Control de Acceso

Proyecto academico en Java para controlar el acceso a Zona Acme. La etapa actual implementa seguridad, empresas, personas, control de visitas, aprobaciones, pases temporales, entradas, salidas, regularizaciones, auditoria y pantallas JavaFX. Aun no incluye el modulo completo de incidentes ni reportes finales.

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

- `domain/entity`: usuarios, empresas, personas, visitas y sus estados.
- `domain/repository`: puertos de persistencia independientes de JDBC.
- `application/service`: autenticacion, autorizacion, sesion, hash y validadores.
- `application/usecase`: operaciones autorizadas de usuarios, empresas, personas y visitas.
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
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /docker-entrypoint-initdb.d/05-control-visitas.sql'
```

Las migraciones son repetibles. La cuarta agrega solamente `visitas.placa_vehiculo`, un indice para visitas abiertas, estados de visita y permisos granulares. No borra ni reemplaza visitas existentes.

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
mvn "-Dtest=SeguridadMysqlIT,PersonasEmpresasMysqlIT,ControlVisitasMysqlIT" test
```

## 4. Inicializar usuarios academicos

La inicializacion genera hashes seguros y cuatro visitas ficticias. No reemplaza usuarios ni visitas existentes:

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

Tambien se puede iniciar `Main` con `--javafx`. El menu lateral incluye Control de visitas con paneles de Guarda, Funcionario e Historial. Los botones se deshabilitan segun RBAC y cada caso de uso vuelve a comprobar el permiso. JDBC se ejecuta fuera del hilo grafico.

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

Los permisos se consultan desde `roles`, `permisos` y `rol_permisos`. `ServicioAutorizacion` comprueba usuario activo, rol activo y codigo de permiso. Las migraciones conservan los codigos anteriores por compatibilidad y agregan permisos granulares que se usan realmente:

- Usuarios: `CREAR_USUARIO`, `CONSULTAR_USUARIO`, `ACTUALIZAR_USUARIO` y `CAMBIAR_ESTADO_USUARIO`.
- Empresas: `CREAR_EMPRESA`, `CONSULTAR_EMPRESA` y `ACTUALIZAR_EMPRESA`.
- Personas: `CREAR_PERSONA`, `CONSULTAR_PERSONA`, `ACTUALIZAR_PERSONA` y `CAMBIAR_ESTADO_ACCESO`.
- Visitas: `PRERREGISTRAR_VISITA`, `SOLICITAR_INGRESO`, `CONSULTAR_VISITA`, `APROBAR_SOLICITUD`, `RECHAZAR_SOLICITUD`, `REGISTRAR_CHECK_IN`, `REGISTRAR_CHECK_OUT`, `REGULARIZAR_SALIDA_OLVIDADA` y `CONSULTAR_HISTORIAL_VISITAS`.

Superusuario y Supervisor poseen los permisos del modulo de visitas. El Guarda puede solicitar ingresos y pases, consultar, registrar entradas y salidas y regularizar. El Funcionario puede prerregistrar y responder solamente solicitudes dirigidas a el y de su empresa. Tambien puede consultar personas para seleccionar invitados, pero no administrarlas. Ocultar un boton nunca sustituye la comprobacion del caso de uso.

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

## Control de visitas

### Cuatro flujos implementados

1. **Invitado prerregistrado:** el Funcionario elige invitado, empresa, anfitrion, fecha futura, motivo y placa opcional. La visita nace `APROBADA`, pero aun no representa presencia fisica. El Guarda busca el documento y realiza el check-in despues de validar autorizacion y estado de acceso.
2. **Invitado no anunciado:** el Guarda crea una solicitud `PENDIENTE_APROBACION`. El anfitrion asignado puede aprobarla o rechazarla; un funcionario ajeno no puede responderla. Solamente una visita aprobada permite el check-in.
3. **Carnet olvidado:** el Guarda crea una solicitud `PENDIENTE_APROBACION_OLVIDO` para un trabajador. Al aprobarse, el codigo de pase y su vencimiento autorizan un unico ingreso durante ese dia. El check-in consume el pase.
4. **Salida olvidada:** al confirmar un nuevo ingreso autorizado, una visita anterior `DENTRO` se cierra como `CERRADA_POR_SISTEMA`. El cierre anterior, su auditoria y el nuevo check-in se confirman en una sola transaccion.

La prohibicion de ingreso siempre prevalece. Una solicitud pendiente, rechazada o expirada no permite registrar entrada.

### Estados y transiciones

Los IDs se consultan por codigo en `visita_estados`; no se codifican numeros en Java.

| Estado actual | Operacion valida | Estado resultante |
| --- | --- | --- |
| `PENDIENTE_APROBACION` | Aprobar / rechazar | `APROBADA` / `RECHAZADA` |
| `PENDIENTE_APROBACION_OLVIDO` | Aprobar / rechazar | `APROBADA` / `RECHAZADA` |
| `APROBADA` | Check-in valido | `DENTRO` |
| `DENTRO` | Check-out | `FUERA` |
| `DENTRO` anterior | Regularizacion | `CERRADA_POR_SISTEMA` |

`EXPIRADA` representa una autorizacion vencida. Se rechazan respuestas repetidas, check-in pendiente o rechazado, dos check-outs y la reutilizacion de un pase consumido.

### Concurrencia, transacciones y auditoria

Las decisiones, entradas, salidas y regularizaciones abren transacciones cortas. `VisitaJdbcRepository` bloquea la fila con `SELECT ... FOR UPDATE`, vuelve a leer el estado y solo entonces escribe. Por eso dos funcionarios no pueden aprobar y rechazar a la vez, dos guardas no registran la misma entrada y una salida no se confirma dos veces.

`EjecutorTransaccionalJdbc` comparte la misma conexion con los cambios y uno o varios eventos de auditoria. Cualquier error ejecuta `rollback`. Se registran `PRERREGISTRO_VISITA`, `SOLICITUD_VISITA_NO_ANUNCIADA`, `SOLICITUD_PASE_TEMPORAL`, `APROBACION_VISITA`, `RECHAZO_VISITA`, `CHECK_IN_VISITA`, `CHECK_OUT_VISITA` y `REGULARIZACION_SALIDA`.

### Actualizacion entre dos sesiones

El panel consulta cada cuatro segundos las solicitudes del anfitrion o las visitas del documento buscado. `ActualizadorPeriodico` usa un `ScheduledExecutorService` daemon, evita consultas superpuestas y entrega los resultados a JavaFX mediante `Platform.runLater`. Se cierra al salir de la sesion o detener la aplicacion. Es **polling**, no una notificacion push; dos instancias conectadas a la misma base ven el cambio sin reiniciarse.

### Pantallas y datos ficticios

Control de visitas contiene tres paneles reutilizables:

- **Guarda:** busca por documento, muestra persona, foto URL y estado; solicita visita o pase; registra check-in, check-out y regularizacion.
- **Funcionario:** prerregistra invitados y aprueba o rechaza solicitudes propias.
- **Historial:** filtra por documento, fecha y estado. Un Funcionario solo ve visitas en las que es anfitrion.

La inicializacion controlada agrega, si faltan, `DEMO-VIS-PRE`, `DEMO-VIS-NO`, `DEMO-PASE-TEMP` y `DEMO-SALIDA-ABIERTA`. Tambien permanece Bruno Restringido Demo para comprobar la denegacion de ingreso.

### Prueba manual con dos usuarios

1. Abra dos terminales en `sica` y ejecute `mvn javafx:run` en cada una.
2. Inicie una como `guarda@sica.example` y otra como `funcionario@sica.example`.
3. En la sesion del Guarda, abra **Control de visitas**, busque el documento de Carla Invitada Demo y elija empresa y anfitrion.
4. Escriba el motivo y pulse **Solicitar ingreso**. La fila queda `PENDIENTE_APROBACION`.
5. En la sesion del Funcionario, espere como maximo cuatro segundos, seleccione la solicitud y pulse **Aprobar**.
6. La sesion del Guarda mostrara `APROBADA` en el siguiente ciclo. Seleccione la visita y pulse **Registrar entrada**; el estado cambiara a `DENTRO`.
7. Al retirarse la persona, seleccione la visita y pulse **Registrar salida**; terminara `FUERA`.

## Principios y patrones aplicados

- Responsabilidad unica: hash, autenticacion, autorizacion, validacion, UI y JDBC estan separados.
- Inversion de dependencias: aplicacion depende de interfaces de repositorio, no de JDBC.
- Abierto/cerrado: puede agregarse otro adaptador de persistencia sin cambiar casos de uso.
- Repository: interfaces del dominio y adaptadores JDBC o en memoria.
- Factory simple: `ConnectionFactory` entrega una conexion nueva por operacion.
- Inyeccion por constructor: las dependencias se ensamblan en `ConfiguracionAplicacion`.
- MVC: las vistas JavaFX capturan/muestran datos y los controladores llaman casos de uso.
- Ejecutor transaccional: reutiliza el limite de transaccion y evita duplicar `commit`, `rollback` y auditoria.
- Fabrica simple: `FabricaVisita` centraliza la construccion valida de cada tipo de solicitud.
- Polling Observer: el actualizador entrega periodicamente una lista a su callback sin acoplarse a la vista.

No se agregaron patrones cuyo uso no fuera necesario.

## Base de datos

Se conservan las 11 tablas originales. Los cambios de seguridad son:

- `roles.requiere_empresa` para expresar la regla desde datos.
- `usuarios.correo NOT NULL` porque el login se realiza por correo.
- `bitacora_auditoria.nombre_usuario_intentado VARCHAR(120)` para aceptar correos completos.
- `personas.foto_url VARCHAR(500)` para una referencia opcional, sin almacenar archivos binarios.
- Estados `ACTIVO` y `PROHIBICION_INGRESO` sin eliminar los estados fisicos previos.
- Veinte permisos granulares agregados entre las etapas de seguridad, personas y visitas, distribuidos segun cada rol.
- `visitas.placa_vehiculo VARCHAR(20)` e indice `(persona_id, estado_visita_id)` para los flujos de acceso.
- Siete estados nuevos de visita; `RECHAZADA` se reutiliza porque ya existia.
- Coherencia entre esquema y migraciones: `usuarios.correo` obligatorio y `empresas.correo` opcional.

Los detalles se encuentran en `database/decisiones-bd.md`.

## Flujo Git

- `main`: entrega estable; no se modifica durante esta etapa.
- `develop`: integracion.
- `feature/control-visitas`: desarrollo de la cuarta etapa.
