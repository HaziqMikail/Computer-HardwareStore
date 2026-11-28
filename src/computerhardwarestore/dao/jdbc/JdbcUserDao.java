package computerhardwarestore.dao.jdbc;

import computerhardwarestore.dao.UserDao;
import computerhardwarestore.exception.DataAccessException;
import computerhardwarestore.model.Role;
import computerhardwarestore.model.User;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class JdbcUserDao implements UserDao {
    private final DatabaseManager db = DatabaseManager.getInstance();

    @Override
    public Optional<User> findByUsername(String username) throws DataAccessException {
        String sql = "SELECT id, first_name, last_name, birth_date, username, password_hash, role "
                + "FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Date bd = rs.getDate("birth_date");
                return Optional.of(new User(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
                        bd == null ? null : bd.toLocalDate(), rs.getString("username"),
                        rs.getString("password_hash"), Role.valueOf(rs.getString("role"))));
            }
        } catch (SQLException e) {
            throw new DataAccessException(DatabaseManager.describe(e), e);
        }
    }

    @Override
    public User create(User user) throws DataAccessException {
        String sql = "INSERT INTO users (first_name, last_name, birth_date, username, password_hash, role) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setDate(3, user.getBirthDate() == null ? null : Date.valueOf(user.getBirthDate()));
            ps.setString(4, user.getUsername());
            ps.setString(5, user.getPasswordHash());
            ps.setString(6, user.getRole().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
            return user;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new DataAccessException("That username is already taken. Please choose another.", e);
            }
            throw new DataAccessException(DatabaseManager.describe(e), e);
        }
    }
}
