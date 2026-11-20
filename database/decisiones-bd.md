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
