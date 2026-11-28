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
            cart.add(new CartItem(live.getId(), live.getName(), live.getPrice(), qty));
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
