ALTER USER 'root' @'localhost' IDENTIFIED BY 'RootAdmin@123';

GRANT ALL PRIVILEGES ON weatheranywhere.* TO 'root' @'localhost';

FLUSH PRIVILEGES;