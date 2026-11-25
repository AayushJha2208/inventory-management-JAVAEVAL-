# Inventory & Sales Management System

Desktop stock register and sales tracking system for a shop. Add, update, delete, search, see low stock, record sales transactions, auto-deduct/remove stock from the database, track sales history, and export comprehensive inventory and sales reports. The packaged macOS app launches the Swing desktop interface.

## Key Features

1. **Stock Inventory Ledger**: Complete stock catalogue management with SKU properties, category tagging, low-stock alerts, and live calculation.
2. **Sales Point-of-Sale (Record Sale)**:
   - Browse and select available products to sell.
   - Enter quantity and selling price with live subtotal calculation.
   - **Database Stock Deduction**: Automatically deducts sold quantities from the database inventory in real time.
   - Option to automatically remove products from the catalog if stock reaches zero.
3. **Sales History Ledger**:
   - Comprehensive audit trail of all transactions with timestamps, product details, quantities, unit prices, totals, and customer notes.
   - Real-time sales statistics: Total Revenue, Units Sold, and Total Orders.
   - Live search across sales records.
4. **Report Generation**: Export both inventory status and sales history reports in CSV (Excel-compatible) and formatted text formats.

## Run

From this folder:

```bat
mvn -q compile exec:java
```

Or double-click `run.bat` (Windows) or `./run.sh` (macOS/Linux).

The window opens with sample products. Milk and Rice are already low on stock.

Data file: `data/inventory.db`  
Log file: `logs/app.log`  
Exported reports: `reports/inventory-report.txt`, `reports/inventory-report.csv`, `reports/sales-history-report.txt`, `reports/sales-history-report.csv`

## Build a macOS app package

On an Apple Silicon Mac with JDK 21+ installed, run:

```bash
mkdir -p target/package-input dist/desktop-release
cp target/inventory-management-system-1.0-all.jar target/package-input/inventory-management-system.jar
jpackage --type dmg --name "Inventory Management System" --app-version 1.0 \
  --input target/package-input --main-jar inventory-management-system.jar \
  --main-class com.thapar.ims.App --dest dist/desktop-release \
  --java-options -Dims.packaged=true
```

The DMG installer is created in `dist/desktop-release/`. Open it and drag the app into Applications. The package includes a bundled Java runtime, so end users do not need to install Java separately. Packaged app data, logs, and reports are saved under `~/Library/Application Support/Inventory Management System/`.

## What to click in a demo

1. **📦 Stock Inventory**: Full product ledger. Click any item to fill the edit form.
2. **💳 Record New Sale**:
   - Click a product from the list (e.g. Notebook or Milk).
   - Enter the quantity to sell (e.g. `2`). Notice the real-time calculation and stock preview.
   - Click **Complete Sale & Deduct Stock**. Confirm the sale.
   - Switch back to **Stock Inventory** — notice the stock has been automatically deducted in the database!
3. **📜 Sales History**:
   - View your newly completed transaction with timestamp and revenue.
   - Check the top header stats: Total Sales Revenue and Units Sold have updated live.
   - Click **Export Sales Report** to export CSV and text sales summaries.
4. **Low Stock**: Filter to items below their reorder threshold.
5. **Search**: Instant live search by ID, product name, or category.

The written report for submission is `docs/Inventory_Management_System_Report.docx`.
