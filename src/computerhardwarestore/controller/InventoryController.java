package computerhardwarestore.controller;

import computerhardwarestore.dao.ProductDao;
import computerhardwarestore.exception.AppException;
import computerhardwarestore.exception.ValidationException;
import computerhardwarestore.model.Product;
import java.math.BigDecimal;
import java.util.List;

/** Admin product management: validates form input, then add/edit/delete through the DAO. */
public class InventoryController {
    private final ProductDao productDao;

    public InventoryController(ProductDao productDao) {
        this.productDao = productDao;
    }

    public List<Product> list(String keyword) throws AppException {
        return productDao.search(keyword, null);
    }

    public List<String> categories() throws AppException {
        return productDao.findCategories();
    }

    /** id = 0 creates a new product; otherwise updates the existing one. */
    public Product save(int id, String name, String category, String priceText, String stockText,
                        String specs, String imagePath) throws AppException {
        String n = name == null ? "" : name.trim();
        String c = category == null ? "" : category.trim();
        if (n.isEmpty()) throw new ValidationException("Product name is required.");
        if (n.length() > 120) throw new ValidationException("Product name is too long (max 120 characters).");
        if (c.isEmpty()) throw new ValidationException("Category is required.");
        if (c.length() > 60) throw new ValidationException("Category is too long (max 60 characters).");

        BigDecimal price;
        try {
            price = new BigDecimal(priceText == null ? "" : priceText.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Price must be a number, for example 249.90.");
        }
        if (price.signum() < 0) throw new ValidationException("Price cannot be negative.");
        if (price.scale() > 2) throw new ValidationException("Price can have at most 2 decimal places.");
        if (price.compareTo(new BigDecimal("99999999")) > 0) throw new ValidationException("Price is too large.");

        int stock;
        try {
            stock = Integer.parseInt(stockText == null ? "" : stockText.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Stock must be a whole number.");
        }
        if (stock < 0) throw new ValidationException("Stock cannot be negative.");

        Product p = new Product(id, n, c, price, stock, specs == null ? "" : specs.trim(),
                imagePath == null ? "" : imagePath.trim());
        if (id == 0) {
            return productDao.create(p);
        }
        productDao.update(p);
        return p;
    }

    public void delete(int id) throws AppException {
        productDao.delete(id);
    }
}
