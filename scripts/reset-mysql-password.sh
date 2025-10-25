#!/bin/bash

# ==============================
# MySQL Root Password Reset Script
# ==============================

echo "=========================================="
echo "MySQL Root Password Reset"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# New password
NEW_PASSWORD="rootadmin"

echo -e "${YELLOW}This script will reset the MySQL root password to: $NEW_PASSWORD${NC}"
echo ""
echo "Steps:"
echo "1. Stop MySQL service"
echo "2. Start MySQL in safe mode (skip grant tables)"
echo "3. Reset root password"
echo "4. Restart MySQL normally"
echo ""
read -p "Press Enter to continue or Ctrl+C to cancel..."

# Step 1: Stop MySQL
echo ""
echo "Step 1: Stopping MySQL service..."
sudo service mysql stop
sleep 2

# Step 2: Start MySQL in safe mode
echo "Step 2: Starting MySQL in safe mode..."
sudo mysqld_safe --skip-grant-tables --skip-networking &
SAFE_PID=$!
sleep 5

# Step 3: Reset password
echo "Step 3: Resetting root password..."
mysql -u root <<EOF
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY '$NEW_PASSWORD';
FLUSH PRIVILEGES;
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Password reset successfully!${NC}"
else
    echo -e "${RED}✗ Failed to reset password${NC}"
    sudo kill $SAFE_PID 2>/dev/null
    exit 1
fi

# Step 4: Stop safe mode and restart normally
echo "Step 4: Restarting MySQL normally..."
sudo kill $SAFE_PID 2>/dev/null
sleep 2
sudo service mysql start
sleep 2

# Test the new password
echo ""
echo "Testing new password..."
mysql -u root -p${NEW_PASSWORD} -e "SELECT 'Connection successful!' AS status;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}=========================================="
    echo -e "✓ MySQL Password Reset Complete!"
    echo -e "==========================================${NC}"
    echo ""
    echo "New credentials:"
    echo "  Username: root"
    echo "  Password: $NEW_PASSWORD"
    echo ""
    echo "You can now run your Spring Boot application!"
else
    echo ""
    echo -e "${RED}Password reset may have failed. Please try manually.${NC}"
    exit 1
fi
