param([switch]$Test)
$ErrorActionPreference = 'Stop'
$gradleVersion = '8.9'
$toolsDirectory = Join-Path $PSScriptRoot '.tools'
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
    if ($Test) { & $gradleExecutable testDebugUnitTest } else { & $gradleExecutable assembleDebug }
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed: $LASTEXITCODE" }
} finally { Pop-Location }
