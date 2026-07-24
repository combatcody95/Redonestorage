$ErrorActionPreference = 'Stop'

$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Push-Location $ProjectDir
try {
    if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'Java was not found. Install a 64-bit Java 21 JDK and add it to PATH.'
    }

    $validated = $false
    $validator = Join-Path $ProjectDir 'tools\validate_port.py'

    if (Get-Command py -ErrorAction SilentlyContinue) {
        & py -3 --version *> $null
        if ($LASTEXITCODE -eq 0) {
            Write-Host 'Checking source resources with Python...'
            & py -3 $validator
            if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
            $validated = $true
        }
    }

    if (-not $validated -and (Get-Command python -ErrorAction SilentlyContinue)) {
        & python --version *> $null
        if ($LASTEXITCODE -eq 0) {
            Write-Host 'Checking source resources with Python...'
            & python $validator
            if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
            $validated = $true
        }
    }

    if (-not $validated) {
        Write-Host 'Python was not found. Skipping the optional source-resource validation.'
    }

    Write-Host 'Building Redone Storage for Minecraft 1.21.1 / NeoForge 21.1.234...'
    & (Join-Path $ProjectDir 'gradlew.bat') clean build
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    Write-Host "Build complete. Check $ProjectDir\build\libs\"
}
finally {
    Pop-Location
}
