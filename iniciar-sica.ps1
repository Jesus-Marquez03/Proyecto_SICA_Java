[CmdletBinding()]
param(
    [switch]$SoloVerificar,
    [switch]$InicializarDemo
)

Set-StrictMode -Version 2.0
$ErrorActionPreference = "Stop"

$raizProyecto = [System.IO.Path]::GetFullPath($PSScriptRoot)
$archivoEntorno = Join-Path $raizProyecto ".env"
$archivoCompose = Join-Path $raizProyecto "compose.yaml"
$directorioModulo = Join-Path $raizProyecto "sica"
$archivoPom = Join-Path $directorioModulo "pom.xml"
$clavesPermitidas = @(
    "SICA_DB_HOST",
    "SICA_DB_PORT",
    "SICA_DB_NAME",
    "SICA_DB_USER",
    "SICA_DB_PASSWORD",
    "SICA_DB_ROOT_PASSWORD",
    "SICA_DB_URL"
)
$clavesObligatorias = @(
    "SICA_DB_HOST",
    "SICA_DB_PORT",
    "SICA_DB_NAME",
    "SICA_DB_USER",
    "SICA_DB_PASSWORD",
    "SICA_DB_ROOT_PASSWORD"
)

$mutex = $null
$mutexAdquirido = $false

function Escribir-Paso {
    param([string]$Mensaje)
    Write-Host "[SICA] $Mensaje" -ForegroundColor Cyan
}

function Convertir-ValorDotEnv {
    param(
        [string]$Valor,
        [int]$NumeroLinea
    )

    $resultado = $Valor.Trim()
    if ($resultado.Length -eq 0) {
        return ""
    }

    $primero = $resultado.Substring(0, 1)
    $ultimo = $resultado.Substring($resultado.Length - 1, 1)
    if (($primero -eq '"' -and $ultimo -eq '"') -or
            ($primero -eq "'" -and $ultimo -eq "'")) {
        return $resultado.Substring(1, $resultado.Length - 2)
    }
    if ($primero -eq '"' -or $primero -eq "'" -or
            $ultimo -eq '"' -or $ultimo -eq "'") {
        throw "Comillas incompletas en .env, linea $NumeroLinea."
    }
    return $resultado
}

function Leer-ConfiguracionLocal {
    param([string]$Ruta)

    if (-not (Test-Path -LiteralPath $Ruta -PathType Leaf)) {
        throw "No existe $Ruta. Copie .env.example como .env y configure sus claves locales."
    }

    $configuracion = @{}
    $numeroLinea = 0
    foreach ($lineaOriginal in [System.IO.File]::ReadAllLines($Ruta)) {
        $numeroLinea++
        $linea = $lineaOriginal.Trim()
        if ($numeroLinea -eq 1) {
            $linea = $linea.TrimStart([char]0xFEFF)
        }
        if ($linea.Length -eq 0 -or $linea.StartsWith("#")) {
            continue
        }

        $separador = $linea.IndexOf("=")
        if ($separador -lt 1) {
            continue
        }
        $clave = $linea.Substring(0, $separador).Trim()
        if ($clavesPermitidas -notcontains $clave) {
            continue
        }
        $valor = Convertir-ValorDotEnv -Valor $linea.Substring($separador + 1) -NumeroLinea $numeroLinea
        $configuracion[$clave] = $valor
    }

    foreach ($clave in $clavesObligatorias) {
        if (-not $configuracion.ContainsKey($clave) -or
                [string]::IsNullOrWhiteSpace($configuracion[$clave])) {
            throw "Falta la clave obligatoria $clave en .env."
        }
    }
    return $configuracion
}

function Resolver-Java {
    $java = $null
    if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        $candidato = Join-Path $env:JAVA_HOME "bin\java.exe"
        if (-not (Test-Path -LiteralPath $candidato -PathType Leaf)) {
            throw "JAVA_HOME no apunta a un JDK valido. Revise JAVA_HOME."
        }
        $java = $candidato
    } else {
        $comando = Get-Command "java.exe" -ErrorAction SilentlyContinue
        if ($null -ne $comando) {
            $java = $comando.Source
        }
    }
    if ($null -eq $java) {
        throw "No se encontro Java. Instale un JDK 17 o superior y configure PATH o JAVA_HOME."
    }

    $preferenciaAnterior = $ErrorActionPreference
    try {
        $ErrorActionPreference = "Continue"
        $salidaCompleta = @(& $java -version 2>&1)
        $codigoJava = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $preferenciaAnterior
    }
    $salida = ($salidaCompleta | Select-Object -First 1).ToString()
    if ($codigoJava -ne 0 -or $salida -notmatch '"(?<mayor>[0-9]+)') {
        throw "No fue posible determinar la version del JDK."
    }
    $versionMayor = [int]$Matches["mayor"]
    if ($versionMayor -lt 17) {
        throw "SICA requiere JDK 17 o superior. Se detecto Java $versionMayor."
    }
    Escribir-Paso "Java $versionMayor detectado."
}

function Resolver-Maven {
    foreach ($nombre in @("mvn.cmd", "mvn")) {
        $comando = Get-Command $nombre -ErrorAction SilentlyContinue
        if ($null -ne $comando) {
            return $comando.Source
        }
    }

    $candidatos = New-Object System.Collections.Generic.List[string]
    if (-not [string]::IsNullOrWhiteSpace($env:MAVEN_HOME)) {
        [void]$candidatos.Add((Join-Path $env:MAVEN_HOME "bin\mvn.cmd"))
    }
    if (-not [string]::IsNullOrWhiteSpace($env:LOCALAPPDATA)) {
        $baseLocal = Join-Path $env:LOCALAPPDATA "Programs\Apache Maven"
        if (Test-Path -LiteralPath $baseLocal -PathType Container) {
            Get-ChildItem -LiteralPath $baseLocal -Directory -Filter "apache-maven-*" |
                Sort-Object Name -Descending |
                ForEach-Object { [void]$candidatos.Add((Join-Path $_.FullName "bin\mvn.cmd")) }
        }
    }
    if (-not [string]::IsNullOrWhiteSpace($env:ProgramFiles)) {
        [void]$candidatos.Add((Join-Path $env:ProgramFiles "Apache\Maven\bin\mvn.cmd"))
    }

    foreach ($candidato in $candidatos) {
        if (Test-Path -LiteralPath $candidato -PathType Leaf) {
            return $candidato
        }
    }
    throw "No se encontro Maven. Agregue mvn al PATH o configure MAVEN_HOME."
}

function Resolver-Docker {
    $comando = Get-Command "docker.exe" -ErrorAction SilentlyContinue
    if ($null -eq $comando) {
        $comando = Get-Command "docker" -ErrorAction SilentlyContinue
    }
    if ($null -eq $comando) {
        throw "No se encontro Docker. Instale e inicie Docker Desktop."
    }

    $docker = $comando.Source
    $versionMotor = & $docker version --format "{{.Server.Version}}" 2>$null
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace(($versionMotor -join ""))) {
        throw "Docker Engine no esta disponible. Inicie Docker Desktop y vuelva a intentar."
    }
    & $docker compose version *> $null
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose no esta disponible. Verifique 'docker compose version'."
    }
    Escribir-Paso "Docker Engine y Docker Compose disponibles."
    return $docker
}

function Esperar-MySql {
    param(
        [string]$Docker,
        [int]$TiempoMaximoSegundos = 180
    )

    $idContenedor = (& $Docker compose --project-directory $raizProyecto -f $archivoCompose ps -q mysql | Out-String).Trim()
    if ([string]::IsNullOrWhiteSpace($idContenedor)) {
        throw "Docker Compose no devolvio el contenedor MySQL de SICA."
    }

    Escribir-Paso "Esperando a que MySQL este saludable..."
    $limite = (Get-Date).AddSeconds($TiempoMaximoSegundos)
    $ultimoEstado = ""
    while ((Get-Date) -lt $limite) {
        $estado = (& $Docker inspect --format "{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" $idContenedor 2>$null | Out-String).Trim()
        if ($estado -ne $ultimoEstado -and -not [string]::IsNullOrWhiteSpace($estado)) {
            Escribir-Paso "Estado de MySQL: $estado"
            $ultimoEstado = $estado
        }
        if ($estado -eq "healthy") {
            return
        }
        if ($estado -eq "unhealthy" -or $estado -eq "exited" -or $estado -eq "dead") {
            throw "MySQL termino con estado '$estado'. Revise 'docker compose ps' y los logs."
        }
        Start-Sleep -Seconds 2
    }
    throw "MySQL no alcanzo el estado healthy en $TiempoMaximoSegundos segundos."
}

try {
    if (-not (Test-Path -LiteralPath $archivoCompose -PathType Leaf)) {
        throw "No se encontro compose.yaml en la raiz del proyecto."
    }
    if (-not (Test-Path -LiteralPath $archivoPom -PathType Leaf)) {
        throw "No se encontro el pom.xml del modulo sica."
    }

    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($raizProyecto.ToLowerInvariant())
        $identificador = ([System.BitConverter]::ToString($sha.ComputeHash($bytes))).Replace("-", "")
    } finally {
        $sha.Dispose()
    }
    $mutex = New-Object System.Threading.Mutex($false, "Local\SICA-$($identificador.Substring(0, 16))")
    $mutexAdquirido = $mutex.WaitOne(0)
    if (-not $mutexAdquirido) {
        throw "SICA ya se esta iniciando o ejecutando desde este proyecto."
    }

    $configuracion = Leer-ConfiguracionLocal -Ruta $archivoEntorno
    foreach ($clave in $configuracion.Keys) {
        [System.Environment]::SetEnvironmentVariable($clave, [string]$configuracion[$clave], "Process")
    }
    Escribir-Paso "Configuracion local cargada sin mostrar credenciales."

    $docker = Resolver-Docker
    Resolver-Java
    $maven = Resolver-Maven
    $salidaMaven = @(& $maven --version)
    $codigoMaven = $LASTEXITCODE
    if ($codigoMaven -ne 0 -or $salidaMaven.Count -eq 0) {
        throw "Maven fue localizado, pero no pudo ejecutarse."
    }
    $versionMaven = $salidaMaven[0].ToString()
    Escribir-Paso $versionMaven

    Escribir-Paso "Iniciando el servicio MySQL existente..."
    & $docker compose --project-directory $raizProyecto -f $archivoCompose up -d mysql
    if ($LASTEXITCODE -ne 0) {
        throw "No fue posible iniciar MySQL con Docker Compose."
    }
    Esperar-MySql -Docker $docker

    Push-Location $directorioModulo
    try {
        Escribir-Paso "Verificando la conexion JDBC..."
        & $maven exec:java "-Dexec.args=--verificar-conexion"
        if ($LASTEXITCODE -ne 0) {
            throw "La conexion JDBC fallo. Revise las claves SICA_DB_* de .env y el estado de MySQL."
        }

        if ($InicializarDemo) {
            Escribir-Paso "Ejecutando la inicializacion academica solicitada..."
            & $maven exec:java "-Dexec.args=--inicializar-demo"
            if ($LASTEXITCODE -ne 0) {
                throw "No fue posible inicializar los datos academicos."
            }
        }

        if ($SoloVerificar) {
            Escribir-Paso "Verificacion completada. JavaFX no se abrio por solicitud."
            return
        }

        Escribir-Paso "Abriendo SICA..."
        & $maven javafx:run
        if ($LASTEXITCODE -ne 0) {
            throw "JavaFX termino con un error. Revise la salida anterior."
        }
    } finally {
        Pop-Location
    }
} catch {
    Write-Host "[ERROR] $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    if ($mutexAdquirido -and $null -ne $mutex) {
        try {
            $mutex.ReleaseMutex()
        } catch {
            # El sistema libera el mutex si el proceso ya esta terminando.
        }
    }
    if ($null -ne $mutex) {
        $mutex.Dispose()
    }
}
