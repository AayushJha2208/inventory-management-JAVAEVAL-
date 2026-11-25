"""Builds the mini-project report Word file. Run once, then the docx is the deliverable."""
from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt, RGBColor

doc = Document()
section = doc.sections[0]
section.top_margin = Inches(1)
section.bottom_margin = Inches(1)
section.left_margin = Inches(1)
section.right_margin = Inches(1)

style = doc.styles["Normal"]
style.font.name = "Times New Roman"
style.font.size = Pt(12)
style._element.rPr.rFonts.set(qn("w:eastAsia"), "Times New Roman")


def center(text, size=16, bold=True, space_after=6):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(space_after)
    run = p.add_run(text)
    run.bold = bold
    run.font.size = Pt(size)
    run.font.name = "Times New Roman"
    return p


def body(text):
    p = doc.add_paragraph(text)
    p.paragraph_format.space_after = Pt(8)
    p.paragraph_format.line_spacing = 1.15
    for run in p.runs:
        run.font.name = "Times New Roman"
        run.font.size = Pt(12)
    return p


def h(text, level=1):
    para = doc.add_heading(text, level=level)
    for run in para.runs:
        run.font.color.rgb = RGBColor(0, 0, 0)
        run.font.name = "Times New Roman"
    return para


def code(text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(8)
    p.paragraph_format.left_indent = Inches(0.3)
    run = p.add_run(text)
    run.font.name = "Consolas"
    run.font.size = Pt(9)
    return p


def bullet(text):
    p = doc.add_paragraph(text, style="List Bullet")
    for run in p.runs:
        run.font.name = "Times New Roman"
        run.font.size = Pt(12)
    return p


def add_field_run(paragraph, element):
    run = OxmlElement("w:r")
    run.append(element)
    paragraph._p.append(run)


def add_toc_field():
    p = doc.add_paragraph()
    begin = OxmlElement("w:fldChar")
    begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = ' TOC \\o "1-2" \\h \\z \\u '
    separate = OxmlElement("w:fldChar")
    separate.set(qn("w:fldCharType"), "separate")
    hint = OxmlElement("w:t")
    hint.text = "Right-click this line in Word and choose Update Field to fill page numbers."
    end = OxmlElement("w:fldChar")
    end.set(qn("w:fldCharType"), "end")
    add_field_run(p, begin)
    add_field_run(p, instr)
    add_field_run(p, separate)
    add_field_run(p, hint)
    add_field_run(p, end)


# ----- Cover -----
center("Thapar Institute of Engineering and Technology, Patiala", 14)
center("Computer Science and Engineering Department", 13, space_after=12)
center("UCS553: Enterprise Web Application", 14)
center("Mini Project Report", 18, space_after=14)
center("Inventory Management System", 20, space_after=16)
center("Under the Guidance of", 12, bold=False, space_after=2)
center("Dr. _______________________________", 13)
center("Assistant Professor", 12, bold=False, space_after=14)
center("Submitted By", 12, bold=False, space_after=6)
center("Roll Number ____________    Name ____________", 12, bold=False, space_after=2)
center("Roll Number ____________    Name ____________", 12, bold=False, space_after=2)
center("Roll Number ____________    Name ____________", 12, bold=False, space_after=14)
center("In Partial Fulfilment of the Requirements for the Degree of", 12, bold=False, space_after=2)
center("Bachelor of Engineering in Computer Engineering", 12, space_after=12)
center("November 2026", 12, bold=False)

doc.add_page_break()

# ----- Abstract -----
h("Abstract")
body(
    "This project is a small Inventory Management System for a shop. The user can add a product, "
    "update it, delete it, search it, see items that are low in stock, and save a text report. "
    "The program is written in Java and uses a Swing window. Product data is stored in a SQLite "
    "file through JDBC. The design is kept in four simple layers: the window, the service "
    "(rules), the DAO (database), and the model (the Product class). The same program also shows "
    "the class topics we used: packages, encapsulation, inheritance, interfaces, exceptions, "
    "ArrayList, HashMap, file writing, logging, and button events."
)

doc.add_page_break()

# ----- TOC -----
h("Table of Contents")
body("In Microsoft Word, right-click the line below and choose Update Field so the page numbers appear.")
add_toc_field()
body("The report follows this order:")
bullet("Cover Page")
bullet("Abstract")
bullet("Table of Contents")
bullet("Chapter 1: Introduction")
bullet("Chapter 2: System Design")
bullet("Chapter 3: Implementation")
bullet("Chapter 4: Results and Screenshots")
bullet("References")

doc.add_page_break()

# ----- Chapter 1 -----
h("Chapter 1: Introduction")
h("1.1 Introduction to the Project", 2)
body(
    "A shop keeps a list of items it sells: the name, the category, the price, how many pieces "
    "are in stock, and the level at which more stock should be ordered. Doing this on paper is "
    "slow and easy to get wrong. This project replaces that list with a small desktop program."
)
body(
    "There are two kinds of products. A regular product, such as a pen, has no expiry date. "
    "A perishable product, such as milk, has an expiry date. In code, PerishableProduct extends "
    "Product. Both are stored in one table and shown in one window."
)

h("1.2 Problem Statement", 2)
body(
    "The shop needs one place to record stock, find an item quickly, notice when quantity is "
    "too low, and print the current list. The program should reject bad input, such as an empty "
    "name or a negative quantity, and tell the user what to fix."
)

h("1.3 Objectives of the Project", 2)
bullet("Add, update, delete, and view products.")
bullet("Search by product id, name, or category.")
bullet("Show products whose quantity is at or below the reorder level.")
bullet("Save the list to a text file.")
bullet("Store data so it is still there after the program is closed.")
bullet("Use the Java topics from class in a way that is easy to point at during the viva.")

h("1.4 Scope of the Project", 2)
body("The program does the following:")
bullet("One user on one computer. There is no login and no web page.")
bullet("One table of products. There is no supplier module and no billing module.")
bullet("SQLite stores the data in the file data/inventory.db. MySQL is not required.")
bullet("The window is built with Swing, which is already part of the JDK.")
body(
    "The class slides also show JavaFX. The event idea is the same. The button is the event "
    "source, the click is the event, and addActionListener is the event handler. Swing was "
    "chosen so the project runs with JDK 21 and Maven only, without a separate JavaFX download."
)

doc.add_page_break()

# ----- Chapter 2 -----
h("Chapter 2: System Design")
h("2.1 System Architecture", 2)
body("The program is split so each part has one job. The window never writes SQL.")
code(
    "InventoryFrame  (Swing window, buttons)\n"
    "        |\n"
    "        v\n"
    "InventoryService  (interface)\n"
    "        |\n"
    "        v\n"
    "InventoryServiceImpl  (checks rules, ArrayList, HashMap)\n"
    "        |\n"
    "        +----> ProductDao / ProductDaoImpl  (JDBC, SQLite)\n"
    "        |\n"
    "        +----> ReportWriter  (FileWriter)\n"
    "        |\n"
    "        +----> AppLogger  (java.util.logging)"
)
body(
    "When the user clicks Add, the window reads the text fields and calls the service. "
    "The service checks the values. If they are valid it asks the DAO to insert a row. "
    "The DAO uses a PreparedStatement. The service then reloads a HashMap and the window "
    "refreshes the table."
)

h("2.2 Use Case Diagram", 2)
body("There is one actor, the shop user.")
code(
    "                    +----------------------+\n"
    "                    |      Shop user       |\n"
    "                    +----------+-----------+\n"
    "                               |\n"
    "       +-----------+-----------+-----------+-----------+\n"
    "       |           |           |           |           |\n"
    "       v           v           v           v           v\n"
    "  Add product  Update product  Delete   Search    Show all\n"
    "                               product\n"
    "       |                                       |\n"
    "       v                                       v\n"
    "  Low stock                              Export report"
)

h("2.3 Class Diagram", 2)
code(
    "Product\n"
    "  - id, name, category, price, quantity, reorderLevel\n"
    "  + getType() : Regular\n"
    "  + isLowStock() : boolean\n"
    "  + getStockValue() : double\n"
    "            ^\n"
    "            | extends\n"
    "  PerishableProduct\n"
    "  - expiryDate\n"
    "  + getType() : Perishable\n"
    "\n"
    "InventoryException extends Exception\n"
    "\n"
    "<<interface>> ProductDao\n"
    "        ^\n"
    "        | implements\n"
    "  ProductDaoImpl\n"
    "\n"
    "<<interface>> InventoryService\n"
    "        ^\n"
    "        | implements\n"
    "  InventoryServiceImpl  ----uses----> ProductDao\n"
    "         |\n"
    "         +---- HashMap<Integer, Product>\n"
    "\n"
    "InventoryFrame ----uses----> InventoryService\n"
    "App ----starts----> Database.init() and InventoryFrame"
)

h("2.4 Activity Diagram", 2)
body("Add product:")
code(
    "User fills the form\n"
    "        |\n"
    "        v\n"
    "Click Add\n"
    "        |\n"
    "        v\n"
    "Are name, price, quantity, and date valid?\n"
    "      /            \\\n"
    "    No              Yes\n"
    "    |                |\n"
    "    v                v\n"
    "Show message     Insert row with JDBC\n"
    "box              Reload list and show it in the table"
)

h("2.5 Database Design", 2)
body("One table is enough for this project. The file is data/inventory.db.")
body("Table name: products")
bullet("id — integer, primary key, created automatically")
bullet("name — text, required")
bullet("category — text, required (Stationery, Grocery, Electronics, or Other)")
bullet("price — real number, must be greater than 0")
bullet("quantity — integer, cannot be negative")
bullet("reorder_level — integer, cannot be negative")
bullet("expiry_date — text, empty for a regular product, yyyy-mm-dd for a perishable product")
body(
    "If expiry_date is empty, the DAO creates a Product object. If it has a date, the DAO "
    "creates a PerishableProduct. The list type is still List<Product>, which is polymorphism."
)
body("Sample rows loaded the first time the table is empty:")
bullet("Notebook, Pen Box, USB Cable — regular products")
bullet("Milk 1L (quantity 8, reorder 10) and Rice 5kg (quantity 4, reorder 6) — perishable and already low in stock")

h("2.6 Module Description", 2)
bullet("model — Product and PerishableProduct. Data only, plus small methods such as isLowStock().")
bullet("dao — ProductDao and ProductDaoImpl. The only classes that contain SQL.")
bullet("service — InventoryService and InventoryServiceImpl. Rules, search, and the HashMap.")
bullet("exception — InventoryException. One checked exception for rule failures and database failures.")
bullet("util — Database (open SQLite and create the table), AppLogger, and ReportWriter.")
bullet("ui — InventoryFrame. The buttons and the table.")
bullet("App — main method. Opens the window. The argument check runs a short text test.")

doc.add_page_break()

# ----- Chapter 3 -----
h("Chapter 3: Implementation")
h("3.1 Technologies and Tools Used", 2)
bullet("Java 21 (JDK)")
bullet("Apache Maven — pom.xml downloads the SQLite driver and compiles the project")
bullet("Swing — window, table, buttons, and dialog boxes (part of the JDK)")
bullet("JDBC and SQLite — database file, no separate database server")
bullet("java.util.logging — console and logs/app.log")
bullet("java.io.FileWriter — the exported report")

h("3.2 Project Structure", 2)
body("Maven standard folders. All of our classes sit under the package com.thapar.ims.")
code(
    "inventory-management-system/\n"
    "  pom.xml\n"
    "  run.bat\n"
    "  src/main/java/com/thapar/ims/\n"
    "    App.java\n"
    "    model/Product.java\n"
    "    model/PerishableProduct.java\n"
    "    dao/ProductDao.java\n"
    "    dao/ProductDaoImpl.java\n"
    "    service/InventoryService.java\n"
    "    service/InventoryServiceImpl.java\n"
    "    exception/InventoryException.java\n"
    "    util/Database.java\n"
    "    util/AppLogger.java\n"
    "    util/ReportWriter.java\n"
    "    ui/InventoryFrame.java\n"
    "  data/inventory.db          (created on first run)\n"
    "  logs/app.log               (created on first run)\n"
    "  reports/inventory-report.txt"
)
body("Run from the project folder:")
code("mvn -q compile exec:java")
body("Or double-click run.bat. Maven compile compiles the .java files. exec:java runs App.")

h("3.3 Description of Classes and Interfaces", 2)
bullet("Product — private fields and public getters/setters (encapsulation). getType() returns Regular.")
bullet("PerishableProduct — extends Product and overrides getType() and getExpiryDate() (inheritance and polymorphism).")
bullet("ProductDao — interface with insert, update, delete, findById, and findAll.")
bullet("ProductDaoImpl — implements ProductDao using JDBC.")
bullet("InventoryService — interface the window is allowed to call.")
bullet("InventoryServiceImpl — implements the rules and keeps a HashMap.")
bullet("InventoryException — extends Exception.")
bullet("Database — DriverManager.getConnection and CREATE TABLE IF NOT EXISTS.")
bullet("ReportWriter — writes the text file.")
bullet("AppLogger — Logger, ConsoleHandler, and FileHandler.")
bullet("InventoryFrame — builds the window and handles button clicks.")
bullet("App — starts the program.")

h("3.4 Implementation of Major Modules", 2)
body("Add / update / delete. The service builds a Product or a PerishableProduct, then the DAO saves it. After each change the HashMap is filled again from the database.")
body("Search. If the user types a number, searchById looks in the HashMap. If the user types text, searchByName walks the ArrayList and keeps names or categories that contain that text.")
body("Low stock. getLowStockProducts loops the list and keeps products where quantity <= reorder level.")
body("Export. ReportWriter writes every product and the total stock value (price times quantity).")
body("First run. Database.init creates the file and inserts five sample products when the table is empty.")

h("3.5 Exception Handling", 2)
body(
    "InventoryException is a checked exception, so the service methods declare throws InventoryException "
    "and the window must catch it. Bad rules throw it with a short message. SQLException from JDBC "
    "is caught in the service, written to the log at level SEVERE, and wrapped in InventoryException "
    "so the window has only one type to catch. The window also catches NumberFormatException when "
    "price or quantity is not a number, and shows a message box."
)
code(
    "if (name == null || name.isBlank()) {\n"
    "    throw new InventoryException(\"Product name is required.\");\n"
    "}\n"
    "if (price <= 0) {\n"
    "    throw new InventoryException(\"Price must be greater than 0.\");\n"
    "}"
)
code(
    "try {\n"
    "    service.addProduct(...);\n"
    "} catch (InventoryException | NumberFormatException ex) {\n"
    "    showError(ex.getMessage());\n"
    "}"
)

h("3.6 Collection Framework Usage", 2)
body("ArrayList stores the product list returned by the DAO and the filtered search results. HashMap<Integer, Product> stores the same products with the id as the key, so search by id does not scan the whole list. Collections sorting is done with Comparator.comparing on the product name before the list is shown.")
code(
    "private final Map<Integer, Product> byId = new HashMap<>();\n"
    "\n"
    "List<Product> products = new ArrayList<>(byId.values());\n"
    "products.sort(Comparator.comparing(\n"
    "        Product::getName, String.CASE_INSENSITIVE_ORDER));"
)

h("3.7 Database Connectivity", 2)
body(
    "The JDBC steps match the class flow. Database.getConnection uses "
    "DriverManager.getConnection(\"jdbc:sqlite:data/inventory.db\"). ProductDaoImpl then creates "
    "a PreparedStatement, sets each ? value, executes it, and reads a ResultSet for SELECT. "
    "try-with-resources closes the connection, statement, and result set. The question marks "
    "are the important safety point: the name typed by the user is a parameter, not a piece of "
    "the SQL string."
)
code(
    "String sql = \"INSERT INTO products (name, category, price, quantity, reorder_level, expiry_date) \"\n"
    "        + \"VALUES (?, ?, ?, ?, ?, ?)\";\n"
    "try (Connection con = Database.getConnection();\n"
    "     PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {\n"
    "    ps.setString(1, product.getName());\n"
    "    ps.setDouble(3, product.getPrice());\n"
    "    ps.executeUpdate();\n"
    "}"
)
body("The SQLite driver is not written by us. Maven reads pom.xml and downloads sqlite-jdbc from Maven Central.")

h("3.8 Important Code Snippets", 2)
body("Inheritance and method overriding:")
code(
    "public class PerishableProduct extends Product {\n"
    "    private String expiryDate;\n"
    "\n"
    "    @Override\n"
    "    public String getType() {\n"
    "        return \"Perishable\";\n"
    "    }\n"
    "}"
)
body("Which object is created while reading a row (polymorphism):")
code(
    "if (expiry == null || expiry.isBlank()) {\n"
    "    product = new Product();\n"
    "} else {\n"
    "    PerishableProduct perishable = new PerishableProduct();\n"
    "    perishable.setExpiryDate(expiry);\n"
    "    product = perishable;\n"
    "}"
)
body("Button event. The button is the source. The listener is the handler:")
code(
    "JButton addButton = new JButton(\"Add\");\n"
    "addButton.addActionListener(event -> onAdd());"
)
body("Logging:")
code(
    "private static final Logger LOG = AppLogger.get(InventoryServiceImpl.class);\n"
    "LOG.info(\"Added product id=\" + product.getId());"
)
body("File output:")
code(
    "try (FileWriter writer = new FileWriter(file)) {\n"
    "    writer.write(\"INVENTORY REPORT\\n\");\n"
    "}"
)

body("Where each class topic appears:")
bullet("Packages — com.thapar.ims and its subpackages model, dao, service, ui, util, exception.")
bullet("Encapsulation — private fields in Product.")
bullet("Inheritance — PerishableProduct extends Product.")
bullet("Polymorphism — getType() and a List<Product> that can hold both kinds.")
bullet("Interface — ProductDao and InventoryService.")
bullet("Exception handling — InventoryException, try/catch in the window and in the service.")
bullet("Collections — ArrayList and HashMap.")
bullet("File handling — ReportWriter.")
bullet("Logging — AppLogger using java.util.logging.")
bullet("JDBC — ProductDaoImpl and Database.")
bullet("Events — ActionListener on each button.")
bullet("Maven — pom.xml, mvn compile, mvn exec:java.")

doc.add_page_break()

# ----- Chapter 4 -----
h("Chapter 4: Results and Screenshots")
h("4.1 Main Application Screen", 2)
body(
    "After mvn -q compile exec:java the window title is Inventory Management System. "
    "The centre is a table of products. The right side is the form and the buttons: "
    "Add, Update, Delete, Show All, Low Stock, Export Report, Clear Form, and Search. "
    "The bottom line is a short status message."
)
body("Paste a screenshot of the first window here.")
body("[ Screenshot: main screen with the five sample products ]")

h("4.2 Functional Screenshots", 2)
body("Show All lists Notebook, Pen Box, USB Cable, Milk 1L, and Rice 5kg. Milk and Rice have type Perishable and an expiry date. The others have type Regular and a blank expiry.")
body("[ Screenshot: full table ]")
body("Low Stock shows only Milk 1L (quantity 8, reorder 10) and Rice 5kg (quantity 4, reorder 6).")
body("[ Screenshot: low stock ]")
body("Search pen shows Pen Box. Search 4 finds Milk by id through the HashMap. The status line says that the id search used the HashMap.")
body("[ Screenshot: search result ]")

h("4.3 Input and Output Demonstrations", 2)
body("Add a regular product: Name Marker, Category Stationery, Price 25, Quantity 30, Reorder level 5, Expiry left empty. After Add, the table shows type Regular.")
body("Add a perishable product: Name Curd, Category Grocery, Price 40, Quantity 6, Reorder level 8, Expiry 2026-12-20. The table shows type Perishable.")
body("Update: click a row. The form fills. Change the quantity and click Update.")
body("Delete: select a row, click Delete, and confirm the message box.")
body("Export Report writes reports/inventory-report.txt. The file has one line per product and a total stock value at the end. logs/app.log contains an INFO line for the add, update, delete, and export.")
body("[ Screenshot: report text file or the message box that shows the file path ]")

h("4.4 Error Handling Demonstrations", 2)
body("These inputs are rejected. The table does not change, and a message box shows the reason.")
bullet("Add with the name left blank — Product name is required.")
bullet("Price 0 or a negative price — Price must be greater than 0.")
bullet("Quantity abc — Quantity must be a whole number.")
bullet("Expiry 12/31/2026 — Expiry date must look like 2026-12-31, or be left empty.")
bullet("Delete with no row selected — Select a product in the table first.")
body("[ Screenshot: message box for an empty product name ]")

doc.add_page_break()

h("References")
bullet("Oracle, The Java Tutorials — Classes, Interfaces, Exceptions, and Collections. https://docs.oracle.com/javase/tutorial/")
bullet("Oracle, java.util.logging package documentation. https://docs.oracle.com/en/java/javase/21/docs/api/java.logging/java/util/logging/package-summary.html")
bullet("Oracle, JDBC overview. https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html")
bullet("SQLite JDBC driver (org.xerial sqlite-jdbc). https://github.com/xerial/sqlite-jdbc")
bullet("Apache Maven documentation. https://maven.apache.org/guides/")
bullet("UCS553 Enterprise Web Application class material, Thapar Institute of Engineering and Technology, 2026.")
bullet("Java Mini Project Report Format, UCS553, November 2026.")

out = r"C:\Users\sid24\Projects\inventory-management-system\docs\Inventory_Management_System_Report.docx"
doc.save(out)
print(out)
