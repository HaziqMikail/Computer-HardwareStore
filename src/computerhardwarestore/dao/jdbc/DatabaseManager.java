package computerhardwarestore.dao.jdbc;

import computerhardwarestore.exception.DataAccessException;
import computerhardwarestore.util.DatabaseConfig;
import computerhardwarestore.util.PasswordUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton that owns the database settings and hands out connections. On first use it creates the database,
 * the tables and the seed data (default admin account plus the original product catalogue) if they are missing.
 */
public final class DatabaseManager {
    private static DatabaseManager instance;

    private final DatabaseConfig config;
    private boolean initialised;

    private DatabaseManager() {
        this.config = DatabaseConfig.load();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /** Opens a new connection. Callers must close it (try-with-resources / finally). */
    public Connection getConnection() throws DataAccessException {
        try {
            return DriverManager.getConnection(config.jdbcUrl(config.name), config.user, config.password);
        } catch (SQLException e) {
            throw new DataAccessException(describe(e), e);
        }
    }

    /** Creates the database, schema and seed data when needed. Safe to call repeatedly. */
    public synchronized void initialise() throws DataAccessException {
        if (initialised) return;
        try {
            ensureDatabaseExists();
        } catch (DataAccessException ignored) {
            // Maybe the user may not connect to the maintenance DB; getConnection below reports the real problem.
        }
        try (Connection c = getConnection()) {
            createSchema(c);
            seedAdmin(c);
            seedProducts(c);
        } catch (SQLException e) {
            throw new DataAccessException("Could not prepare the database: " + e.getMessage(), e);
        }
        initialised = true;
    }

    private void ensureDatabaseExists() throws DataAccessException {
        try (Connection c = DriverManager.getConnection(config.jdbcUrl("postgres"), config.user, config.password);
             PreparedStatement check = c.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
            check.setString(1, config.name);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    try (Statement st = c.createStatement()) {
                        st.executeUpdate("CREATE DATABASE \"" + config.name.replace("\"", "") + "\"");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(describe(e), e);
        }
    }

    private void createSchema(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "id SERIAL PRIMARY KEY,"
                    + "first_name VARCHAR(50) NOT NULL,"
                    + "last_name VARCHAR(50) NOT NULL,"
                    + "birth_date DATE,"
                    + "username VARCHAR(50) NOT NULL UNIQUE,"
                    + "password_hash VARCHAR(255) NOT NULL,"
                    + "role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER')");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS products ("
                    + "id SERIAL PRIMARY KEY,"
                    + "name VARCHAR(120) NOT NULL UNIQUE,"
                    + "category VARCHAR(60) NOT NULL,"
                    + "price NUMERIC(10,2) NOT NULL CHECK (price >= 0),"
                    + "stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),"
                    + "specs TEXT NOT NULL DEFAULT '',"
                    + "image_path VARCHAR(255) NOT NULL DEFAULT '')");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS orders ("
                    + "id SERIAL PRIMARY KEY,"
                    + "user_id INTEGER NOT NULL REFERENCES users(id),"
                    + "order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "subtotal NUMERIC(12,2) NOT NULL,"
                    + "delivery_charge NUMERIC(10,2) NOT NULL,"
                    + "total NUMERIC(12,2) NOT NULL,"
                    + "payment_method VARCHAR(40) NOT NULL,"
                    + "payment_detail VARCHAR(120) NOT NULL DEFAULT '')");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS order_items ("
                    + "id SERIAL PRIMARY KEY,"
                    + "order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,"
                    + "product_id INTEGER REFERENCES products(id) ON DELETE SET NULL,"
                    + "product_name VARCHAR(120) NOT NULL,"
                    + "unit_price NUMERIC(10,2) NOT NULL,"
                    + "quantity INTEGER NOT NULL CHECK (quantity > 0))");
        }
    }

    /** Default admin account (username "admin", password "Admin@123"), created only when no admin exists. */
    private void seedAdmin(Connection c) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")) {
            rs.next();
            if (rs.getInt(1) > 0) return;
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO users (first_name, last_name, birth_date, username, password_hash, role) "
                        + "VALUES ('Store', 'Admin', NULL, 'admin', ?, 'ADMIN') ON CONFLICT (username) DO NOTHING")) {
            ps.setString(1, PasswordUtil.hash("Admin@123"));
            ps.executeUpdate();
        }
    }

    private void seedProducts(Connection c) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM products")) {
            rs.next();
            if (rs.getInt(1) > 0) return;
        }
        String[][] seed = {
            {"Dell XPS", "Laptops", "4000", "Processor: i7, RAM: 16GB, Storage: 512GB SSD", "images/computer1.jpeg"},
            {"MacBook Pro", "Laptops", "6000", "Processor: M1, RAM: 16GB, Storage: 512GB SSD", "images/computer2.jpg"},
            {"HP Pavilion", "Laptops", "3000", "Processor: Ryzen 5, RAM: 8GB, Storage: 1TB HDD", "images/computer3.jpg"},
            {"MSI Stealth 15 A13VE", "Laptops", "4899", "Processor: Intel Core i7, RAM: 16GB, Storage: 1TB SSD", "images/computer4.png"},
            {"Corsair K95 RGB Platinum", "Keyboards", "749", "Switches: Cherry MX Speed, Backlighting: Per-key RGB, Macro Keys: 6", "images/keyboard1.jpg"},
            {"Razer BlackWidow V3", "Keyboards", "459", "Switches: Razer Green/Yellow, Backlighting: Razer Chroma RGB, Macro Keys: Fully programmable", "images/keyboard2.jpg"},
            {"Logitech G915 TKL", "Keyboards", "869", "Switches: Low-profile GL, Backlighting: LIGHTSYNC RGB, Macro Keys: 5", "images/keyboard3.png"},
            {"SteelSeries Apex Pro", "Keyboards", "800", "Switches: Adjustable OmniPoint, Backlighting: Per-key RGB, Macro Keys: No dedicated macro keys", "images/keyboard4.jpg"},
            {"Razer Kraken X", "Headphones", "179", "Driver Size: 40mm, Surround Sound: 7.1, Microphone: Cardioid", "images/headphone1.jpg"},
            {"HyperX Cloud II Wireless", "Headphones", "489", "Driver Size: 53mm, Surround Sound: Virtual 7.1, Microphone: Detachable noise-cancelling", "images/headphone2.jpg"},
            {"SteelSeries Arctis 7", "Headphones", "699", "Driver Size: 40mm, Surround Sound: DTS Headphone2.0, Microphone: Bidirectional ClearCast", "images/headphone3.jpg"},
            {"Razer BlackShark V2 Pro", "Headphones", "600", "Driver Size: 50mm, Surround Sound: THX Spatial Audio, Microphone: Detachable", "images/headphone4.png"},
            {"ASUS Prime", "Motherboards", "500", "Socket: LGA 1200, Chipset: Intel Z490, RAM Support: 128GB", "images/m1.png"},
            {"MSI Pro", "Motherboards", "450", "Socket: AM4, Chipset: B450, RAM Support: 64GB", "images/m2.png"},
            {"Gigabyte Aorus", "Motherboards", "600", "Socket: LGA 1151, Chipset: Intel Z390, RAM Support: 128GB", "images/m3.png"},
            {"ASRock Steel", "Motherboards", "550", "Socket: AM4, Chipset: B550, RAM Support: 128GB", "images/m4.png"},
            {"Corsair Vengeance", "RAM", "350", "Capacity: 16GB, Type: DDR4, Speed: 3200MHz", "images/ra1.jpeg"},
            {"G.Skill Ripjaws", "RAM", "320", "Capacity: 16GB, Type: DDR4, Speed: 3200MHz", "images/ra2.jpeg"},
            {"Kingston HyperX", "RAM", "280", "Capacity: 16GB, Type: DDR4, Speed: 3200MHz", "images/ra3.jpg"},
            {"Crucial Ballistix", "RAM", "300", "Capacity: 16GB, Type: DDR4, Speed: 3200MHz", "images/ra4.jpg"},
            {"Samsung EVO", "Storage", "300", "Capacity: 500GB, Type: SSD, Speed: 550MB/s", "images/ro1.jpg"},
            {"WD Blue", "Storage", "250", "Capacity: 1TB, Type: HDD, Speed: 5400RPM", "images/ro2.jpg"},
            {"Kingston A400", "Storage", "200", "Capacity: 240GB, Type: SSD, Speed: 500MB/s", "images/ro3.jpg"},
            {"Crucial MX500", "Storage", "270", "Capacity: 500GB, Type: SSD, Speed: 560MB/s", "images/ro4.jpg"},
        };
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO products (name, category, price, stock, specs, image_path) VALUES (?, ?, ?, 20, ?, ?) "
                        + "ON CONFLICT (name) DO NOTHING")) {
            for (String[] p : seed) {
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setBigDecimal(3, new java.math.BigDecimal(p[2]));
                ps.setString(4, p[3]);
                ps.setString(5, p[4]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Turns low-level SQL errors into a message a customer can act on. */
    static String describe(SQLException e) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        if (state.startsWith("08")) {
            return "Cannot reach the database server. Please make sure PostgreSQL is running and the connection "
                    + "settings in db.properties are correct.";
        }
        if (state.equals("28P01") || state.equals("28000")) {
            return "The database rejected the username or password. Please check db.properties.";
        }
        if (state.equals("3D000")) {
            return "The database does not exist.";
        }
        return "A database error occurred: " + e.getMessage();
    }
}
