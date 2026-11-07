param([switch]$DriverOnly)
$ErrorActionPreference = 'Stop'
$driverRoot = Join-Path $env:USERPROFILE '.gradle\caches\modules-2\files-2.1\com.mysql\mysql-connector-j'
$driverJar = Get-ChildItem -LiteralPath $driverRoot -Filter 'mysql-connector-j-*.jar' -Recurse |
    Where-Object { $_.Name -notmatch '-(sources|javadoc)\.jar$' } |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $driverJar) { throw 'Connector/J not found in Gradle cache. Resolve backend runtimeClasspath first.' }
Write-Output ('Driver JAR: ' + $driverJar.Name)
$probeArgs = @('--class-path', $driverJar.FullName, (Join-Path $PSScriptRoot 'JdbcCheck.java'))
if ($DriverOnly) { $probeArgs += '--driver-only' }
& java @probeArgs
exit $LASTEXITCODE
