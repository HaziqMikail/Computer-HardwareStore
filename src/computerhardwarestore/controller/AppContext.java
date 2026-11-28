package computerhardwarestore.controller;

import computerhardwarestore.dao.OrderDao;
import computerhardwarestore.dao.ProductDao;
import computerhardwarestore.dao.UserDao;
import computerhardwarestore.dao.jdbc.JdbcOrderDao;
import computerhardwarestore.dao.jdbc.JdbcProductDao;
import computerhardwarestore.dao.jdbc.JdbcUserDao;
import computerhardwarestore.payment.PaymentRegistry;

/**
 * Composition root: the only place that knows which concrete DAO classes are used. Everything else depends on the
 * DAO interfaces, so swapping the storage means changing these three lines.
 */
public final class AppContext {
    public final AuthController auth;
    public final CatalogController catalog;
    public final CartController cart;
    public final OrderController orders;
    public final InventoryController inventory;
    public final PaymentRegistry payments;

    public AppContext() {
        UserDao userDao = new JdbcUserDao();
        ProductDao productDao = new JdbcProductDao();
        OrderDao orderDao = new JdbcOrderDao();
        this.auth = new AuthController(userDao);
        this.catalog = new CatalogController(productDao);
        this.cart = new CartController(productDao);
        this.orders = new OrderController(orderDao);
        this.inventory = new InventoryController(productDao);
        this.payments = new PaymentRegistry();
    }
}
