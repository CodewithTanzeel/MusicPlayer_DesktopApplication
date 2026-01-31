$APP_NAME = "VibeMusic"
$APP_VERSION = "1.0.0"
$INPUT_DIR = "target"
$MAIN_JAR = "vibe-music-player-1.0-SNAPSHOT.jar"
$MAIN_CLASS = "com.vibe.Main"

# Ensure JAVA_HOME is set and jpackage is available
if (-not (Get-Command "jpackage" -ErrorAction SilentlyContinue)) {
    Write-Host "Error: jpackage not found in PATH. Please ensure you are using JDK 14+."
    exit 1
}

# Run Maven to build the jar
Write-Host "Building project..."
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed."
    exit 1
}

# Create Installer
Write-Host "Creating Installer..."
jpackage `
  --name $APP_NAME `
  --app-version $APP_VERSION `
  --input $INPUT_DIR `
  --main-jar $MAIN_JAR `
  --main-class $MAIN_CLASS `
  --type exe `
  --win-dir-chooser `
  --win-shortcut `
  --win-menu `
  --win-menu-group "Vibe Music" `
  --dest dist

Write-Host "Installer created in 'dist' folder."
