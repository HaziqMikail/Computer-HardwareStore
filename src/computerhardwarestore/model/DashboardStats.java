package computerhardwarestore.model;

import java.math.BigDecimal;
import java.util.List;

/** Summary numbers for the admin dashboard. */
public record DashboardStats(BigDecimal revenue, int orderCount, int productCount, int customerCount,
                             List<Product> lowStock, List<Order> recentOrders) {
}
