# SICA - Sistema Integrado de Control de Acceso

Proyecto academico en Java para controlar el acceso a Zona Acme. Esta primera etapa prepara la arquitectura hexagonal, la base de datos MySQL, Docker y la conexion JDBC. Todavia no implementa login, CRUD, visitas, incidentes, reportes ni interfaz Swing.

## Tecnologias

- Java 17 como version objetivo (se puede compilar con JDK 21).
- Maven.
- MySQL 8.4 mediante Docker Desktop.
- JDBC con MySQL Connector/J 9.2.0.
- JUnit 5 para pruebas unitarias.

No se utiliza Spring Boot, Hibernate, JPA, Lombok ni JavaFX.

## Arquitectura actual

El codigo usa el paquete base `com.proyecto.sica`:

- `config`: carga propiedades y variables de entorno.
- `infrastructure/database`: crea conexiones JDBC y comprueba MySQL.
- `Main.java`: punto de entrada unico.

Los paquetes `domain/entity`, `domain/repository`, `application/usecase`, `application/service`, `infrastructure/persistence` e `infrastructure/ui` se agregaran cuando existan entidades, casos de uso, repositorios o pantallas reales. No se crean paquetes ni clases vacias. La futura interfaz Swing usara MVC dentro de `infrastructure/ui`.

## 1. Configurar el entorno

Desde la raiz del repositorio, cree el archivo local `.env`:

```powershell
Copy-Item .env.example .env
code .env
```

Cambie `SICA_DB_PASSWORD` y `SICA_DB_ROOT_PASSWORD`. El archivo `.env` esta ignorado por Git. No use las claves de ejemplo fuera de un entorno local.

Docker Compose lee `.env` automaticamente. Para ejecutar Java desde la misma terminal, defina las variables equivalentes. La clave debe coincidir con la configurada en `.env`:

```powershell
$env:SICA_DB_HOST = "localhost"
$env:SICA_DB_PORT = "3307"
$env:SICA_DB_NAME = "sica_db"
$env:SICA_DB_USER = "sica_app"
$env:SICA_DB_PASSWORD = "su_clave_local"
```

Tambien puede definir `SICA_DB_URL` para reemplazar la URL JDBC completa. Los valores no sensibles por defecto estan en `sica/src/main/resources/application.properties`.

## 2. Iniciar MySQL con Docker

Abra Docker Desktop y espere hasta ver que el motor esta en ejecucion. Luego, desde la raiz:

```powershell
docker compose up -d
docker compose ps
docker compose logs mysql
```

En el primer inicio de un volumen vacio, MySQL ejecuta automaticamente `database/schema.sql` y `database/data.sql`. Para detener el servicio sin borrar datos:

```powershell
docker compose stop
```

No use `docker compose down -v` si desea conservar la base de datos.

### Ejecutar nuevamente los scripts

Los scripts no eliminan tablas ni datos. Para crear objetos faltantes y actualizar los catalogos iniciales de manera controlada:

```powershell
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /docker-entrypoint-initdb.d/01-schema.sql'
docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" < /docker-entrypoint-initdb.d/02-data.sql'
```

Los detalles del modelo y sus decisiones estan en `database/decisiones-bd.md`.

## 3. Compilar y probar

Ejecute Maven desde la carpeta que contiene `pom.xml`:

```powershell
cd sica
mvn clean compile
mvn test
```

El proyecto se compila para Java 17 aunque Maven se ejecute con JDK 21.

## 4. Ejecutar

Mostrar la ayuda inicial:

```powershell
mvn exec:java
```

Comprobar la conexion con MySQL:

```powershell
mvn exec:java -Dexec.args="--verificar-conexion"
```

Una conexion correcta muestra el nombre y la version de MySQL. Una configuracion incompleta o una conexion fallida termina con un mensaje breve y codigo de salida distinto de cero.

## Base de datos

El esquema conserva estas 11 tablas:

1. `roles`
2. `permisos`
3. `rol_permisos`
4. `usuarios`
5. `persona_estados_acceso`
6. `visita_estados`
7. `empresas`
8. `personas`
9. `visitas`
10. `incidentes`
11. `bitacora_auditoria`

Los datos iniciales incluyen cuatro roles, trece permisos, los estados necesarios y dos empresas de demostracion. No incluyen usuarios ni contrasenas.

## Flujo Git

- `main`: se reserva para la entrega final.
- `develop`: integra funcionalidades terminadas.
- `feature/configuracion-inicial`: trabajo de esta etapa.

Los cambios se registran mediante Conventional Commits y la feature se integra en `develop` sin modificar `main`.
