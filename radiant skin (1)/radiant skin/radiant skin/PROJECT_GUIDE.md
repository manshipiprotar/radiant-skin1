# Radiant Skin - Skincare E-Commerce Platform (BCA Sem 5 Academic Project)

---

## 📌 Project Overview
**Radiant Skin** is a full-featured Skincare & Beauty e-commerce web application with Customer Storefront, Shopping Cart, Checkout, Order Tracking, and an Admin Management Dashboard.

---

## 🚀 How to Run the Project (કોઈપણ Setup વગર)

### 1. Frontend & Admin Panel (Direct Run):
1. આ ફોલ્ડરમાંથી `radiant-skin-frontend` ફોલ્ડર ખોલો.
2. સીધું [**`index.html`**](file:///c:/Users/Nikhil/Downloads/radiant%20skin/radiant%20skin/radiant-skin-frontend/index.html) ફાઇલ પર ડબલ ક્લિક કરીને બ્રાઉઝરમાં ખોલો અથવા VS Code માં **"Open with Live Server"** કરો.
3. તમામ ફીચર્સ (પ્રોડક્ટ્સ, કાર્ટ, વિશલિસ્ટ, ચેકઆઉટ, ઓર્ડર ટ્રેકિંગ, રિવ્યૂ) તરત જ બ્રાઉઝરના LocalStorage દ્વારા કામ કરશે.

### 2. Admin Panel Access:
- **Login URL:** `admin-login.html`
- **Email:** `admin@radiantskin.com`
- **Password:** `admin123`
- **Admin Features:**
  - View Dashboard stats (Revenue, Orders, Users, Products)
  - Manage & Add Products
  - Manage Orders (Step-by-step: Processing ➔ Shipped ➔ Delivered / Cancelled)
  - View Registered Users

---

## 🗄️ Database Setup (Optional for Java Backend / Viva):
1. **MySQL Workbench** અથવા XAMPP phpMyAdmin ખોલો.
2. આ પ્રોજેક્ટના મુખ્ય ફોલ્ડરમાં રહેલી **`database.sql`** ફાઇલ ઈમ્પોર્ટ (Import) કરો અથવા રન કરો.
3. આ સ્ક્રિપ્ટ આપમેળે `radiant_skin` નામનો ડેટાબેઝ અને `users`, `products`, `orders` ટેબલ્સ બનાવી દેશે.

---

## 📂 Technologies Used:
- **Frontend:** HTML5, CSS3, JavaScript (ES6), Bootstrap 5.3.3, Bootstrap Icons, Google Fonts.
- **Backend:** Java Servlets (`RegisterServlet.java`, `LoginServlet.java`), JDBC.
- **Database:** MySQL.
