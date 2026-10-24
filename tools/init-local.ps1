# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$envPath = Join-Path $projectRoot '.env'
New-Item -ItemType Directory -Force -Path (Join-Path $projectRoot '.local') | Out-Null
if (-not (Test-Path -LiteralPath $envPath)) {
    $keys = 'JWT_SECRET','MYSQL_ROOT_PASSWORD','DB_PASSWORD','ADMIN_PASSWORD','CAJERO_PASSWORD','ALMACEN_PASSWORD','CONFIG_SERVER_PASSWORD'
    $lines = foreach ($key in $keys) {
        $bytes = New-Object byte[] 32
        $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
        try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
        $value = [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+','-').Replace('/','_')
        "$key=$value"
    }
    [IO.File]::WriteAllLines($envPath, $lines, [Text.UTF8Encoding]::new($false))
}
# Docker builds can use the same trusted certificate authorities as this computer.
$caPath = Join-Path $projectRoot '.local/ca.pem'
if ($env:OS -eq 'Windows_NT') {
    $certs = Get-ChildItem Cert:\LocalMachine\Root,Cert:\CurrentUser\Root
    $bundle = ($certs | ForEach-Object {
        "-----BEGIN CERTIFICATE-----`n" + [Convert]::ToBase64String($_.RawData,[Base64FormattingOptions]::InsertLineBreaks) + "`n-----END CERTIFICATE-----"
    }) -join "`n"
    [IO.File]::WriteAllText($caPath, $bundle, [Text.UTF8Encoding]::new($false))
} elseif (-not (Test-Path $caPath)) {
    if (Test-Path '/etc/ssl/certs/ca-certificates.crt') { Copy-Item '/etc/ssl/certs/ca-certificates.crt' $caPath }
    else { [IO.File]::WriteAllText($caPath, '') }
}
$values = @{}
Get-Content -LiteralPath $envPath | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
    $parts = $_.Split('=',2); $values[$parts[0]] = $parts[1]
}
$access = @"
# Acceso local

URL: http://localhost:4200

| Usuario | Contraseña inicial | Rol |
|---|---|---|
| admin | $($values['ADMIN_PASSWORD']) | Administrador |
| cajero | $($values['CAJERO_PASSWORD']) | Cajero |
| almacen | $($values['ALMACEN_PASSWORD']) | Almacén |

Credenciales generadas para esta instalación. No compartir este archivo ni .env.
Al reutilizar un volumen MySQL, los usuarios existentes conservan sus contraseñas.
"@
[IO.File]::WriteAllText((Join-Path $projectRoot '.local/ACCESO_LOCAL.md'), $access, [Text.UTF8Encoding]::new($false))
Write-Host 'Configuración local lista. Credenciales en .local/ACCESO_LOCAL.md (excluidas de Git).'
