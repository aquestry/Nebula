$sourceDir = "C:\Users\anton\IdeaProjects\Nebula\build\libs\nebula-1.0.jar"
$destinationDir = "C:\Users\anton\Documents\Projekte\Developing\Dev Velocity Server\plugins\nebula.jar"

if (Test-Path $sourceDir) {
    try {
        New-Item -ItemType Directory -Path (Split-Path -Parent $destinationDir) -Force | Out-Null
        Move-Item -Path $sourceDir -Destination $destinationDir -Force
        Write-Host "File moved successfully."
    } catch {
        Write-Host "An error occurred: $_"
    }
} else {
    Write-Host "Source file not found: $sourceDir"
}