package computerhardwarestore.dao;

import computerhardwarestore.exception.DataAccessException;
import computerhardwarestore.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
    /** @param keyword matches name or specs (null/blank = any); @param category exact match (null/blank = any) */
    List<Product> search(String keyword, String category) throws DataAccessException;

    Optional<Product> findById(int id) throws DataAccessException;

    List<String> findCategories() throws DataAccessException;

    Product create(Product product) throws DataAccessException;

    void update(Product product) throws DataAccessException;

    void delete(int id) throws DataAccessException;
}
