# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
& "$PSScriptRoot/init-local.ps1"
Push-Location $projectRoot
try {
    if (-not $SkipBuild) {
        $oldMavenOptions = $env:MAVEN_OPTS
        try {
            if ($env:OS -eq 'Windows_NT') { $env:MAVEN_OPTS = "$oldMavenOptions -Djavax.net.ssl.trustStoreType=Windows-ROOT -Djavax.net.ssl.trustStore=NONE" }
            & mvn -B package
            if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación o una prueba. No se inició la aplicación.' }
        } finally { $env:MAVEN_OPTS = $oldMavenOptions }
    }
    & docker compose up -d --build
    if ($LASTEXITCODE -ne 0) { throw 'Docker Compose no pudo iniciar la aplicación.' }
    $values = @{}
    Get-Content '.env' | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
        $parts = $_.Split('=',2); $values[$parts[0]] = $parts[1]
    }
    $ready = $false
    for ($attempt = 0; $attempt -lt 90; $attempt++) {
        try {
            $health = Invoke-RestMethod 'http://localhost:4200/api/actuator/health' -TimeoutSec 4
            if ($health.status -eq 'UP') {
                $loginBody = @{userName='admin';password=$values.ADMIN_PASSWORD} | ConvertTo-Json -Compress
                $session = Invoke-RestMethod 'http://localhost:4200/api/auth/login' -Method Post -ContentType 'application/json' -Body $loginBody -TimeoutSec 5
                $headers = @{Authorization="Bearer $($session.token)"}
                $null = Invoke-RestMethod 'http://localhost:4200/api/libros' -Headers $headers -TimeoutSec 5
                $null = Invoke-RestMethod 'http://localhost:4200/api/cliente' -Headers $headers -TimeoutSec 5
                $null = Invoke-RestMethod 'http://localhost:4200/api/venta/pendiente' -Headers $headers -TimeoutSec 5
                $ready = $true; break
            }
        } catch {}
        Start-Sleep -Seconds 2
    }
    if (-not $ready) { throw 'El gateway no respondió a tiempo. Consulta docker compose logs --tail 80.' }
    Write-Host 'Aplicación disponible en http://localhost:4200. Ejecuta tools/smoke-local.ps1 para verificar las API.'
} finally { Pop-Location }
