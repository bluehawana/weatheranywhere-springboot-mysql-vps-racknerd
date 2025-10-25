# VPS Database Migration Guide

## Overview

Migrate all databases from `D:/H3c/mysqlbackup` to your VPS and restore them.

**Databases Found:**
- chatapp (6 tables)
- ecoeats (11 tables)
- ecoeatsdb (9 tables)
- ekorental (3 tables)
- fleetmanager (2 tables)
- ingress (7 tables)

**Total: 38 SQL files**

---

## Prerequisites

Before running the migration, you need:

1. ✅ VPS SSH access (IP address, username, SSH key/password)
2. ✅ VPS MySQL credentials (username, password)
3. ✅ MySQL installed on VPS
4. ✅ Sufficient disk space on VPS

---

## Step 1: Configure Migration Script

Edit the script: `scripts/migrate-to-vps.sh`

Update these values (around line 20-25):

```bash
VPS_HOST="your.vps.ip.address"     # Your VPS IP or hostname
VPS_USER="root"                     # Your VPS SSH username
VPS_SSH_PORT="22"                   # SSH port (usually 22)
VPS_MYSQL_USER="root"               # MySQL username on VPS
VPS_MYSQL_PASS="your-mysql-pass"    # MySQL password on VPS
```

**Example:**
```bash
VPS_HOST="203.0.113.45"
VPS_USER="ubuntu"
VPS_SSH_PORT="22"
VPS_MYSQL_USER="root"
VPS_MYSQL_PASS="SecurePass123"
```

---

## Step 2: Test VPS Connection

Before migrating, test your VPS access:

```bash
# Test SSH connection
ssh your-username@your-vps-ip

# Test MySQL on VPS
mysql -u root -p
```

If both work, you're ready to migrate!

---

## Step 3: Run Migration

From WSL terminal:

```bash
cd /mnt/c/Users/BLUEH/projects/weatheranywhere
./scripts/migrate-to-vps.sh
```

The script will:
1. ✓ Test VPS SSH connection
2. ✓ Create temp directory on VPS
3. ✓ Upload all 38 SQL files
4. ✓ Create databases on VPS
5. ✓ Restore all tables
6. ✓ Verify restoration

---

## What Happens During Migration

### On Your Local Machine:
- Reads SQL files from `D:/H3c/mysqlbackup`
- Uploads files to VPS `/tmp/mysql-restore/`

### On VPS:
- Creates databases: chatapp, ecoeats, ecoeatsdb, ekorental, fleetmanager, ingress
- Restores each table from SQL files
- Sets UTF8MB4 character set

---

## After Migration

### Verify Databases on VPS:

```bash
# SSH to VPS
ssh your-username@your-vps-ip

# Check databases
mysql -u root -p
SHOW DATABASES;

# Check tables in a database
USE chatapp;
SHOW TABLES;

# Check data
SELECT * FROM users LIMIT 5;
```

### Cleanup (Optional):

```bash
# On VPS, remove uploaded files
ssh your-username@your-vps-ip
rm -rf /tmp/mysql-restore
```

---

## Troubleshooting

### "SSH connection failed"

**Solution:**
- Verify VPS IP address: `ping your-vps-ip`
- Check SSH key: `ssh-copy-id your-username@your-vps-ip`
- Try with password: `ssh -o PreferredAuthentications=password your-username@your-vps-ip`

### "MySQL access denied"

**Solution on VPS:**
```bash
# Reset MySQL password
sudo mysql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

### "Upload failed"

**Solution:**
- Check disk space on VPS: `df -h`
- Check network connection
- Try manual upload: `scp test.sql your-user@your-vps:/tmp/`

### "Database restore failed"

**Solution:**
- Check MySQL is running on VPS: `sudo systemctl status mysql`
- Check MySQL error log: `sudo tail /var/log/mysql/error.log`
- Restore manually:
  ```bash
  cd /tmp/mysql-restore
  mysql -u root -p dbname < tablename.sql
  ```

---

## Manual Migration (Alternative)

If the script doesn't work, migrate manually:

### 1. Upload Files:
```bash
scp /mnt/d/H3c/mysqlbackup/*.sql your-user@your-vps:/tmp/
```

### 2. SSH to VPS:
```bash
ssh your-user@your-vps
```

### 3. Create Databases:
```bash
mysql -u root -p << 'EOF'
CREATE DATABASE chatapp;
CREATE DATABASE ecoeats;
CREATE DATABASE ecoeatsdb;
CREATE DATABASE ekorental;
CREATE DATABASE fleetmanager;
CREATE DATABASE ingress;
EOF
```

### 4. Restore Tables:
```bash
cd /tmp
for file in chatapp_*.sql; do
    mysql -u root -p chatapp < "$file"
done

for file in ecoeats_*.sql; do
    mysql -u root -p ecoeats < "$file"
done

# Repeat for other databases...
```

---

## VPS Information Needed

To run the migration, please provide:

| Information | Example | Your Value |
|------------|---------|------------|
| VPS IP Address | 203.0.113.45 | ___________ |
| SSH Username | ubuntu or root | ___________ |
| SSH Port | 22 | ___________ |
| MySQL Username | root | ___________ |
| MySQL Password | SecurePass123 | ___________ |

---

## Security Notes

⚠️ **Important:**
- Don't commit VPS credentials to Git
- Use SSH keys instead of passwords when possible
- Remove temporary files after migration
- Update MySQL passwords after restore
- Configure firewall on VPS

---

## Next Steps After Migration

1. ✅ Verify all databases restored correctly
2. ✅ Update application connection strings to use VPS MySQL
3. ✅ Test applications with VPS database
4. ✅ Set up automated backups on VPS
5. ✅ Configure MySQL security (firewall, users)

---

**Ready to migrate? Update the script with your VPS credentials and run it!**
