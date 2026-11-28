package computerhardwarestore.dao.jdbc;

import computerhardwarestore.dao.ProductDao;
import computerhardwarestore.exception.DataAccessException;
import computerhardwarestore.model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductDao implements ProductDao {
    private static final String COLUMNS = "id, name, category, price, stock, specs, image_path";
    private final DatabaseManager db = DatabaseManager.getInstance();

    private static Product map(ResultSet rs) throws SQLException {
        return new Product(rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                rs.getBigDecimal("price"), rs.getInt("stock"), rs.getString("specs"), rs.getString("image_path"));
    }

    @Override
    public List<Product> search(String keyword, String category) throws DataAccessException {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM products WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(specs) LIKE ?)");
            String like = "%" + keyword.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        sql.append(" ORDER BY category, name");
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException(DatabaseManager.describe(e), e);
        }
    }

    @Override
    public Optional<Product> findById(int id) throws DataAccessException {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLUMNS + " FROM products WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException(DatabaseManager.describe(e), e);
        }
    }

    @Override
    public List<String> findCategories() throws DataAccessException {
        try (Connection c = db.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT category FROM products ORDER BY category")) {
            List<String> list = new ArrayList<>();
            while (rs.next()) list.add(rs.getString(1));
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(DatabaseManager.describe(e), e);
        }
    }

    @Override
    public Product create(Product p) throws DataAccessException {
        String sql = "INSERT INTO products (name, category, price, stock, specs, image_path) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
            return p;
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    @Override
    public void update(Product p) throws DataAccessException {
        String sql = "UPDATE products SET name=?, category=?, price=?, stock=?, specs=?, image_path=? WHERE id=?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.setInt(7, p.getId());
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("That product no longer exists.");
            }
        } catch (SQLException e) {
            throw translate(e);
        }
    }

    @Override
    public void delete(int id) throws DataAccessException {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(DatabaseManager.describe(e), e);
        }
    }

    private static void bind(PreparedStatement ps, Product p) throws SQLException {
        ps.setString(1, p.getName());
        ps.setString(2, p.getCategory());
        ps.setBigDecimal(3, p.getPrice());
        ps.setInt(4, p.getStock());
        ps.setString(5, p.getSpecs());
        ps.setString(6, p.getImagePath());
    }

    private static DataAccessException translate(SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new DataAccessException("A product with that name already exists.", e);
        }
        if ("23514".equals(e.getSQLState())) {
            return new DataAccessException("Price and stock cannot be negative.", e);
        }
        return new DataAccessException(DatabaseManager.describe(e), e);
    }
}
