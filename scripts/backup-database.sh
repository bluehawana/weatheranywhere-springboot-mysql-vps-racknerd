#!/bin/bash
# Database Backup Script for WeatherAnywhere
# Run this on a machine with mysqldump installed

set -e

echo "=== WeatherAnywhere Database Backup ==="
echo "Backing up from JawsDB (Heroku)..."

BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="weatheranywhere-backup-${BACKUP_DATE}.sql"

# JawsDB credentials
DB_HOST="nba02whlntki5w2p.cbetxkdyhwsb.us-east-1.rds.amazonaws.com"
DB_PORT="3306"
DB_NAME="r6rrjlqt6d8haf12"
DB_USER="uf2mv6u7ijz0ut8d"
DB_PASS="lmz4u9k4garaowya"

echo "Connecting to: ${DB_HOST}:${DB_PORT}/${DB_NAME}"

# Create backup
mysqldump -h "${DB_HOST}" \
  -P "${DB_PORT}" \
  -u "${DB_USER}" \
  -p"${DB_PASS}" \
  "${DB_NAME}" \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  > "${BACKUP_FILE}"

if [ $? -eq 0 ]; then
  echo "✓ Backup successful: ${BACKUP_FILE}"
  echo "  File size: $(du -h ${BACKUP_FILE} | cut -f1)"
else
  echo "✗ Backup failed!"
  exit 1
fi

# Create compressed backup
gzip "${BACKUP_FILE}"
echo "✓ Compressed: ${BACKUP_FILE}.gz"

echo ""
echo "=== Backup Complete ==="
echo "File: ${BACKUP_FILE}.gz"
echo ""
echo "To restore on new server:"
echo "  gunzip ${BACKUP_FILE}.gz"
echo "  mysql -u weatherapp -p weatheranywhere < ${BACKUP_FILE}"
