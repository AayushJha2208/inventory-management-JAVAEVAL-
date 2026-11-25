# Inventory Management System

Desktop stock register for a shop. Add, update, delete, search, see low stock, and export reports. The packaged macOS app launches the Swing desktop interface.

## Run

From this folder:

```bat
mvn -q compile exec:java
```

Or double-click `run.bat`.

The window opens with five sample products. Milk and Rice are already low on stock.

Data file: `data/inventory.db`  
Log file: `logs/app.log`  
Exported reports: `reports/inventory-report.txt`, `reports/inventory-report.csv`, and `reports/inventory-report.html`

## Build a macOS app package

On an Apple Silicon Mac with JDK 21+ and Maven installed, run:

```bash
mvn -q -DskipTests package
mkdir -p target/package-input dist/desktop-release
cp target/inventory-management-system-1.0-all.jar target/package-input/inventory-management-system.jar
jpackage --type dmg --name "Inventory Management System" --app-version 1.0 \
  --input target/package-input --main-jar inventory-management-system.jar \
  --main-class com.thapar.ims.App --dest dist/desktop-release \
  --java-options -Dims.packaged=true
```

The DMG is created in `dist/desktop-release/`. Open it and drag the app into Applications. The package includes a Java runtime, so users do not need to install Java separately. Packaged app data, logs, and reports are saved under `~/Library/Application Support/Inventory Management System/`.

This command creates a macOS Apple Silicon package. Packages for Windows or Intel Macs need to be built on those target platforms.

## What to click in a demo

1. Show All — full list.
2. Click Milk — the form fills. That row is a perishable product (it has an expiry date).
3. Low Stock — only Milk and Rice.
4. Add a product. Leave expiry empty for a normal item, or type `2026-12-31` for a perishable item.
5. Try Add with the name left blank. A message box shows the error.
6. Search `pen` or search an id number such as `1`.
7. Type in the search box — matching products appear immediately in the table.
8. Export Report — creates PDF, Word, and Excel versions.

The written report for submission is `docs/Inventory_Management_System_Report.docx`. Fill in your name, roll number, and guide before you submit it.
