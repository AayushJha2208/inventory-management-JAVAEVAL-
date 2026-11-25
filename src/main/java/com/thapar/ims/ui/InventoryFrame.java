package com.thapar.ims.ui;

import com.thapar.ims.exception.InventoryException;
import com.thapar.ims.model.Product;
import com.thapar.ims.service.InventoryService;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Inventory Management System window.
 * Balanced layout inspired by the enterprise Tailwind web dashboard.
 */
public class InventoryFrame extends JFrame {

    private final InventoryService service;

    // Form inputs (fixed height, modern placeholders)
    private final PlaceholderTextField nameField = new PlaceholderTextField("e.g. Wireless Mouse");
    private final JComboBox<String> categoryBox = new JComboBox<>(
            new String[]{"Stationery", "Grocery", "Electronics", "Hardware", "Office Supplies", "Other"});
    private final PlaceholderTextField priceField = new PlaceholderTextField("0.00");
    private final PlaceholderTextField quantityField = new PlaceholderTextField("0");
    private final PlaceholderTextField reorderField = new PlaceholderTextField("e.g. 10");
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"Regular", "Perishable"});
    private final PlaceholderTextField expiryField = new PlaceholderTextField("YYYY-MM-DD");

    // Search bar
    private final PlaceholderTextField searchField = new PlaceholderTextField("Search by ID, name, category...");

    // Action buttons
    private final ModernButton addButton = new ModernButton("Add Item", ModernButton.Variant.PRIMARY);
    private final ModernButton updateButton = new ModernButton("Update", ModernButton.Variant.SECONDARY);
    private final ModernButton deleteButton = new ModernButton("Delete", ModernButton.Variant.DANGER);
    private final ModernButton clearButton = new ModernButton("Clear Form", ModernButton.Variant.SECONDARY);

    // Filter toolbar buttons
    private final ModernButton showAllBtn = new ModernButton("Show All", ModernButton.Variant.PRIMARY);
    private final ModernButton lowStockBtn = new ModernButton("Low Stock", ModernButton.Variant.WARNING);
    private final ModernButton exportBtn = new ModernButton("Export Report", ModernButton.Variant.SECONDARY);

    // Header stats labels
    private final JLabel totalValueLabel = new JLabel("Rs 0.00");
    private final JLabel skuCountLabel = new JLabel("0 Items");
    private final JLabel lowStockCountLabel = new JLabel("0 Alerts");

    // Form badges & calculated preview
    private final BadgeLabel formModeBadge = new BadgeLabel("Create Mode", new Color(5, 150, 105), Color.WHITE, null);
    private final JLabel formHeading = new JLabel("Item Management");
    private final JLabel formSubheading = new JLabel("Add new stock or edit the selected item");
    private final JLabel previewValueLabel = new JLabel("Rs 0.00");

    // Table & status labels
    private final JLabel statusLabel = new JLabel("Ready");
    private final JLabel rowCountLabel = new JLabel("Showing 0 entries");

    // Theme color constants
    public static final Color GREEN = new Color(22, 101, 52);
    public static final Color ACCENT = new Color(5, 150, 105);
    public static final Color PALE_GREEN = new Color(240, 253, 244);
    public static final Color SURFACE = new Color(248, 250, 248);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color TEXT = new Color(15, 23, 42);
    public static final Color MUTED = new Color(100, 116, 139);
    public static final Color WARNING = new Color(180, 83, 9);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Item Name", "Category", "Price", "Qty", "Reorder", "Expiry", "Type", "Stock Value"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    // State
    private String activeFilter = "all"; // "all" or "lowstock"

    public InventoryFrame(InventoryService service) {
        super("Inventory Management System");
        this.service = service;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1380, 840);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);

        buildUi();
        setupEvents();

        // Initial load
        setFormMode(false);
        loadTableSafe(() -> service.getAllProducts(), "System ready. Loaded all inventory items.");
    }

    private void buildUi() {
        getContentPane().setBackground(SURFACE);
        setLayout(new BorderLayout());

        // 1. Header
        add(headerPanel(), BorderLayout.NORTH);

        // 2. Main Workspace (Ledger on Center/Left, Form on East/Right)
        JPanel workspace = new JPanel(new BorderLayout(20, 0));
        workspace.setBackground(SURFACE);
        workspace.setBorder(BorderFactory.createEmptyBorder(18, 24, 14, 24));
        workspace.add(ledgerPanel(), BorderLayout.CENTER);
        workspace.add(formPanel(), BorderLayout.EAST);
        add(workspace, BorderLayout.CENTER);

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
                BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));

        // Brand on Left
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        brand.setOpaque(false);

        // Icon Box
        BadgeLabel iconBadge = new BadgeLabel("IMS", GREEN, Color.WHITE, null);
        iconBadge.setFont(new Font("SansSerif", Font.BOLD, 13));
        iconBadge.setPreferredSize(new Dimension(42, 42));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("Inventory Management System");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT);

        JPanel subtitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        subtitleRow.setOpaque(false);
        JLabel subtitle = new JLabel("Enterprise Stock & Warehouse Control");
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
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        rightPanel.add(statCard("TOTAL VALUE", totalValueLabel, GREEN, PALE_GREEN));
        rightPanel.add(statCard("SKU COUNT", skuCountLabel, ACCENT, new Color(236, 253, 245)));
        rightPanel.add(statCard("LOW STOCK", lowStockCountLabel, WARNING, new Color(255, 251, 235)));

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel statCard(String heading, JLabel value, Color color, Color bg) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 2));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        JLabel label = new JLabel(heading);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(MUTED);

        value.setFont(new Font("SansSerif", Font.BOLD, 15));
        value.setForeground(color);

        card.add(label);
        card.add(value);
        return card;
    }

    private JPanel ledgerPanel() {
        JPanel ledger = new JPanel(new BorderLayout(0, 14));
        ledger.setBackground(SURFACE);

        // 1. Toolbar (Search on left, Filters on right)
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

        // 2. Table Ledger Card
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createLineBorder(BORDER));

        // Card Caption
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

        // Table setup
        setupTable();
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        // Table Footer
        JPanel tableFooter = new JPanel(new BorderLayout());
        tableFooter.setBackground(Color.WHITE);
        tableFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        JLabel footerNote = new JLabel("Reports support PDF, Word, and Excel export");
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

    private void setupTable() {
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

        // Column widths
        int[] widths = {55, 180, 110, 85, 75, 80, 100, 100, 110};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Custom renderers
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

        // Form Header
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

        // Fields Container using GridBagLayout with FIXED heights
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

        // 8. Vertical Glue to absorb extra space so fields never stretch!
        gbc.gridy = row++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        fieldsPanel.add(Box.createGlue(), gbc);

        formCard.add(fieldsPanel, BorderLayout.CENTER);
        return formCard;
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
        // Form buttons
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
            loadTableSafe(() -> service.getAllProducts(), "Product added successfully: " + name);
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
            loadTableSafe(() -> service.getAllProducts(), "Product updated successfully (ID: " + id + ")");
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
            loadTableSafe(() -> service.getAllProducts(), "Deleted product ID #" + id);
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
                // Ignore parsing errors during typing
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
            skuCountLabel.setText(all.size() + " Items");
            lowStockCountLabel.setText(lowStock + " Alerts");

            showAllBtn.setCountBadge(all.size());
            lowStockBtn.setCountBadge(lowStock);
        } catch (InventoryException ex) {
            totalValueLabel.setText("Rs 0.00");
            skuCountLabel.setText("0 Items");
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
        JOptionPane.showMessageDialog(this, message, "Input Warning", JOptionPane.WARNING_MESSAGE);
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
                int reorder = Integer.parseInt(String.valueOf(table.getModel().getValueAt(row, 5)));
                if (qty <= reorder) {
                    setForeground(WARNING);
                    setFont(new Font("SansSerif", Font.BOLD, 13));
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
}
