package computerhardwarestore.controller;

import computerhardwarestore.dao.OrderDao;
import computerhardwarestore.exception.AppException;
import computerhardwarestore.exception.PaymentException;
import computerhardwarestore.model.Cart;
import computerhardwarestore.model.CartItem;
import computerhardwarestore.model.Order;
import computerhardwarestore.model.OrderItem;
import computerhardwarestore.model.User;
import computerhardwarestore.payment.PaymentMethod;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Checkout (simulated payment) and order history. */
public class OrderController {
    public static final BigDecimal DELIVERY_CHARGE = new BigDecimal("5.99");

    private final OrderDao orderDao;

    public OrderController(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    /** Validates payment details, saves the order, reduces stock and empties the cart. */
    public Order checkout(User user, Cart cart, PaymentMethod method, Map<String, String> details)
            throws AppException {
        if (cart.isEmpty()) {
            throw new PaymentException("Your cart is empty.");
        }
        if (method == null) {
            throw new PaymentException("Please select a payment method.");
        }
        method.validate(details);

        List<OrderItem> lines = new ArrayList<>();
        for (CartItem i : cart.getItems()) {
            lines.add(new OrderItem(i.getProductId(), i.getName(), i.getUnitPrice(), i.getQuantity()));
        }
        BigDecimal subtotal = cart.getSubtotal();
        Order order = new Order(0, user.getId(), user.getFullName(), null, lines, subtotal, DELIVERY_CHARGE,
                subtotal.add(DELIVERY_CHARGE), method.getName(), method.describe(details));
        orderDao.placeOrder(order);
        cart.clear();
        return order;
    }

    public List<Order> historyFor(User user) throws AppException {
        return orderDao.findByUser(user.getId());
    }

    public List<Order> allOrders() throws AppException {
        return orderDao.findAll();
    }

    public computerhardwarestore.model.DashboardStats dashboard() throws AppException {
        return orderDao.dashboardStats(computerhardwarestore.model.Product.LOW_STOCK, 6);
    }
}
