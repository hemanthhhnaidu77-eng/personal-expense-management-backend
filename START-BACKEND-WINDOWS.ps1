# Run this file from the backend folder after starting MySQL.
$env:DB_URL = "jdbc:mysql://localhost:3306/personal_expense_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "root"
$env:JWT_SECRET = "this-is-a-long-development-secret-at-least-32-characters"
$env:JWT_EXPIRATION_MS = "86400000"
Write-Host "Starting Expense Management backend on http://localhost:8080" -ForegroundColor Green
mvn clean spring-boot:run
