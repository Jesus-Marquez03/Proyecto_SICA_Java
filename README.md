# SICA - Sistema Integrado de Control de Acceso

Proyecto academico en Java para controlar el acceso a Zona Acme. La sexta etapa incluye seguridad, empresas, personas, visitas, incidentes, auditoria consultable, reportes reales y una interfaz JavaFX monocromatica con dashboard Bento.

## Tecnologias

- JDK 17 o superior (probado con JDK 21), compilando con `--release 17`.
- Maven.
- MySQL 8.4 y JDBC con MySQL Connector/J 9.2.0.
- JavaFX 17.0.17 y su plugin Maven oficial.
- JUnit 5.
- Docker Desktop y Docker Compose.

No se utiliza Spring Boot, Hibernate, JPA, Lombok, FXML ni librerias visuales externas.

## Arquitectura

El paquete base es `com.proyecto.sica`:

- `domain/entity`: usuarios, empresas, personas, visitas, incidentes y registros de auditoria.
- `domain/repository`: puertos de persistencia independientes de JDBC.
- `application/service`: autenticacion, autorizacion, sesion, hash y validadores.
- `application/usecase`: operaciones autorizadas de usuarios, empresas, personas, visitas, incidentes y auditoria.
- `infrastructure/database`: fabrica y comprobacion de conexiones.
- `infrastructure/persistence`: adaptadores JDBC y transacciones.
- `infrastructure/ui`: vistas JavaFX, controladores MVC y tareas asincronas.
- `config`: propiedades y ensamblado manual de dependencias.
- `Main.java`: unico punto de entrada.

La interfaz llama servicios y casos de uso; nunca ejecuta SQL directamente.

## Inicio automatico habitual

Despues de preparar el equipo una sola vez, ejecute desde cualquier directorio:

Windows 11:

```powershell
& "C:\ruta\al\proyecto\iniciar-sica.ps1"
```

Si la terminal ya esta ubicada en la raiz del repositorio:

```powershell
.\iniciar-sica.ps1
```

Linux:

```bash
bash "/ruta/al/proyecto/iniciar-sica.sh"
```

El iniciador localiza la raiz del repositorio, lee `.env`, configura las variables
solo para sus procesos hijos, comprueba Java, Maven, Docker y Compose, inicia el
servicio `mysql`, espera su estado `healthy`, verifica JDBC y abre JavaFX. Al
cerrar SICA, MySQL y su volumen permanecen activos. No se solicitan contrasenas
en el inicio habitual y no se modifican variables globales del sistema.

Para comprobar requisitos, Docker y JDBC sin abrir la interfaz:

```powershell
.\iniciar-sica.ps1 -SoloVerificar
```

```bash
bash iniciar-sica.sh --solo-verificar
```

## Primera instalacion en Windows

1. Instale o compruebe JDK 17 o superior, Maven, Docker Desktop y Docker Compose:

   ```powershell
   java -version
   mvn --version
   docker version
   docker compose version
   ```

   El script usa `mvn` del `PATH`. Si no esta disponible, tambien busca
   `MAVEN_HOME` y las ubicaciones habituales de Maven en Windows.

2. Desde la raiz del repositorio cree la configuracion local:

   ```powershell
   Copy-Item .env.example .env
   notepad .env
   ```

3. Cambie ambas contrasenas de ejemplo y conserve `SICA_DB_HOST=localhost` y el
   puerto publicado `3307`, salvo que su entorno requiera otro puerto.

4. Inicie Docker Desktop y valide todo:

   ```powershell
   .\iniciar-sica.ps1 -SoloVerificar
   ```

5. En una base nueva, cree de forma explicita los usuarios y datos academicos.
   Esta opcion es idempotente y no se ejecuta en los inicios normales:

   ```powershell
   .\iniciar-sica.ps1 -InicializarDemo
   ```

Si la politica local impide ejecutar scripts y el administrador lo permite,
puede habilitar scripts locales una sola vez para su usuario:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

En un equipo administrado donde no pueda cambiar esa politica, utilice
`powershell.exe -ExecutionPolicy Bypass -File .\iniciar-sica.ps1`; esta
excepcion se limita al proceso actual.

## Primera instalacion en Linux

1. Compruebe JDK 17 o superior, Maven, Docker Engine y el complemento Compose:

   ```bash
   java -version
   mvn --version
   docker version
   docker compose version
   ```

   El script no usa `sudo`. Si Docker responde con permiso denegado, solicite al
   administrador que habilite su usuario para utilizar Docker o siga la politica
   de la universidad.

2. Cree el archivo local de configuracion:

   ```bash
   cp .env.example .env
   nano .env
   ```

3. Compruebe el arranque y JDBC:

   ```bash
   bash iniciar-sica.sh --solo-verificar
   ```

4. Inicialice una sola vez los usuarios y datos academicos:

   ```bash
   bash iniciar-sica.sh --inicializar-demo
   ```

5. Para los siguientes inicios use solamente:

   ```bash
   bash iniciar-sica.sh
   ```

El archivo se versiona con finales de linea LF. Opcionalmente puede hacerlo
ejecutable con `chmod +x iniciar-sica.sh`. JavaFX requiere una sesion grafica
X11 o Wayland y las bibliotecas nativas habituales de GTK 3. Maven descarga los
artefactos JavaFX apropiados para el sistema operativo; el proyecto no incluye
binarios JavaFX exclusivos de Windows.

## Configuracion local y seguridad

`.env` esta ignorado por Git. Los iniciadores interpretan unicamente:

- `SICA_DB_HOST`
- `SICA_DB_PORT`
- `SICA_DB_NAME`
- `SICA_DB_USER`
- `SICA_DB_PASSWORD`
- `SICA_DB_ROOT_PASSWORD`
- `SICA_DB_URL`, solamente si se agrega como URL JDBC opcional.

No se ejecuta `.env` como codigo, no se utiliza `eval` y las contrasenas no se
incluyen en argumentos de procesos ni se imprimen. Se aceptan valores sin
comillas o encerrados entre comillas simples o dobles. Las variables se exportan
unicamente al proceso de Docker/Maven iniciado por el script, por eso no
desaparecen como requisito cuando se reinicia el computador: se vuelven a cargar
automaticamente en cada ejecucion.

Si `.env` falta, el iniciador se detiene y solicita copiar `.env.example`; nunca
sobrescribe una configuracion existente.

## MySQL, esquema y migraciones

En una instalacion nueva, `docker compose up` crea el volumen
`sica_mysql_data`. MySQL ejecuta automaticamente, en orden, `schema.sql`,
`data.sql` y todas las migraciones montadas en `docker-entrypoint-initdb.d`.
Estos archivos solo se ejecutan automaticamente cuando el volumen esta vacio.

Para una base creada con una version anterior, aplique una sola vez las
migraciones que le falten:

```powershell
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /docker-entrypoint-initdb.d/03-usuarios-seguridad.sql'
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /docker-entrypoint-initdb.d/04-personas-empresas.sql'
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /docker-entrypoint-initdb.d/05-control-visitas.sql'
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /docker-entrypoint-initdb.d/06-incidentes-auditoria.sql'
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /docker-entrypoint-initdb.d/07-reportes.sql'
```

El iniciador nunca elimina contenedores, volúmenes o datos y no vuelve a aplicar
el esquema en cada arranque. Para detener MySQL conservando el volumen:

```powershell
docker compose stop
```

No use `docker compose down -v` si desea conservar la base. Un volumen de Docker
en Windows no se transfiere automaticamente al computador Linux; cada equipo
tiene datos locales independientes.

## Compilar, probar y ejecutar manualmente

Maven se ejecuta desde `sica`, donde se encuentra `pom.xml`:

```powershell
cd sica
mvn clean compile
mvn test
mvn exec:java "-Dexec.args=--verificar-conexion"
mvn javafx:run
```

La prueba de integracion MySQL es explicita y escribe datos controlados:

```powershell
mvn "-Dtest=SeguridadMysqlIT,PersonasEmpresasMysqlIT,ControlVisitasMysqlIT,IncidentesAuditoriaMysqlIT,ReportesMysqlIT" test
```

## Usuarios academicos

La inicializacion genera hashes seguros, visitas y tres incidentes ficticios sin
reemplazar usuarios existentes. Tambien puede ejecutarse manualmente desde
`sica` cuando las variables ya esten configuradas:

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

## Abrir JavaFX manualmente

```powershell
mvn javafx:run
```

Tambien se puede iniciar `Main` con `--javafx`. El menu lateral contiene nueve opciones: Dashboard, Control de acceso, Visitas, Personas, Empresas, Incidentes, Reportes, Usuarios y roles, y Bitacora. Los elementos se deshabilitan segun RBAC y cada caso de uso vuelve a comprobar el permiso. JDBC se ejecuta fuera del hilo grafico.

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
- Incidentes: `REGISTRAR_INCIDENTE`, `CONSULTAR_INCIDENTE` y `ACTUALIZAR_INCIDENTE`.
- Auditoria: se reutiliza `CONSULTAR_AUDITORIA` porque ya existia y expresaba exactamente la operacion.
- Reportes: `GENERAR_REPORTES`, combinado con los permisos de consulta de los datos mostrados.

Superusuario y Supervisor poseen los permisos del modulo de visitas. El Guarda puede solicitar ingresos y pases, consultar, registrar entradas y salidas y regularizar. El Funcionario puede prerregistrar y responder solamente solicitudes dirigidas a el y de su empresa. Tambien puede consultar personas para seleccionar invitados, pero no administrarlas. Ocultar un boton nunca sustituye la comprobacion del caso de uso.

En incidentes, Superusuario y Supervisor registran, consultan y actualizan. El Guarda registra y consulta, pero no altera el seguimiento. La bitacora completa solo es visible para Superusuario y Supervisor. El Funcionario no recibe acceso general a incidentes ni auditoria.

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

## Incidentes de seguridad y trazabilidad

Un **incidente** es un acontecimiento de seguridad ocurrido durante una visita. Un **evento de auditoria** es la evidencia de una accion ejecutada en SICA. Registrar un incidente crea una sola fila en `incidentes` y una entrada `CREACION_INCIDENTE` en la bitacora; no duplica el incidente como otro objeto de negocio.

### Operaciones

- Registrar un incidente relacionado con una visita existente.
- Consultar todos o filtrar por visita, persona, responsable, fecha y estado.
- Consultar detalle por ID.
- Actualizar gravedad, estado, descripcion y acciones con justificacion obligatoria.
- Aplicar `PROHIBICION_INGRESO` mediante el caso de uso existente de estados de acceso.

La descripcion original no desaparece sin evidencia: cada actualizacion registra en auditoria un resumen anterior/nuevo y la justificacion. Un incidente cerrado no puede reabrirse. No existen operaciones de eliminacion fisica.

### Consultas de auditoria

La pantalla de Auditoria permite filtrar los ultimos 200 eventos por usuario, accion, rango de fechas, tabla, registro, persona o visita. Los filtros de persona y visita incluyen relaciones verificables con `visitas` e `incidentes`. `ConsultaAuditoriaRepository` solo declara lectura; no hay casos de uso para editar o eliminar eventos.

La aplicacion protege la bitacora mediante RBAC y ausencia de operaciones de escritura historica en UI/casos de uso. Esto no vuelve inviolable la tabla frente a un administrador de MySQL o alguien con acceso directo a las credenciales de base de datos; esa es la limitacion real del entorno academico actual.

### Transacciones y pantallas

Crear y actualizar incidentes utiliza `EjecutorTransaccionalJdbc`: cambio y auditoria comparten conexion, `commit` y `rollback`. La actualizacion bloquea la fila con `SELECT ... FOR UPDATE` y rechaza una version modificada por otra sesion.

La pantalla **Incidentes** lista, filtra, registra, muestra detalle, guarda seguimiento y permite aplicar una prohibicion explicita segun permisos. La pantalla **Auditoria** es de solo lectura. Ambos controladores usan `EjecutorTareasUi` para no bloquear JavaFX.

Para probar:

1. Ejecute `mvn exec:java "-Dexec.args=--inicializar-demo"`.
2. Inicie JavaFX con el Supervisor de Seguridad.
3. Abra **Incidentes**, seleccione o escriba un ID de visita, complete tipo, gravedad y descripcion y registre.
4. Seleccione la fila, escriba una justificacion y guarde el seguimiento.
5. Abra **Auditoria** y filtre `CREACION_INCIDENTE` o `ACTUALIZACION_INCIDENTE`.

Datos idempotentes: `DEMO_NOVEDAD`, `DEMO_SUPERVISION` y `DEMO_RESTRICCION`. El ultimo utiliza una visita historica de Bruno Restringido Demo y demuestra que la prohibicion no elimina la visita ni el incidente.

## Reportes y dashboard final

El flujo de lectura es `Vista JavaFX -> Controlador -> Caso de uso -> ReporteRepository -> ReporteJdbcRepository -> MySQL`. Las vistas y controladores no contienen SQL. `ReporteJdbcRepository` usa `COUNT`, `SUM`, `GROUP BY`, rangos semiabiertos de fecha, `JOIN`, `PreparedStatement` y `try-with-resources`; MySQL realiza las agregaciones para evitar descargar tablas completas.

Reportes disponibles:

- **Accesos por periodo:** ingresos fisicos por `fecha_ingreso`, salidas por `fecha_salida`, serie diaria y diferencia entre los dos ultimos dias del filtro. Crear o aprobar una solicitud no cuenta como ingreso.
- **Personas dentro:** solamente visitas cuyo estado actual es `DENTRO`, con persona, documento, empresa destino, tipo, ingreso y visita asociada.
- **Estados de visita:** conteo por codigo para solicitudes creadas dentro del periodo seleccionado.
- **Incidentes:** total, abiertos, en revision, cerrados, gravedad y serie por fecha de ocurrencia.
- **Trazabilidad:** rechazos y pases por fecha de solicitud; regularizaciones por `fecha_cierre_sistema`; solicitudes pendientes actuales del periodo.

El dashboard consulta accesos de los ultimos siete dias, presencia actual, pendientes, incidentes abiertos y actividad reciente. El progreso mensual se define asi:

```text
visitas finalizadas = FUERA + CERRADA_POR_SISTEMA
visitas elegibles = finalizadas + APROBADA + DENTRO + EXPIRADA
progreso = finalizadas / elegibles * 100
```

Si no existen visitas elegibles muestra `Sin datos`; nunca divide por cero. La actividad se ordena por la ultima fecha operativa disponible. El permiso `GENERAR_REPORTES` protege la pantalla y `CONSULTAR_INCIDENTE` protege las estadisticas de incidentes. Un rol empresarial, si recibe estos permisos, queda limitado a visitas cuyo anfitrion pertenece a su empresa; no ve totales globales.

## Interfaz JavaFX definitiva

La interfaz conserva MVC y reutiliza los casos de uso anteriores. `DashboardView` es solo el marco de navegacion; `DashboardInicioView`, `ReportesView`, `UsuariosView` y las vistas de negocio mantienen responsabilidades separadas. `EjecutorTareasUi` ejecuta dashboard, reportes y operaciones administrativas fuera del hilo JavaFX. El polling de visitas existente no se duplico y se detiene al cerrar sesion.

Elementos visuales implementados:

- Fondo arquitectonico abstracto construido con formas JavaFX locales, sin imagenes ni licencias externas.
- Sidebar blanco, contenedor translucido, tarjetas Bento, tarjeta principal negra y superficies grises.
- Familia central de iconos `SVGPath` para las nueve opciones y acciones comunes; no se usan emojis ni PNG escalados.
- CSS unico y clases reutilizables para encabezados, tablas, formularios, botones, filtros, alertas, scroll y estados de seleccion.
- Contenido desplazable para ventanas intermedias; el diseno base se abre a 1366 x 820 y tiene minimo de 1050 x 680.
- Administracion de usuarios con listado, alta, edicion, activacion/desactivacion y consulta de roles/permisos existentes. No existe editor de asignaciones porque no hay un caso de uso autorizado.

`Control de acceso` y `Visitas` seleccionan paneles diferentes de la misma vista para no duplicar el modulo. La gestion de estados de acceso se conserva como pestana de Control de acceso. Todas las opciones navegan a contenido real y las operaciones siguen protegidas en aplicacion, no solo en botones.

### Confirmacion visual de operaciones

La biometria ya no es una seccion independiente ni contiene controles de simulacion. `NotificacionOperacion` reutiliza la huella vectorial y muestra una confirmacion compacta en la esquina superior derecha despues de una operacion real exitosa, como registrar una entrada, guardar una persona o aprobar una solicitud. Una nueva confirmacion sustituye la anterior y las transiciones se detienen al cerrar sesion.

Esta animacion no implementa autenticacion biometrica: no captura, compara, transmite ni almacena huellas, y tampoco concede permisos ni registra accesos por si sola. Los errores y las operaciones sin cambios no muestran el check de exito.

### Limitaciones visuales conocidas

Las imagenes adjuntas se usaron como referencia para composicion, sidebar e iconos. El video MP4 no pudo decodificarse porque el entorno no dispone de `ffmpeg` ni un decodificador accesible, por lo que sus fotogramas no se afirman como inspeccionados. Ademas, el servicio de captura de ventanas de Windows devolvio `native pipe is unavailable`: se verifico que JavaFX inicia y que no produce advertencias CSS, pero la comparacion visual completa debe hacerse manualmente.

Lista manual recomendada: iniciar con cada rol, recorrer las nueve opciones, probar 1366 x 768 y una ventana maximizada, comprobar scroll, tablas, formularios, confirmaciones visuales y logout.

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
- Read model: `RegistroAuditoria` expone una lectura inmutable separada de `EventoAuditoria`, que representa una escritura nueva.
- Componentes reutilizables: `IconosSica`, `TarjetaMetrica`, `EncabezadoModulo`, `FondoArquitectonico` y `HuellaBiometrica` concentran solo presentacion compartida.

No se agregaron patrones cuyo uso no fuera necesario.

## Base de datos

Se conservan las 11 tablas originales. Los cambios de seguridad son:

- `roles.requiere_empresa` para expresar la regla desde datos.
- `usuarios.correo NOT NULL` porque el login se realiza por correo.
- `bitacora_auditoria.nombre_usuario_intentado VARCHAR(120)` para aceptar correos completos.
- `personas.foto_url VARCHAR(500)` para una referencia opcional, sin almacenar archivos binarios.
- Estados `ACTIVO` y `PROHIBICION_INGRESO` sin eliminar los estados fisicos previos.
- Veintitres permisos granulares agregados entre las etapas, distribuidos segun cada rol.
- `visitas.placa_vehiculo VARCHAR(20)` e indice `(persona_id, estado_visita_id)` para los flujos de acceso.
- Siete estados nuevos de visita; `RECHAZADA` se reutiliza porque ya existia.
- Indices por visita, persona y responsable de incidente, y por entidad/registro de auditoria.
- Coherencia entre esquema y migraciones: `usuarios.correo` obligatorio y `empresas.correo` opcional.
- Indices de fecha para solicitud, ingreso, salida y cierre por sistema, usados por los reportes de la sexta etapa.

Los detalles se encuentran en `database/decisiones-bd.md`.

## Flujo Git

- `main`: entrega estable; no se modifica durante esta etapa.
- `develop`: integracion.
- `feature/reportes-interfaz-final`: desarrollo de la sexta etapa.
