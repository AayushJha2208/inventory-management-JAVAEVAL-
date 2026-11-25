package com.thapar.ims.ui;

import com.thapar.ims.exception.InventoryException;
import com.thapar.ims.model.Product;
import com.thapar.ims.model.Sale;
import com.thapar.ims.service.InventoryService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Inventory & Sales Management System desktop window.
 * Features:
 * - Stock Inventory Ledger & SKU Management
 * - New Sales Page (Record Sales & auto-deduct/remove from database)
 * - Sales History Ledger & Revenue Analytics
 */
public class InventoryFrame extends JFrame {

    private final InventoryService service;

    // Card Layout Views
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainCardContainer = new JPanel(cardLayout);

    private static final String VIEW_INVENTORY = "INVENTORY";
    private static final String VIEW_SALE = "RECORD_SALE";
    private static final String VIEW_HISTORY = "SALES_HISTORY";

    // Nav Buttons
    private final ModernButton navInventoryBtn = new ModernButton("Stock Inventory", ModernButton.Variant.PRIMARY);
    private final ModernButton navSaleBtn = new ModernButton("Record New Sale", ModernButton.Variant.SECONDARY);
    private final ModernButton navHistoryBtn = new ModernButton("Sales History", ModernButton.Variant.SECONDARY);

    // ==========================================
    // 1. INVENTORY MANAGEMENT VIEW COMPONENTS
    // ==========================================
    private final PlaceholderTextField nameField = new PlaceholderTextField("e.g. Wireless Mouse");
    private final JComboBox<String> categoryBox = new JComboBox<>(
            new String[]{"Stationery", "Grocery", "Electronics", "Hardware", "Office Supplies", "Other"});
    private final PlaceholderTextField priceField = new PlaceholderTextField("0.00");
    private final PlaceholderTextField quantityField = new PlaceholderTextField("0");
    private final PlaceholderTextField reorderField = new PlaceholderTextField("e.g. 10");
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"Regular", "Perishable"});
    private final PlaceholderTextField expiryField = new PlaceholderTextField("YYYY-MM-DD");

    private final PlaceholderTextField searchField = new PlaceholderTextField("Search by ID, name, category...");

    private final ModernButton addButton = new ModernButton("Add Item", ModernButton.Variant.PRIMARY);
    private final ModernButton updateButton = new ModernButton("Update", ModernButton.Variant.SECONDARY);
    private final ModernButton deleteButton = new ModernButton("Delete", ModernButton.Variant.DANGER);
    private final ModernButton clearButton = new ModernButton("Clear Form", ModernButton.Variant.SECONDARY);

    private final ModernButton showAllBtn = new ModernButton("Show All", ModernButton.Variant.PRIMARY);
    private final ModernButton lowStockBtn = new ModernButton("Low Stock", ModernButton.Variant.WARNING);
    private final ModernButton exportBtn = new ModernButton("Export Report", ModernButton.Variant.SECONDARY);

    private final BadgeLabel formModeBadge = new BadgeLabel("Create Mode", new Color(5, 150, 105), Color.WHITE, null);
    private final JLabel formHeading = new JLabel("Item Management");
    private final JLabel formSubheading = new JLabel("Add new stock or edit the selected item");
    private final JLabel previewValueLabel = new JLabel("Rs 0.00");
    private final JLabel rowCountLabel = new JLabel("Showing 0 entries");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Item Name", "Category", "Price", "Qty", "Reorder", "Expiry", "Type", "Stock Value"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    // ==========================================
    // 2. RECORD NEW SALE VIEW COMPONENTS
    // ==========================================
    private final DefaultTableModel saleProductTableModel = new DefaultTableModel(
            new String[]{"ID", "Product Name", "Category", "Price (Rs)", "Available Stock", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable saleProductTable = new JTable(saleProductTableModel);
    private final PlaceholderTextField saleProductSearchField = new PlaceholderTextField("Search product to sell...");

    private final JLabel saleSelectedProductName = new JLabel("No product selected");
    private final BadgeLabel saleSelectedCategoryBadge = new BadgeLabel("None");
    private final BadgeLabel saleStockBadge = new BadgeLabel("Stock: -", new Color(241, 245, 249), new Color(100, 116, 139), BORDER);

    private final PlaceholderTextField saleQtyField = new PlaceholderTextField("1");
    private final PlaceholderTextField saleUnitPriceField = new PlaceholderTextField("0.00");
    private final PlaceholderTextField saleNotesField = new PlaceholderTextField("Customer name or invoice ref...");
    private final JCheckBox saleDeductStockCheck = new JCheckBox("Deduct sold quantity from inventory database", true);
    private final JCheckBox saleRemoveIfZeroCheck = new JCheckBox("Remove product from catalog if stock reaches 0", false);

    private final JLabel saleTotalAmountLabel = new JLabel("Rs 0.00");
    private final JLabel saleRemainingStockLabel = new JLabel("Remaining Stock: -");

    private final ModernButton completeSaleBtn = new ModernButton("Complete Sale & Deduct Stock", ModernButton.Variant.PRIMARY);
    private final ModernButton clearSaleBtn = new ModernButton("Clear Selection", ModernButton.Variant.SECONDARY);

    private Product selectedProductForSale = null;

    // ==========================================
    // 3. SALES HISTORY VIEW COMPONENTS
    // ==========================================
    private final DefaultTableModel salesHistoryTableModel = new DefaultTableModel(
            new String[]{"Sale ID", "Date & Time", "Product Name", "Category", "Qty Sold", "Unit Price", "Total (Rs)", "Notes"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable salesHistoryTable = new JTable(salesHistoryTableModel);
    private final PlaceholderTextField salesHistorySearchField = new PlaceholderTextField("Search sales by product, category, notes...");
    private final ModernButton exportSalesBtn = new ModernButton("Export Sales Report", ModernButton.Variant.SECONDARY);
    private final ModernButton refreshSalesBtn = new ModernButton("Refresh", ModernButton.Variant.SECONDARY);
    private final JLabel salesHistorySummaryLabel = new JLabel("0 transactions recorded");

    // ==========================================
    // HEADER STAT LABELS
    // ==========================================
    private final JLabel totalValueLabel = new JLabel("Rs 0.00");
    private final JLabel totalRevenueLabel = new JLabel("Rs 0.00");
    private final JLabel totalSoldLabel = new JLabel("0 Units");
    private final JLabel lowStockCountLabel = new JLabel("0 Alerts");
    private final JLabel statusLabel = new JLabel("Ready");

    // Theme color constants
    public static final Color GREEN = new Color(22, 101, 52);
    public static final Color ACCENT = new Color(5, 150, 105);
    public static final Color PALE_GREEN = new Color(240, 253, 244);
    public static final Color SURFACE = new Color(248, 250, 248);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color TEXT = new Color(15, 23, 42);
    public static final Color MUTED = new Color(100, 116, 139);
    public static final Color WARNING = new Color(180, 83, 9);

    private String activeFilter = "all";

    public InventoryFrame(InventoryService service) {
        super("Inventory Management System & Sales Register");
        this.service = service;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 860);
        setMinimumSize(new Dimension(1150, 750));
        setLocationRelativeTo(null);

        buildUi();
        setupEvents();

        // Initial Data Load
        setFormMode(false);
        refreshAllData();
    }

    private void buildUi() {
        getContentPane().setBackground(SURFACE);
        setLayout(new BorderLayout());

        // 1. Top Section: Global Header + Navigation Bar
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(headerPanel(), BorderLayout.NORTH);
        topContainer.add(navBar(), BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // 2. Main Center Card Container
        mainCardContainer.setBackground(SURFACE);
        mainCardContainer.add(inventoryViewPanel(), VIEW_INVENTORY);
        mainCardContainer.add(recordSaleViewPanel(), VIEW_SALE);
        mainCardContainer.add(salesHistoryViewPanel(), VIEW_HISTORY);
        add(mainCardContainer, BorderLayout.CENTER);

        // 3. Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(6, 24, 6, 24)
        ));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(MUTED);
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel headerPanel() {
        JPanel header = new JPanel(new BorderLayout(24, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        // Brand on Left
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        brand.setOpaque(false);

        BadgeLabel iconBadge = new BadgeLabel("IMS", GREEN, Color.WHITE, null);
        iconBadge.setFont(new Font("SansSerif", Font.BOLD, 13));
        iconBadge.setPreferredSize(new Dimension(42, 42));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("Inventory & Sales Management");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT);

        JPanel subtitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        subtitleRow.setOpaque(false);
        JLabel subtitle = new JLabel("Enterprise Stock Control & Sales Tracking");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        JLabel dot = new JLabel("•");
        dot.setForeground(BORDER);

        JLabel syncBadge = new JLabel("● Live Sync Active");
        syncBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        syncBadge.setForeground(ACCENT);

        subtitleRow.add(subtitle);
        subtitleRow.add(dot);
        subtitleRow.add(syncBadge);

        titleBlock.add(title);
        titleBlock.add(subtitleRow);

        brand.add(iconBadge);
        brand.add(titleBlock);
        header.add(brand, BorderLayout.WEST);

        // Stats on Right
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        rightPanel.add(statCard("TOTAL STOCK VALUE", totalValueLabel, GREEN, PALE_GREEN));
        rightPanel.add(statCard("SALES REVENUE", totalRevenueLabel, ACCENT, new Color(236, 253, 245)));
        rightPanel.add(statCard("UNITS SOLD", totalSoldLabel, new Color(13, 148, 136), new Color(240, 253, 250)));
        rightPanel.add(statCard("LOW STOCK", lowStockCountLabel, WARNING, new Color(255, 251, 235)));

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel navBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(8, 24, 8, 24)
        ));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);

        navInventoryBtn.setActive(true);
        navSaleBtn.setActive(false);
        navHistoryBtn.setActive(false);

        navInventoryBtn.addActionListener(e -> switchView(VIEW_INVENTORY));
        navSaleBtn.addActionListener(e -> switchView(VIEW_SALE));
        navHistoryBtn.addActionListener(e -> switchView(VIEW_HISTORY));

        buttons.add(navInventoryBtn);
        buttons.add(navSaleBtn);
        buttons.add(navHistoryBtn);

        nav.add(buttons, BorderLayout.WEST);
        return nav;
    }

    private void switchView(String viewName) {
        cardLayout.show(mainCardContainer, viewName);
        navInventoryBtn.setActive(VIEW_INVENTORY.equals(viewName));
        navSaleBtn.setActive(VIEW_SALE.equals(viewName));
        navHistoryBtn.setActive(VIEW_HISTORY.equals(viewName));

        if (VIEW_SALE.equals(viewName)) {
            refreshSaleProductsTable();
            status("Sales Point of Sale opened. Choose a product to sell.");
        } else if (VIEW_HISTORY.equals(viewName)) {
            refreshSalesHistoryTable();
            status("Sales History Ledger opened.");
        } else {
            status("Stock Inventory Ledger view.");
        }
        updateDashboardStats();
    }

    private JPanel statCard(String heading, JLabel value, Color color, Color bg) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 2));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        JLabel label = new JLabel(heading);
        label.setFont(new Font("SansSerif", Font.BOLD, 9));
        label.setForeground(MUTED);

        value.setFont(new Font("SansSerif", Font.BOLD, 14));
        value.setForeground(color);

        card.add(label);
        card.add(value);
        return card;
    }

    // =========================================================================
    // VIEW 1: INVENTORY MANAGEMENT PANEL
    // =========================================================================
    private JPanel inventoryViewPanel() {
        JPanel workspace = new JPanel(new BorderLayout(20, 0));
        workspace.setBackground(SURFACE);
        workspace.setBorder(BorderFactory.createEmptyBorder(16, 24, 14, 24));
        workspace.add(ledgerPanel(), BorderLayout.CENTER);
        workspace.add(formPanel(), BorderLayout.EAST);
        return workspace;
    }

    private JPanel ledgerPanel() {
        JPanel ledger = new JPanel(new BorderLayout(0, 14));
        ledger.setBackground(SURFACE);

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(14, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        // Search group
        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchGroup.setOpaque(false);
        searchField.setPreferredSize(new Dimension(280, 36));

        ModernButton searchBtn = new ModernButton("Search", ModernButton.Variant.PRIMARY);
        ModernButton clearSearchBtn = new ModernButton("Clear", ModernButton.Variant.SECONDARY);

        searchBtn.addActionListener(e -> onSearch());
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            if ("lowstock".equals(activeFilter)) {
                loadTableSafe(() -> service.getLowStockProducts(), "Showing low stock products");
            } else {
                loadTableSafe(() -> service.getAllProducts(), "Showing all products");
            }
        });

        searchGroup.add(searchField);
        searchGroup.add(searchBtn);
        searchGroup.add(clearSearchBtn);

        // Filter chips group
        JPanel filterGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterGroup.setOpaque(false);

        showAllBtn.setActive(true);
        showAllBtn.addActionListener(e -> {
            activeFilter = "all";
            showAllBtn.setActive(true);
            lowStockBtn.setActive(false);
            searchField.setText("");
            loadTableSafe(() -> service.getAllProducts(), "Showing all products");
        });

        lowStockBtn.addActionListener(e -> {
            activeFilter = "lowstock";
            showAllBtn.setActive(false);
            lowStockBtn.setActive(true);
            searchField.setText("");
            loadTableSafe(() -> service.getLowStockProducts(), "Showing low stock products");
        });

        exportBtn.addActionListener(e -> onExport());

        filterGroup.add(showAllBtn);
        filterGroup.add(lowStockBtn);
        filterGroup.add(Box.createHorizontalStrut(4));
        filterGroup.add(exportBtn);

        toolbar.add(searchGroup, BorderLayout.WEST);
        toolbar.add(filterGroup, BorderLayout.EAST);

        // Table Ledger Card
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel caption = new JPanel(new BorderLayout());
        caption.setBackground(PALE_GREEN);
        caption.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel ledgerTitle = new JLabel("Stock Inventory Ledger");
        ledgerTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        ledgerTitle.setForeground(TEXT);

        JLabel hint = new JLabel("Click any row to select & edit properties");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hint.setForeground(MUTED);

        caption.add(ledgerTitle, BorderLayout.WEST);
        caption.add(hint, BorderLayout.EAST);
        tableCard.add(caption, BorderLayout.NORTH);

        setupInventoryTable();
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        // Footer
        JPanel tableFooter = new JPanel(new BorderLayout());
        tableFooter.setBackground(Color.WHITE);
        tableFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        JLabel footerNote = new JLabel("Inventory database backed by SQLite");
        footerNote.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footerNote.setForeground(MUTED);

        rowCountLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        rowCountLabel.setForeground(TEXT);

        tableFooter.add(footerNote, BorderLayout.WEST);
        tableFooter.add(rowCountLabel, BorderLayout.EAST);
        tableCard.add(tableFooter, BorderLayout.SOUTH);

        ledger.add(toolbar, BorderLayout.NORTH);
        ledger.add(tableCard, BorderLayout.CENTER);
        return ledger;
    }

    private void setupInventoryTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(36);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(220, 252, 231));
        table.setSelectionForeground(GREEN);

        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(PALE_GREEN);
        table.getTableHeader().setForeground(GREEN);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        int[] widths = {55, 180, 110, 85, 75, 80, 100, 100, 110};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new CategoryBadgeRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new CurrencyRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new QuantityBadgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new RightRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new ExpiryRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new TypeBadgeRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new CurrencyRenderer());

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });
    }

    private JPanel formPanel() {
        JPanel formCard = new JPanel(new BorderLayout(0, 12));
        formCard.setBackground(Color.WHITE);
        formCard.setPreferredSize(new Dimension(360, 0));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Heading
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        textBlock.setOpaque(false);
        formHeading.setFont(new Font("SansSerif", Font.BOLD, 17));
        formHeading.setForeground(TEXT);

        formSubheading.setFont(new Font("SansSerif", Font.PLAIN, 12));
        formSubheading.setForeground(MUTED);

        textBlock.add(formHeading);
        textBlock.add(formSubheading);

        heading.add(textBlock, BorderLayout.WEST);
        heading.add(formModeBadge, BorderLayout.EAST);
        heading.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(0, 0, 14, 0)
        ));
        formCard.add(heading, BorderLayout.NORTH);

        // Fields
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 12, 0);

        int row = 0;

        // 1. Item Name
        gbc.gridy = row++;
        fieldsPanel.add(fieldContainer("Item Name *", "SKU title", nameField), gbc);

        // 2. Category Dropdown
        styleComboBox(categoryBox);
        gbc.gridy = row++;
        fieldsPanel.add(fieldContainer("Category *", "Classification", categoryBox), gbc);

        // 3. Price & Quantity (2 columns)
        JPanel priceQtyRow = new JPanel(new GridLayout(1, 2, 10, 0));
        priceQtyRow.setOpaque(false);
        priceQtyRow.add(fieldContainer("Price (Rs) *", null, priceField));
        priceQtyRow.add(fieldContainer("Quantity *", null, quantityField));
        gbc.gridy = row++;
        fieldsPanel.add(priceQtyRow, gbc);

        // 4. Reorder Level & Type (2 columns)
        styleComboBox(typeBox);
        JPanel reorderTypeRow = new JPanel(new GridLayout(1, 2, 10, 0));
        reorderTypeRow.setOpaque(false);
        reorderTypeRow.add(fieldContainer("Reorder Level *", null, reorderField));
        reorderTypeRow.add(fieldContainer("Inventory Type", null, typeBox));
        gbc.gridy = row++;
        fieldsPanel.add(reorderTypeRow, gbc);

        // 5. Expiry Date
        gbc.gridy = row++;
        fieldsPanel.add(fieldContainer("Expiry (optional)", "Required if perishable", expiryField), gbc);

        // 6. Calculated Preview Banner
        JPanel previewBanner = new JPanel(new BorderLayout());
        previewBanner.setBackground(PALE_GREEN);
        previewBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 247, 208)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        JLabel previewTitle = new JLabel("Calculated Stock Value:");
        previewTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        previewTitle.setForeground(MUTED);

        previewValueLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        previewValueLabel.setForeground(GREEN);

        previewBanner.add(previewTitle, BorderLayout.WEST);
        previewBanner.add(previewValueLabel, BorderLayout.EAST);
        gbc.gridy = row++;
        fieldsPanel.add(previewBanner, gbc);

        // 7. Action Buttons (2x2 grid)
        JPanel actions = new JPanel(new GridLayout(2, 2, 8, 8));
        actions.setOpaque(false);
        actions.add(addButton);
        actions.add(updateButton);
        actions.add(deleteButton);
        actions.add(clearButton);
        gbc.gridy = row++;
        gbc.insets = new Insets(6, 0, 12, 0);
        fieldsPanel.add(actions, gbc);

        // 8. Vertical Glue
        gbc.gridy = row++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        fieldsPanel.add(Box.createGlue(), gbc);

        formCard.add(fieldsPanel, BorderLayout.CENTER);
        return formCard;
    }

    // =========================================================================
    // VIEW 2: NEW SALES PAGE (RECORD SALE & DEDUCT/REMOVE FROM DATABASE)
    // =========================================================================
    private JPanel recordSaleViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 14, 24));

        // LEFT: Products Browser to select item to sell
        JPanel productBrowserCard = new JPanel(new BorderLayout(0, 12));
        productBrowserCard.setBackground(Color.WHITE);
        productBrowserCard.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PALE_GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Available Inventory for Sale");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Click any product row to populate sales terminal");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        productBrowserCard.add(header, BorderLayout.NORTH);

        // Search Bar for sales products
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(Color.WHITE);
        searchBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        saleProductSearchField.setPreferredSize(new Dimension(300, 36));
        saleProductSearchField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() { filterSaleProductsTable(); }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        searchBar.add(new JLabel("Quick Search: "), BorderLayout.WEST);
        searchBar.add(saleProductSearchField, BorderLayout.CENTER);
        productBrowserCard.add(searchBar, BorderLayout.CENTER);

        // Table
        setupSaleProductTable();
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.add(searchBar, BorderLayout.NORTH);
        tableWrap.add(new JScrollPane(saleProductTable), BorderLayout.CENTER);
        productBrowserCard.add(tableWrap, BorderLayout.CENTER);

        // RIGHT: Sale Checkout Terminal Form
        JPanel checkoutCard = new JPanel(new BorderLayout(0, 12));
        checkoutCard.setBackground(Color.WHITE);
        checkoutCard.setPreferredSize(new Dimension(420, 0));
        checkoutCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Checkout Header
        JPanel checkHeader = new JPanel(new BorderLayout());
        checkHeader.setOpaque(false);
        checkHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(0, 0, 14, 0)
        ));

        JPanel checkTitleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        checkTitleBlock.setOpaque(false);
        JLabel cTitle = new JLabel("Sales Terminal");
        cTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        cTitle.setForeground(TEXT);

        JLabel cSub = new JLabel("Record transaction & update inventory stock");
        cSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cSub.setForeground(MUTED);

        checkTitleBlock.add(cTitle);
        checkTitleBlock.add(cSub);

        BadgeLabel termBadge = new BadgeLabel("POS Active", ACCENT, Color.WHITE, null);

        checkHeader.add(checkTitleBlock, BorderLayout.WEST);
        checkHeader.add(termBadge, BorderLayout.EAST);
        checkoutCard.add(checkHeader, BorderLayout.NORTH);

        // Form Fields
        JPanel cFields = new JPanel(new GridBagLayout());
        cFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 12, 0);

        int r = 0;

        // Selected Product Card
        JPanel selCard = new JPanel(new BorderLayout(8, 4));
        selCard.setBackground(PALE_GREEN);
        selCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 247, 208)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        saleSelectedProductName.setFont(new Font("SansSerif", Font.BOLD, 14));
        saleSelectedProductName.setForeground(TEXT);

        JPanel badgesRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badgesRow.setOpaque(false);
        badgesRow.add(saleSelectedCategoryBadge);
        badgesRow.add(saleStockBadge);

        selCard.add(saleSelectedProductName, BorderLayout.WEST);
        selCard.add(badgesRow, BorderLayout.EAST);

        gbc.gridy = r++;
        cFields.add(fieldContainer("Selected Product", null, selCard), gbc);

        // Quantity & Unit Price Grid
        JPanel qtyPriceGrid = new JPanel(new GridLayout(1, 2, 10, 0));
        qtyPriceGrid.setOpaque(false);
        qtyPriceGrid.add(fieldContainer("Quantity to Sell *", "Units", saleQtyField));
        qtyPriceGrid.add(fieldContainer("Unit Selling Price (Rs) *", "Unit Rate", saleUnitPriceField));

        gbc.gridy = r++;
        cFields.add(qtyPriceGrid, gbc);

        // Customer / Notes
        gbc.gridy = r++;
        cFields.add(fieldContainer("Customer / Invoice Notes", "Optional reference", saleNotesField), gbc);

        // Database Update Options
        JPanel optionsPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        saleDeductStockCheck.setFont(new Font("SansSerif", Font.BOLD, 12));
        saleDeductStockCheck.setForeground(TEXT);
        saleDeductStockCheck.setOpaque(false);

        saleRemoveIfZeroCheck.setFont(new Font("SansSerif", Font.PLAIN, 12));
        saleRemoveIfZeroCheck.setForeground(MUTED);
        saleRemoveIfZeroCheck.setOpaque(false);

        optionsPanel.add(saleDeductStockCheck);
        optionsPanel.add(saleRemoveIfZeroCheck);

        gbc.gridy = r++;
        cFields.add(optionsPanel, gbc);

        // Total Sale Amount Preview Banner
        JPanel salePreview = new JPanel(new BorderLayout());
        salePreview.setBackground(new Color(236, 253, 245));
        salePreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(167, 243, 208)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JPanel saleLeft = new JPanel(new GridLayout(2, 1, 0, 2));
        saleLeft.setOpaque(false);
        JLabel sLabel = new JLabel("TOTAL SALE AMOUNT:");
        sLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        sLabel.setForeground(MUTED);

        saleTotalAmountLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        saleTotalAmountLabel.setForeground(GREEN);

        saleLeft.add(sLabel);
        saleLeft.add(saleTotalAmountLabel);

        saleRemainingStockLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        saleRemainingStockLabel.setForeground(MUTED);

        salePreview.add(saleLeft, BorderLayout.WEST);
        salePreview.add(saleRemainingStockLabel, BorderLayout.EAST);

        gbc.gridy = r++;
        cFields.add(salePreview, gbc);

        // Action Buttons
        JPanel saleBtns = new JPanel(new GridLayout(2, 1, 0, 8));
        saleBtns.setOpaque(false);
        completeSaleBtn.setPreferredSize(new Dimension(completeSaleBtn.getPreferredSize().width, 42));
        saleBtns.add(completeSaleBtn);
        saleBtns.add(clearSaleBtn);

        gbc.gridy = r++;
        gbc.insets = new Insets(8, 0, 12, 0);
        cFields.add(saleBtns, gbc);

        // Glue
        gbc.gridy = r++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        cFields.add(Box.createGlue(), gbc);

        checkoutCard.add(cFields, BorderLayout.CENTER);

        panel.add(productBrowserCard, BorderLayout.CENTER);
        panel.add(checkoutCard, BorderLayout.EAST);
        return panel;
    }

    private void setupSaleProductTable() {
        saleProductTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        saleProductTable.setRowHeight(36);
        saleProductTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        saleProductTable.setForeground(TEXT);
        saleProductTable.setGridColor(BORDER);
        saleProductTable.setShowVerticalLines(false);
        saleProductTable.setFillsViewportHeight(true);
        saleProductTable.setSelectionBackground(new Color(220, 252, 231));
        saleProductTable.setSelectionForeground(GREEN);

        saleProductTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        saleProductTable.getTableHeader().setBackground(PALE_GREEN);
        saleProductTable.getTableHeader().setForeground(GREEN);
        saleProductTable.getTableHeader().setPreferredSize(new Dimension(0, 38));

        int[] widths = {55, 200, 110, 95, 110, 95};
        for (int i = 0; i < widths.length && i < saleProductTable.getColumnCount(); i++) {
            saleProductTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        saleProductTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());
        saleProductTable.getColumnModel().getColumn(2).setCellRenderer(new CategoryBadgeRenderer());
        saleProductTable.getColumnModel().getColumn(3).setCellRenderer(new CurrencyRenderer());
        saleProductTable.getColumnModel().getColumn(4).setCellRenderer(new CenterRenderer());
        saleProductTable.getColumnModel().getColumn(5).setCellRenderer(new StockStatusRenderer());

        saleProductTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                selectProductForSaleFromRow();
            }
        });
    }

    private void selectProductForSaleFromRow() {
        int row = saleProductTable.getSelectedRow();
        if (row < 0) return;

        int id = Integer.parseInt(String.valueOf(saleProductTableModel.getValueAt(row, 0)));
        try {
            Product p = service.searchById(id);
            if (p != null) {
                selectedProductForSale = p;
                saleSelectedProductName.setText("#" + p.getId() + " " + p.getName());
                saleSelectedCategoryBadge.setText(p.getCategory());
                saleStockBadge.setText("Stock: " + p.getQuantity());

                if (p.getQuantity() <= 0) {
                    saleStockBadge.setBadgeColors(new Color(254, 226, 226), new Color(220, 38, 38), new Color(252, 165, 165));
                } else if (p.isLowStock()) {
                    saleStockBadge.setBadgeColors(new Color(254, 243, 199), WARNING, new Color(252, 211, 77));
                } else {
                    saleStockBadge.setBadgeColors(new Color(236, 253, 245), ACCENT, new Color(167, 243, 208));
                }

                saleUnitPriceField.setText(String.format("%.2f", p.getPrice()));
                saleQtyField.setText("1");
                updateSaleCalculation();
            }
        } catch (InventoryException ex) {
            showError("Could not load product: " + ex.getMessage());
        }
    }

    private void updateSaleCalculation() {
        if (selectedProductForSale == null) {
            saleTotalAmountLabel.setText("Rs 0.00");
            saleRemainingStockLabel.setText("Remaining Stock: -");
            return;
        }

        try {
            int qty = Integer.parseInt(saleQtyField.getText().trim());
            double price = Double.parseDouble(saleUnitPriceField.getText().trim());

            if (qty <= 0 || price < 0) {
                saleTotalAmountLabel.setText("Rs 0.00");
                return;
            }

            double total = qty * price;
            saleTotalAmountLabel.setText(String.format("Rs %.2f", total));

            int remaining = selectedProductForSale.getQuantity() - qty;
            if (remaining < 0) {
                saleRemainingStockLabel.setText("⚠️ EXCEEDS STOCK by " + Math.abs(remaining));
                saleRemainingStockLabel.setForeground(new Color(220, 38, 38));
            } else {
                saleRemainingStockLabel.setText("Remaining: " + remaining + " units");
                saleRemainingStockLabel.setForeground(remaining <= selectedProductForSale.getReorderLevel() ? WARNING : GREEN);
            }
        } catch (Exception ex) {
            saleTotalAmountLabel.setText("Rs 0.00");
            saleRemainingStockLabel.setText("Remaining: -");
        }
    }

    private void onCompleteSale() {
        if (selectedProductForSale == null) {
            showError("Please select a product from the inventory list first.");
            return;
        }

        try {
            int qty = readInt(saleQtyField, "Quantity to sell");
            double unitPrice = readDouble(saleUnitPriceField, "Unit selling price");
            String notes = saleNotesField.getText().trim();
            boolean removeIfZero = saleRemoveIfZeroCheck.isSelected();

            if (qty <= 0) {
                showError("Quantity to sell must be at least 1.");
                return;
            }

            if (qty > selectedProductForSale.getQuantity()) {
                showError("Insufficient stock! Available: " + selectedProductForSale.getQuantity()
                        + ", requested: " + qty);
                return;
            }

            // Confirm
            int choice = JOptionPane.showConfirmDialog(this,
                    "Complete sale of " + qty + "x \"" + selectedProductForSale.getName()
                            + "\" for Rs " + String.format("%.2f", (qty * unitPrice)) + "?\n\n"
                            + "This will immediately deduct " + qty + " units from the database."
                            + (removeIfZero && (selectedProductForSale.getQuantity() - qty == 0) ? "\n(Item will be removed from catalog because stock reaches 0)" : ""),
                    "Confirm Sale & Deduct Stock",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            Sale sale = service.recordSale(selectedProductForSale.getId(), qty, unitPrice, notes, removeIfZero);

            JOptionPane.showMessageDialog(this,
                    "Sale Completed Successfully!\n\n"
                            + "Sale Transaction ID: #" + sale.getId() + "\n"
                            + "Product: " + sale.getProductName() + "\n"
                            + "Quantity Sold: " + sale.getQuantity() + "\n"
                            + "Unit Price: Rs " + String.format("%.2f", sale.getUnitPrice()) + "\n"
                            + "Total Amount: Rs " + String.format("%.2f", sale.getTotalAmount()) + "\n"
                            + "Date: " + sale.getSaleDate() + "\n\n"
                            + "Inventory database has been updated.",
                    "Sale Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);

            clearSaleForm();
            refreshAllData();
            status("Sale #" + sale.getId() + " recorded: " + sale.getProductName() + " (Rs " + String.format("%.2f", sale.getTotalAmount()) + ")");
        } catch (InventoryException | NumberFormatException ex) {
            showError(ex.getMessage());
        }
    }

    private void clearSaleForm() {
        selectedProductForSale = null;
        saleSelectedProductName.setText("No product selected");
        saleSelectedCategoryBadge.setText("None");
        saleStockBadge.setText("Stock: -");
        saleStockBadge.setBadgeColors(new Color(241, 245, 249), new Color(100, 116, 139), BORDER);

        saleQtyField.setText("1");
        saleUnitPriceField.setText("0.00");
        saleNotesField.setText("");
        saleTotalAmountLabel.setText("Rs 0.00");
        saleRemainingStockLabel.setText("Remaining Stock: -");
        saleProductTable.clearSelection();
    }

    private void refreshSaleProductsTable() {
        try {
            List<Product> products = service.getAllProducts();
            saleProductTableModel.setRowCount(0);
            for (Product p : products) {
                String status = p.getQuantity() <= 0 ? "Out of Stock" : (p.isLowStock() ? "Low Stock" : "In Stock");
                saleProductTableModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        p.getCategory(),
                        String.format("%.2f", p.getPrice()),
                        p.getQuantity(),
                        status
                });
            }
        } catch (InventoryException ex) {
            showError("Could not load products for sale: " + ex.getMessage());
        }
    }

    private void filterSaleProductsTable() {
        String q = saleProductSearchField.getText().trim().toLowerCase();
        try {
            List<Product> products = service.getAllProducts();
            saleProductTableModel.setRowCount(0);
            for (Product p : products) {
                if (q.isEmpty() || String.valueOf(p.getId()).equals(q)
                        || p.getName().toLowerCase().contains(q)
                        || p.getCategory().toLowerCase().contains(q)) {
                    String status = p.getQuantity() <= 0 ? "Out of Stock" : (p.isLowStock() ? "Low Stock" : "In Stock");
                    saleProductTableModel.addRow(new Object[]{
                            p.getId(),
                            p.getName(),
                            p.getCategory(),
                            String.format("%.2f", p.getPrice()),
                            p.getQuantity(),
                            status
                    });
                }
            }
        } catch (InventoryException ex) {
            // ignore
        }
    }

    // =========================================================================
    // VIEW 3: SALES HISTORY PANEL
    // =========================================================================
    private JPanel salesHistoryViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 24, 14, 24));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(14, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        // Search group
        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchGroup.setOpaque(false);
        salesHistorySearchField.setPreferredSize(new Dimension(300, 36));

        ModernButton sSearchBtn = new ModernButton("Search", ModernButton.Variant.PRIMARY);
        ModernButton sClearBtn = new ModernButton("Clear", ModernButton.Variant.SECONDARY);

        sSearchBtn.addActionListener(e -> filterSalesHistory());
        sClearBtn.addActionListener(e -> {
            salesHistorySearchField.setText("");
            refreshSalesHistoryTable();
        });

        searchGroup.add(salesHistorySearchField);
        searchGroup.add(sSearchBtn);
        searchGroup.add(sClearBtn);

        // Actions on right
        JPanel actionsGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsGroup.setOpaque(false);

        exportSalesBtn.addActionListener(e -> onExportSales());
        refreshSalesBtn.addActionListener(e -> refreshSalesHistoryTable());

        actionsGroup.add(exportSalesBtn);
        actionsGroup.add(refreshSalesBtn);

        toolbar.add(searchGroup, BorderLayout.WEST);
        toolbar.add(actionsGroup, BorderLayout.EAST);

        // Table Card
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel caption = new JPanel(new BorderLayout());
        caption.setBackground(PALE_GREEN);
        caption.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Completed Sales Transactions Ledger");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(TEXT);

        JLabel hint = new JLabel("Audit trail of all checkout sales and database stock deductions");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hint.setForeground(MUTED);

        caption.add(title, BorderLayout.WEST);
        caption.add(hint, BorderLayout.EAST);
        tableCard.add(caption, BorderLayout.NORTH);

        setupSalesHistoryTable();
        tableCard.add(new JScrollPane(salesHistoryTable), BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        salesHistorySummaryLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        salesHistorySummaryLabel.setForeground(TEXT);

        JLabel exportNote = new JLabel("Export supports CSV spreadsheet and formatted text reports");
        exportNote.setFont(new Font("SansSerif", Font.PLAIN, 12));
        exportNote.setForeground(MUTED);

        footer.add(exportNote, BorderLayout.WEST);
        footer.add(salesHistorySummaryLabel, BorderLayout.EAST);
        tableCard.add(footer, BorderLayout.SOUTH);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private void setupSalesHistoryTable() {
        salesHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        salesHistoryTable.setRowHeight(36);
        salesHistoryTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        salesHistoryTable.setForeground(TEXT);
        salesHistoryTable.setGridColor(BORDER);
        salesHistoryTable.setShowVerticalLines(false);
        salesHistoryTable.setFillsViewportHeight(true);
        salesHistoryTable.setSelectionBackground(new Color(220, 252, 231));
        salesHistoryTable.setSelectionForeground(GREEN);

        salesHistoryTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        salesHistoryTable.getTableHeader().setBackground(PALE_GREEN);
        salesHistoryTable.getTableHeader().setForeground(GREEN);
        salesHistoryTable.getTableHeader().setPreferredSize(new Dimension(0, 38));

        int[] widths = {70, 150, 180, 110, 80, 100, 120, 180};
        for (int i = 0; i < widths.length && i < salesHistoryTable.getColumnCount(); i++) {
            salesHistoryTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        salesHistoryTable.getColumnModel().getColumn(0).setCellRenderer(new CenterRenderer());
        salesHistoryTable.getColumnModel().getColumn(1).setCellRenderer(new CenterRenderer());
        salesHistoryTable.getColumnModel().getColumn(3).setCellRenderer(new CategoryBadgeRenderer());
        salesHistoryTable.getColumnModel().getColumn(4).setCellRenderer(new QuantityBadgeRenderer());
        salesHistoryTable.getColumnModel().getColumn(5).setCellRenderer(new CurrencyRenderer());
        salesHistoryTable.getColumnModel().getColumn(6).setCellRenderer(new CurrencyRenderer());
    }

    private void refreshSalesHistoryTable() {
        try {
            List<Sale> sales = service.getAllSales();
            populateSalesTable(sales);
        } catch (InventoryException ex) {
            showError("Could not load sales history: " + ex.getMessage());
        }
    }

    private void filterSalesHistory() {
        String q = salesHistorySearchField.getText().trim();
        try {
            List<Sale> sales = service.searchSales(q);
            populateSalesTable(sales);
            status("Found " + sales.size() + " sales matching \"" + q + "\"");
        } catch (InventoryException ex) {
            showError("Search failed: " + ex.getMessage());
        }
    }

    private void populateSalesTable(List<Sale> sales) {
        salesHistoryTableModel.setRowCount(0);
        double rev = 0;
        int units = 0;
        for (Sale s : sales) {
            rev += s.getTotalAmount();
            units += s.getQuantity();
            salesHistoryTableModel.addRow(new Object[]{
                    "#" + s.getId(),
                    s.getSaleDate(),
                    s.getProductName(),
                    s.getCategory() == null ? "General" : s.getCategory(),
                    s.getQuantity(),
                    String.format("%.2f", s.getUnitPrice()),
                    String.format("%.2f", s.getTotalAmount()),
                    s.getNotes()
            });
        }
        salesHistorySummaryLabel.setText(String.format("Showing %d orders | Total Units: %d | Revenue: Rs %.2f",
                sales.size(), units, rev));
    }

    private void onExportSales() {
        try {
            List<String> paths = service.exportSalesReport();
            String message = String.join("\n", paths);
            status("Sales reports exported successfully");
            JOptionPane.showMessageDialog(this,
                    "Sales history reports generated successfully:\n\n" + message,
                    "Export Sales Report",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (InventoryException ex) {
            showError(ex.getMessage());
        }
    }

    // =========================================================================
    // GENERAL HELPERS & DATA BINDING
    // =========================================================================
    private void refreshAllData() {
        loadTableSafe(() -> service.getAllProducts(), "System ready. Loaded all inventory items.");
        refreshSaleProductsTable();
        refreshSalesHistoryTable();
        updateDashboardStats();
    }

    private JPanel fieldContainer(String labelText, String hintText, Component field) {
        JPanel block = new JPanel(new BorderLayout(0, 4));
        block.setOpaque(false);

        JPanel labelRow = new JPanel(new BorderLayout());
        labelRow.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT);
        labelRow.add(label, BorderLayout.WEST);

        if (hintText != null && !hintText.isEmpty()) {
            JLabel hint = new JLabel(hintText);
            hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
            hint.setForeground(MUTED);
            labelRow.add(hint, BorderLayout.EAST);
        }

        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        block.add(labelRow, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        return block;
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setFont(new Font("SansSerif", Font.PLAIN, 13));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, 36));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    private void setupEvents() {
        // Inventory Form buttons
        addButton.addActionListener(e -> onAdd());
        updateButton.addActionListener(e -> onUpdate());
        deleteButton.addActionListener(e -> onDelete());
        clearButton.addActionListener(e -> clearForm());

        // Dynamic stock value preview calculation
        DocumentListener calcListener = new DocumentListener() {
            private void update() { updatePreviewCalculation(); }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        };
        priceField.getDocument().addDocumentListener(calcListener);
        quantityField.getDocument().addDocumentListener(calcListener);

        // Sales Form Dynamic calculation
        DocumentListener saleCalcListener = new DocumentListener() {
            private void update() { updateSaleCalculation(); }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        };
        saleQtyField.getDocument().addDocumentListener(saleCalcListener);
        saleUnitPriceField.getDocument().addDocumentListener(saleCalcListener);

        completeSaleBtn.addActionListener(e -> onCompleteSale());
        clearSaleBtn.addActionListener(e -> clearSaleForm());

        // Live search on keystroke
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void refresh() { updateLiveSearch(); }
            @Override public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh(); }
        });

        // Type toggle updates placeholder/expiry requirement
        typeBox.addActionListener(e -> {
            boolean isPerishable = "Perishable".equals(typeBox.getSelectedItem());
            if (isPerishable && expiryField.getText().isEmpty()) {
                expiryField.setPlaceholder("Required: YYYY-MM-DD");
            } else {
                expiryField.setPlaceholder("YYYY-MM-DD");
            }
        });
    }

    private void updatePreviewCalculation() {
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            int qty = Integer.parseInt(quantityField.getText().trim());
            previewValueLabel.setText(String.format("Rs %.2f", price * qty));
        } catch (Exception ex) {
            previewValueLabel.setText("Rs 0.00");
        }
    }

    private void setFormMode(boolean editMode) {
        if (editMode) {
            formHeading.setText("Edit Selected Item");
            formSubheading.setText("Update or delete this inventory record");
            formModeBadge.setText("Edit Mode");
            formModeBadge.setBadgeColors(GREEN, Color.WHITE, null);

            addButton.setEnabled(false);
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
        } else {
            formHeading.setText("Item Management");
            formSubheading.setText("Add new stock or edit the selected item");
            formModeBadge.setText("Create Mode");
            formModeBadge.setBadgeColors(ACCENT, Color.WHITE, null);

            addButton.setEnabled(true);
            updateButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }

    private void fillFormFromSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            setFormMode(false);
            return;
        }

        setFormMode(true);
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        categoryBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 2)));
        priceField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        quantityField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        reorderField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        expiryField.setText(String.valueOf(tableModel.getValueAt(row, 6)));
        typeBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 7)));

        updatePreviewCalculation();
    }

    private void clearForm() {
        nameField.setText("");
        priceField.setText("");
        quantityField.setText("");
        reorderField.setText("");
        expiryField.setText("");
        categoryBox.setSelectedIndex(0);
        typeBox.setSelectedIndex(0);
        previewValueLabel.setText("Rs 0.00");
        table.clearSelection();
        setFormMode(false);
    }

    private void onAdd() {
        try {
            double price = readDouble(priceField, "Price");
            int quantity = readInt(quantityField, "Quantity");
            int reorder = readInt(reorderField, "Reorder level");
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                throw new InventoryException("Product name is required.");
            }
            String category = String.valueOf(categoryBox.getSelectedItem());
            String expiry = expiryField.getText().trim();
            String type = String.valueOf(typeBox.getSelectedItem());

            if ("Perishable".equals(type) && expiry.isEmpty()) {
                throw new InventoryException("Expiry date (YYYY-MM-DD) is required for perishable items.");
            }

            service.addProduct(name, category, price, quantity, reorder, expiry);
            clearForm();
            refreshAllData();
            status("Product added successfully: " + name);
        } catch (InventoryException | NumberFormatException ex) {
            showError(ex.getMessage());
        }
    }

    private void onUpdate() {
        int id = selectedId();
        if (id < 0) {
            showError("Select a product from the table first.");
            return;
        }
        try {
            double price = readDouble(priceField, "Price");
            int quantity = readInt(quantityField, "Quantity");
            int reorder = readInt(reorderField, "Reorder level");
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                throw new InventoryException("Product name is required.");
            }
            String category = String.valueOf(categoryBox.getSelectedItem());
            String expiry = expiryField.getText().trim();
            String type = String.valueOf(typeBox.getSelectedItem());

            if ("Perishable".equals(type) && expiry.isEmpty()) {
                throw new InventoryException("Expiry date (YYYY-MM-DD) is required for perishable items.");
            }

            service.updateProduct(id, name, category, price, quantity, reorder, expiry);
            clearForm();
            refreshAllData();
            status("Product updated successfully (ID: " + id + ")");
        } catch (InventoryException | NumberFormatException ex) {
            showError(ex.getMessage());
        }
    }

    private void onDelete() {
        int id = selectedId();
        if (id < 0) {
            showError("Select a product from the table first.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete product ID #" + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.deleteProduct(id);
            clearForm();
            refreshAllData();
            status("Deleted product ID #" + id);
        } catch (InventoryException ex) {
            showError(ex.getMessage());
        }
    }

    private void onSearch() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            loadTableSafe(() -> service.getAllProducts(), "Showing all products");
            return;
        }
        if (text.matches("\\d+")) {
            try {
                Product product = service.searchById(Integer.parseInt(text));
                tableModel.setRowCount(0);
                if (product == null) {
                    status("No product found with ID " + text);
                    rowCountLabel.setText("Showing 0 entries");
                } else {
                    addRow(product);
                    status("Found product ID #" + text);
                    rowCountLabel.setText("Showing 1 of 1 entries");
                }
            } catch (InventoryException ex) {
                showError(ex.getMessage());
            }
            return;
        }
        loadTableSafe(() -> service.searchByName(text), "Search results for \"" + text + "\"");
    }

    private void updateLiveSearch() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            if ("lowstock".equals(activeFilter)) {
                loadTableSafe(() -> service.getLowStockProducts(), "Showing low stock products");
            } else {
                loadTableSafe(() -> service.getAllProducts(), "Showing all products");
            }
            return;
        }
        if (text.matches("\\d+")) {
            try {
                Product p = service.searchById(Integer.parseInt(text));
                List<Product> list = (p != null) ? List.of(p) : List.of();
                showProducts(list);
                status((p != null) ? "Matched product ID #" + text : "No product with ID " + text);
            } catch (Exception ex) {
                // Ignore
            }
            return;
        }
        loadTableSafe(() -> service.searchByName(text), "Search results for \"" + text + "\"");
    }

    private void onExport() {
        try {
            List<String> paths = service.exportReports();
            String message = String.join("\n", paths);
            status("Reports exported successfully to disk");
            JOptionPane.showMessageDialog(this,
                    "Reports generated successfully:\n\n" + message,
                    "Export Completed",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (InventoryException ex) {
            showError(ex.getMessage());
        }
    }

    private int selectedId() {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        return Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 0)));
    }

    private void loadTableSafe(ProductLoader loader, String message) {
        try {
            List<Product> products = loader.load();
            showProducts(products);
            status(message);
        } catch (InventoryException ex) {
            showError(ex.getMessage());
        }
    }

    private void showProducts(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product product : products) {
            addRow(product);
        }
        rowCountLabel.setText("Showing " + products.size() + " entries");
        updateDashboardStats();
    }

    private void updateDashboardStats() {
        try {
            List<Product> all = service.getAllProducts();
            double totalValue = 0;
            int lowStock = 0;
            for (Product p : all) {
                totalValue += p.getStockValue();
                if (p.isLowStock()) lowStock++;
            }
            totalValueLabel.setText(String.format("Rs %.2f", totalValue));
            lowStockCountLabel.setText(lowStock + " Alerts");

            showAllBtn.setCountBadge(all.size());
            lowStockBtn.setCountBadge(lowStock);

            double salesRev = service.getTotalSalesRevenue();
            int itemsSold = service.getTotalItemsSold();
            totalRevenueLabel.setText(String.format("Rs %.2f", salesRev));
            totalSoldLabel.setText(itemsSold + " Units");
        } catch (InventoryException ex) {
            totalValueLabel.setText("Rs 0.00");
            totalRevenueLabel.setText("Rs 0.00");
            totalSoldLabel.setText("0 Units");
            lowStockCountLabel.setText("0 Alerts");
        }
    }

    private void addRow(Product product) {
        tableModel.addRow(new Object[]{
                product.getId(),
                product.getName(),
                product.getCategory(),
                String.format("%.2f", product.getPrice()),
                product.getQuantity(),
                product.getReorderLevel(),
                product.getExpiryDate().isEmpty() ? "None" : product.getExpiryDate(),
                product.getType(),
                String.format("%.2f", product.getStockValue())
        });
    }

    private double readDouble(JTextField field, String label) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException ex) {
            throw new NumberFormatException(label + " must be a valid number.");
        }
    }

    private int readInt(JTextField field, String label) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException ex) {
            throw new NumberFormatException(label + " must be a whole number.");
        }
    }

    private void showError(String message) {
        status(message);
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    private void status(String message) {
        statusLabel.setText(" " + message);
    }

    @FunctionalInterface
    private interface ProductLoader {
        List<Product> load() throws InventoryException;
    }

    // --- Custom Table Renderers ---

    private static class CenterRenderer extends DefaultTableCellRenderer {
        public CenterRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    private static class RightRenderer extends DefaultTableCellRenderer {
        public RightRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        }
    }

    private static class CurrencyRenderer extends DefaultTableCellRenderer {
        public CurrencyRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
            setFont(new Font("Monospaced", Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText("Rs " + value);
            return this;
        }
    }

    private static class ExpiryRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String text = String.valueOf(value);
            if ("None".equals(text)) {
                setForeground(MUTED);
            } else {
                setForeground(TEXT);
            }
            return this;
        }
    }

    private static class CategoryBadgeRenderer extends DefaultTableCellRenderer {
        private final BadgeLabel badge = new BadgeLabel("");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            badge.setText(String.valueOf(value));
            if (isSelected) {
                badge.setBadgeColors(GREEN, Color.WHITE, null);
            } else {
                badge.setBadgeColors(new Color(241, 245, 249), new Color(51, 65, 85), BORDER);
            }
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            wrapper.setOpaque(true);
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            wrapper.add(badge);
            return wrapper;
        }
    }

    private static class TypeBadgeRenderer extends DefaultTableCellRenderer {
        private final BadgeLabel badge = new BadgeLabel("");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String type = String.valueOf(value);
            badge.setText(type);
            if ("Perishable".equalsIgnoreCase(type)) {
                badge.setBadgeColors(new Color(236, 253, 245), ACCENT, new Color(167, 243, 208));
            } else {
                badge.setBadgeColors(new Color(241, 245, 249), MUTED, BORDER);
            }
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            wrapper.setOpaque(true);
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            wrapper.add(badge);
            return wrapper;
        }
    }

    private static class QuantityBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

            try {
                int qty = Integer.parseInt(String.valueOf(value));
                if (table.getColumnCount() >= 6) {
                    int reorder = Integer.parseInt(String.valueOf(table.getModel().getValueAt(row, 5)));
                    if (qty <= reorder) {
                        setForeground(WARNING);
                        setFont(new Font("SansSerif", Font.BOLD, 13));
                    } else {
                        setForeground(TEXT);
                        setFont(new Font("SansSerif", Font.PLAIN, 13));
                    }
                } else {
                    setForeground(TEXT);
                    setFont(new Font("SansSerif", Font.PLAIN, 13));
                }
            } catch (Exception ex) {
                setForeground(TEXT);
            }
            return this;
        }
    }

    private static class StockStatusRenderer extends DefaultTableCellRenderer {
        private final BadgeLabel badge = new BadgeLabel("");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = String.valueOf(value);
            badge.setText(status);
            if ("Out of Stock".equalsIgnoreCase(status)) {
                badge.setBadgeColors(new Color(254, 226, 226), new Color(220, 38, 38), new Color(252, 165, 165));
            } else if ("Low Stock".equalsIgnoreCase(status)) {
                badge.setBadgeColors(new Color(254, 243, 199), WARNING, new Color(252, 211, 77));
            } else {
                badge.setBadgeColors(new Color(236, 253, 245), ACCENT, new Color(167, 243, 208));
            }
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
            wrapper.setOpaque(true);
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            wrapper.add(badge);
            return wrapper;
        }
    }
}
