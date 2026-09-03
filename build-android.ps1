[CmdletBinding()]
param(
    [ValidateSet('build', 'check', 'install', 'submission', 'bundle')]
    [string]$Mode = 'check'
)

$ErrorActionPreference = 'Stop'
$repositoryRoot = $PSScriptRoot
$androidRoot = Join-Path $repositoryRoot 'apps\android'

function Find-Jdk21 {
    $candidates = [System.Collections.Generic.List[string]]::new()
    if ($env:JAVA_HOME) { $candidates.Add($env:JAVA_HOME) }

    foreach ($base in @(
        (Join-Path $env:ProgramFiles 'Java'),
        (Join-Path $env:ProgramFiles 'Eclipse Adoptium')
    )) {
        if (Test-Path -LiteralPath $base) {
            Get-ChildItem -LiteralPath $base -Directory -Filter '*21*' -ErrorAction SilentlyContinue |
                ForEach-Object { $candidates.Add($_.FullName) }
        }
    }

    foreach ($candidate in $candidates | Select-Object -Unique) {
        $java = Join-Path $candidate 'bin\java.exe'
        if (-not (Test-Path -LiteralPath $java)) { continue }
        $previousErrorPreference = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        $version = (& $java -version 2>&1 | Out-String)
        $ErrorActionPreference = $previousErrorPreference
        if ($version -match 'version "21\.') { return $candidate }
    }
    throw 'JDK 21 was not found. Install JDK 21 LTS or set JAVA_HOME.'
}

$jdk21 = Find-Jdk21
$env:JAVA_HOME = $jdk21
$env:Path = "$(Join-Path $jdk21 'bin');$env:Path"

$gradleArguments = switch ($Mode) {
    'build' { @('assembleDebug') }
    'check' { @('testDebugUnitTest', 'lintDebug', 'assembleDebug') }
    'install' { @('testDebugUnitTest', 'installDebug') }
    'submission' {
        @(
            'testDebugUnitTest',
            'lintDebug',
            'assembleDebug',
            'assembleRelease',
            '-PtraceSplitApks=true'
        )
    }
    'bundle' { @('testDebugUnitTest', 'lintDebug', 'bundleRelease') }
}

Push-Location $androidRoot
try {
    & '.\gradlew.bat' @gradleArguments
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE." }
} finally {
    Pop-Location
}

if ($Mode -eq 'submission') {
    $releaseDirectory = Join-Path $androidRoot 'app\build\outputs\apk\release'
    $sourceApk = Get-ChildItem -LiteralPath $releaseDirectory -Filter '*arm64-v8a*release*unsigned*.apk' |
        Select-Object -First 1 -ExpandProperty FullName
    if (-not $sourceApk) { throw "Unsigned ARM64 release APK was not found in $releaseDirectory" }

    $sdkCandidates = @($env:ANDROID_SDK_ROOT, $env:ANDROID_HOME) | Where-Object { $_ }
    $localProperties = Join-Path $androidRoot 'local.properties'
    if (Test-Path -LiteralPath $localProperties) {
        $sdkLine = Get-Content -LiteralPath $localProperties |
            Where-Object { $_ -match '^sdk\.dir=' } |
            Select-Object -First 1
        if ($sdkLine) {
            $sdkCandidates += (($sdkLine -replace '^sdk\.dir=', '') -replace '\\:', ':')
        }
    }
    $androidSdk = $sdkCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
    if (-not $androidSdk) { throw 'Android SDK was not found.' }

    $buildTools = Get-ChildItem -LiteralPath (Join-Path $androidSdk 'build-tools') -Directory |
        Sort-Object { [version]$_.Name } -Descending |
        Select-Object -First 1
    $apksigner = Join-Path $buildTools.FullName 'apksigner.bat'
    $debugKeystore = Join-Path $env:USERPROFILE '.android\debug.keystore'
    if (-not (Test-Path -LiteralPath $apksigner)) { throw 'apksigner was not found.' }
    if (-not (Test-Path -LiteralPath $debugKeystore)) { throw 'Android debug keystore was not found.' }

    $apkDirectory = Join-Path $repositoryRoot 'apk'
    New-Item -ItemType Directory -Force -Path $apkDirectory | Out-Null
    $targetApk = Join-Path $apkDirectory 'app-release.apk'
    $previousErrorPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    & $apksigner sign `
        --ks $debugKeystore `
        --ks-key-alias androiddebugkey `
        --ks-pass pass:android `
        --key-pass pass:android `
        --out $targetApk `
        $sourceApk
    $signExitCode = $LASTEXITCODE
    $ErrorActionPreference = $previousErrorPreference
    if ($signExitCode -ne 0) { throw "APK signing failed with exit code $signExitCode." }
    $stream = [System.IO.File]::OpenRead($targetApk)
    $sha256 = [System.Security.Cryptography.SHA256]::Create()
    try {
        $hash = -join ($sha256.ComputeHash($stream) | ForEach-Object { $_.ToString('X2') })
    } finally {
        $sha256.Dispose()
        $stream.Dispose()
    }
    Write-Host "APK: $targetApk"
    Write-Host "SHA-256: $hash"
}
