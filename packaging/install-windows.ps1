#Requires -RunAsAdministrator
$ErrorActionPreference = "Stop"

$MinJava = 25
$ServiceId = "ultron"
$DefaultInstallDir = "C:\Ultron"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$InstallDir = if ($env:ULTRON_INSTALL_DIR) { $env:ULTRON_INSTALL_DIR } else { $DefaultInstallDir }

function Get-JavaExecutable {
    $candidates = @()
    if ($env:JAVA_HOME) {
        $candidates += (Join-Path $env:JAVA_HOME "bin\java.exe")
    }
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) {
        $candidates += $cmd.Source
    }

    foreach ($java in $candidates) {
        if (-not (Test-Path $java)) { continue }
        $output = & $java -version 2>&1 | Out-String
        if ($output -match 'version "(\d+)') {
            $major = [int]$Matches[1]
            if ($major -ge $MinJava) {
                return (Resolve-Path $java).Path
            }
        }
    }

    throw "Se requiere Java $MinJava+. Instalar Temurin ${MinJava}: https://adoptium.net/"
}

Write-Host "Instalando Ultron en $InstallDir"
$JavaBin = Get-JavaExecutable
Write-Host "Java: $JavaBin"

New-Item -ItemType Directory -Force -Path $InstallDir | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $InstallDir "logs") | Out-Null

Copy-Item -Force (Join-Path $ScriptDir "ultron.jar") (Join-Path $InstallDir "ultron.jar")
Copy-Item -Force (Join-Path $ScriptDir "windows\ultron.exe") (Join-Path $InstallDir "ultron.exe")

$XmlSrc = Get-Content -Raw (Join-Path $ScriptDir "windows\ultron.xml")
$XmlSrc = $XmlSrc.Replace("__JAVA_BIN__", $JavaBin)
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText((Join-Path $InstallDir "ultron.xml"), $XmlSrc, $utf8NoBom)

$Exe = Join-Path $InstallDir "ultron.exe"
$Existing = Get-Service -Name $ServiceId -ErrorAction SilentlyContinue
if ($Existing) {
    & $Exe stop
    & $Exe uninstall
}

Push-Location $InstallDir
try {
    & $Exe install
    & $Exe start
} finally {
    Pop-Location
}

Set-Service -Name $ServiceId -StartupType Automatic
sc.exe config $ServiceId start= delayed-auto | Out-Null

Write-Host ""
Write-Host "Ultron instalado y habilitado al iniciar Windows."
Write-Host "  Directorio: $InstallDir"
Write-Host ""
Write-Host "Comandos:"
Write-Host "  Get-Service ultron"
Write-Host "  Restart-Service ultron"
Write-Host "  logs en $InstallDir\logs"
