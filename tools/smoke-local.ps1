# Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md.
#requires -Version 7
param([string]$BaseUrl = 'http://localhost:4200/api')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$values = @{}
Get-Content (Join-Path $projectRoot '.env') | Where-Object { $_ -match '^[A-Z_]+=' } | ForEach-Object {
    $parts = $_.Split('=',2); $values[$parts[0]] = $parts[1]
}
$checks = [Collections.Generic.List[string]]::new()
function Request([string]$Method,[string]$Path,[string]$Token='', $Data=$null, $Extra=@{}) {
    $headers = @{} + $Extra
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $parameters = @{Method=$Method;Uri="$BaseUrl$Path";Headers=$headers;SkipHttpErrorCheck=$true;TimeoutSec=45}
    if ($null -ne $Data) { $parameters.Body = ($Data | ConvertTo-Json -Depth 8 -Compress); $parameters.ContentType='application/json' }
    Invoke-WebRequest @parameters
}
function Expect($Response,[int]$Status,[string]$Name) {
    if ([int]$Response.StatusCode -ne $Status) { throw "$Name esperaba $Status y recibió $($Response.StatusCode): $($Response.Content)" }
    $checks.Add($Name)
}
function Login([string]$User,[string]$Password) {
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        $response = Request POST '/auth/login' '' @{userName=$User;password=$Password}
        if ([int]$response.StatusCode -eq 200) { break }
        Start-Sleep -Seconds 2
    }
    Expect $response 200 "Login $User"
    ($response.Content | ConvertFrom-Json).token
}
$admin = Login 'admin' $values.ADMIN_PASSWORD
$cashier = Login 'cajero' $values.CAJERO_PASSWORD
$warehouse = Login 'almacen' $values.ALMACEN_PASSWORD
Expect (Request GET '/libros') 401 'Catálogo protegido sin token'
Expect (Request POST '/auth/create' '' @{userName='intruso';password='invalid';rol='ROLE_ADMIN'}) 401 'Registro administrativo anónimo bloqueado'
Expect (Request GET '/auth/usuarios' $cashier) 403 'Cajero no administra usuarios'
Expect (Request POST '/carrito/agregar' $warehouse @{libroId=1;cantidad=1}) 403 'Almacén no realiza ventas'
Expect (Request GET '/ms-auth-service/auth/usuarios' $admin) 403 'Sin ruta alternativa por discovery'
Expect (Request GET '/auth/usuarios' $admin) 200 'Administrador consulta usuarios'
$catalog = Request GET '/libros' $cashier
Expect $catalog 200 'Catálogo disponible a través de Eureka y Gateway'
$book = @($catalog.Content | ConvertFrom-Json) | Where-Object { $_.stock -ge 2 -and $_.estado -eq 'ACTIVO' } | Select-Object -First 1
if (-not $book) { throw 'Se necesita un producto activo con al menos dos unidades para la prueba de venta.' }
$before = [int]$book.stock
Expect (Request POST '/carrito/agregar' $cashier @{libroId=$book.id;cantidad=2}) 200 'Agregar producto al carrito'
$operation = [guid]::NewGuid().ToString()
$reference = 'PRUEBA-' + $operation.Substring(0,8)
$salePath = '/venta/realizar?clienteNombre=Verificacion%20local&metodoPago=TRANSFERENCIA&nroOperacion=' + $reference
$headers = @{'Idempotency-Key'=$operation}
$response = Request POST $salePath $cashier $null $headers
Expect $response 200 'Registrar venta con referencia de pago'
$sale = $response.Content | ConvertFrom-Json
if ($sale.estado -ne 'COMPLETADA' -or $sale.metodoPago -ne 'TRANSFERENCIA' -or $sale.nroOperacion -ne $reference) { throw 'La venta no conserva su estado o datos de pago.' }
$checks.Add('Método y referencia de pago persistidos')
$afterResponse = Request GET "/libros/$($book.id)" $cashier
$after = ($afterResponse.Content | ConvertFrom-Json).stock
if ($after -ne ($before - 2)) { throw "Stock incorrecto: antes=$before después=$after" }
$checks.Add('Stock descontado exactamente una vez')
$repeat = Request POST $salePath $cashier $null $headers
Expect $repeat 200 'Reintento idempotente aceptado'
if (($repeat.Content | ConvertFrom-Json).id -ne $sale.id) { throw 'Se duplicó la venta al reintentar.' }
if (((Request GET "/libros/$($book.id)" $cashier).Content | ConvertFrom-Json).stock -ne $after) { throw 'Se descontó stock dos veces.' }
$checks.Add('Reintento conserva venta y stock')
$changedPath = $salePath.Replace('metodoPago=TRANSFERENCIA','metodoPago=YAPE_PLIN')
Expect (Request POST $changedPath $cashier $null $headers) 409 'Misma operación no admite cambiar el pago'
$pdf = Request GET "/venta/$($sale.id)/recibo" $cashier
Expect $pdf 200 'Comprobante PDF disponible'
$bytes = [byte[]]$pdf.Content
if ([Text.Encoding]::ASCII.GetString($bytes,0,4) -ne '%PDF') { throw 'El comprobante no es un PDF válido.' }
[IO.File]::WriteAllBytes((Join-Path $projectRoot '.local/smoke-recibo.pdf'),$bytes)
$report = [ordered]@{fecha=(Get-Date).ToString('o');baseUrl=$BaseUrl;ventaId=$sale.id;comprobaciones=$checks;resultado='OK'}
$report | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $projectRoot '.local/verificacion-api.json') -Encoding utf8
$checks | ForEach-Object { Write-Host "OK: $_" }
Write-Host "Verificación completada: $($checks.Count) comprobaciones. Venta de prueba $($sale.id)."
