# Warehouse Dispatch & Return Authorisation System

## Files
- `pom.xml` — Maven configuration and MySQL JDBC driver.
- `src/main/java/com/warehouse/WarehouseApp.java` — complete Java Swing application, UI, business logic and JDBC persistence.
- `sql/schema.sql` — database/table creation script (the Java app also creates tables automatically).

## Requirements
- JDK 17 or newer
- Maven
- MySQL Server (optional for temporary in-memory demo; required for persistent storage)

## Run on macOS
1. Extract this ZIP.
2. Open Terminal and move into the extracted folder (the folder containing `pom.xml`):
   `cd ~/Desktop/WarehouseDispatchReturnSystem`
3. Confirm Java and Maven:
   `java -version`
   `mvn -version`
4. Run the application:
   `mvn clean compile exec:java`

The app opens in a window. If MySQL is not available, it starts in temporary in-memory mode and shows a warning at the bottom. Data will reset when the app closes.

## Enable MySQL persistence
1. Start MySQL Server.
2. In MySQL Workbench or the MySQL terminal, run `sql/schema.sql` (or create the database with `CREATE DATABASE warehouse_db;`).
3. In the same Terminal window used to run Maven, set your credentials:
   `export DB_URL="jdbc:mysql://localhost:3306/warehouse_db?serverTimezone=UTC"`
   `export DB_USER="root"`
   `export DB_PASSWORD="YOUR_MYSQL_PASSWORD"`
4. Run:
   `mvn clean compile exec:java`

The bottom status line should say: `Storage: MySQL database connected — data is persistent.`

## Demo workflow
- Inventory: add/search/update products.
- Dispatch: enter a product ID (e.g. P1001), quantity and recipient. Stock is deducted.
- Returns: use the generated Dispatch ID, enter quantity and reason, request a return, select it in the table and approve it. Stock is restored once.

Note: This is a compact academic starter implementation. Review it with your team and test database credentials and workflows before submission.
