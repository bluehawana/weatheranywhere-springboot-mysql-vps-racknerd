# PowerShell deployment script for WeatherAnywhere
param(
    [string]$VpsUser = "harvad",
    [string]$VpsHost = "107.175.235.220"
)

$JarFile = "target\EasyWeather-0.0.1-SNAPSHOT.jar"
$AppDir = "/opt/weatheranywhere"

Write-Host "=== WeatherAnywhere Deployment ===" -ForegroundColor Green
Write-Host "VPS: $VpsUser@$VpsHost"
Write-Host "JAR: $JarFile"
Write-Host ""

# Check if JAR exists
if (-not (Test-Path $JarFile)) {
    Write-Host "ERROR: JAR file not found: $JarFile" -ForegroundColor Red
    Write-Host "Please run: mvn clean package"
    exit 1
}

Write-Host "Step 1: Uploading JAR to VPS..." -ForegroundColor Yellow
& scp $JarFile "${VpsUser}@${VpsHost}:/tmp/weatheranywhere.jar"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Upload failed. Please check your SSH connection." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Step 2: Deploying on VPS..." -ForegroundColor Yellow

$commands = "sudo systemctl stop weatheranywhere 2>/dev/null || true; " +
    "if [ -f $AppDir/EasyWeather-0.0.1-SNAPSHOT.jar ]; then " +
    "sudo cp $AppDir/EasyWeather-0.0.1-SNAPSHOT.jar $AppDir/EasyWeather-0.0.1-SNAPSHOT.jar.backup.`$(date +%Y%m%d_%H%M%S); fi; " +
    "sudo mv /tmp/weatheranywhere.jar $AppDir/EasyWeather-0.0.1-SNAPSHOT.jar; " +
    "sudo chown weatherapp:weatherapp $AppDir/EasyWeather-0.0.1-SNAPSHOT.jar 2>/dev/null || sudo chown ${VpsUser}:${VpsUser} $AppDir/EasyWeather-0.0.1-SNAPSHOT.jar; " +
    "sudo systemctl start weatheranywhere; " +
    "sleep 3; " +
    "sudo systemctl status weatheranywhere --no-pager || echo 'Service status check failed'"

& ssh "${VpsUser}@${VpsHost}" $commands

Write-Host ""
Write-Host "=== Deployment Complete! ===" -ForegroundColor Green
Write-Host ""
Write-Host "Check your application at: http://$VpsHost" -ForegroundColor Cyan
Write-Host ""
Write-Host "To view logs, run:" -ForegroundColor Yellow
Write-Host "ssh $VpsUser@$VpsHost 'sudo journalctl -u weatheranywhere -f'"
