$javaExe = $null

if (Get-Command java -ErrorAction SilentlyContinue) {
    $javaExe = "java"
} elseif (Test-Path "C:\Program Files\Android\Android Studio\jbr\bin\java.exe") {
    $javaExe = "C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
} elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaExe = "$env:JAVA_HOME\bin\java.exe"
}

if (-not $javaExe) {
    Write-Error "Java executable not found. Please install Java or add it to PATH."
    exit 1
}

$jarPath = Join-Path $PSScriptRoot "dist\ComputerHardwareStore.jar"
Write-Host "Starting Computer Hardware Store..."
Start-Process $javaExe -ArgumentList "-jar", "`"$jarPath`""

