@echo off
REM ==============================
REM Complete MySQL Database Backup Script
REM Backs up ALL databases from MySQL Server 8.0
REM ==============================

setlocal enabledelayedexpansion

echo ==========================================
echo MySQL Complete Database Backup
echo ==========================================
echo.

REM Configuration
set MYSQL_USER=root
set MYSQL_PASS=rootadmin
set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin
set BACKUP_ROOT=C:\mysql-backups
set TIMESTAMP=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set BACKUP_DIR=%BACKUP_ROOT%\backup_%TIMESTAMP%

REM Create backup directories
if not exist "%BACKUP_ROOT%" mkdir "%BACKUP_ROOT%"
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

echo Backup Directory: %BACKUP_DIR%
echo Timestamp: %TIMESTAMP%
echo.

REM Check if MySQL binaries exist
if not exist "%MYSQL_BIN%\mysqldump.exe" (
    echo [ERROR] MySQL binaries not found at: %MYSQL_BIN%
    echo.
    echo Please update MYSQL_BIN path in this script.
    echo Common locations:
    echo   - C:\Program Files\MySQL\MySQL Server 8.0\bin
    echo   - C:\Program Files\MySQL\MySQL Server 8.4\bin
    echo   - C:\xampp\mysql\bin
    pause
    exit /b 1
)

echo [INFO] MySQL binaries found: %MYSQL_BIN%
echo.

REM Test MySQL connection
echo [STEP 1] Testing MySQL connection...
"%MYSQL_BIN%\mysql.exe" -u %MYSQL_USER% -p%MYSQL_PASS% -e "SELECT 'Connection successful!' AS status;" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Cannot connect to MySQL!
    echo.
    echo Please verify:
    echo   - MySQL Server is running
    echo   - Username: %MYSQL_USER%
    echo   - Password: %MYSQL_PASS%
    echo.
    pause
    exit /b 1
)
echo [SUCCESS] Connected to MySQL successfully!
echo.

REM Get list of databases
echo [STEP 2] Getting list of databases...
"%MYSQL_BIN%\mysql.exe" -u %MYSQL_USER% -p%MYSQL_PASS% -e "SHOW DATABASES;" -s --skip-column-names > "%BACKUP_DIR%\database_list.txt"

echo Databases to backup:
echo -------------------
type "%BACKUP_DIR%\database_list.txt"
echo -------------------
echo.

REM Count databases
set DB_COUNT=0
for /f "tokens=*" %%a in (%BACKUP_DIR%\database_list.txt) do (
    set /a DB_COUNT+=1
)
echo Total databases: %DB_COUNT%
echo.

REM Backup each database individually
echo [STEP 3] Backing up individual databases...
echo.
set BACKED_UP=0
set FAILED=0

for /f "tokens=*" %%i in (%BACKUP_DIR%\database_list.txt) do (
    set DB_NAME=%%i

    REM Skip system databases for individual backups (but include in full backup)
    if not "!DB_NAME!"=="information_schema" (
        if not "!DB_NAME!"=="performance_schema" (
            if not "!DB_NAME!"=="mysql" (
                if not "!DB_NAME!"=="sys" (
                    echo Backing up: !DB_NAME!
                    "%MYSQL_BIN%\mysqldump.exe" -u %MYSQL_USER% -p%MYSQL_PASS% --databases !DB_NAME! --single-transaction --routines --triggers --events > "%BACKUP_DIR%\!DB_NAME!.sql" 2>nul
                    if !ERRORLEVEL! EQU 0 (
                        set /a BACKED_UP+=1
                        echo   [OK] !DB_NAME!.sql
                    ) else (
                        set /a FAILED+=1
                        echo   [FAILED] !DB_NAME!
                    )
                )
            )
        )
    )
)

echo.
echo [STEP 4] Creating complete backup (all databases in one file)...
"%MYSQL_BIN%\mysqldump.exe" -u %MYSQL_USER% -p%MYSQL_PASS% --all-databases --single-transaction --routines --triggers --events --add-drop-database > "%BACKUP_DIR%\ALL_DATABASES_COMPLETE.sql" 2>nul

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Complete backup created: ALL_DATABASES_COMPLETE.sql
) else (
    echo [ERROR] Complete backup failed!
)

echo.
echo [STEP 5] Creating backup metadata...
(
    echo Backup Metadata
    echo ===============
    echo Date: %date% %time%
    echo MySQL User: %MYSQL_USER%
    echo Backup Directory: %BACKUP_DIR%
    echo.
    echo Individual Databases Backed Up: %BACKED_UP%
    echo Failed: %FAILED%
    echo.
    echo Files in this backup:
    echo --------------------
) > "%BACKUP_DIR%\BACKUP_INFO.txt"

dir /b "%BACKUP_DIR%\*.sql" >> "%BACKUP_DIR%\BACKUP_INFO.txt"

echo.
echo ==========================================
echo Backup Summary
echo ==========================================
echo.
echo Backup Location: %BACKUP_DIR%
echo Individual databases backed up: %BACKED_UP%
echo Failed backups: %FAILED%
echo.
echo Files created:
dir /b "%BACKUP_DIR%\*.sql"
echo.

REM Calculate total size
for /f "tokens=3" %%a in ('dir "%BACKUP_DIR%" /-c ^| find "File(s)"') do set BACKUP_SIZE=%%a
echo Total backup size: %BACKUP_SIZE% bytes
echo.

REM Create a latest symlink/copy for easy access
copy "%BACKUP_DIR%\ALL_DATABASES_COMPLETE.sql" "%BACKUP_ROOT%\LATEST_BACKUP.sql" >nul
echo Quick access: %BACKUP_ROOT%\LATEST_BACKUP.sql
echo.

echo ==========================================
echo Backup Complete!
echo ==========================================
echo.
echo To restore a database:
echo   mysql -u %MYSQL_USER% -p%MYSQL_PASS% database_name ^< backup_file.sql
echo.
echo To restore all databases:
echo   mysql -u %MYSQL_USER% -p%MYSQL_PASS% ^< ALL_DATABASES_COMPLETE.sql
echo.

REM Open backup folder
echo Opening backup folder...
explorer "%BACKUP_DIR%"

pause
