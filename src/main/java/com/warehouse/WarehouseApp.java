package com.warehouse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class WarehouseApp extends JFrame {
    private final Store store = new Store();
    private final DefaultTableModel inventoryModel = model("Product ID", "Name", "Category", "Stock", "Unit Price");
    private final DefaultTableModel dispatchModel = model("Dispatch ID", "Product", "Quantity", "Recipient", "Status");
    private final DefaultTableModel returnModel = model("Return ID", "Product", "Quantity", "Reason", "Status");
    private JTable inventoryTable;
    private JTextField nameField, categoryField, stockField, priceField, searchField;
    private JTextField dispatchProductField, dispatchQtyField, recipientField;
    private JTextField returnDispatchField, returnQtyField, reasonField;
    private JLabel statusLabel, dashboardLabel;

    public WarehouseApp() {
        super("Warehouse Dispatch & Return Authorisation System");
        store.initialize();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        JLabel heading = new JLabel("  Warehouse Dispatch & Return Authorisation System");
        heading.setOpaque(true);
        heading.setBackground(new Color(25, 47, 82));
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setPreferredSize(new Dimension(1000, 58));
        add(heading, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dashboardPanel());
        tabs.addTab("Inventory", inventoryPanel());
        tabs.addTab("Dispatch", dispatchPanel());
        tabs.addTab("Returns", returnsPanel());
        add(tabs, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(statusLabel, BorderLayout.SOUTH);
        refreshAll();
    }

    private JPanel dashboardPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        dashboardLabel = new JLabel("", SwingConstants.CENTER);
        dashboardLabel.setFont(new Font("Arial", Font.BOLD, 22));
        p.add(dashboardLabel, BorderLayout.NORTH);
        JTextArea info = new JTextArea(
                "SYSTEM WORKFLOW\n\n" +
                "1. Add and maintain products in Inventory.\n" +
                "2. Create a dispatch using a Product ID and quantity.\n" +
                "3. Dispatching deducts the quantity from available stock.\n" +
                "4. Record a return using its Dispatch ID.\n" +
                "5. Approve a return to add the approved quantity back to stock.\n\n" +
                "Use the Inventory tab to search, add, and update products.");
        info.setEditable(false);
        info.setFont(new Font("Arial", Font.PLAIN, 17));
        info.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        p.add(info, BorderLayout.CENTER);
        return p;
    }

    private JPanel inventoryPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        form.setBorder(BorderFactory.createTitledBorder("Product details"));
        nameField = new JTextField(12); categoryField = new JTextField(10);
        stockField = new JTextField(5); priceField = new JTextField(7);
        JButton add = button("Add Product", new Color(35, 180, 100));
        JButton update = button("Update Selected", new Color(50, 115, 235));
        JButton clear = button("Clear Form", new Color(145, 75, 235));
        form.add(new JLabel("Name:")); form.add(nameField);
        form.add(new JLabel("Category:")); form.add(categoryField);
        form.add(new JLabel("Stock:")); form.add(stockField);
        form.add(new JLabel("Unit price:")); form.add(priceField);
        form.add(add); form.add(update); form.add(clear);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        search.setBorder(BorderFactory.createTitledBorder("Search inventory (ID, name or category)"));
        searchField = new JTextField(20);
        JButton find = button("Search", new Color(50, 115, 235));
        JButton all = button("Show All", new Color(145, 75, 235));
        search.add(searchField); search.add(find); search.add(all);
        top.add(form); top.add(search); p.add(top, BorderLayout.NORTH);

        inventoryTable = new JTable(inventoryModel);
        inventoryTable.setRowHeight(27);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inventoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && inventoryTable.getSelectedRow() >= 0) {
                int r = inventoryTable.convertRowIndexToModel(inventoryTable.getSelectedRow());
                nameField.setText(String.valueOf(inventoryModel.getValueAt(r, 1)));
                categoryField.setText(String.valueOf(inventoryModel.getValueAt(r, 2)));
                stockField.setText(String.valueOf(inventoryModel.getValueAt(r, 3)));
                priceField.setText(String.valueOf(inventoryModel.getValueAt(r, 4)));
            }
        });
        p.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        add.addActionListener(e -> addProduct());
        update.addActionListener(e -> updateProduct());
        clear.addActionListener(e -> clearProductForm());
        find.addActionListener(e -> loadInventory(searchField.getText().trim()));
        all.addActionListener(e -> { searchField.setText(""); loadInventory(""); });
        return p;
    }

    private JPanel dispatchPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 18));
        form.setBorder(BorderFactory.createTitledBorder("Create a dispatch"));
        dispatchProductField = new JTextField(10);
        dispatchQtyField = new JTextField(6);
        recipientField = new JTextField(14);
        JButton dispatch = button("Create Dispatch", new Color(35, 150, 95));
        form.add(new JLabel("Product ID:")); form.add(dispatchProductField);
        form.add(new JLabel("Quantity:")); form.add(dispatchQtyField);
        form.add(new JLabel("Recipient:")); form.add(recipientField);
        form.add(dispatch);
        p.add(form, BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(dispatchModel)), BorderLayout.CENTER);
        dispatch.addActionListener(e -> createDispatch());
        return p;
    }

    private JPanel returnsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 18));
        form.setBorder(BorderFactory.createTitledBorder("Request / approve a return"));
        returnDispatchField = new JTextField(9);
        returnQtyField = new JTextField(6);
        reasonField = new JTextField(16);
        JButton request = button("Request Return", new Color(50, 115, 235));
        JButton approve = button("Approve Selected Return", new Color(35, 150, 95));
        form.add(new JLabel("Dispatch ID:")); form.add(returnDispatchField);
        form.add(new JLabel("Quantity:")); form.add(returnQtyField);
        form.add(new JLabel("Reason:")); form.add(reasonField);
        form.add(request); form.add(approve);
        JTable table = new JTable(returnModel);
        p.add(form, BorderLayout.NORTH); p.add(new JScrollPane(table), BorderLayout.CENTER);
        request.addActionListener(e -> requestReturn());
        approve.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { message("Select a return request first."); return; }
            String id = String.valueOf(returnModel.getValueAt(table.convertRowIndexToModel(row), 0));
            try { store.approveReturn(id); refreshAll(); message("Return approved and stock updated."); }
            catch (Exception ex) { message(ex.getMessage()); }
        });
        return p;
    }

    private void addProduct() {
        try {
            Product x = new Product(store.nextProductId(), required(nameField), required(categoryField),
                    Integer.parseInt(required(stockField)), Double.parseDouble(required(priceField)));
            if (x.stock < 0 || x.price < 0) throw new IllegalArgumentException("Stock and price cannot be negative.");
            store.addProduct(x); refreshAll(); clearProductForm(); message("Product added: " + x.id);
        } catch (Exception ex) { message("Could not add product: " + ex.getMessage()); }
    }

    private void updateProduct() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) { message("Select a product row to update."); return; }
        String id = String.valueOf(inventoryModel.getValueAt(inventoryTable.convertRowIndexToModel(row), 0));
        try {
            store.updateProduct(id, required(nameField), required(categoryField),
                    Integer.parseInt(required(stockField)), Double.parseDouble(required(priceField)));
            refreshAll(); message("Product updated.");
        } catch (Exception ex) { message("Could not update: " + ex.getMessage()); }
    }

    private void createDispatch() {
        try {
            String id = store.createDispatch(required(dispatchProductField),
                    Integer.parseInt(required(dispatchQtyField)), required(recipientField));
            refreshAll(); dispatchProductField.setText(""); dispatchQtyField.setText(""); recipientField.setText("");
            message("Dispatch created successfully. Dispatch ID: " + id);
        } catch (Exception ex) { message("Dispatch failed: " + ex.getMessage()); }
    }

    private void requestReturn() {
        try {
            String id = store.requestReturn(required(returnDispatchField),
                    Integer.parseInt(required(returnQtyField)), required(reasonField));
            refreshAll(); message("Return request created: " + id);
        } catch (Exception ex) { message("Return request failed: " + ex.getMessage()); }
    }

    private void refreshAll() {
        loadInventory("");
        dispatchModel.setRowCount(0);
        for (Dispatch d : store.dispatches()) dispatchModel.addRow(new Object[]{d.id, d.productId, d.quantity, d.recipient, d.status});
        returnModel.setRowCount(0);
        for (ReturnRequest r : store.returns()) returnModel.addRow(new Object[]{r.id, r.productId, r.quantity, r.reason, r.status});
        if (dashboardLabel != null)
            dashboardLabel.setText("Products: " + store.products().size() + "     Dispatches: " +
                    store.dispatches().size() + "     Return Requests: " + store.returns().size());
        if (statusLabel != null) statusLabel.setText(store.isConnected()
                ? "Storage: MySQL database connected — data is persistent."
                : "Storage: IN-MEMORY (temporary) — configure MySQL to save data permanently.");
    }

    private void loadInventory(String query) {
        inventoryModel.setRowCount(0);
        for (Product x : store.products()) {
            String q = query.toLowerCase(Locale.ROOT);
            if (q.isEmpty() || x.id.toLowerCase().contains(q) || x.name.toLowerCase().contains(q)
                    || x.category.toLowerCase().contains(q))
                inventoryModel.addRow(new Object[]{x.id, x.name, x.category, x.stock, String.format(Locale.US, "%.2f", x.price)});
        }
    }

    private void clearProductForm() {
        nameField.setText(""); categoryField.setText(""); stockField.setText(""); priceField.setText("");
        inventoryTable.clearSelection();
    }
    private static String required(JTextField f) {
        String s = f.getText().trim();
        if (s.isEmpty()) throw new IllegalArgumentException("Please fill in all fields.");
        return s;
    }
    private void message(String s) { JOptionPane.showMessageDialog(this, s); }
    private static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    }
    private static JButton button(String text, Color color) {
        JButton b = new JButton(text); b.setBackground(color); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); return b;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WarehouseApp().setVisible(true));
    }

    // Data and persistence layer: JDBC/MySQL when available; in-memory fallback for demo/testing.
    static class Store {
        private Connection connection;
        private final Map<String, Product> memoryProducts = new LinkedHashMap<>();
        private final List<Dispatch> memoryDispatches = new ArrayList<>();
        private final List<ReturnRequest> memoryReturns = new ArrayList<>();

        void initialize() {
            String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/warehouse_db?serverTimezone=UTC");
            String user = System.getenv().getOrDefault("DB_USER", "root");
            String pass = System.getenv().getOrDefault("DB_PASSWORD", "");
            try {
                connection = DriverManager.getConnection(url, user, pass);
                try (Statement s = connection.createStatement()) {
                    s.executeUpdate("CREATE TABLE IF NOT EXISTS products (id VARCHAR(20) PRIMARY KEY, name VARCHAR(120), category VARCHAR(80), stock INT NOT NULL, price DOUBLE NOT NULL)");
                    s.executeUpdate("CREATE TABLE IF NOT EXISTS dispatches (id VARCHAR(20) PRIMARY KEY, product_id VARCHAR(20), quantity INT, recipient VARCHAR(120), status VARCHAR(30))");
                    s.executeUpdate("CREATE TABLE IF NOT EXISTS returns (id VARCHAR(20) PRIMARY KEY, dispatch_id VARCHAR(20), product_id VARCHAR(20), quantity INT, reason VARCHAR(255), status VARCHAR(30))");
                }
                seedIfEmpty();
            } catch (SQLException ex) {
                connection = null;
                seedMemory();
            }
        }
        boolean isConnected() { return connection != null; }

        private void seedIfEmpty() throws SQLException {
            try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM products")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    Product[] sample = samples();
                    for (Product p : sample) addProduct(p);
                }
            }
        }
        private void seedMemory() {
            if (memoryProducts.isEmpty()) for (Product p : samples()) memoryProducts.put(p.id, p);
        }
        private Product[] samples() {
            return new Product[]{
                new Product("P1001","Laptop 15 inch","Electronics",22,850.00),
                new Product("P1002","Wireless Mouse","Accessories",100,15.50),
                new Product("P1003","Mechanical Keyboard","Accessories",60,45.00),
                new Product("P1004","27 inch Monitor","Electronics",18,220.00),
                new Product("P1005","USB-C Hub","Accessories",80,28.75),
                new Product("P1006","Office Chair","Furniture",12,130.00)
            };
        }
        String nextProductId() {
            int max = 1000;
            for (Product p : products()) try { max = Math.max(max, Integer.parseInt(p.id.substring(1))); } catch (Exception ignored) {}
            return "P" + (max + 1);
        }
        void addProduct(Product p) throws SQLException {
            if (connection == null) { memoryProducts.put(p.id, p); return; }
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO products VALUES(?,?,?,?,?)")) {
                ps.setString(1,p.id); ps.setString(2,p.name); ps.setString(3,p.category); ps.setInt(4,p.stock); ps.setDouble(5,p.price); ps.executeUpdate();
            }
        }
        void updateProduct(String id, String name, String category, int stock, double price) throws SQLException {
            if (stock < 0 || price < 0) throw new IllegalArgumentException("Stock and price cannot be negative.");
            if (connection == null) {
                Product old = memoryProducts.get(id); if (old == null) throw new IllegalArgumentException("Product not found.");
                memoryProducts.put(id, new Product(id,name,category,stock,price)); return;
            }
            try (PreparedStatement ps = connection.prepareStatement("UPDATE products SET name=?,category=?,stock=?,price=? WHERE id=?")) {
                ps.setString(1,name); ps.setString(2,category); ps.setInt(3,stock); ps.setDouble(4,price); ps.setString(5,id);
                if (ps.executeUpdate() == 0) throw new IllegalArgumentException("Product not found.");
            }
        }
        List<Product> products() {
            List<Product> out = new ArrayList<>();
            if (connection == null) return new ArrayList<>(memoryProducts.values());
            try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM products ORDER BY id")) {
                while (rs.next()) out.add(new Product(rs.getString("id"),rs.getString("name"),rs.getString("category"),rs.getInt("stock"),rs.getDouble("price")));
            } catch (SQLException ex) { ex.printStackTrace(); }
            return out;
        }
        String createDispatch(String productId, int qty, String recipient) throws SQLException {
            if (qty <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
            Product p = findProduct(productId);
            if (p == null) throw new IllegalArgumentException("Product ID not found.");
            if (p.stock < qty) throw new IllegalArgumentException("Insufficient stock. Available: " + p.stock);
            String id = "D" + (dispatches().size() + 1001);
            if (connection == null) {
                memoryProducts.put(p.id, new Product(p.id,p.name,p.category,p.stock-qty,p.price));
                memoryDispatches.add(new Dispatch(id,productId,qty,recipient,"DISPATCHED"));
            } else {
                connection.setAutoCommit(false);
                try {
                    try (PreparedStatement u = connection.prepareStatement("UPDATE products SET stock=stock-? WHERE id=? AND stock>=?")) {
                        u.setInt(1,qty); u.setString(2,productId); u.setInt(3,qty);
                        if (u.executeUpdate() == 0) throw new SQLException("Stock changed or insufficient.");
                    }
                    try (PreparedStatement i = connection.prepareStatement("INSERT INTO dispatches VALUES(?,?,?,?,?)")) {
                        i.setString(1,id); i.setString(2,productId); i.setInt(3,qty); i.setString(4,recipient); i.setString(5,"DISPATCHED"); i.executeUpdate();
                    }
                    connection.commit();
                } catch (SQLException ex) { connection.rollback(); throw ex; }
                finally { connection.setAutoCommit(true); }
            }
            return id;
        }
        String requestReturn(String dispatchId, int qty, String reason) throws SQLException {
            if (qty <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
            Dispatch d = findDispatch(dispatchId);
            if (d == null) throw new IllegalArgumentException("Dispatch ID not found.");
            if (qty > d.quantity) throw new IllegalArgumentException("Return quantity cannot exceed dispatched quantity.");
            String id = "R" + (returns().size() + 1001);
            if (connection == null) memoryReturns.add(new ReturnRequest(id,dispatchId,d.productId,qty,reason,"PENDING"));
            else try (PreparedStatement ps = connection.prepareStatement("INSERT INTO returns VALUES(?,?,?,?,?,?)")) {
                ps.setString(1,id); ps.setString(2,dispatchId); ps.setString(3,d.productId); ps.setInt(4,qty); ps.setString(5,reason); ps.setString(6,"PENDING"); ps.executeUpdate();
            }
            return id;
        }
        void approveReturn(String returnId) throws SQLException {
            ReturnRequest r = findReturn(returnId);
            if (r == null) throw new IllegalArgumentException("Return request not found.");
            if (!r.status.equals("PENDING")) throw new IllegalArgumentException("This return has already been processed.");
            if (connection == null) {
                Product p = findProduct(r.productId);
                memoryProducts.put(p.id,new Product(p.id,p.name,p.category,p.stock+r.quantity,p.price));
                r.status = "APPROVED";
            } else {
                connection.setAutoCommit(false);
                try {
                    try (PreparedStatement u = connection.prepareStatement("UPDATE products SET stock=stock+? WHERE id=?")) {
                        u.setInt(1,r.quantity); u.setString(2,r.productId); u.executeUpdate();
                    }
                    try (PreparedStatement u = connection.prepareStatement("UPDATE returns SET status='APPROVED' WHERE id=? AND status='PENDING'")) {
                        u.setString(1,returnId);
                        if (u.executeUpdate() == 0) throw new SQLException("Return was already processed.");
                    }
                    connection.commit();
                } catch (SQLException ex) { connection.rollback(); throw ex; }
                finally { connection.setAutoCommit(true); }
            }
        }
        private Product findProduct(String id) { return products().stream().filter(p -> p.id.equalsIgnoreCase(id)).findFirst().orElse(null); }
        List<Dispatch> dispatches() {
            if (connection == null) return new ArrayList<>(memoryDispatches);
            List<Dispatch> out = new ArrayList<>();
            try (Statement s=connection.createStatement(); ResultSet rs=s.executeQuery("SELECT * FROM dispatches ORDER BY id")) {
                while(rs.next()) out.add(new Dispatch(rs.getString("id"),rs.getString("product_id"),rs.getInt("quantity"),rs.getString("recipient"),rs.getString("status")));
            } catch(SQLException ex) { ex.printStackTrace(); } return out;
        }
        List<ReturnRequest> returns() {
            if (connection == null) return new ArrayList<>(memoryReturns);
            List<ReturnRequest> out = new ArrayList<>();
            try (Statement s=connection.createStatement(); ResultSet rs=s.executeQuery("SELECT * FROM returns ORDER BY id")) {
                while(rs.next()) out.add(new ReturnRequest(rs.getString("id"),rs.getString("dispatch_id"),rs.getString("product_id"),rs.getInt("quantity"),rs.getString("reason"),rs.getString("status")));
            } catch(SQLException ex) { ex.printStackTrace(); } return out;
        }
        private Dispatch findDispatch(String id) { return dispatches().stream().filter(d -> d.id.equalsIgnoreCase(id)).findFirst().orElse(null); }
        private ReturnRequest findReturn(String id) { return returns().stream().filter(r -> r.id.equalsIgnoreCase(id)).findFirst().orElse(null); }
    }

    static class Product {
        String id,name,category; int stock; double price;
        Product(String id,String name,String category,int stock,double price) { this.id=id;this.name=name;this.category=category;this.stock=stock;this.price=price; }
    }
    static class Dispatch {
        String id,productId,recipient,status; int quantity;
        Dispatch(String id,String productId,int quantity,String recipient,String status) { this.id=id;this.productId=productId;this.quantity=quantity;this.recipient=recipient;this.status=status; }
    }
    static class ReturnRequest {
        String id,dispatchId,productId,reason,status; int quantity;
        ReturnRequest(String id,String dispatchId,String productId,int quantity,String reason,String status) { this.id=id;this.dispatchId=dispatchId;this.productId=productId;this.quantity=quantity;this.reason=reason;this.status=status; }
    }
}
