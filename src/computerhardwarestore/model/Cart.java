package computerhardwarestore.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Pure data holder for the shopping cart. Business rules (stock, quantity) live in CartController. */
public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }
    public boolean isEmpty() { return items.isEmpty(); }
    public void clear() { items.clear(); }

    public Optional<CartItem> find(int productId) {
        return items.stream().filter(i -> i.getProductId() == productId).findFirst();
    }

    public void add(CartItem item) { items.add(item); }
    public void remove(int productId) { items.removeIf(i -> i.getProductId() == productId); }

    public int getTotalQuantity() { return items.stream().mapToInt(CartItem::getQuantity).sum(); }

    public BigDecimal getSubtotal() {
        return items.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
