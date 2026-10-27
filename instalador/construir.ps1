param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$Maven = "",
    [string]$PostgresOrigen = "C:\Program Files\PostgreSQL\18",
    [switch]$SoloNormal
)

$ErrorActionPreference = "Stop"

$Nombre = "Sistema de Envíos"
$Version = "1.0"
$UpgradeUuid = "6f1d2c3a-8b4e-4f7a-9c21-5e0b7d3a1f42"
$Raiz = Split-Path -Parent $PSScriptRoot
$Trabajo = Join-Path $Raiz "target\jpackage"
$Salida = Join-Path $Raiz "target\instaladores"

if (-not $JavaHome -or -not (Test-Path (Join-Path $JavaHome "bin\jpackage.exe"))) {
    $JavaHome = Get-ChildItem "$env:USERPROFILE\.jdks" -Directory -Filter "*21*" -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not $JavaHome -or -not (Test-Path (Join-Path $JavaHome "bin\jpackage.exe"))) {
    throw "No se encontró un JDK 21 con jpackage. Indica la ruta con -JavaHome."
}
$Jpackage = Join-Path $JavaHome "bin\jpackage.exe"

if (-not $Maven) {
    $Comando = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($Comando) {
        $Maven = $Comando.Source
    } else {
        $Maven = Get-ChildItem "C:\Program Files\JetBrains\*\plugins\maven\lib\maven3\bin\mvn.cmd" -ErrorAction SilentlyContinue |
            Select-Object -Last 1 -ExpandProperty FullName
    }
}
if (-not $Maven -or -not (Test-Path $Maven)) {
    throw "No se encontró Maven. Indica la ruta de mvn.cmd con -Maven."
}

Write-Host "== Compilando con Maven" -ForegroundColor Cyan
$env:JAVA_HOME = $JavaHome
& $Maven -B -q -f (Join-Path $Raiz "pom.xml") clean package
if ($LASTEXITCODE -ne 0) { throw "Falló la compilación con Maven." }

$Entrada = Join-Path $Trabajo "entrada"
New-Item -ItemType Directory -Force $Entrada, $Salida | Out-Null
Copy-Item (Join-Path $Raiz "target\libs\*.jar") $Entrada
$Jar = Get-ChildItem (Join-Path $Raiz "target") -Filter "sistema-envios-*.jar" | Select-Object -First 1
if (-not $Jar) { throw "No se encontró el .jar del proyecto en target." }
Copy-Item $Jar.FullName $Entrada

$Comunes = @(
    "--type", "exe",
    "--name", $Nombre,
    "--app-version", $Version,
    "--vendor", "Grupo Envíos",
    "--description", $Nombre,
    "--input", "entrada",
    "--main-jar", $Jar.Name,
    "--main-class", "com.grupo.envios.Launcher",
    "--java-options", "-Dfile.encoding=UTF-8",
    "--install-dir", "SistemaEnvios",
    "--win-dir-chooser",
    "--win-shortcut",
    "--win-menu",
    "--win-menu-group", $Nombre,
    "--win-upgrade-uuid", $UpgradeUuid
)

function Crear-Instalador([string]$Archivo, [string[]]$Extra) {
    $Temporal = Join-Path $Trabajo "salida"
    if (Test-Path $Temporal) { Remove-Item -Recurse -Force $Temporal }
    Push-Location $Trabajo
    try {
        & $Jpackage @Comunes @Extra --dest "salida"
    } finally {
        Pop-Location
    }
    if ($LASTEXITCODE -ne 0) { throw "jpackage falló al crear $Archivo." }
    $Destino = Join-Path $Salida $Archivo
    if (Test-Path $Destino) { Remove-Item -Force $Destino }
    Get-ChildItem $Temporal -Filter "*.exe" | Select-Object -First 1 | Move-Item -Destination $Destino
    Write-Host "   Listo: $Destino" -ForegroundColor Green
}

Write-Host "== Instalador normal" -ForegroundColor Cyan
Crear-Instalador "SistemaEnvios-$Version.exe" @()

if ($SoloNormal) { return }

Write-Host "== Preparando PostgreSQL portátil desde $PostgresOrigen" -ForegroundColor Cyan
if (-not (Test-Path (Join-Path $PostgresOrigen "bin\pg_ctl.exe"))) {
    throw "No se encontró PostgreSQL en $PostgresOrigen. Indica la ruta con -PostgresOrigen."
}
$Contenido = Join-Path $Trabajo "contenido"
$Pgsql = Join-Path $Contenido "pgsql"
if (Test-Path $Contenido) { Remove-Item -Recurse -Force $Contenido }
New-Item -ItemType Directory -Force $Pgsql | Out-Null
foreach ($Carpeta in "bin", "lib", "share") {
    robocopy (Join-Path $PostgresOrigen $Carpeta) (Join-Path $Pgsql $Carpeta) /E /NFL /NDL /NJH /NJS /NP `
        /XF *.pdb *.lib pgAdmin*.exe stackbuilder*.exe /XD pgAdmin* doc | Out-Null
    if ($LASTEXITCODE -ge 8) { throw "Falló la copia de $Carpeta de PostgreSQL." }
}
foreach ($Dll in "vcruntime140.dll", "vcruntime140_1.dll", "msvcp140.dll") {
    $Origen = Join-Path $env:SystemRoot "System32\$Dll"
    if (Test-Path $Origen) { Copy-Item $Origen (Join-Path $Pgsql "bin") -Force }
}

Write-Host "== Instalador completo" -ForegroundColor Cyan
Crear-Instalador "SistemaEnvios-$Version-Completo.exe" @("--app-content", "contenido\pgsql")

Write-Host ""
Write-Host "Instaladores en: $Salida" -ForegroundColor Green
