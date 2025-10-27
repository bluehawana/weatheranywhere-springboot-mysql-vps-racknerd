#!/bin/bash
# Update VPS MySQL root password

sshpass -p '11' ssh harvad@107.175.235.220 << 'ENDSSH'
# Create SQL file for MariaDB
cat > /tmp/update_mysql.sql << 'EOF'
SET PASSWORD FOR 'root'@'localhost' = PASSWORD('RootAdmin@123');
GRANT ALL PRIVILEGES ON weatheranywhere.* TO 'root'@'localhost' IDENTIFIED BY 'RootAdmin@123';
FLUSH PRIVILEGES;
EOF

# Execute with sudo (MariaDB)
echo "11" | sudo -S mariadb < /tmp/update_mysql.sql

# Restart service
echo "11" | sudo -S systemctl restart weatheranywhere

# Wait and check status
sleep 5
echo "11" | sudo -S systemctl status weatheranywhere --no-pager

echo "Done!"
ENDSSH
