package computerhardwarestore.model;

import java.math.BigDecimal;

public class CartItem {
    private final int productId;
    private final String name;
    private BigDecimal unitPrice;
    private final String imagePath;
    private int quantity;

    public CartItem(int productId, String name, BigDecimal unitPrice, int quantity, String imagePath) {
        this.productId = productId;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.imagePath = imagePath;
    }

    public String getImagePath() { return imagePath; }

    public int getProductId() { return productId; }
    public String getName() { return name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getSubtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
