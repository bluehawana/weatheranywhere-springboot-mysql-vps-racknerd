# ==============================
# MySQL Complete Database Backup Script (PowerShell)
# Backs up ALL databases from MySQL Server 8.0
# ==============================

param(
    [string]$MySQLUser = "root",
    [string]$MySQLPass = "rootadmin",
    [string]$MySQLBin = "C:\Program Files\MySQL\MySQL Server 8.0\bin",
    [string]$BackupRoot = "C:\mysql-backups"
)

# Set error action preference
$ErrorActionPreference = "Continue"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "MySQL Complete Database Backup (PowerShell)" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Create timestamp
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupDir = Join-Path $BackupRoot "backup_$timestamp"

# Create directories
if (-not (Test-Path $BackupRoot)) {
    New-Item -ItemType Directory -Path $BackupRoot | Out-Null
    Write-Host "[INFO] Created root backup directory: $BackupRoot" -ForegroundColor Yellow
}

if (-not (Test-Path $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir | Out-Null
}

Write-Host "Backup Directory: $backupDir" -ForegroundColor Green
Write-Host "Timestamp: $timestamp" -ForegroundColor Green
Write-Host ""

# Check MySQL binaries
$mysqldump = Join-Path $MySQLBin "mysqldump.exe"
$mysql = Join-Path $MySQLBin "mysql.exe"

if (-not (Test-Path $mysqldump)) {
    Write-Host "[ERROR] mysqldump.exe not found at: $mysqldump" -ForegroundColor Red
    Write-Host ""
    Write-Host "Common MySQL installation paths:" -ForegroundColor Yellow
    Write-Host "  - C:\Program Files\MySQL\MySQL Server 8.0\bin" -ForegroundColor Yellow
    Write-Host "  - C:\Program Files\MySQL\MySQL Server 8.4\bin" -ForegroundColor Yellow
    Write-Host "  - C:\xampp\mysql\bin" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please update the MySQLBin parameter or install MySQL." -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host "[INFO] MySQL binaries found: $MySQLBin" -ForegroundColor Green
Write-Host ""

# Test connection
Write-Host "[STEP 1] Testing MySQL connection..." -ForegroundColor Cyan
$testQuery = "SELECT 'Connection successful!' AS status;"
$result = & $mysql -u $MySQLUser -p"$MySQLPass" -e $testQuery 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Cannot connect to MySQL!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error details:" -ForegroundColor Yellow
    Write-Host $result -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please verify:" -ForegroundColor Yellow
    Write-Host "  - MySQL Server is running (check Services)" -ForegroundColor Yellow
    Write-Host "  - Username: $MySQLUser" -ForegroundColor Yellow
    Write-Host "  - Password: $MySQLPass" -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host "[SUCCESS] Connected to MySQL successfully!" -ForegroundColor Green
Write-Host ""

# Get database list
Write-Host "[STEP 2] Getting list of databases..." -ForegroundColor Cyan
$dbListFile = Join-Path $backupDir "database_list.txt"
& $mysql -u $MySQLUser -p"$MySQLPass" -e "SHOW DATABASES;" -s --skip-column-names | Out-File -FilePath $dbListFile -Encoding UTF8

$databases = Get-Content $dbListFile

Write-Host "Databases found:" -ForegroundColor Yellow
Write-Host "-------------------" -ForegroundColor Yellow
$databases | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
Write-Host "-------------------" -ForegroundColor Yellow
Write-Host "Total databases: $($databases.Count)" -ForegroundColor Green
Write-Host ""

# Backup individual databases
Write-Host "[STEP 3] Backing up individual databases..." -ForegroundColor Cyan
Write-Host ""

$systemDatabases = @("information_schema", "performance_schema", "mysql", "sys")
$backedUp = 0
$failed = 0
$results = @()

foreach ($db in $databases) {
    $db = $db.Trim()

    if ($systemDatabases -notcontains $db -and $db -ne "") {
        Write-Host "Backing up: $db" -ForegroundColor Yellow -NoNewline

        $backupFile = Join-Path $backupDir "$db.sql"

        try {
            & $mysqldump -u $MySQLUser -p"$MySQLPass" --databases $db --single-transaction --routines --triggers --events | Out-File -FilePath $backupFile -Encoding UTF8

            if ($LASTEXITCODE -eq 0) {
                $fileSize = (Get-Item $backupFile).Length
                $fileSizeKB = [math]::Round($fileSize / 1KB, 2)
                Write-Host " [OK] ($fileSizeKB KB)" -ForegroundColor Green
                $backedUp++
                $results += [PSCustomObject]@{
                    Database = $db
                    Status = "Success"
                    Size = "$fileSizeKB KB"
                    File = "$db.sql"
                }
            } else {
                Write-Host " [FAILED]" -ForegroundColor Red
                $failed++
                $results += [PSCustomObject]@{
                    Database = $db
                    Status = "Failed"
                    Size = "N/A"
                    File = "N/A"
                }
            }
        } catch {
            Write-Host " [ERROR] $_" -ForegroundColor Red
            $failed++
        }
    }
}

Write-Host ""

# Complete backup
Write-Host "[STEP 4] Creating complete backup (all databases)..." -ForegroundColor Cyan
$completeBackupFile = Join-Path $backupDir "ALL_DATABASES_COMPLETE.sql"

& $mysqldump -u $MySQLUser -p"$MySQLPass" --all-databases --single-transaction --routines --triggers --events --add-drop-database | Out-File -FilePath $completeBackupFile -Encoding UTF8

if ($LASTEXITCODE -eq 0) {
    $completeSize = (Get-Item $completeBackupFile).Length
    $completeSizeMB = [math]::Round($completeSize / 1MB, 2)
    Write-Host "[SUCCESS] Complete backup created: ALL_DATABASES_COMPLETE.sql ($completeSizeMB MB)" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Complete backup failed!" -ForegroundColor Red
}

Write-Host ""

# Create metadata
Write-Host "[STEP 5] Creating backup metadata..." -ForegroundColor Cyan
$metadataFile = Join-Path $backupDir "BACKUP_INFO.txt"

$metadata = @"
Backup Metadata
===============
Date: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
MySQL User: $MySQLUser
MySQL Server: localhost
Backup Directory: $backupDir

Summary:
--------
Total Databases: $($databases.Count)
Individual Backups: $backedUp
Failed: $failed
System Databases (not individually backed up): $($systemDatabases.Count)

Individual Database Backups:
----------------------------
"@

$metadata | Out-File -FilePath $metadataFile -Encoding UTF8

$results | Format-Table -AutoSize | Out-File -FilePath $metadataFile -Append -Encoding UTF8

Write-Host "[SUCCESS] Metadata saved" -ForegroundColor Green
Write-Host ""

# Summary
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Backup Summary" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Backup Location: $backupDir" -ForegroundColor Green
Write-Host "Individual databases backed up: $backedUp" -ForegroundColor $(if($backedUp -gt 0){"Green"}else{"Yellow"})
Write-Host "Failed backups: $failed" -ForegroundColor $(if($failed -eq 0){"Green"}else{"Red"})
Write-Host ""

# Calculate total size
$totalSize = (Get-ChildItem $backupDir -Filter "*.sql" | Measure-Object -Property Length -Sum).Sum
$totalSizeMB = [math]::Round($totalSize / 1MB, 2)
Write-Host "Total backup size: $totalSizeMB MB" -ForegroundColor Green
Write-Host ""

# Create quick access copy
$latestBackup = Join-Path $BackupRoot "LATEST_BACKUP.sql"
Copy-Item $completeBackupFile $latestBackup -Force
Write-Host "Quick access copy: $latestBackup" -ForegroundColor Yellow
Write-Host ""

# List files
Write-Host "Backup files created:" -ForegroundColor Cyan
Get-ChildItem $backupDir -Filter "*.sql" | Select-Object Name, @{Name="Size (KB)";Expression={[math]::Round($_.Length/1KB,2)}} | Format-Table -AutoSize

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Backup Complete!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Restore Instructions:" -ForegroundColor Yellow
Write-Host "  Restore single database:" -ForegroundColor White
Write-Host "    mysql -u $MySQLUser -p < $backupDir\database_name.sql" -ForegroundColor Gray
Write-Host ""
Write-Host "  Restore all databases:" -ForegroundColor White
Write-Host "    mysql -u $MySQLUser -p < $completeBackupFile" -ForegroundColor Gray
Write-Host ""

# Open backup folder
Write-Host "Opening backup folder..." -ForegroundColor Yellow
Start-Process "explorer.exe" -ArgumentList $backupDir

Write-Host ""
Write-Host "Press any key to exit..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
