#!/bin/bash
# ==============================
# MySQL Backup Migration to VPS
# ==============================
# This script uploads and restores MySQL databases to VPS

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================="
echo -e "MySQL Database Migration to VPS"
echo -e "==========================================${NC}"
echo ""

# Configuration
VPS_HOST="107.175.235.220"
VPS_USER="harvad"
VPS_SSH_PORT="22"
BACKUP_DIR="/mnt/d/H3c/mysqlbackup"
VPS_MYSQL_USER="root"
VPS_MYSQL_PASS="rootadmin"
VPS_TEMP_DIR="/tmp/mysql-restore"

echo -e "${YELLOW}Configuration:${NC}"
echo "  Local backup: $BACKUP_DIR"
echo "  VPS Host: $VPS_HOST"
echo "  VPS User: $VPS_USER"
echo "  VPS MySQL User: $VPS_MYSQL_USER"
echo ""

# Check if VPS credentials are set
if [ "$VPS_HOST" == "your-vps-ip-or-hostname" ]; then
    echo -e "${RED}[ERROR] Please update VPS_HOST in this script!${NC}"
    echo ""
    echo "Edit this file and set:"
    echo "  VPS_HOST=\"your.vps.ip.address\""
    echo "  VPS_USER=\"your-username\""
    echo "  VPS_MYSQL_PASS=\"your-mysql-password\""
    exit 1
fi

# Check if backup directory exists
if [ ! -d "$BACKUP_DIR" ]; then
    echo -e "${RED}[ERROR] Backup directory not found: $BACKUP_DIR${NC}"
    exit 1
fi

echo -e "${GREEN}[STEP 1] Counting backup files...${NC}"
SQL_FILES=$(find "$BACKUP_DIR" -name "*.sql" | wc -l)
echo "  Found $SQL_FILES SQL files to upload"
echo ""

echo -e "${GREEN}[STEP 2] Testing VPS SSH connection...${NC}"
if ssh -p $VPS_SSH_PORT $VPS_USER@$VPS_HOST "echo 'SSH connection successful'" 2>/dev/null; then
    echo -e "  ${GREEN}✓ SSH connection OK${NC}"
else
    echo -e "${RED}  ✗ Cannot connect to VPS via SSH${NC}"
    echo ""
    echo "Please check:"
    echo "  1. VPS_HOST is correct"
    echo "  2. VPS_USER is correct"
    echo "  3. SSH key is set up or you have password access"
    echo "  4. VPS is reachable: ping $VPS_HOST"
    exit 1
fi
echo ""

echo -e "${GREEN}[STEP 3] Creating temp directory on VPS...${NC}"
ssh -p $VPS_SSH_PORT $VPS_USER@$VPS_HOST "mkdir -p $VPS_TEMP_DIR"
echo -e "  ${GREEN}✓ Directory created: $VPS_TEMP_DIR${NC}"
echo ""

echo -e "${GREEN}[STEP 4] Uploading SQL files to VPS...${NC}"
echo "  This may take a while..."

# Upload all SQL files
rsync -avz --progress -e "ssh -p $VPS_SSH_PORT" "$BACKUP_DIR/"*.sql "$VPS_USER@$VPS_HOST:$VPS_TEMP_DIR/"

if [ $? -eq 0 ]; then
    echo -e "  ${GREEN}✓ Upload complete${NC}"
else
    echo -e "${RED}  ✗ Upload failed${NC}"
    exit 1
fi
echo ""

echo -e "${GREEN}[STEP 5] Extracting database names from SQL files...${NC}"

# Create a list of databases
cat > /tmp/db_list.txt << 'DBLIST'
# Database name extraction from filenames
# Format: dbname_tablename.sql -> dbname
DBLIST

cd "$BACKUP_DIR"
for file in *.sql; do
    # Extract database name (part before first underscore)
    dbname=$(echo "$file" | cut -d'_' -f1)
    echo "$dbname"
done | sort -u >> /tmp/db_list.txt

DATABASES=$(cat /tmp/db_list.txt | grep -v "^#" | sort -u)
DB_COUNT=$(echo "$DATABASES" | wc -l)

echo "  Databases to restore:"
echo "$DATABASES" | sed 's/^/    /'
echo ""
echo "  Total: $DB_COUNT databases"
echo ""

echo -e "${GREEN}[STEP 6] Creating restore script for VPS...${NC}"

# Create VPS restore script
cat > /tmp/vps_restore.sh << 'VPSSCRIPT'
#!/bin/bash
set -e

MYSQL_USER="VPS_MYSQL_USER_PLACEHOLDER"
MYSQL_PASS="VPS_MYSQL_PASS_PLACEHOLDER"
TEMP_DIR="VPS_TEMP_DIR_PLACEHOLDER"

cd $TEMP_DIR

echo "Creating databases..."

# Get unique database names
DBS=$(ls *.sql | cut -d'_' -f1 | sort -u)

for db in $DBS; do
    echo "  Creating database: $db"
    mysql -u $MYSQL_USER -p"$MYSQL_PASS" -e "CREATE DATABASE IF NOT EXISTS \`$db\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || true
done

echo ""
echo "Restoring tables..."

# Restore each SQL file
for file in *.sql; do
    dbname=$(echo "$file" | cut -d'_' -f1)
    echo "  Restoring: $file -> $dbname"
    mysql -u $MYSQL_USER -p"$MYSQL_PASS" "$dbname" < "$file" 2>/dev/null || echo "    [WARNING] Failed to restore $file"
done

echo ""
echo "Verifying databases..."
mysql -u $MYSQL_USER -p"$MYSQL_PASS" -e "SHOW DATABASES;" | grep -v -E "information_schema|performance_schema|mysql|sys|Database"

echo ""
echo "Restore complete!"
VPSSCRIPT

# Replace placeholders
sed -i "s/VPS_MYSQL_USER_PLACEHOLDER/$VPS_MYSQL_USER/g" /tmp/vps_restore.sh
sed -i "s/VPS_MYSQL_PASS_PLACEHOLDER/$VPS_MYSQL_PASS/g" /tmp/vps_restore.sh
sed -i "s|VPS_TEMP_DIR_PLACEHOLDER|$VPS_TEMP_DIR|g" /tmp/vps_restore.sh

# Upload restore script
scp -P $VPS_SSH_PORT /tmp/vps_restore.sh "$VPS_USER@$VPS_HOST:$VPS_TEMP_DIR/restore.sh"
ssh -p $VPS_SSH_PORT $VPS_USER@$VPS_HOST "chmod +x $VPS_TEMP_DIR/restore.sh"

echo -e "  ${GREEN}✓ Restore script uploaded${NC}"
echo ""

echo -e "${YELLOW}[STEP 7] Ready to restore databases on VPS${NC}"
echo ""
echo -e "${RED}WARNING: This will create/overwrite databases on your VPS!${NC}"
echo ""
read -p "Continue with database restoration? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "Restore cancelled."
    echo ""
    echo "Files are uploaded to VPS: $VPS_TEMP_DIR"
    echo "You can manually restore later by running:"
    echo "  ssh $VPS_USER@$VPS_HOST"
    echo "  cd $VPS_TEMP_DIR"
    echo "  ./restore.sh"
    exit 0
fi

echo ""
echo -e "${GREEN}[STEP 8] Restoring databases on VPS...${NC}"

ssh -p $VPS_SSH_PORT $VPS_USER@$VPS_HOST "$VPS_TEMP_DIR/restore.sh"

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}=========================================="
    echo -e "✓ Migration Complete!"
    echo -e "==========================================${NC}"
    echo ""
    echo "Summary:"
    echo "  - Databases restored: $DB_COUNT"
    echo "  - SQL files processed: $SQL_FILES"
    echo "  - VPS location: $VPS_TEMP_DIR"
    echo ""
    echo "Verify databases:"
    echo "  ssh $VPS_USER@$VPS_HOST"
    echo "  mysql -u $VPS_MYSQL_USER -p"
    echo "  SHOW DATABASES;"
else
    echo -e "${RED}[ERROR] Restore failed!${NC}"
    echo ""
    echo "Check logs on VPS:"
    echo "  ssh $VPS_USER@$VPS_HOST"
    echo "  cd $VPS_TEMP_DIR"
    exit 1
fi

echo ""
echo -e "${YELLOW}Cleanup (optional):${NC}"
echo "To remove uploaded files from VPS:"
echo "  ssh $VPS_USER@$VPS_HOST"
echo "  rm -rf $VPS_TEMP_DIR"
