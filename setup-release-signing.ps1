$ErrorActionPreference = 'Stop'
$signingFile = Join-Path $PSScriptRoot 'signing.properties'
$keyDirectory = Join-Path $PSScriptRoot '.signing'
$keyFile = Join-Path $keyDirectory 'WriteOrder-release.p12'
if ((Test-Path -LiteralPath $signingFile) -or (Test-Path -LiteralPath $keyFile)) {
    throw 'Signing files already exist. Preserve and reuse the existing release key; do not regenerate it for an update.'
}
$jdkDirectory = Get-ChildItem (Join-Path $PSScriptRoot '.tools/jdk17') -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
$jdkHome = if ($jdkDirectory) { $jdkDirectory.FullName } else { $env:JAVA_HOME }
if (!$jdkHome -or !(Test-Path (Join-Path $jdkHome 'bin/keytool.exe'))) { throw 'JDK 17 is required. Set JAVA_HOME.' }
New-Item -ItemType Directory -Force $keyDirectory | Out-Null
$randomBytes = New-Object byte[] 32
$generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try { $generator.GetBytes($randomBytes) } finally { $generator.Dispose() }
$password = [Convert]::ToBase64String($randomBytes)
$previousPassword = $env:WRITEORDER_KEY_PASSWORD
try {
    $env:WRITEORDER_KEY_PASSWORD = $password
    & (Join-Path $jdkHome 'bin/keytool.exe') -genkeypair -noprompt -keystore $keyFile -storetype PKCS12 -alias writeorder -keyalg RSA -keysize 3072 -sigalg SHA256withRSA -validity 10000 -dname 'CN=WriteOrder, OU=Android App' -storepass:env WRITEORDER_KEY_PASSWORD -keypass:env WRITEORDER_KEY_PASSWORD
    if ($LASTEXITCODE -ne 0) { throw 'Release key generation failed.' }
    $properties = @('storeFile=.signing/WriteOrder-release.p12', "storePassword=$password", 'keyAlias=writeorder', "keyPassword=$password")
    Set-Content -LiteralPath $signingFile -Value $properties -Encoding ascii
} finally {
    $env:WRITEORDER_KEY_PASSWORD = $previousPassword
    $password = $null
}
Write-Output 'Release signing created. Back up .signing/ and signing.properties securely; both are excluded from Git.'
