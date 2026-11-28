package computerhardwarestore.dao;

import computerhardwarestore.exception.DataAccessException;
import computerhardwarestore.exception.OutOfStockException;
import computerhardwarestore.model.DashboardStats;
import computerhardwarestore.model.Order;
import java.util.List;

public interface OrderDao {
    /**
     * Atomically saves the order and reduces stock for every line. If any line lacks stock nothing is saved and
     * OutOfStockException is thrown. Fills in the order's generated id and timestamp.
     */
    void placeOrder(Order order) throws DataAccessException, OutOfStockException;

    List<Order> findByUser(int userId) throws DataAccessException;

    List<Order> findAll() throws DataAccessException;

    /** Dashboard figures: revenue, counts, products at or below the low-stock level, and the newest orders. */
    DashboardStats dashboardStats(int lowStockLevel, int recentLimit) throws DataAccessException;
}
