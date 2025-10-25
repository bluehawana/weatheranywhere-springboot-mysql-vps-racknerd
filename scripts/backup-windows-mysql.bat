@echo off
REM ==============================
REM Windows MySQL Backup Script
REM ==============================
REM This script backs up all MySQL databases from Windows MySQL Server
REM Username: root
REM Password: rootadmin

echo ==========================================
echo MySQL Database Backup Script
echo ==========================================
echo.

REM Set backup directory
set BACKUP_DIR=C:\mysql-backups
set DATE=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set DATE=%DATE: =0%

REM Create backup directory if it doesn't exist
if not exist "%BACKUP_DIR%" (
    mkdir "%BACKUP_DIR%"
    echo Created backup directory: %BACKUP_DIR%
)

echo Backup directory: %BACKUP_DIR%
echo Timestamp: %DATE%
echo.

REM Backup all databases
echo Backing up all databases...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe" -u root -prootadmin --all-databases > "%BACKUP_DIR%\all_databases_%DATE%.sql"

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] All databases backed up successfully!
    echo File: %BACKUP_DIR%\all_databases_%DATE%.sql
) else (
    echo [ERROR] Backup failed! Error code: %ERRORLEVEL%
    echo.
    echo Please check:
    echo 1. MySQL Server is running
    echo 2. Username and password are correct
    echo 3. mysqldump path is correct
    goto :end
)

echo.
echo ==========================================
echo Backup Complete
echo ==========================================
echo.
echo Backup location: %BACKUP_DIR%
dir "%BACKUP_DIR%\*.sql" /O-D
echo.

:end
pause
