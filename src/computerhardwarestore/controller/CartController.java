package computerhardwarestore.controller;

import computerhardwarestore.dao.ProductDao;
import computerhardwarestore.exception.AppException;
import computerhardwarestore.exception.DataAccessException;
import computerhardwarestore.exception.InvalidQuantityException;
import computerhardwarestore.exception.OutOfStockException;
import computerhardwarestore.model.Cart;
import computerhardwarestore.model.CartItem;
import computerhardwarestore.model.Product;
import computerhardwarestore.util.Validator;

/** Cart rules: quantity validation and stock checks against the live database stock. */
public class CartController {
    private final Cart cart = new Cart();
    private final ProductDao productDao;

    public CartController(ProductDao productDao) {
        this.productDao = productDao;
    }

    public Cart getCart() { return cart; }

    public void add(Product shown, String quantityText) throws AppException {
        int qty = Validator.quantity(quantityText);
        Product live = load(shown.getId());
        int already = cart.find(live.getId()).map(CartItem::getQuantity).orElse(0);
        requireStock(live, already + qty, already);
        if (already > 0) {
            cart.find(live.getId()).get().setQuantity(already + qty);
        } else {
            cart.add(new CartItem(live.getId(), live.getName(), live.getPrice(), qty, live.getImagePath()));
        }
    }

    /** Sets the cart line to an exact quantity (0 or less is rejected; use remove instead). */
    public void updateQuantity(int productId, String quantityText) throws AppException {
        int qty = Validator.quantity(quantityText);
        CartItem item = cart.find(productId)
                .orElseThrow(() -> new InvalidQuantityException("That item is no longer in your cart."));
        Product live = load(productId);
        requireStock(live, qty, item.getQuantity());
        item.setQuantity(qty);
    }

    public void remove(int productId) { cart.remove(productId); }

    public void clear() { cart.clear(); }

    /**
     * Re-reads live prices and stock for every cart line and fixes the cart. Returns one message per change
     * (price changed, quantity reduced to the available stock, product no longer available).
     */
    public java.util.List<String> refreshPrices() throws DataAccessException {
        java.util.List<String> changes = new java.util.ArrayList<>();
        for (CartItem item : new java.util.ArrayList<>(cart.getItems())) {
            java.util.Optional<Product> found = productDao.findById(item.getProductId());
            if (found.isEmpty() || found.get().getStock() <= 0) {
                cart.remove(item.getProductId());
                changes.add("\"" + item.getName() + "\" is no longer available and was removed from your cart.");
                continue;
            }
            Product live = found.get();
            if (live.getPrice().compareTo(item.getUnitPrice()) != 0) {
                changes.add("The price of \"" + item.getName() + "\" changed from "
                        + computerhardwarestore.util.Formats.money(item.getUnitPrice()) + " to "
                        + computerhardwarestore.util.Formats.money(live.getPrice()) + ".");
                item.setUnitPrice(live.getPrice());
            }
            if (item.getQuantity() > live.getStock()) {
                changes.add("Only " + live.getStock() + " of \"" + item.getName()
                        + "\" are left, so your quantity was reduced.");
                item.setQuantity(live.getStock());
            }
        }
        return changes;
    }

    private Product load(int productId) throws DataAccessException, OutOfStockException {
        return productDao.findById(productId)
                .orElseThrow(() -> new OutOfStockException("Sorry, this product is no longer available."));
    }

    private void requireStock(Product live, int wanted, int alreadyInCart) throws OutOfStockException {
        if (live.getStock() <= 0) {
            throw new OutOfStockException("\"" + live.getName() + "\" is out of stock.");
        }
        if (wanted > live.getStock()) {
            int canAdd = Math.max(0, live.getStock() - alreadyInCart);
            throw new OutOfStockException("Only " + live.getStock() + " of \"" + live.getName()
                    + "\" in stock (you can add " + canAdd + " more).");
        }
    }
}
