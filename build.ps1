param([switch]$Test, [switch]$Release)
$ErrorActionPreference = 'Stop'
if ($Test -and $Release) { throw 'Use either -Test or -Release.' }
$gradleVersion = '8.9'
$toolsDirectory = Join-Path $PSScriptRoot '.tools'
$localJdk = Get-ChildItem (Join-Path $toolsDirectory 'jdk17') -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
if ($localJdk -and (Test-Path (Join-Path $localJdk.FullName 'bin/java.exe'))) {
    $env:JAVA_HOME = $localJdk.FullName
}
$gradleExecutable = Join-Path $toolsDirectory "gradle-$gradleVersion/bin/gradle.bat"
if (!(Test-Path $gradleExecutable)) {
    New-Item -ItemType Directory -Force $toolsDirectory | Out-Null
    $archive = Join-Path $toolsDirectory 'gradle.zip'
    Invoke-WebRequest "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip" -OutFile $archive
    $expected = (Invoke-RestMethod "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip.sha256").Trim()
    if ((Get-FileHash $archive -Algorithm SHA256).Hash.ToLower() -ne $expected) { throw 'Gradle checksum mismatch' }
    Expand-Archive -LiteralPath $archive -DestinationPath $toolsDirectory -Force
}
Push-Location $PSScriptRoot
try {
    if ($Test) { & $gradleExecutable testDebugUnitTest }
    elseif ($Release) { & $gradleExecutable assembleRelease }
    else { & $gradleExecutable assembleDebug }
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed: $LASTEXITCODE" }
} finally { Pop-Location }
