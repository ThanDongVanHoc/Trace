param(
    [ValidateSet('all', 'member1', 'member2', 'member3', 'member4')]
    [string] $Member = 'all'
)

. (Join-Path $PSScriptRoot 'common.ps1')
Use-TraceJdk21

$repositoryRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$playgroundRoot = Join-Path $repositoryRoot 'playground'
$wrapper = Join-Path $repositoryRoot 'apps\android\gradlew.bat'
$task = switch ($Member) {
    'member1' { ':member1-enrollment:test' }
    'member2' { ':member2-recognition:test' }
    'member3' { ':member3-memory:test' }
    'member4' { ':member4-vault:test' }
    default { 'test' }
}

& $wrapper -p $playgroundRoot $task
exit $LASTEXITCODE
