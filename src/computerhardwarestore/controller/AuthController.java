package computerhardwarestore.controller;

import computerhardwarestore.dao.UserDao;
import computerhardwarestore.exception.AppException;
import computerhardwarestore.exception.DataAccessException;
import computerhardwarestore.exception.InvalidLoginException;
import computerhardwarestore.exception.ValidationException;
import computerhardwarestore.model.Role;
import computerhardwarestore.model.User;
import computerhardwarestore.util.PasswordUtil;
import computerhardwarestore.util.Validator;
import java.time.LocalDate;

/** Registration, login and the currently logged-in user. */
public class AuthController {
    private final UserDao userDao;
    private User currentUser;

    public AuthController(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getCurrentUser() { return currentUser; }

    public void logout() { currentUser = null; }

    public User login(String username, char[] password) throws AppException {
        if (username == null || username.isBlank() || password == null || password.length == 0) {
            throw new InvalidLoginException("Please enter your username and password.");
        }
        String pw = new String(password);
        User user = userDao.findByUsername(username.trim())
                .orElseThrow(() -> new InvalidLoginException("Invalid username or password."));
        if (!PasswordUtil.verify(pw, user.getPasswordHash())) {
            throw new InvalidLoginException("Invalid username or password.");
        }
        currentUser = user;
        return user;
    }

    public User register(String firstName, String lastName, String day, String month, String year,
                         String username, char[] password, char[] repeat, boolean acceptedTerms)
            throws AppException {
        String first = Validator.name(firstName, "First name");
        String last = Validator.name(lastName, "Last name");
        LocalDate birth = Validator.birthDate(day, month, year);
        String user = Validator.username(username);
        Validator.password(new String(password), new String(repeat));
        if (!acceptedTerms) {
            throw new ValidationException("You must accept the Terms and Conditions.");
        }
        if (userDao.findByUsername(user).isPresent()) {
            throw new DataAccessException("That username is already taken. Please choose another.");
        }
        return userDao.create(new User(0, first, last, birth, user,
                PasswordUtil.hash(new String(password)), Role.CUSTOMER));
    }
}
