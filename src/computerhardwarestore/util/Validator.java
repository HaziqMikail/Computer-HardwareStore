package computerhardwarestore.util;

import computerhardwarestore.exception.InvalidQuantityException;
import computerhardwarestore.exception.ValidationException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/** Reusable input validation. Every method throws a friendly exception instead of returning flags. */
public final class Validator {
    private static final Pattern NAME = Pattern.compile("[a-zA-Z]+( [a-zA-Z]+)*");
    private static final Pattern PASSWORD = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=|\\[\\]{};:'\",.<>?/`~]).{8,}$");
    private static final List<String> MONTHS =
            List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

    private Validator() {}

    public static String name(String value, String label) throws ValidationException {
        String v = value == null ? "" : value.trim();
        if (!NAME.matcher(v).matches()) {
            throw new ValidationException(label + " must contain only letters.");
        }
        return v;
    }

    public static LocalDate birthDate(String day, String month, String year) throws ValidationException {
        int m = MONTHS.indexOf(month);
        if (day == null || year == null || m < 0 || "Day".equals(day) || "Year".equals(year)) {
            throw new ValidationException("Please select a valid birthdate.");
        }
        LocalDate date;
        try {
            date = LocalDate.of(Integer.parseInt(year), m + 1, Integer.parseInt(day));
        } catch (DateTimeException | NumberFormatException e) {
            throw new ValidationException("That birthdate does not exist. Please check the day and month.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Birthdate cannot be in the future.");
        }
        return date;
    }

    public static String username(String value) throws ValidationException {
        String v = value == null ? "" : value.trim();
        if (v.length() < 4) {
            throw new ValidationException("Username must be at least 4 characters long.");
        }
        if (v.length() > 50 || !v.matches("[A-Za-z0-9._-]+")) {
            throw new ValidationException("Username may only contain letters, digits, '.', '_' and '-' (max 50).");
        }
        return v;
    }

    public static void password(String password, String repeat) throws ValidationException {
        if (!PASSWORD.matcher(password).matches()) {
            throw new ValidationException("Password must be at least 8 characters long and contain at least one "
                    + "uppercase letter, lowercase letter, number and special character.");
        }
        if (!password.equals(repeat)) {
            throw new ValidationException("Passwords do not match.");
        }
    }

    /** Parses a positive whole-number quantity from user text. */
    public static int quantity(String text) throws InvalidQuantityException {
        if (text == null || text.isBlank()) {
            throw new InvalidQuantityException("Please enter a quantity.");
        }
        int q;
        try {
            q = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidQuantityException("Quantity must be a whole number (no letters or decimals).");
        }
        if (q <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than 0.");
        }
        return q;
    }
}
