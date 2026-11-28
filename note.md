# CodeFusion - Notes

Quick reference for running, using and maintaining the project.

## 1. Logins

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `Admin@123` |
| Customer | register your own on the Register tab | must have 8+ chars with upper, lower, number and special character |

The admin account is created automatically on first run. Change that password if this is ever shown to anyone.

## 2. How to open the admin page

There is no separate admin URL or button. The **role decides the screens**:

1. Start the app.
2. On the login screen type `admin` / `Admin@123` and press Login.
3. You land on **Inventory Management** straight away.

Admin menu bar:
- **Admin > Inventory** - add, edit, delete products.
- **Admin > All orders & sales** - every customer's orders plus total revenue. Double-click an order (or select it and click *View receipt*) to open its receipt.
- **View > Shop preview** - see the shop as customers see it (admins have no cart).
- **File > Log out / Exit**.

### Make another user an admin
Register them normally, then in pgAdmin open **Query Tool** on the `hardwarestore` database and run:

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'theirusername';
```

They must log out and log in again. To make them a customer again, use `'CUSTOMER'`.

### Forgot / want to reset the admin password
There is no "change password" screen yet. In pgAdmin Query Tool run:

```sql
DELETE FROM users WHERE username = 'admin';
```

Restart the app. When no admin exists it recreates `admin` / `Admin@123`.
(If that admin already placed orders the delete will be refused; use the role UPDATE above on another account instead.)

## 3. The new flow

### Customer
```
Register  ->  Login  ->  Shop (search / filter by category)
   ->  Add to Cart (quantity checked against live stock)
   ->  Cart (change quantity / remove)
   ->  Checkout (choose Cash / Card / Online Banking, pay - simulated)
   ->  Receipt window (Save as file / Print)  ->  My Orders (history)
```
- **Shop** menu: *Browse products*, *Cart (n)*, *My orders*.
- The header shows the cart count. Double-click any past order to reopen its receipt.
- Cart is kept in memory only; it is emptied on logout or after a successful payment.

### Admin
```
Login (admin)  ->  Inventory  ->  pick a product in the table  ->  edit on the right  ->  Save
                                 or click New to add one, Delete to remove one
              ->  All orders & sales  ->  view any receipt
```
- Price must be a number with at most 2 decimals; stock must be a whole number >= 0.
- **Image > Choose...** copies the picture into a `product-images/` folder next to where you run the app.
- Deleting a product does **not** damage past orders; they keep the name and price they were bought at.
- Stock goes down automatically after every successful purchase. If two customers race for the last item, the second gets a clear "not enough stock" message and nothing is charged or saved.

## 4. Payment is a dummy
No money moves anywhere. Details are only checked for format (card: 16 digits, expiry not in the past, 3-digit CVV; bank: 10-digit account). The receipt shows only the last 4 digits. Card number and CVV are never stored.

## 5. pgAdmin tips

- **See the data:** Servers > PostgreSQL > Databases > `hardwarestore` > Schemas > public > Tables, right-click a table > *View/Edit Data > All Rows*.
- Tables: `users`, `products`, `orders`, `order_items`.
- **Passwords** in `users.password_hash` look like `120000:...:...` - that is correct (salted hash), it is not readable on purpose.
- **Wipe all orders but keep users/products:**
  ```sql
  DELETE FROM orders;   -- order_items are removed with them
  ```
- **Restock everything to 20:** `UPDATE products SET stock = 20;`
- **Start completely fresh:** in pgAdmin right-click the `hardwarestore` database > Delete, create it again (empty), restart the app.
- The 24 starting products are only inserted when the `products` table is empty. Adding rows later is done from the admin page.

## 6. Troubleshooting

| Message | Meaning / fix |
|---|---|
| "Cannot reach the database server..." | PostgreSQL is not running, or host/port in `db.properties` is wrong. Check pgAdmin can connect. |
| "The database rejected the username or password" | `db.user` / `db.password` in `db.properties` do not match your pgAdmin login. |
| "The database does not exist" | Create a database named exactly as `db.name` (default `hardwarestore`) in pgAdmin. |
| Product shows "No image" | The image path is missing. Re-pick it in Inventory. Bundled images come from `src/images`. |
| Changes to `db.properties` ignored | It is read from the folder you start the app in (the project root). Environment variables override it. |

## 7. Project layout

```
src/computerhardwarestore/
  model/       data classes
  view/        Swing screens
  controller/  business rules
  dao/, dao/jdbc/  database access (interfaces + PostgreSQL)
  payment/     payment methods (add a new one here)
  exception/   custom exceptions
  util/        validation, hashing, formatting
lib/           PostgreSQL JDBC driver
db.properties  your DB settings (not committed)
```

To add a payment method: create a class implementing `PaymentMethod` and add one line in `PaymentRegistry`.

## 8. Known limitations
- No "change password" or "forgot password" screen.
- Cart is not saved between sessions.
- No product categories screen: a category is created by typing a new name in the Category box when saving a product.
- Written and compiled, but not yet run against a real PostgreSQL server at the time of writing - report anything odd you see.
