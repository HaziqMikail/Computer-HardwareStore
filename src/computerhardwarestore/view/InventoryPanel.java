package computerhardwarestore.view;

import computerhardwarestore.controller.AppContext;
import computerhardwarestore.model.Product;
import computerhardwarestore.util.Formats;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/** Admin inventory: table of products plus a form to add, edit, delete, change price/stock and pick an image. */
public class InventoryPanel extends JPanel implements Refreshable {
    private final AppContext ctx;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Category", "Price", "Stock"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField search = new JTextField(14);
    private List<Product> products = new ArrayList<>();

    private final JTextField name = new JTextField(20);
    private final JComboBox<String> category = new JComboBox<>();
    private final JTextField price = new JTextField(10);
    private final JTextField stock = new JTextField(10);
    private final JTextArea specs = new JTextArea(3, 20);
    private final JTextField imagePath = new JTextField(20);
    private final JLabel preview = new JLabel("", SwingConstants.CENTER);
    private final JLabel formTitle = Theme.heading("New product");
    private int editingId = 0;

    public InventoryPanel(AppContext ctx) {
        super(new BorderLayout(10, 10));
        this.ctx = ctx;
        setBackground(Theme.LIGHT);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        add(Theme.title("Inventory Management"), BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                edit(products.get(table.getSelectedRow()));
            }
        });

        JPanel left = Theme.panel(new BorderLayout(6, 6));
        JPanel bar = Theme.panel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        bar.add(new JLabel("Search:"));
        bar.add(search);
        JButton go = Theme.button("Search");
        bar.add(go);
        left.add(bar, BorderLayout.NORTH);
        left.add(new JScrollPane(table), BorderLayout.CENTER);
        go.addActionListener(e -> refresh());
        search.addActionListener(e -> refresh());

        category.setEditable(true);
        add(left, BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CREAM);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 3), BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        form.add(formTitle, g);
        g.gridwidth = 1;

        row(form, g, 1, "Name:", name);
        row(form, g, 2, "Category:", category);
        row(form, g, 3, "Price (RM):", price);
        row(form, g, 4, "Stock:", stock);
        specs.setLineWrap(true);
        specs.setWrapStyleWord(true);
        specs.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY));
        row(form, g, 5, "Specs:", new JScrollPane(specs));

        JButton browse = Theme.button("Choose...");
        JPanel img = new JPanel(new BorderLayout(4, 0));
        img.setOpaque(false);
        imagePath.setEditable(false);
        img.add(imagePath, BorderLayout.CENTER);
        img.add(browse, BorderLayout.EAST);
        row(form, g, 6, "Image:", img);
        browse.addActionListener(e -> chooseImage());

        preview.setPreferredSize(new Dimension(160, 120));
        preview.setBorder(BorderFactory.createLineBorder(java.awt.Color.LIGHT_GRAY));
        g.gridx = 0;
        g.gridy = 7;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.CENTER;
        form.add(preview, g);

        JButton save = Theme.button("Save");
        JButton fresh = Theme.button("New");
        JButton delete = Theme.button("Delete");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        buttons.setOpaque(false);
        buttons.add(save);
        buttons.add(fresh);
        buttons.add(delete);
        g.gridy = 8;
        form.add(buttons, g);

        save.addActionListener(e -> save());
        fresh.addActionListener(e -> clearForm());
        delete.addActionListener(e -> delete());
        clearForm();
        return form;
    }

    private static void row(JPanel form, GridBagConstraints g, int y, String label, java.awt.Component c) {
        g.gridy = y;
        g.gridx = 0;
        g.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(Theme.BODY_BOLD);
        form.add(l, g);
        g.gridx = 1;
        g.weightx = 1;
        form.add(c, g);
    }

    @Override
    public void refresh() {
        try {
            products = ctx.inventory.list(search.getText());
            List<String> cats = ctx.inventory.categories();
            String current = String.valueOf(category.getEditor().getItem());
            category.removeAllItems();
            for (String c : cats) category.addItem(c);
            category.setSelectedItem(current.equals("null") ? "" : current);
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
    }

    private void clearForm() {
        editingId = 0;
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
        preview.setIcon(ImageLoader.load(path, 150, 110));
    }

    private void save() {
        try {
            Object cat = category.getEditor().getItem();
            ctx.inventory.save(editingId, name.getText(), cat == null ? "" : cat.toString(), price.getText(),
                    stock.getText(), specs.getText(), imagePath.getText());
            Dialogs.info(this, editingId == 0 ? "Product added." : "Product updated.");
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
            }
            setImage(dest.toString().replace('\\', '/'));
        } catch (IOException ex) {
            Dialogs.warn(this, "Could not use that image: " + ex.getMessage());
        }
    }
}
