#Requires -RunAsAdministrator
$ErrorActionPreference = "Stop"

$ServiceId = "ultron"
$DefaultInstallDir = "C:\Ultron"
$InstallDir = if ($env:ULTRON_INSTALL_DIR) { $env:ULTRON_INSTALL_DIR } else { $DefaultInstallDir }
$Exe = Join-Path $InstallDir "ultron.exe"

if (Test-Path $Exe) {
    Push-Location $InstallDir
    try {
        & $Exe stop
        & $Exe uninstall
    } catch {
        Write-Host "El servicio ya no estaba registrado."
    } finally {
        Pop-Location
    }
} elseif (Get-Service -Name $ServiceId -ErrorAction SilentlyContinue) {
    Stop-Service -Name $ServiceId -Force -ErrorAction SilentlyContinue
    sc.exe delete $ServiceId | Out-Null
}

if (Test-Path $InstallDir) {
    Remove-Item -Recurse -Force $InstallDir
    Write-Host "Eliminado $InstallDir"
}

Write-Host "Servicio Ultron desinstalado."
