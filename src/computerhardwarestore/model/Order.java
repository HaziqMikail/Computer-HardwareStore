package computerhardwarestore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int id;
    private final int userId;
    private final String customerName;
    private LocalDateTime orderDate;
    private final List<OrderItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal deliveryCharge;
    private final BigDecimal total;
    private final String paymentMethod;
    private final String paymentDetail;

    public Order(int id, int userId, String customerName, LocalDateTime orderDate, List<OrderItem> items,
                 BigDecimal subtotal, BigDecimal deliveryCharge, BigDecimal total,
                 String paymentMethod, String paymentDetail) {
        this.id = id;
        this.userId = userId;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.items = items;
        this.subtotal = subtotal;
        this.deliveryCharge = deliveryCharge;
        this.total = total;
        this.paymentMethod = paymentMethod;
        this.paymentDetail = paymentDetail;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public String getCustomerName() { return customerName; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDeliveryCharge() { return deliveryCharge; }
    public BigDecimal getTotal() { return total; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentDetail() { return paymentDetail; }
    public int getItemCount() { return items.stream().mapToInt(OrderItem::getQuantity).sum(); }
}
