package computerhardwarestore.dao;

import computerhardwarestore.exception.DataAccessException;
import computerhardwarestore.model.User;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username) throws DataAccessException;

    /** Inserts the user and returns it with its generated id. Throws if the username is taken. */
    User create(User user) throws DataAccessException;
}
