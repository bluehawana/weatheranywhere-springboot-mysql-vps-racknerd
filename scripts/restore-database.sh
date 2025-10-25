#!/bin/bash
# Database Restore Script for WeatherAnywhere
# Run this on your RackNerd VPS after deployment

set -e

if [ $# -eq 0 ]; then
  echo "Usage: $0 <backup-file.sql>"
  echo "Example: $0 weatheranywhere-backup-20241024.sql"
  exit 1
fi

BACKUP_FILE=$1

if [ ! -f "$BACKUP_FILE" ]; then
  echo "Error: Backup file not found: $BACKUP_FILE"
  exit 1
fi

echo "=== WeatherAnywhere Database Restore ==="
echo "Restoring from: $BACKUP_FILE"

# Check if file is gzipped
if [[ $BACKUP_FILE == *.gz ]]; then
  echo "Decompressing backup..."
  gunzip "$BACKUP_FILE"
  BACKUP_FILE="${BACKUP_FILE%.gz}"
fi

# Database credentials (update these)
DB_NAME="weatheranywhere"
DB_USER="weatherapp"

echo "Connecting to database: ${DB_NAME}"
echo "User: ${DB_USER}"
echo ""

# Restore
mysql -u "${DB_USER}" -p "${DB_NAME}" < "${BACKUP_FILE}"

if [ $? -eq 0 ]; then
  echo "✓ Database restored successfully!"
  echo ""
  echo "Verifying tables..."
  mysql -u "${DB_USER}" -p "${DB_NAME}" -e "SHOW TABLES;"
else
  echo "✗ Restore failed!"
  exit 1
fi
