package computerhardwarestore.dao.jdbc;

import computerhardwarestore.dao.OrderDao;
import computerhardwarestore.exception.DataAccessException;
import computerhardwarestore.exception.OutOfStockException;
import computerhardwarestore.model.DashboardStats;
import computerhardwarestore.model.Order;
import computerhardwarestore.model.OrderItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdbcOrderDao implements OrderDao {
    private final DatabaseManager db = DatabaseManager.getInstance();

    @Override
    public void placeOrder(Order order) throws DataAccessException, OutOfStockException {
        Connection c = db.getConnection();
        try {
            c.setAutoCommit(false);
            try {
                // Lock rows in a fixed order (by product id) so concurrent orders cannot deadlock.
                List<OrderItem> lines = new ArrayList<>(order.getItems());
                lines.sort(Comparator.comparingInt(OrderItem::getProductId));
                for (OrderItem line : lines) {
                    reserveStock(c, line);
                }
                insertOrder(c, order);
                c.commit();
            } catch (SQLException | OutOfStockException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(DatabaseManager.describe(e), e);
        } finally {
            try {
                c.close();
            } catch (SQLException ignored) {
                // nothing useful to do
            }
        }
    }

    /** Atomic conditional decrement: only succeeds while enough stock remains. */
    private void reserveStock(Connection c, OrderItem line) throws SQLException, OutOfStockException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?")) {
            ps.setInt(1, line.getQuantity());
            ps.setInt(2, line.getProductId());
            ps.setInt(3, line.getQuantity());
            if (ps.executeUpdate() == 0) {
                throw new OutOfStockException("Sorry, \"" + line.getProductName()
                        + "\" no longer has enough stock (or was removed). Please update your cart.");
            }
        }
    }

    private void insertOrder(Connection c, Order o) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO orders (user_id, subtotal, delivery_charge, total, payment_method, payment_detail) "
                        + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, order_date")) {
            ps.setInt(1, o.getUserId());
            ps.setBigDecimal(2, o.getSubtotal());
            ps.setBigDecimal(3, o.getDeliveryCharge());
            ps.setBigDecimal(4, o.getTotal());
            ps.setString(5, o.getPaymentMethod());
            ps.setString(6, o.getPaymentDetail());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                o.setId(rs.getInt("id"));
                o.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity) "
                        + "VALUES (?, ?, ?, ?, ?)")) {
            for (OrderItem i : o.getItems()) {
                ps.setInt(1, o.getId());
                ps.setInt(2, i.getProductId());
                ps.setString(3, i.getProductName());
                ps.setBigDecimal(4, i.getUnitPrice());
                ps.setInt(5, i.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public List<Order> findByUser(int userId) throws DataAccessException {
        return query("WHERE o.user_id = ?", userId);
    }

    @Override
    public List<Order> findAll() throws DataAccessException {
        return query("", null);
    }

    @Override
    public DashboardStats dashboardStats(int lowStockLevel, int recentLimit) throws DataAccessException {
        try (Connection c = db.getConnection()) {
            java.math.BigDecimal revenue;
            int orders;
            int products;
            int customers;
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(total), 0), COUNT(*) FROM orders")) {
                    rs.next();
                    revenue = rs.getBigDecimal(1);
                    orders = rs.getInt(2);
                }
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM products")) {
                    rs.next();
                    products = rs.getInt(1);
                }
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER'")) {
                    rs.next();
                    customers = rs.getInt(1);
                }
            }
            List<computerhardwarestore.model.Product> low = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id, name, category, price, stock, specs, image_path FROM products "
                            + "WHERE stock <= ? ORDER BY stock, name LIMIT 8")) {
                ps.setInt(1, lowStockLevel);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        low.add(new computerhardwarestore.model.Product(rs.getInt("id"), rs.getString("name"),
                                rs.getString("category"), rs.getBigDecimal("price"), rs.getInt("stock"),
                                rs.getString("specs"), rs.getString("image_path")));
                    }
                }
            }
            List<Order> all = query("", null);
            List<Order> recent = all.subList(0, Math.min(recentLimit, all.size()));
            return new DashboardStats(revenue, orders, products, customers, low, new ArrayList<>(recent));
        } catch (SQLException e) {
            throw new DataAccessException(DatabaseManager.describe(e), e);
        }
    }

    private List<Order> query(String where, Integer userId) throws DataAccessException {
        String sql = "SELECT o.id, o.user_id, u.first_name || ' ' || u.last_name AS customer, o.order_date, "
                + "o.subtotal, o.delivery_charge, o.total, o.payment_method, o.payment_detail, "
                + "i.product_id, i.product_name, i.unit_price, i.quantity "
                + "FROM orders o JOIN users u ON u.id = o.user_id "
                + "LEFT JOIN order_items i ON i.order_id = o.id " + where
                + " ORDER BY o.order_date DESC, o.id DESC, i.id";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (userId != null) ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                Map<Integer, Order> orders = new LinkedHashMap<>();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Order o = orders.get(id);
                    if (o == null) {
                        Timestamp ts = rs.getTimestamp("order_date");
                        o = new Order(id, rs.getInt("user_id"), rs.getString("customer"), ts.toLocalDateTime(),
                                new ArrayList<>(), rs.getBigDecimal("subtotal"), rs.getBigDecimal("delivery_charge"),
                                rs.getBigDecimal("total"), rs.getString("payment_method"),
                                rs.getString("payment_detail"));
                        orders.put(id, o);
                    }
                    String name = rs.getString("product_name");
                    if (name != null) {
                        o.getItems().add(new OrderItem(rs.getInt("product_id"), name,
                                rs.getBigDecimal("unit_price"), rs.getInt("quantity")));
                    }
                }
                return new ArrayList<>(orders.values());
            }
        } catch (SQLException e) {
            throw new DataAccessException(DatabaseManager.describe(e), e);
        }
    }
}
