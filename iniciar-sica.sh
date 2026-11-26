#!/usr/bin/env bash

set -Eeuo pipefail

SOLO_VERIFICAR=false
INICIALIZAR_DEMO=false

for argumento in "$@"; do
    case "$argumento" in
        --solo-verificar) SOLO_VERIFICAR=true ;;
        --inicializar-demo) INICIALIZAR_DEMO=true ;;
        *)
            printf '[ERROR] Opcion desconocida: %s\n' "$argumento" >&2
            exit 1
            ;;
    esac
done

if (( BASH_VERSINFO[0] < 4 )); then
    printf '[ERROR] SICA requiere Bash 4 o superior.\n' >&2
    exit 1
fi

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$SCRIPT_DIR"
ENV_FILE="$ROOT_DIR/.env"
COMPOSE_FILE="$ROOT_DIR/compose.yaml"
MODULE_DIR="$ROOT_DIR/sica"
POM_FILE="$MODULE_DIR/pom.xml"
LOCK_DIR=""

info() {
    printf '[SICA] %s\n' "$1"
}

fail() {
    printf '[ERROR] %s\n' "$1" >&2
    exit 1
}

trim() {
    local value="$1"
    value="${value#"${value%%[![:space:]]*}"}"
    value="${value%"${value##*[![:space:]]}"}"
    printf '%s' "$value"
}

cleanup() {
    if [[ -n "$LOCK_DIR" && -d "$LOCK_DIR" ]]; then
        rm -f -- "$LOCK_DIR/pid"
        rmdir -- "$LOCK_DIR" 2>/dev/null || true
    fi
}
trap cleanup EXIT INT TERM

[[ -f "$ENV_FILE" ]] ||
    fail "No existe $ENV_FILE. Copie .env.example como .env y configure sus claves locales."
[[ -f "$COMPOSE_FILE" ]] || fail "No se encontro compose.yaml en la raiz del proyecto."
[[ -f "$POM_FILE" ]] || fail "No se encontro el pom.xml del modulo sica."

lock_key="$(printf '%s' "$ROOT_DIR" | cksum | awk '{print $1}')"
LOCK_DIR="${TMPDIR:-/tmp}/sica-${UID:-$(id -u)}-${lock_key}.lock"
if ! mkdir -- "$LOCK_DIR" 2>/dev/null; then
    existing_pid=""
    if [[ -f "$LOCK_DIR/pid" ]]; then
        IFS= read -r existing_pid < "$LOCK_DIR/pid" || true
    fi
    if [[ "$existing_pid" =~ ^[0-9]+$ ]] && kill -0 "$existing_pid" 2>/dev/null; then
        fail "SICA ya se esta iniciando o ejecutando desde este proyecto."
    fi
    rm -f -- "$LOCK_DIR/pid"
    rmdir -- "$LOCK_DIR" 2>/dev/null ||
        fail "No fue posible recuperar el bloqueo temporal $LOCK_DIR."
    mkdir -- "$LOCK_DIR" || fail "No fue posible crear el bloqueo temporal de SICA."
fi
printf '%s\n' "$$" > "$LOCK_DIR/pid"

declare -A CONFIG=()
ALLOWED_KEYS=(
    SICA_DB_HOST
    SICA_DB_PORT
    SICA_DB_NAME
    SICA_DB_USER
    SICA_DB_PASSWORD
    SICA_DB_ROOT_PASSWORD
    SICA_DB_URL
)
REQUIRED_KEYS=(
    SICA_DB_HOST
    SICA_DB_PORT
    SICA_DB_NAME
    SICA_DB_USER
    SICA_DB_PASSWORD
    SICA_DB_ROOT_PASSWORD
)

line_number=0
while IFS= read -r raw_line || [[ -n "$raw_line" ]]; do
    ((line_number += 1))
    raw_line="${raw_line%$'\r'}"
    line="$(trim "$raw_line")"
    if (( line_number == 1 )); then
        line="${line#$'\xEF\xBB\xBF'}"
    fi
    [[ -z "$line" || "${line:0:1}" == "#" ]] && continue
    [[ "$line" == *=* ]] || continue

    key="$(trim "${line%%=*}")"
    value="$(trim "${line#*=}")"
    allowed=false
    for candidate in "${ALLOWED_KEYS[@]}"; do
        if [[ "$key" == "$candidate" ]]; then
            allowed=true
            break
        fi
    done
    [[ "$allowed" == true ]] || continue

    if [[ ${#value} -ge 2 &&
            (("${value:0:1}" == '"' && "${value: -1}" == '"') ||
             ("${value:0:1}" == "'" && "${value: -1}" == "'")) ]]; then
        value="${value:1:${#value}-2}"
    elif [[ "${value:0:1}" == '"' || "${value:0:1}" == "'" ||
            "${value: -1}" == '"' || "${value: -1}" == "'" ]]; then
        fail "Comillas incompletas en .env, linea $line_number."
    fi
    CONFIG["$key"]="$value"
done < "$ENV_FILE"

for key in "${REQUIRED_KEYS[@]}"; do
    if [[ ! ${CONFIG[$key]+existe} || -z "${CONFIG[$key]}" ]]; then
        fail "Falta la clave obligatoria $key en .env."
    fi
done

for key in "${ALLOWED_KEYS[@]}"; do
    if [[ ${CONFIG[$key]+existe} ]]; then
        export "$key=${CONFIG[$key]}"
    fi
done
info "Configuracion local cargada sin mostrar credenciales."

command -v docker >/dev/null 2>&1 ||
    fail "No se encontro Docker. Instale Docker Engine y Docker Compose."
docker version --format '{{.Server.Version}}' >/dev/null 2>&1 ||
    fail "Docker Engine no esta disponible. Inicie el servicio o revise los permisos del usuario sobre Docker."
docker compose version >/dev/null 2>&1 ||
    fail "Docker Compose no esta disponible. Verifique 'docker compose version'."
info "Docker Engine y Docker Compose disponibles."

command -v java >/dev/null 2>&1 ||
    fail "No se encontro Java. Instale un JDK 17 o superior y configure PATH o JAVA_HOME."
java_line="$(java -version 2>&1 | sed -n '1p')"
if [[ "$java_line" =~ \"([0-9]+)(\.([0-9]+))? ]]; then
    java_major="${BASH_REMATCH[1]}"
    if [[ "$java_major" == "1" ]]; then
        java_major="${BASH_REMATCH[3]:-0}"
    fi
else
    fail "No fue posible determinar la version del JDK."
fi
(( java_major >= 17 )) ||
    fail "SICA requiere JDK 17 o superior. Se detecto Java $java_major."
if [[ -n "${JAVA_HOME:-}" && ! -x "$JAVA_HOME/bin/java" ]]; then
    fail "JAVA_HOME no apunta a un JDK valido. Revise JAVA_HOME."
fi
info "Java $java_major detectado."

command -v mvn >/dev/null 2>&1 ||
    fail "No se encontro Maven. Instale Maven y agregue mvn al PATH."
maven_line="$(mvn --version | sed -n '1p')"
info "$maven_line"

compose=(docker compose --project-directory "$ROOT_DIR" -f "$COMPOSE_FILE")
info "Iniciando el servicio MySQL existente..."
"${compose[@]}" up -d mysql

container_id="$("${compose[@]}" ps -q mysql)"
[[ -n "$container_id" ]] ||
    fail "Docker Compose no devolvio el contenedor MySQL de SICA."

info "Esperando a que MySQL este saludable..."
deadline=$((SECONDS + 180))
last_status=""
status=""
while (( SECONDS < deadline )); do
    status="$(docker inspect --format \
        '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
        "$container_id" 2>/dev/null || true)"
    if [[ -n "$status" && "$status" != "$last_status" ]]; then
        info "Estado de MySQL: $status"
        last_status="$status"
    fi
    [[ "$status" == "healthy" ]] && break
    case "$status" in
        unhealthy|exited|dead)
            fail "MySQL termino con estado '$status'. Revise 'docker compose ps' y los logs."
            ;;
    esac
    sleep 2
done
[[ "$status" == "healthy" ]] ||
    fail "MySQL no alcanzo el estado healthy en 180 segundos."

cd -- "$MODULE_DIR"
info "Verificando la conexion JDBC..."
if ! mvn exec:java "-Dexec.args=--verificar-conexion"; then
    fail "La conexion JDBC fallo. Revise las claves SICA_DB_* de .env y el estado de MySQL."
fi

if [[ "$INICIALIZAR_DEMO" == true ]]; then
    info "Ejecutando la inicializacion academica solicitada..."
    mvn exec:java "-Dexec.args=--inicializar-demo" ||
        fail "No fue posible inicializar los datos academicos."
fi

if [[ "$SOLO_VERIFICAR" == true ]]; then
    info "Verificacion completada. JavaFX no se abrio por solicitud."
    exit 0
fi

info "Abriendo SICA..."
mvn javafx:run || fail "JavaFX termino con un error. Revise la salida anterior."
