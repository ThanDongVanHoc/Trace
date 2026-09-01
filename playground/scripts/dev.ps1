. (Join-Path $PSScriptRoot 'common.ps1')
Use-TraceJdk21

$repositoryRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$playgroundRoot = Join-Path $repositoryRoot 'playground'
$wrapper = Join-Path $repositoryRoot 'apps\android\gradlew.bat'
$dataDirectory = Join-Path $playgroundRoot 'data'
New-Item -ItemType Directory -Path $dataDirectory -Force | Out-Null

$watchOutput = Join-Path $dataDirectory 'compile-watch.log'
$watchError = Join-Path $dataDirectory 'compile-watch-error.log'
Write-Host 'Starting TRACE Kotlin playground...'
Write-Host 'Swagger: http://localhost:8080/docs'
Write-Host 'SQLite:  playground\data\trace-dev.db'
Write-Host 'Compile errors: playground\data\compile-watch-error.log'
Write-Host 'Press Ctrl+C to stop.'

$watcher = Start-Process `
    -FilePath $wrapper `
    -ArgumentList @('-p', $playgroundRoot, 'assemble', '--continuous', '-x', 'test') `
    -WindowStyle Hidden `
    -RedirectStandardOutput $watchOutput `
    -RedirectStandardError $watchError `
    -PassThru

try {
    & $wrapper -p $playgroundRoot :dev-server:run
    exit $LASTEXITCODE
}
finally {
    if (-not $watcher.HasExited) {
        Stop-Process -Id $watcher.Id -Force -ErrorAction SilentlyContinue
    }
}
