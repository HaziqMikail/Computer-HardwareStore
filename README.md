# 💻 Computer & Hardware - Computer & Hardware Store (Java Swing + PostgreSQL)

A desktop store application. Customers browse products, manage a cart, pay (**simulated - no real payment**) and get a
receipt. Administrators manage inventory and view every order. Data is stored in PostgreSQL.

## Features
- **Accounts:** sign-up/login with validation, PBKDF2-hashed passwords, Customer and Admin roles.
- **Catalogue:** products loaded from the database with images, categories, search, category filter and live stock.
- **Cart:** quantity validation, stock checks, change quantity, remove.
- **Payment (dummy):** Cash, Card, Online Banking. Each is a `PaymentMethod` class; add a new one by implementing the
  interface and registering it in `PaymentRegistry`. Only the format of details is checked; nothing is charged.
- **Receipts:** built from the saved order (order ID, date/time, payment method), shown as a clean summary and can be saved to a text file.
- **Inventory (admin):** add, edit, delete products, change price/stock, choose an image. Stock drops automatically
  after each purchase.
- **Orders:** customers see their own history; admins see all orders with total revenue.
- **Error handling:** custom exceptions (`InvalidLoginException`, `OutOfStockException`, `InvalidQuantityException`,
  ...) are shown as friendly dialogs. Database errors are caught in the data layer; connections are always closed.

## Database setup
Using **pgAdmin 4** (installed together with PostgreSQL by the EnterpriseDB installer).

1. **Check the server is running.** Open pgAdmin and expand *Servers > PostgreSQL*. Enter the master password you chose
   during installation when asked. If it connects (green icon), the server is running.
   Note the **port** (right-click the server > *Properties > Connection*; default `5432`) and the **username**
   (default `postgres`) with the password you set for it.
2. **Create the database** (recommended, so you can see it before the app runs):
   right-click *Databases > Create > Database...*, name it `hardwarestore`, owner `postgres`, click *Save*.
   You can skip this step: the app will try to create it itself.
3. **Tell the app how to connect.** In the project folder copy `db.properties.example` to `db.properties` and edit it:
   ```
   db.host=localhost
   db.port=5432
   db.name=hardwarestore
   db.user=postgres
   db.password=<the password you use to log in to pgAdmin's server>
   ```
   (Alternatively set the `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` environment variables.)
4. **Run the app.** On first run it creates the tables (`users`, `products`, `orders`, `order_items`) and the
   starting data (24 products, 20 in stock each, plus the admin account).
5. **Check in pgAdmin:** right-click `hardwarestore` > *Refresh*, then *Schemas > public > Tables*. Right-click a
   table > *View/Edit Data > All Rows* to see the rows.

**Default admin login:** username `admin`, password `Admin@123` - change it for anything beyond a demo.

If the app cannot connect it shows a message explaining why. See `note.md` for fixes, SQL snippets for pgAdmin's
Query Tool, and how to open the admin page.

## How to run
- **NetBeans:** open the project and press Run. Main class: `computerhardwarestore.ComputerHardwareStore`.
  The libraries in `lib/` (PostgreSQL driver and FlatLaf for the modern look) are already on the classpath.
- **Command line:**
  ```
  javac -encoding UTF-8 -cp "lib/postgresql-42.7.4.jar:lib/flatlaf-3.5.4.jar" -d out $(find src -name "*.java")
  cp -r src/images out/images
  java -cp "out:lib/postgresql-42.7.4.jar:lib/flatlaf-3.5.4.jar" computerhardwarestore.ComputerHardwareStore   (use ; instead of : on Windows)
  ```

## Architecture
| Package | Role |
|---|---|
| `model` | Data classes: `User`, `Product`, `Cart`, `Order`, ... |
| `view` | Swing UI. `AppWindow` shows login, then a role shell: `shell/CustomerShell` (top bar) or `shell/AdminShell` (sidebar). Pages grouped by module: `auth`, `shop`, `cart` (cart + checkout), `order` (history + receipt), `admin` (dashboard + inventory), `common` (theme, cards, buttons, toast) |
| `controller` | Application rules (`AuthController`, `CartController`, `OrderController`, `InventoryController`) |
| `dao` / `dao.jdbc` | DAO interfaces and their PostgreSQL implementations; `DatabaseManager` is the **Singleton** |
| `payment` | `PaymentMethod` interface plus Cash, Card, Online Banking |
| `exception` | Custom checked exceptions |
| `util` | Validation, password hashing, formatting, config |

Patterns: **MVC**, **Singleton** (`DatabaseManager`), **DAO**. Controllers depend on the DAO interfaces; the concrete
classes are chosen in one place (`AppContext`).

## Author
Mikail - Diploma in Software Engineering, German Malaysian Institute
