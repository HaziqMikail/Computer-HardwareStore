package computerhardwarestore.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Connection settings. Order of precedence: environment variables (DB_HOST, DB_PORT, DB_NAME, DB_USER,
 * DB_PASSWORD), then db.properties in the working directory, then local defaults.
 */
public final class DatabaseConfig {
    public final String host;
    public final int port;
    public final String name;
    public final String user;
    public final String password;

    private DatabaseConfig(String host, int port, String name, String user, String password) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.user = user;
        this.password = password;
    }

    public static DatabaseConfig load() {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (IOException ignored) {
            // No file: fall back to defaults / environment.
        }
        int port;
        try {
            port = Integer.parseInt(pick("DB_PORT", p, "db.port", "5432").trim());
        } catch (NumberFormatException e) {
            port = 5432;
        }
        return new DatabaseConfig(
                pick("DB_HOST", p, "db.host", "localhost"),
                port,
                pick("DB_NAME", p, "db.name", "hardwarestore"),
                pick("DB_USER", p, "db.user", "postgres"),
                pick("DB_PASSWORD", p, "db.password", "postgres"));
    }

    private static String pick(String env, Properties p, String key, String def) {
        String v = System.getenv(env);
        if (v != null && !v.isBlank()) return v;
        return p.getProperty(key, def);
    }

    public String jdbcUrl(String database) {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }
}
