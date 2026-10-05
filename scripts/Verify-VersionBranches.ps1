param(
    [string]$Workspace = 'C:/Users/derko/Desktop/minecraft',
    [string]$JavaHome = 'C:/Program Files/Java/jdk-25',
    [string]$EvidenceSuffix = 'final-1'
)
$ErrorActionPreference='Stop'
$env:JAVA_HOME=$JavaHome
$evidence=Join-Path $Workspace 'qa-artifacts/seamless-dogs'
$versions=@('1.21.1','1.21.11','26.1','26.1.2','26.2','1.20.1','26.3')
foreach($version in $versions) {
    $product=if($version -eq '26.3'){Join-Path $Workspace 'seamless-dogs'}else{Join-Path $Workspace ".ports/dogs-multiversion/mc$version/seamless-dogs"}
    $dependency=if($version -eq '26.3'){Join-Path $Workspace '.ports/github-mc26.3/seamless-api'}else{Join-Path $Workspace ".ports/dogs-multiversion/mc$version/seamless-api"}
    foreach($entry in @(@{name='api';path=$dependency},@{name='dogs';path=$product})) {
        $log=Join-Path $evidence "$($entry.name)-$version-$EvidenceSuffix.log"
        if(Test-Path -LiteralPath $log){throw "Evidence exists: $log"}
        Push-Location -LiteralPath $entry.path
        try {
            & .\gradlew.bat clean check build --no-daemon --console=plain *> $log
            if($LASTEXITCODE -ne 0){throw "$($entry.name) $version failed: $log"}
            Write-Output "PASS clean/check/build: $($entry.name) $version"
        } finally {Pop-Location}
    }
}
