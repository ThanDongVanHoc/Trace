Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Use-TraceJdk21 {
    $candidates = [System.Collections.Generic.List[string]]::new()
    if ($env:JAVA_HOME) {
        $candidates.Add($env:JAVA_HOME)
    }

    $searchRoots = @(
        @{ Path = 'C:\Program Files\Java'; Pattern = 'jdk-21*' },
        @{ Path = 'C:\Program Files\Eclipse Adoptium'; Pattern = 'jdk-21*' },
        @{ Path = 'C:\Program Files\Microsoft'; Pattern = 'jdk-21*' }
    )
    foreach ($root in $searchRoots) {
        Get-ChildItem -LiteralPath $root.Path -Directory -Filter $root.Pattern -ErrorAction SilentlyContinue |
            ForEach-Object { $candidates.Add($_.FullName) }
    }
    $candidates.Add('C:\Program Files\Android\Android Studio\jbr')

    foreach ($candidate in $candidates | Select-Object -Unique) {
        $java = Join-Path $candidate 'bin\java.exe'
        if (-not (Test-Path -LiteralPath $java -PathType Leaf)) {
            continue
        }
        $processInfo = [System.Diagnostics.ProcessStartInfo]::new()
        $processInfo.FileName = $java
        $processInfo.Arguments = '-version'
        $processInfo.UseShellExecute = $false
        $processInfo.RedirectStandardError = $true
        $process = [System.Diagnostics.Process]::Start($processInfo)
        $version = $process.StandardError.ReadLine()
        $process.WaitForExit()
        if ($version -match 'version "21(?:\.|\")') {
            $env:JAVA_HOME = $candidate
            $env:Path = "$(Join-Path $candidate 'bin');$env:Path"
            Write-Host "Using JDK 21: $candidate"
            return
        }
    }

    throw @'
TRACE playground requires JDK 21 LTS, but it was not found.
Install Temurin 21, then run this command again:
https://adoptium.net/temurin/releases/?version=21
'@
}
