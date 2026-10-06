param(
    [string]$Workspace = 'C:/Users/derko/Desktop/minecraft',
    [string]$JavaHome = 'C:/Program Files/Java/jdk-25',
    [string]$EvidenceSuffix = 'standalone-final'
)
$ErrorActionPreference='Stop'
$env:JAVA_HOME=$JavaHome
$evidence=Join-Path $Workspace 'qa-artifacts/seamless-dogs'
foreach($version in @('26.3','26.2','1.21.1','1.20.1')) {
    $product=if($version -eq '26.3'){Join-Path $Workspace 'seamless-dogs'}else{Join-Path $Workspace ".ports/dogs-multiversion/mc$version/seamless-dogs"}
    $log=Join-Path $evidence "dogs-$version-$EvidenceSuffix.log"
    if(Test-Path -LiteralPath $log){throw "Evidence exists: $log"}
    Push-Location -LiteralPath $product
    try {
        & .\gradlew.bat clean check build --no-daemon --console=plain *> $log
        if($LASTEXITCODE -ne 0){throw "$version failed: $log"}
        Write-Output "PASS standalone clean/check/build: $version"
    } finally {Pop-Location}
}
