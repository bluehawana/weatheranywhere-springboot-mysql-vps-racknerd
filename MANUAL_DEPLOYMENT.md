# Manual Deployment Guide for WeatherAnywhere

## Current Status

- ✅ Code pushed to GitHub: https://github.com/bluehawana/weatheranywhere-springboot-mysql-vps-racknerd
- ✅ JAR file built: `target/EasyWeather-0.0.1-SNAPSHOT.jar`
- ⏸️ SSH key authentication needs to be configured

## Option 1: Deploy via GitHub (Recommended)

If you can SSH into your VPS (via console or after fixing SSH key):

```bash
# SSH to VPS (use RackNerd console if SSH key doesn't work)
ssh harvad@107.175.235.220

# Navigate to app directory
cd /opt/weatheranywhere

# Pull latest changes from GitHub
git pull origin main

# Build the application on VPS
mvn clean package -DskipTests

# Stop the service
sudo systemctl stop weatheranywhere

# The JAR is now in target/ directory
sudo systemctl start weatheranywhere

# Check status
sudo systemctl status weatheranywhere

# View logs
sudo journalctl -u weatheranywhere -f
```

## Option 2: Upload JAR Manually

### Step 1: Access VPS Console

1. Login to RackNerd control panel
2. Open VPS console/terminal

### Step 2: Prepare for Upload

On VPS console, run:

```bash
# Create temp directory
mkdir -p /tmp/upload
chmod 777 /tmp/upload
```

### Step 3: Upload JAR

From your Windows machine, use one of these methods:

**Method A: Using WinSCP or FileZilla**

- Install WinSCP: https://winscp.net/
- Connect to: 107.175.235.220
- Username: harvad
- Upload: `C:\Users\BLUEH\projects\weatheranywhere\target\EasyWeather-0.0.1-SNAPSHOT.jar`
- To: `/tmp/upload/`

**Method B: Using Python HTTP Server**
On Windows (in project directory):

```powershell
cd target
python -m http.server 8000
```

On VPS:

```bash
cd /tmp/upload
wget http://YOUR_WINDOWS_IP:8000/EasyWeather-0.0.1-SNAPSHOT.jar
```

### Step 4: Deploy on VPS

Once JAR is uploaded, run on VPS:

```bash
# Stop service
sudo systemctl stop weatheranywhere

# Backup current JAR
sudo cp /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar \
        /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar.backup.$(date +%Y%m%d_%H%M%S)

# Move new JAR
sudo mv /tmp/upload/EasyWeather-0.0.1-SNAPSHOT.jar /opt/weatheranywhere/

# Fix permissions
sudo chown weatherapp:weatherapp /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar

# Start service
sudo systemctl start weatheranywhere

# Check status
sudo systemctl status weatheranywhere

# View logs
sudo journalctl -u weatheranywhere -n 50
```

## Option 3: Fix SSH Key and Use Automated Script

### Add SSH Key to VPS:

1. Access VPS via RackNerd console
2. Run these commands:

```bash
mkdir -p ~/.ssh
echo "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIJXOfdG+tnnrNc3MaJE+XlRrvstLbqBd/gLKM5PBDjop harvad@vps-2025" >> ~/.ssh/authorized_keys
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
```

3. Test from Windows:

```powershell
ssh -i ~/.ssh/id_ed25519_vps_2025 harvad@107.175.235.220 "echo 'Success!'"
```

4. If successful, run deployment:

```powershell
powershell -ExecutionPolicy Bypass -File deploy-vps.ps1
```

## Verify Deployment

After deployment, check:

1. Service status: `sudo systemctl status weatheranywhere`
2. Application logs: `sudo journalctl -u weatheranywhere -n 100`
3. Web access: http://107.175.235.220
4. Test mobile responsiveness on your phone
5. Test theme switching (should be white at 8:19 AM)

## Changes Deployed

This deployment includes:

- ✅ Mobile responsive CSS (works on phones)
- ✅ Fixed theme switching (white 08:00-17:59, dark 18:00-07:59)
- ✅ Updated marketing copy ("No Plugins" instead of "No Java")
- ✅ SVG/images constrain to screen width
- ✅ Touch-friendly buttons (44px minimum)

## Troubleshooting

### Service won't start

```bash
# Check logs
sudo journalctl -u weatheranywhere -n 100 --no-pager

# Check if port is in use
sudo netstat -tulpn | grep :8080

# Check JAR file
ls -lh /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar
```

### Application errors

```bash
# Check application logs
sudo tail -f /var/log/weatheranywhere/app.log

# Check database connection
mysql -u weatherapp -p weatheranywhere
```

### Need to rollback

```bash
# Stop service
sudo systemctl stop weatheranywhere

# Restore backup
sudo cp /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar.backup.YYYYMMDD_HHMMSS \
        /opt/weatheranywhere/EasyWeather-0.0.1-SNAPSHOT.jar

# Start service
sudo systemctl start weatheranywhere
```
