package computerhardwarestore.model;

import java.math.BigDecimal;

public class Product {
    private int id;
    private String name;
    private String category;
    private BigDecimal price;
    private int stock;
    private String specs;
    private String imagePath;

    public Product(int id, String name, String category, BigDecimal price, int stock, String specs, String imagePath) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.specs = specs;
        this.imagePath = imagePath;
    }

    /** Stock at or below this (but above 0) counts as "low stock". */
    public static final int LOW_STOCK = 5;

    public boolean isOutOfStock() { return stock <= 0; }
    public boolean isLowStock() { return stock > 0 && stock <= LOW_STOCK; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getSpecs() { return specs; }
    public void setSpecs(String specs) { this.specs = specs; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
