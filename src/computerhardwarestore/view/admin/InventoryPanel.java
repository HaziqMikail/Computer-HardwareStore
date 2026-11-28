package computerhardwarestore.view.admin;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.Product;
import computerhardwarestore.util.Formats;
import computerhardwarestore.view.common.Card;
import computerhardwarestore.view.common.Dialogs;
import computerhardwarestore.view.common.ImageLoader;
import computerhardwarestore.view.common.PageHeader;
import computerhardwarestore.view.common.Refreshable;
import computerhardwarestore.view.common.Theme;
import computerhardwarestore.view.common.Toast;
import computerhardwarestore.view.common.Ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Admin inventory: product table with a side form to add, edit, delete, change price/stock and pick an image. */
public class InventoryPanel extends JPanel implements Refreshable {
    private final AppContext ctx;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Category", "Price", "Stock"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField search = new JTextField();
    private List<Product> products = new ArrayList<>();

    private final JTextField name = new JTextField();
    private final JComboBox<String> category = new JComboBox<>();
    private final JTextField price = new JTextField();
    private final JTextField stock = new JTextField();
    private final JTextArea specs = new JTextArea(3, 20);
    private final JTextField imagePath = new JTextField();
    private final JLabel preview = new JLabel("", SwingConstants.CENTER);
    private final JLabel formTitle = Ui.h2("New product");
    private final JButton saveButton = Ui.primary("Add product");
    private final JButton deleteButton = Ui.danger("Delete");
    private int editingId = 0;

    public InventoryPanel(AppContext ctx) {
        super(new BorderLayout(Theme.GAP, 0));
        this.ctx = ctx;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

        JPanel left = Ui.transparent(new BorderLayout(0, Theme.GAP));
        JButton add = Ui.accent("+ New product");
        add.addActionListener(e -> startNew());
        JButton reload = Ui.secondary("Refresh");
        reload.addActionListener(e -> {
            refresh();
            Toast.show(this, "Inventory refreshed");
        });
        left.add(new PageHeader("Inventory", "Add products, update prices and stock.").action(reload).action(add),
                BorderLayout.NORTH);

        JPanel body = Ui.transparent(new BorderLayout(0, 10));
        search.putClientProperty("JTextField.placeholderText", "Search products...");
        search.putClientProperty("JTextField.showClearButton", true);
        body.add(search, BorderLayout.NORTH);
        Ui.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setCellRenderer(aligned(SwingConstants.RIGHT));
        table.getColumnModel().getColumn(4).setCellRenderer(new StockRenderer());
        table.getColumnModel().getColumn(4).setMaxWidth(90);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                edit(products.get(table.getSelectedRow()));
            }
        });
        Card tableCard = new Card(new BorderLayout()).pad(6, 6, 6, 6);
        tableCard.add(Ui.scroll(table), BorderLayout.CENTER);
        body.add(tableCard, BorderLayout.CENTER);
        left.add(body, BorderLayout.CENTER);
        add(left, BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);

        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { reloadList(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { reloadList(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { reloadList(); }
        });
        category.setEditable(true);
    }

    private JPanel buildForm() {
        Card form = new Card(new BorderLayout(0, 12)).pad(18, 20, 18, 20);
        form.setPreferredSize(new Dimension(370, 10));
        form.add(formTitle, BorderLayout.NORTH);

        JPanel fields = Ui.transparent(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 10, 0);
        int y = 0;
        y = field(fields, g, y, "Name", name);
        y = field(fields, g, y, "Category", category);
        JPanel two = Ui.transparent(new GridLayout(1, 2, 10, 0));
        two.add(labelled("Price (RM)", price));
        two.add(labelled("Stock", stock));
        g.gridy = y++;
        fields.add(two, g);
        specs.setLineWrap(true);
        specs.setWrapStyleWord(true);
        specs.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        javax.swing.JScrollPane specScroll = new javax.swing.JScrollPane(specs);
        specScroll.setPreferredSize(new Dimension(10, 70));
        y = field(fields, g, y, "Specs (separate with commas)", specScroll);

        JPanel imgRow = Ui.transparent(new BorderLayout(8, 0));
        imagePath.setEditable(false);
        JButton browse = Ui.secondary("Choose...");
        imgRow.add(imagePath, BorderLayout.CENTER);
        imgRow.add(browse, BorderLayout.EAST);
        y = field(fields, g, y, "Image", imgRow);
        browse.addActionListener(e -> chooseImage());

        preview.setOpaque(true);
        preview.setBackground(new Color(0xFAF7F2));
        preview.setPreferredSize(new Dimension(10, 130));
        g.gridy = y++;
        fields.add(preview, g);
        form.add(Ui.scroll(fields), BorderLayout.CENTER);

        JButton save = saveButton;
        JButton delete = deleteButton;
        JPanel buttons = Ui.transparent(new GridLayout(1, 2, 10, 0));
        buttons.add(save);
        buttons.add(delete);
        form.add(buttons, BorderLayout.SOUTH);
        save.addActionListener(e -> save());
        delete.addActionListener(e -> delete());
        clearForm();
        return form;
    }

    private static int field(JPanel p, GridBagConstraints g, int y, String label, Component c) {
        g.gridy = y;
        p.add(labelled(label, c), g);
        return y + 1;
    }

    private static JPanel labelled(String label, Component c) {
        JPanel p = Ui.transparent(new BorderLayout(0, 4));
        p.add(Ui.muted(label), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private static DefaultTableCellRenderer aligned(int alignment) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(alignment);
        return r;
    }

    /** Colours the stock number: red when empty, amber when low. */
    private static class StockRenderer extends DefaultTableCellRenderer {
        StockRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            int n = v instanceof Integer i ? i : 0;
            setFont(Theme.BOLD);
            if (!sel) setForeground(n <= 0 ? Theme.DANGER : n <= Product.LOW_STOCK ? Theme.WARNING : Theme.SUCCESS);
            return this;
        }
    }

    @Override
    public void refresh() {
        reloadList();
        try {
            List<String> cats = ctx.inventory.categories();
            Object current = category.getEditor().getItem();
            category.removeAllItems();
            for (String c : cats) category.addItem(c);
            category.setSelectedItem(current == null ? "" : current);
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private void reloadList() {
        try {
            products = ctx.inventory.list(search.getText());
        } catch (Exception ex) {
            Dialogs.error(this, ex);
            return;
        }
        model.setRowCount(0);
        for (Product p : products) {
            model.addRow(new Object[]{p.getId(), p.getName(), p.getCategory(), Formats.money(p.getPrice()),
                    p.getStock()});
        }
    }

    private void edit(Product p) {
        editingId = p.getId();
        formTitle.setText("Edit product #" + p.getId());
        name.setText(p.getName());
        category.setSelectedItem(p.getCategory());
        price.setText(p.getPrice().toPlainString());
        stock.setText(String.valueOf(p.getStock()));
        specs.setText(p.getSpecs());
        setImage(p.getImagePath());
        updateMode();
    }

    /** The button text and Delete state always say whether the form will add a new product or edit one. */
    private void updateMode() {
        saveButton.setText(editingId == 0 ? "Add product" : "Save changes");
        deleteButton.setEnabled(editingId != 0);
    }

    private void startNew() {
        clearForm();
        name.requestFocusInWindow();
        Toast.show(this, "Fill in the form on the right, then click Add product");
    }

    private void clearForm() {
        editingId = 0;
        updateMode();
        formTitle.setText("New product");
        table.clearSelection();
        name.setText("");
        category.setSelectedItem("");
        price.setText("");
        stock.setText("0");
        specs.setText("");
        setImage("");
    }

    private void setImage(String path) {
        imagePath.setText(path);
        preview.setIcon(ImageLoader.load(path, 200, 120));
    }

    private void save() {
        try {
            Object cat = category.getEditor().getItem();
            boolean isNew = editingId == 0;
            ctx.inventory.save(editingId, name.getText(), cat == null ? "" : cat.toString(), price.getText(),
                    stock.getText(), specs.getText(), imagePath.getText());
            Toast.show(this, isNew ? "Product added" : "Product updated");
            clearForm();
            refresh();
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    private void delete() {
        if (editingId == 0) {
            Dialogs.warn(this, "Select a product in the table first.");
            return;
        }
        if (!Dialogs.confirm(this, "Delete \"" + name.getText() + "\"? Past orders keep their record of it.")) return;
        try {
            ctx.inventory.delete(editingId);
            Toast.show(this, "Product deleted");
            clearForm();
            refresh();
        } catch (Exception ex) {
            Dialogs.error(this, ex);
        }
    }

    /** Copies the chosen picture into ./product-images so the app keeps working if the original is moved. */
    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images (png, jpg, jpeg, gif)", "png", "jpg", "jpeg", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File src = chooser.getSelectedFile();
        try {
            Path dir = Path.of("product-images");
            Files.createDirectories(dir);
            Path dest = dir.resolve(src.getName());
            if (!src.toPath().toAbsolutePath().equals(dest.toAbsolutePath())) {
                Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                ImageLoader.clearCache();
            }
            setImage(dest.toString().replace('\\', '/'));
        } catch (IOException ex) {
            Dialogs.warn(this, "Could not use that image: " + ex.getMessage());
        }
    }
}
