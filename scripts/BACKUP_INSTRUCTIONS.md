# MySQL Database Backup Scripts

## Quick Start - Backup ALL Your Windows MySQL Databases

### Option 1: Batch Script (Easiest - Double Click)

1. **Navigate to project folder**:
   ```
   C:\Users\BLUEH\projects\weatheranywhere\scripts
   ```

2. **Double-click**: `backup-all-mysql-databases.bat`

3. **Done!** Your backups will be in: `C:\mysql-backups\backup_[timestamp]\`

---

### Option 2: PowerShell Script (Recommended - More Features)

1. **Right-click PowerShell** and select "Run as Administrator"

2. **Navigate to project folder**:
   ```powershell
   cd C:\Users\BLUEH\projects\weatheranywhere\scripts
   ```

3. **Run the script**:
   ```powershell
   .\backup-all-mysql-databases.ps1
   ```

   If you get an execution policy error, run this first:
   ```powershell
   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
   ```

4. **Done!** Your backups will be in: `C:\mysql-backups\backup_[timestamp]\`

---

### Option 3: Command Line (Manual Control)

```cmd
cd C:\Users\BLUEH\projects\weatheranywhere\scripts
backup-all-mysql-databases.bat
```

---

## What Gets Backed Up?

✅ **ALL your databases** including:
- weatheranywhere
- Any other projects
- All your development databases

❌ **System databases** (included in complete backup but not individually):
- information_schema
- performance_schema
- mysql
- sys

---

## Backup Output Structure

```
C:\mysql-backups\
├── backup_20251025_143000\          ← Timestamped folder
│   ├── ALL_DATABASES_COMPLETE.sql   ← Complete backup (all DBs in one file)
│   ├── weatheranywhere.sql          ← Individual database backups
│   ├── your_project1.sql
│   ├── your_project2.sql
│   ├── database_list.txt            ← List of all databases
│   └── BACKUP_INFO.txt              ← Backup metadata
│
└── LATEST_BACKUP.sql                ← Quick access to latest complete backup
```

---

## Restore Instructions

### Restore a Single Database

**Windows Command Prompt:**
```cmd
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
mysql -u root -prootadmin database_name < C:\mysql-backups\backup_...\database_name.sql
```

**PowerShell:**
```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -prootadmin database_name < C:\mysql-backups\backup_...\database_name.sql
```

### Restore ALL Databases

**Warning**: This will overwrite existing databases!

```cmd
cd "C:\Program Files\MySQL\MySQL Server 8.0\bin"
mysql -u root -prootadmin < C:\mysql-backups\LATEST_BACKUP.sql
```

---

## Configuration

Both scripts use these default settings:

| Setting | Value |
|---------|-------|
| MySQL User | root |
| MySQL Password | rootadmin |
| MySQL Binary Path | C:\Program Files\MySQL\MySQL Server 8.0\bin |
| Backup Location | C:\mysql-backups |

### To Change Settings:

**Batch Script** - Edit `backup-all-mysql-databases.bat`:
```batch
set MYSQL_USER=root
set MYSQL_PASS=rootadmin
set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin
set BACKUP_ROOT=C:\mysql-backups
```

**PowerShell Script** - Use parameters:
```powershell
.\backup-all-mysql-databases.ps1 -MySQLUser "root" -MySQLPass "your_password" -BackupRoot "D:\backups"
```

---

## Troubleshooting

### "MySQL binaries not found"

**Solution**: Update the MySQL path in the script to match your installation:
- Check: `C:\Program Files\MySQL\MySQL Server 8.0\bin`
- Or: `C:\Program Files\MySQL\MySQL Server 8.4\bin`
- Or: `C:\xampp\mysql\bin`

### "Access denied for user 'root'@'localhost'"

**Solution**: Verify your MySQL password:
1. Open MySQL Workbench
2. Connect successfully
3. Update the password in the script

### "MySQL Server is not running"

**Solution**: Start MySQL:
1. Press `Win + R`
2. Type: `services.msc`
3. Find "MySQL80" (or similar)
4. Right-click → Start

### PowerShell "execution policy" error

**Solution**: Run PowerShell as Administrator and execute:
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

---

## Backup Schedule Recommendations

| Frequency | Method | Retention |
|-----------|--------|-----------|
| **Weekly** | Run batch/PowerShell script | Keep last 4 backups |
| **Before major changes** | Manual run | Keep until change is stable |
| **Monthly** | Archive to cloud storage | Keep 12 months |

---

## Cloud Backup (Optional)

After creating local backups, consider uploading to:
- **Google Drive**: `C:\mysql-backups\LATEST_BACKUP.sql`
- **Dropbox**: Sync the entire `C:\mysql-backups` folder
- **OneDrive**: Move backups to OneDrive folder

---

## File Sizes (Approximate)

| Database | Typical Size |
|----------|--------------|
| weatheranywhere | 1-5 MB |
| Empty database | < 1 KB |
| Complete backup (all DBs) | 10-50 MB |

---

## Quick Reference

**Backup NOW:**
```cmd
C:\Users\BLUEH\projects\weatheranywhere\scripts\backup-all-mysql-databases.bat
```

**View backups:**
```cmd
explorer C:\mysql-backups
```

**Latest backup location:**
```
C:\mysql-backups\LATEST_BACKUP.sql
```

---

## Support

If you encounter issues:
1. Check MySQL is running in Windows Services
2. Verify credentials in MySQL Workbench
3. Ensure sufficient disk space
4. Check the error messages in the script output

---

**Created**: 2025-10-25
**For**: Windows MySQL Server 8.0
**Purpose**: Complete database backup solution
