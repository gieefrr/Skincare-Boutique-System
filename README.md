# Skincare Boutique System

Radiant Glow is a desktop system for running a skincare shop, built with Java Swing. It manages products, customer orders, sales and staff accounts. Data is saved in text files, so no database setup is needed.

Built for IT 203 – Object-Oriented Programming.

Members: Cajutay, Maria Angelica V.
         Cruz, Derone James C. 
         BSIT2AG2

## Setup Instructions

You need JDK 25 and Apache NetBeans.

1. Download or clone this repository.
2. In NetBeans, choose File > Open Project and select the project folder.
3. Run the project. The main class is skincareboutique.Main.

The data folder is created on the first run, with the default accounts and 15 sample products.

## Default Accounts

| Username | Password | Access |
|---|---|---|
| admin | admin123 | Admin |
| staff | staff123 | Staff |

The customers and products in these screenshots are sample data.

---

## Login

Admin and Staff log in here. Only an admin can create new accounts.

<img src="screenshots/login.png" alt="Login form" width="600">

After 3 wrong tries, a Staff account is blocked and the system closes. Only an admin can unblock it.

<img src="screenshots/login-blocked.png" alt="Account blocked message" width="600">

---

## Products

Admins can add, update and delete products. Product IDs are numbered automatically, and required fields turn red if left empty. The line under the table counts low-stock products (5 or less).

Everyone can search by keyword, find a product by ID, and sort by ID, price or name.

<img src="screenshots/products-admin.png" alt="Products tab as admin" width="800">

**View Info** shows a product's key ingredients, what it's best for and its benefits.

<img src="screenshots/product-view-info.png" alt="Product information" width="420">

Deleting a product asks for an admin password, and **Undo Delete** can bring it back. The same password check protects deleting users and voiding sales.

<img src="screenshots/admin-password-check.png" alt="Admin password check" width="800">

---

## Orders and Sales

One order can hold many products. Buying 3 or more of one sunscreen gives 5% off, and customers can get a Loyalty Card (5%), Student (10%), or Senior Citizen / PWD (20%) discount.

<img src="screenshots/order-discount-total.png" alt="Order with a student discount" width="800">

Placed orders wait in a first-come, first-served queue. A wrong order can be cancelled from anywhere in the line.

<img src="screenshots/order-queue.png" alt="Three orders in the queue" width="800">

Process Next Order checks the stock of every product, lowers it, and shows a receipt. If a product is short, the whole order is cancelled.

<img src="screenshots/order-receipt.png" alt="Order receipt" width="380">

Sales Report shows weekly or monthly totals, the best seller and the list of orders. If a wrong order was already processed, an admin can use Void Last Sale to cancel it and return its stock.

<img src="screenshots/report-weekly.png" alt="Weekly sales report" width="560">

---

## User Accounts

Only admins see this tab. They can add, block, unblock and delete users, but can't block or delete their own account or delete the last admin.

<img src="screenshots/users.png" alt="User Accounts tab" width="800">

When adding a user, the form shows errors while the admin types. A username needs 4 to 20 letters, numbers or underscores. A password needs at least 6 characters with both letters and numbers.

<img src="screenshots/user-add-errors.png" alt="Sign-up form with errors" width="400">

Every user can also change their password and log out using the buttons at the top of the dashboard.

---

## Staff View

Staff can view products and handle orders. The product form, the User Accounts tab and the Void Last Sale button are hidden.

<img src="screenshots/staff-products.png" alt="Products tab as staff" width="800">

---

## UML Class Diagram

<a href="uml/uml-class-diagram.png"><img src="uml/uml-class-diagram.png" alt="UML class diagram of the Skincare Boutique System" width="800"></a>

---

## Project Structure

| Package | Contents |
|---|---|
| skincareboutique | Main, where the program starts |
| gui | The login form, dashboard, tabs, dialogs and theme |
| data | DataStore for saving and loading, SortSearch for sorting and searching |
| model | User, Admin, Staff, Product, the 10 product types, Sellable, Order and OrderItem |
