# Decisiones del esquema de SICA

## Alcance y limitacion de la referencia

Se conservaron las 11 tablas indicadas para SICA. El repositorio publico de referencia permite revisar la organizacion Java y su dependencia JDBC, pero no contiene el DDL original de SICA. El ZIP mencionado no estaba en el proyecto ni en su carpeta padre. Por ello, las relaciones se definieron a partir de los flujos descritos y deben compararse con el script original del profesor cuando este se encuentre disponible.

## Campos incorporados y justificacion

- `usuarios.empresa_id`: relaciona al funcionario con su empresa y permite limitar las solicitudes que puede consultar. Es opcional porque los usuarios de seguridad pueden pertenecer a la administracion general.
- `visitas.anfitrion_usuario_id`: identifica al funcionario responsable de recibir al visitante. Es obligatorio para que toda solicitud tenga un anfitrion.
- `visitas.fecha_programada`: permite prerregistrar una visita futura. Es opcional para una visita no anunciada.
- `visitas.tipo_solicitud`: distingue `PRERREGISTRADA`, `NO_ANUNCIADA` y `PASE_TEMPORAL` sin crear tablas innecesarias.
- `visitas.fecha_respuesta`: registra cuando se aprobo o rechazo una solicitud.
- `visitas.motivo`: conserva la razon de la visita o del pase y es obligatorio.
- `visitas.fecha_cierre_sistema`, `regularizada_por_usuario_id` y `motivo_regularizacion`: forman un conjunto obligatorio cuando se corrige una salida olvidada. La fecha sola no permitiria saber quien regularizo ni por que.
- `visitas.codigo_pase` y `fecha_vencimiento_pase`: identifican y limitan el pase temporal del trabajador con carnet olvidado. Son obligatorios solamente para ese tipo de solicitud.
- `visitas.registrado_por_usuario_id` y `respondido_por_usuario_id`: separan al usuario que crea la solicitud del que toma la decision.

## Estados y flujos

Los estados de acceso permiten saber si la persona esta fuera, dentro, pendiente de regularizacion o bloqueada. Los estados de visita cubren solicitud pendiente, autorizacion o rechazo, ingreso en curso, salida pendiente de regularizacion, finalizacion, cancelacion y vencimiento.

Las solicitudes pendientes se obtienen consultando `visitas` por `estado_visita_id` y, cuando corresponda, por `anfitrion_usuario_id`. No se creo una tabla `notificaciones`, porque los flujos actuales no requieren mensajeria persistente ni entrega asincrona.

## Seguridad y auditoria

`bitacora_auditoria.usuario_id` acepta `NULL` y `nombre_usuario_intentado` guarda el correo digitado. Su longitud se amplio a 120 caracteres para coincidir con `usuarios.correo`. Esto permite registrar un login fallido aunque no exista un usuario autenticado. Los indices priorizan consultas por fecha, usuario, accion y resultado.

Las contrasenas se almacenan con PBKDF2-HMAC-SHA256, 600 000 iteraciones, sal aleatoria individual de 16 bytes y hash de 256 bits. Los usuarios demo se crean mediante una orden Java explicita; no hay contrasenas planas ni hashes fijos en SQL.

`usuarios.correo` paso a ser obligatorio porque es el identificador de autenticacion. `roles.requiere_empresa` evita codificar en Java que el Funcionario de Empresa debe relacionarse con una empresa valida. La migracion `002_usuarios_seguridad.sql` aplica estos cambios a una base existente.

## Integridad e indices

Las claves foraneas usan `RESTRICT` en registros historicos para evitar eliminaciones accidentales y `SET NULL` donde la relacion es opcional. Las relaciones incluidas en un `CHECK` tambien restringen cambios de identificador porque MySQL 8.4 no permite combinar esas columnas con `ON UPDATE CASCADE`. Las restricciones `CHECK` validan tipos, estados y coherencia basica de fechas. Se agregaron indices para las busquedas habituales de usuarios, personas, solicitudes pendientes, incidentes y auditoria.

Los scripts no eliminan bases ni tablas. `schema.sql` crea los objetos faltantes y `data.sql` puede ejecutarse nuevamente sin duplicar los catalogos iniciales. Para reconstruir completamente el entorno debe usarse una base vacia de forma deliberada.

## Tercera etapa: personas y empresas

La tabla `empresas` ya tenia `activa`, por lo que no se agrego otra columna de estado. Su correo sigue siendo opcional: el contacto principal puede registrarse mediante los campos existentes `telefono` y `correo`. El NIT permanece como clave unica y el nombre duplicado se evita desde el caso de uso.

La tabla `personas` ya podia representar trabajadores e invitados mediante `tipo_persona`; no se dividio en dos tablas. Se agrego solamente `foto_url VARCHAR(500)`, opcional, porque una URL no requiere almacenar imagenes en MySQL. Un trabajador exige una empresa activa desde la aplicacion. La empresa de un invitado es opcional y sirve como contexto para el futuro flujo de visitas; el funcionario anfitrion se relacionara en `visitas.anfitrion_usuario_id`, no se duplica en `personas`.

Se conservaron `FUERA`, `DENTRO`, `SALIDA_PENDIENTE` y `BLOQUEADO` porque describen situaciones fisicas de acceso. Se agregaron `ACTIVO` y `PROHIBICION_INGRESO` como estados administrativos solicitados para esta etapa. Cambiar el estado nunca elimina la persona ni sus visitas.

La migracion `003_personas_empresas.sql` tambien corrige una diferencia encontrada entre el esquema reconstruible y la base migrada: `usuarios.correo` debe ser obligatorio y `empresas.correo` opcional. Inserta los permisos granulares, sus relaciones por codigo y tres personas ficticias mediante operaciones repetibles que no sobrescriben datos locales.

Los permisos antiguos se mantienen para no romper etapas previas. Los casos de uso de usuarios migraron a cuatro permisos especificos, y empresas/personas usan siete permisos adicionales. La autorizacion no depende de comparar nombres de roles en Java.

## Cuarta etapa: control de visitas

La tabla `visitas` ya contenia anfitrion, programacion, tipo de solicitud, aprobador, fechas de entrada y salida, campos de pase y datos de regularizacion. No se duplicaron. Solo faltaba `placa_vehiculo VARCHAR(20)`, opcional, y un indice por persona y estado para localizar rapidamente una visita abierta.

Se agregaron los codigos `APROBADA`, `PENDIENTE_APROBACION`, `PENDIENTE_APROBACION_OLVIDO`, `DENTRO`, `FUERA`, `EXPIRADA` y `CERRADA_POR_SISTEMA`. El estado `RECHAZADA` existente se reutiliza. Los codigos anteriores permanecen para conservar registros historicos y compatibilidad. La aplicacion resuelve cada ID desde `visita_estados`, por lo que no depende de un numero fijo.

Los campos `codigo_pase` y `fecha_vencimiento_pase` existentes permiten un pase temporal de un solo uso: el check-in comprueba vigencia y ausencia de una entrada previa. `fecha_cierre_sistema`, `regularizada_por_usuario_id` y `motivo_regularizacion` guardan la salida olvidada sin eliminar ni alterar el historial anterior.

Se agregaron nueve permisos granulares de visitas. El Funcionario atiende unicamente registros cuyo `anfitrion_usuario_id` coincide con su usuario y cuya empresa coincide con la suya. No se creo `notificaciones`: las pantallas realizan consultas periodicas sobre `visitas`, una solucion suficiente para dos aplicaciones de escritorio conectadas a la misma base.

Las operaciones que compiten usan `SELECT ... FOR UPDATE` y vuelven a validar el estado dentro de una transaccion. La auditoria comparte esa conexion. Para tolerar una diferencia entre el reloj de la JVM y el de MySQL, `fecha_respuesta` usa el mayor valor entre `CURRENT_TIMESTAMP` y `fecha_solicitud`, manteniendo la restriccion temporal sin confiar en el reloj del cliente.

La migracion `004_control_visitas.sql` es repetible: consulta `information_schema` antes de crear columna e indice, e inserta catalogos y relaciones con claves unicas. No elimina ni modifica visitas preexistentes.

## Quinta etapa: incidentes y consultas de auditoria

La tabla `incidentes` reconstruida ya era mas completa que el minimo descrito por el profesor: ademas de visita, responsable, fecha y descripcion, contenia `persona_id`, `tipo`, `gravedad`, `estado`, `acciones_tomadas`, fecha de reporte y cierre. Por ello no se agregaron columnas ni una tabla de seguimiento. Se reutilizan los estados `ABIERTO`, `EN_REVISION` y `CERRADO`, y cualquier correccion queda resumida en la bitacora.

Se agregaron solamente tres permisos granulares: registrar, consultar y actualizar incidentes. `CONSULTAR_AUDITORIA` ya existia y se reutilizo. Superusuario y Supervisor tienen seguimiento y auditoria; el Guarda registra y consulta incidentes; el Funcionario no recibe acceso general. Esta distribucion se consulta desde MySQL y se vuelve a comprobar en cada caso de uso.

Los indices `idx_incidentes_visita_fecha`, `idx_incidentes_persona_fecha`, `idx_incidentes_reportado_fecha` e `idx_bitacora_entidad_registro_fecha` corresponden a filtros reales de las pantallas. La migracion `005_incidentes_auditoria.sql` consulta `information_schema`, puede repetirse y no elimina datos.

La bitacora es inmutable desde los casos de uso y la interfaz porque solo existe un puerto publico de consulta y no hay acciones de edicion o eliminacion. Sin embargo, el usuario de base de datos conserva permisos propios del entorno Docker; un administrador de MySQL podria modificar la tabla directamente. No se afirma inmutabilidad absoluta.

Durante la revision preventiva se reforzo el check-in: despues de bloquear la fila de la persona, la transaccion vuelve a comprobar `activa` y el estado de acceso. El vencimiento de pases se compara con `CURRENT_TIMESTAMP` de MySQL. Esto cierra una ventana de carrera entre una prohibicion y un ingreso sin cambiar el flujo funcional existente.

## Sexta etapa: reportes y dashboard

No se agregaron tablas ni columnas. Todos los indicadores se derivan de `visitas`, `visita_estados`, `personas`, `usuarios`, `empresas` e `incidentes`. Los rangos usan inicio inclusivo y dia posterior al final como limite exclusivo, de modo que incluyen correctamente todas las horas del ultimo dia.

Se agregaron `idx_visitas_fecha_solicitud`, `idx_visitas_fecha_ingreso`, `idx_visitas_fecha_salida` e `idx_visitas_fecha_cierre_sistema`. Corresponden a filtros reales y evitan recorridos completos al consultar periodos. La migracion `006_reportes.sql` comprueba `information_schema`, puede repetirse y no modifica datos.

El alcance empresarial se obtiene mediante `visitas.anfitrion_usuario_id -> usuarios.empresa_id`, que representa la empresa destino. No se usa `personas.empresa_id` para ese filtro porque un invitado puede no pertenecer a ninguna empresa o trabajar para una distinta. Las regularizaciones se filtran por `fecha_cierre_sistema` independientemente de la fecha en que se creo la visita original.
