package computerhardwarestore.controller;

import computerhardwarestore.dao.ProductDao;
import computerhardwarestore.exception.DataAccessException;
import computerhardwarestore.model.Product;
import java.util.List;

/** Read access to the product catalogue for customers (search, filter, categories). */
public class CatalogController {
    private final ProductDao productDao;

    public CatalogController(ProductDao productDao) {
        this.productDao = productDao;
    }

    public List<Product> search(String keyword, String category) throws DataAccessException {
        return productDao.search(keyword, category);
    }

    public List<String> categories() throws DataAccessException {
        return productDao.findCategories();
    }
}
