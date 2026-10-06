param(
    [string]$Workspace = 'C:/Users/derko/Desktop/minecraft',
    [string]$JavaHome = 'C:/Program Files/Java/jdk-25',
    [string]$EvidenceSuffix = 'client-probes-1',
    [string[]]$OnlyLoaders = @()
)
$ErrorActionPreference='Stop'
$env:JAVA_HOME=$JavaHome
foreach($version in @('26.3','26.2','1.21.1','1.20.1')) {
    $repo=if($version -eq '26.3'){Join-Path $Workspace 'seamless-dogs'}else{Join-Path $Workspace ".ports/dogs-multiversion/mc$version/seamless-dogs"}
    $loaders=if($version -eq '1.20.1'){@('fabric','forge')}else{@('fabric','forge','neoforge')}
    if($OnlyLoaders.Count){$loaders=@($loaders | Where-Object {$_ -in $OnlyLoaders})}
    foreach($loader in $loaders) {
        $log=Join-Path $Workspace "qa-artifacts/seamless-dogs/qa-$version-$loader-$EvidenceSuffix.log"
        if(Test-Path -LiteralPath $log){throw "Evidence exists: $log"}
        Push-Location -LiteralPath $repo
        try {
            $task=if($version.StartsWith('26.')){'jar'}else{'remapJar'}
            & .\gradlew.bat -p qa-client clean $task "-PqaLoader=$loader" --no-daemon --console=plain *> $log
            if($LASTEXITCODE -ne 0){throw "Probe $version/$loader failed: $log"}
            $archiveDirectory=Join-Path $repo 'qa-client/artifacts'
            New-Item -ItemType Directory -Force $archiveDirectory | Out-Null
            Copy-Item -LiteralPath (Join-Path $repo "qa-client/build/libs/seamless-dogs-qa-$loader.jar") -Destination $archiveDirectory
            Write-Output "PASS packaged client probe: $version/$loader"
        } finally {Pop-Location}
    }
}
