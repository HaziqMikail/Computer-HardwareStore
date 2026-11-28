package computerhardwarestore.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Formats {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private Formats() {}

    public static String money(BigDecimal amount) {
        return String.format("RM %,.2f", amount);
    }

    /** Escapes text so it can be placed safely inside Swing's HTML labels. */
    public static String html(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String dateTime(LocalDateTime dt) {
        return dt == null ? "" : DATE_TIME.format(dt);
    }
}
