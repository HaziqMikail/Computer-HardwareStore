# Compiles the project into .\out and starts the store.
# Usage:  .\run.ps1            build and run
#         .\run.ps1 -BuildOnly build only (no window)
param([switch]$BuildOnly)

Set-Location $PSScriptRoot

function Find-JavaTool($name) {
    if (Get-Command $name -ErrorAction SilentlyContinue) { return $name }
    $candidates = @(
        "C:\Program Files\Android\Android Studio\jbr\bin\$name.exe",
        "$env:JAVA_HOME\bin\$name.exe"
    )
    foreach ($c in $candidates) { if ($c -and (Test-Path $c)) { return $c } }
    return $null
}

$javac = Find-JavaTool "javac"
$java = Find-JavaTool "java"
if (-not $javac -or -not $java) {
    Write-Error "Java (JDK) not found. Install a JDK or add it to PATH."
    exit 1
}

if (-not (Test-Path "db.properties")) {
    Write-Warning "db.properties not found - using defaults (localhost:5432, user postgres). Copy db.properties.example to db.properties and set your password."
}

$libs = "lib\postgresql-42.7.4.jar;lib\flatlaf-3.5.4.jar"
$sources = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }

if (Test-Path out) { Remove-Item out -Recurse -Force }
New-Item -ItemType Directory -Path out | Out-Null

Write-Host "Compiling..."
& $javac -encoding UTF-8 -cp $libs -d out $sources
if ($LASTEXITCODE -ne 0) { Write-Error "Compilation failed."; exit 1 }

Copy-Item -Path src\images -Destination out\images -Recurse
Write-Host "Build OK."
if ($BuildOnly) { exit 0 }

Write-Host "Starting Computer & Hardware Store..."
& $java -cp "out;$libs" computerhardwarestore.ComputerHardwareStore
