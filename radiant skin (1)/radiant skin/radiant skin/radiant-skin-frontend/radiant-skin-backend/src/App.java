import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

public class App {
    private static final int PORT = 8080;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/radiant_skin?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   Radiant Skin — Java Backend Server Starting   ");
        System.out.println("==================================================");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initDatabase();

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // API Endpoints
            server.createContext("/api/health", new HealthHandler());
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/register", new RegisterHandler());
            server.createContext("/api/users", new UsersHandler());
            server.createContext("/api/products", new ProductsHandler());
            server.createContext("/api/categories", new CategoriesHandler());
            server.createContext("/api/orders", new OrdersHandler());
            server.createContext("/api/contact", new ContactHandler());
            server.createContext("/api/reviews", new ReviewsHandler());
            server.createContext("/api/reports", new ReportsHandler());

            // Static File Server for Frontend
            server.createContext("/", new StaticFileHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("🚀 Backend Server running successfully on http://localhost:" + PORT);
            System.out.println("🎉 MySQL Database connected and initialized.");
            System.out.println("==================================================");
        } catch (Exception e) {
            System.err.println("❌ Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private static void initDatabase() {
        try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
            // 1. Users table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "phone VARCHAR(20), " +
                    "city VARCHAR(100), " +
                    "password VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(20) DEFAULT 'customer', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'active'");
            } catch (Exception ignored) {}

            // 2. Categories table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS categories (" +
                    "category_key VARCHAR(50) PRIMARY KEY, " +
                    "label VARCHAR(100) NOT NULL, " +
                    "color VARCHAR(20), " +
                    "accent VARCHAR(20), " +
                    "shape VARCHAR(20))");

            // 3. Products table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS products (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "product_code VARCHAR(50) UNIQUE, " +
                    "name VARCHAR(150) NOT NULL, " +
                    "brand VARCHAR(100), " +
                    "category_key VARCHAR(50), " +
                    "price DECIMAL(10,2) NOT NULL, " +
                    "old_price DECIMAL(10,2), " +
                    "discount VARCHAR(20), " +
                    "rating DECIMAL(2,1) DEFAULT 4.5, " +
                    "reviews_count INT DEFAULT 0, " +
                    "image_url LONGTEXT, " +
                    "description TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            try {
                stmt.executeUpdate("ALTER TABLE products MODIFY COLUMN image_url LONGTEXT");
            } catch (Exception ignored) {}

            // 4. Orders table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "order_number VARCHAR(50) UNIQUE, " +
                    "user_id INT, " +
                    "customer_name VARCHAR(100), " +
                    "customer_email VARCHAR(100), " +
                    "customer_phone VARCHAR(20), " +
                    "shipping_address TEXT, " +
                    "city VARCHAR(100), " +
                    "state VARCHAR(50) DEFAULT 'Gujarat', " +
                    "pincode VARCHAR(20), " +
                    "payment_method VARCHAR(50), " +
                    "total_amount DECIMAL(10,2) NOT NULL, " +
                    "order_status VARCHAR(50) DEFAULT 'Processing', " +
                    "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            try {
                stmt.executeUpdate("ALTER TABLE orders ADD COLUMN state VARCHAR(50) DEFAULT 'Gujarat'");
            } catch (Exception ignored) {}

            // 5. Order items table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS order_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "order_id INT, " +
                    "product_id VARCHAR(50), " +
                    "product_name VARCHAR(150), " +
                    "price DECIMAL(10,2), " +
                    "quantity INT, " +
                    "total DECIMAL(10,2), " +
                    "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE)");

            // 6. Contact messages table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS contact_messages (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "email VARCHAR(100), " +
                    "subject VARCHAR(150), " +
                    "message TEXT, " +
                    "status VARCHAR(20) DEFAULT 'Unread', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            try {
                stmt.executeUpdate("ALTER TABLE contact_messages ADD COLUMN status VARCHAR(20) DEFAULT 'Unread'");
            } catch (Exception ignored) {}

            // 7. Product reviews table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS reviews (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "product_id VARCHAR(50) NOT NULL, " +
                    "product_name VARCHAR(150), " +
                    "user_name VARCHAR(100), " +
                    "user_email VARCHAR(100), " +
                    "rating INT DEFAULT 5, " +
                    "title VARCHAR(150), " +
                    "comment TEXT, " +
                    "review_text TEXT, " +
                    "status VARCHAR(20) DEFAULT 'Approved', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            try { stmt.executeUpdate("ALTER TABLE reviews ADD COLUMN product_name VARCHAR(150)"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE reviews ADD COLUMN user_email VARCHAR(100)"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE reviews ADD COLUMN title VARCHAR(150)"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE reviews ADD COLUMN comment TEXT"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE reviews ADD COLUMN review_text TEXT"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE reviews ADD COLUMN status VARCHAR(20) DEFAULT 'Approved'"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE reviews DROP FOREIGN KEY reviews_ibfk_1"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE reviews DROP FOREIGN KEY fk_reviews_products"); } catch (Exception ignored) {}

            // Insert default Admin if not present
            stmt.executeUpdate("INSERT INTO users (name, email, phone, city, password, role) VALUES " +
                    "('Admin User', 'admin@radiantskin.com', '9876543210', 'Rajkot', 'admin123', 'admin') " +
                    "ON DUPLICATE KEY UPDATE password=VALUES(password)");

            // Clean demo user if present so only real registered users remain
            try {
                stmt.executeUpdate("DELETE FROM users WHERE email='user@gmail.com'");
            } catch (Exception ignored) {}

            System.out.println("✅ MySQL Tables verified.");
        } catch (Exception e) {
            System.err.println("⚠️ DB Init Notice: " + e.getMessage());
        }
    }

    // CORS & Response Helper
    private static void setCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        setCORSHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static Map<String, String> parseJsonSimple(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;
        body = body.trim();
        if (body.startsWith("{") && body.endsWith("}")) {
            body = body.substring(1, body.length() - 1);
            String[] pairs = body.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String pair : pairs) {
                String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
                if (kv.length == 2) {
                    String k = kv[0].trim().replaceAll("^\"|\"$", "");
                    String v = kv[1].trim();
                    if (v.startsWith("\"") && v.endsWith("\"")) {
                        v = v.substring(1, v.length() - 1);
                    }
                    // Unescape standard json escapes
                    v = v.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n");
                    map.put(k, v);
                }
            }
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Handlers
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            sendJsonResponse(exchange, 200, "{\"status\":\"OK\",\"db\":\"connected\"}");
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = parseJsonSimple(body);
            String email = data.get("email");
            String password = data.get("password");

            if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Email and password are required\"}");
                return;
            }

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE LOWER(email) = LOWER(?) AND password = ?")) {
                ps.setString(1, email.trim());
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("name");
                        String role = rs.getString("role");
                        String city = rs.getString("city");
                        String phone = rs.getString("phone");
                        String json = String.format("{\"success\":true,\"message\":\"Login successful\",\"user\":{\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"city\":\"%s\",\"role\":\"%s\"}}",
                                escapeJson(name), escapeJson(email), escapeJson(phone != null ? phone : ""), escapeJson(city != null ? city : ""), escapeJson(role != null ? role : "customer"));
                        sendJsonResponse(exchange, 200, json);
                    } else {
                        sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid email or password. Please register first.\"}");
                    }
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> data = parseJsonSimple(body);
            String name = data.get("name");
            String email = data.get("email");
            String password = data.get("password");
            String phone = data.get("phone");
            String city = data.get("city");

            if (name == null || email == null || password == null || name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Name, email, and password are required\"}");
                return;
            }

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement("INSERT INTO users (name, email, password, phone, city, role) VALUES (?, ?, ?, ?, ?, 'customer')")) {
                ps.setString(1, name.trim());
                ps.setString(2, email.trim());
                ps.setString(3, password);
                ps.setString(4, phone != null ? phone.trim() : "");
                ps.setString(5, city != null ? city.trim() : "");
                ps.executeUpdate();
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Account registered successfully in MySQL database!\"}");
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"An account with this email already exists.\"}");
                } else {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"Database error: " + escapeJson(e.getMessage()) + "\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"Error: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                try (Connection con = getConnection();
                     Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM users ORDER BY id ASC")) {
                    List<String> list = new ArrayList<>();
                    while (rs.next()) {
                        String statusVal = "active";
                        try { statusVal = rs.getString("status"); } catch(Exception e){}
                        if (statusVal == null) statusVal = "active";
                        list.add(String.format("{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"city\":\"%s\",\"role\":\"%s\",\"status\":\"%s\",\"created_at\":\"%s\"}",
                                rs.getInt("id"),
                                escapeJson(rs.getString("name")),
                                escapeJson(rs.getString("email")),
                                escapeJson(rs.getString("phone")),
                                escapeJson(rs.getString("city")),
                                escapeJson(rs.getString("role")),
                                escapeJson(statusVal),
                                rs.getTimestamp("created_at")));
                    }
                    sendJsonResponse(exchange, 200, "[" + String.join(",", list) + "]");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseJsonSimple(body);
                String email = data.get("email");
                String name = data.get("name");
                String phone = data.get("phone");
                String city = data.get("city");

                if (email != null && !email.trim().isEmpty()) {
                    try (Connection con = getConnection();
                         PreparedStatement ps = con.prepareStatement("UPDATE users SET name=?, phone=?, city=? WHERE LOWER(email)=LOWER(?)")) {
                        ps.setString(1, name != null ? name.trim() : "");
                        ps.setString(2, phone != null ? phone.trim() : "");
                        ps.setString(3, city != null ? city.trim() : "");
                        ps.setString(4, email.trim());
                        int count = ps.executeUpdate();
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"updated\":" + count + "}");
                    } catch (Exception e) {
                        sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                    }
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Email parameter is required\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                String email = null;
                if (query != null && query.contains("email=")) {
                    email = URLDecoder.decode(query.split("email=")[1].split("&")[0], StandardCharsets.UTF_8);
                }
                if (email == null || email.trim().isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Email parameter is required\"}");
                    return;
                }
                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE LOWER(email) = LOWER(?)")) {
                    ps.setString(1, email.trim());
                    int count = ps.executeUpdate();
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"deleted\":" + count + "}");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    static class ProductsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                try (Connection con = getConnection();
                     Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM products ORDER BY id ASC")) {
                    List<String> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(String.format("{\"id\":\"%s\",\"name\":\"%s\",\"brand\":\"%s\",\"category\":\"%s\",\"price\":%.2f,\"oldPrice\":%.2f,\"discount\":\"%s\",\"rating\":%.1f,\"reviewsCount\":%d,\"image\":\"%s\",\"desc\":\"%s\"}",
                                rs.getString("id"),
                                escapeJson(rs.getString("name")),
                                escapeJson(rs.getString("brand")),
                                escapeJson(rs.getString("category_key")),
                                rs.getDouble("price"),
                                rs.getDouble("old_price"),
                                escapeJson(rs.getString("discount")),
                                rs.getDouble("rating"),
                                rs.getInt("reviews_count"),
                                escapeJson(rs.getString("image_url")),
                                escapeJson(rs.getString("description"))));
                    }
                    sendJsonResponse(exchange, 200, "[" + String.join(",", list) + "]");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseJsonSimple(body);

                String prodId = data.get("id");
                if (prodId == null || prodId.trim().isEmpty()) {
                    prodId = "P" + String.format("%03d", (int)(Math.random() * 900 + 100));
                } else {
                    prodId = prodId.trim();
                }

                String name = data.get("name");
                String brand = data.getOrDefault("brand", "Radiant Skin");
                String category = data.get("category");
                double price = Double.parseDouble(data.getOrDefault("price", "0"));
                double oldPrice = Double.parseDouble(data.getOrDefault("oldPrice", "0"));
                int discount = 0;
                try {
                    discount = Integer.parseInt(data.getOrDefault("discount", "0").replaceAll("[^0-9]", ""));
                } catch(Exception ignored){}
                String image = data.getOrDefault("image", "");
                String desc = data.getOrDefault("desc", "");

                try (Connection con = getConnection()) {
                    // Try inserting with VARCHAR id first (matches DB schema id varchar(20))
                    String sql = "INSERT INTO products (id, name, brand, category_key, price, old_price, discount, image_url, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE name=VALUES(name), brand=VALUES(brand), category_key=VALUES(category_key), price=VALUES(price), old_price=VALUES(old_price), discount=VALUES(discount), image_url=VALUES(image_url), description=VALUES(description)";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, prodId);
                        ps.setString(2, name);
                        ps.setString(3, brand);
                        ps.setString(4, category);
                        ps.setDouble(5, price);
                        ps.setDouble(6, oldPrice);
                        ps.setInt(7, discount);
                        ps.setString(8, image);
                        ps.setString(9, desc);
                        ps.executeUpdate();
                        System.out.println("➕ Inserted/Saved in MySQL: product id=" + prodId + ", name=" + name);
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":\"" + escapeJson(prodId) + "\",\"message\":\"Product inserted into MySQL DB ✓\"}");
                        return;
                    } catch (SQLException se) {
                        // Fallback for auto-increment int id
                        String sqlFallback = "INSERT INTO products (name, brand, category_key, price, old_price, discount, image_url, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement ps2 = con.prepareStatement(sqlFallback, Statement.RETURN_GENERATED_KEYS)) {
                            ps2.setString(1, name);
                            ps2.setString(2, brand);
                            ps2.setString(3, category);
                            ps2.setDouble(4, price);
                            ps2.setDouble(5, oldPrice);
                            ps2.setInt(6, discount);
                            ps2.setString(7, image);
                            ps2.setString(8, desc);
                            ps2.executeUpdate();
                            try (ResultSet rs = ps2.getGeneratedKeys()) {
                                if (rs.next()) prodId = String.valueOf(rs.getInt(1));
                            }
                            System.out.println("➕ Inserted in MySQL (Auto-Inc): product id=" + prodId + ", name=" + name);
                            sendJsonResponse(exchange, 200, "{\"success\":true,\"id\":\"" + escapeJson(prodId) + "\",\"message\":\"Product inserted into MySQL DB ✓\"}");
                            return;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Insert Error: " + e.getMessage());
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseJsonSimple(body);

                String id = data.get("id");
                String name = data.get("name");
                String brand = data.getOrDefault("brand", "Radiant Skin");
                String category = data.get("category");
                double price = Double.parseDouble(data.getOrDefault("price", "0"));
                double oldPrice = Double.parseDouble(data.getOrDefault("oldPrice", "0"));
                int discount = 0;
                try {
                    discount = Integer.parseInt(data.getOrDefault("discount", "0").replaceAll("[^0-9]", ""));
                } catch(Exception ignored){}
                String image = data.getOrDefault("image", "");
                String desc = data.getOrDefault("desc", "");

                String rawId = id != null ? id.trim() : "";
                String numOnly = rawId.replaceAll("[^0-9]", "");
                int intId = 0;
                try { if (!numOnly.isEmpty()) intId = Integer.parseInt(numOnly); } catch (Exception ignored) {}
                String pPadded = (intId > 0) ? String.format("P%03d", intId) : rawId;

                try (Connection con = getConnection()) {
                    String sql = "UPDATE products SET name=?, brand=?, category_key=?, price=?, old_price=?, discount=?, image_url=?, description=? WHERE id=? OR id=? OR id=? OR id=?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, name);
                        ps.setString(2, brand);
                        ps.setString(3, category);
                        ps.setDouble(4, price);
                        ps.setDouble(5, oldPrice);
                        ps.setInt(6, discount);
                        ps.setString(7, image);
                        ps.setString(8, desc);
                        ps.setString(9, rawId);
                        ps.setString(10, numOnly.isEmpty() ? rawId : numOnly);
                        ps.setString(11, String.valueOf(intId));
                        ps.setString(12, pPadded);
                        int count = ps.executeUpdate();
                        System.out.println("✏️ Updated in MySQL: " + count + " product(s) with id=" + rawId);
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"updated\":" + count + ",\"message\":\"Product updated in MySQL DB ✓\"}");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Update Error: " + e.getMessage());
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                String id = null;
                if (query != null && query.contains("id=")) {
                    id = query.split("id=")[1].split("&")[0];
                }
                if (id == null) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"ID parameter required\"}");
                    return;
                }
                String rawId = URLDecoder.decode(id, StandardCharsets.UTF_8).trim();
                String numOnly = rawId.replaceAll("[^0-9]", "");
                int intId = 0;
                try { if (!numOnly.isEmpty()) intId = Integer.parseInt(numOnly); } catch (Exception ignored) {}
                String pPadded = (intId > 0) ? String.format("P%03d", intId) : rawId;

                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM products WHERE id=? OR id=? OR id=? OR id=?")) {
                    ps.setString(1, rawId);
                    ps.setString(2, numOnly.isEmpty() ? rawId : numOnly);
                    ps.setString(3, String.valueOf(intId));
                    ps.setString(4, pPadded);
                    int count = ps.executeUpdate();
                    System.out.println("🗑️ Deleted from MySQL: " + count + " product(s) with id=" + rawId);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"deleted\":" + count + ",\"message\":\"Product deleted from MySQL DB ✓\"}");
                } catch (Exception e) {
                    System.err.println("❌ Delete Error: " + e.getMessage());
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            }
        }
    }

    static class CategoriesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                try (Connection con = getConnection();
                     Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM categories")) {
                    Map<String, String> map = new HashMap<>();
                    while (rs.next()) {
                        String key = rs.getString("category_key");
                        String val = String.format("{\"label\":\"%s\",\"color\":\"%s\",\"accent\":\"%s\",\"shape\":\"%s\"}",
                                escapeJson(rs.getString("label")),
                                escapeJson(rs.getString("color")),
                                escapeJson(rs.getString("accent")),
                                escapeJson(rs.getString("shape")));
                        map.put("\"" + escapeJson(key) + "\"", val);
                    }
                    List<String> pairs = new ArrayList<>();
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        pairs.add(entry.getKey() + ":" + entry.getValue());
                    }
                    sendJsonResponse(exchange, 200, "{" + String.join(",", pairs) + "}");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseJsonSimple(body);
                String key = data.get("key");
                String label = data.get("label");
                String color = data.get("color");
                String accent = data.get("accent");
                String shape = data.get("shape");

                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement("REPLACE INTO categories (category_key, label, color, accent, shape) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, key);
                    ps.setString(2, label);
                    ps.setString(3, color);
                    ps.setString(4, accent);
                    ps.setString(5, shape);
                    int count = ps.executeUpdate();
                    System.out.println("🏷️ Category saved in MySQL: " + key + " (" + label + ")");
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Category saved in MySQL DB ✓\"}");
                } catch (Exception e) {
                    System.err.println("❌ Category Save Error: " + e.getMessage());
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                String key = null;
                if (query != null && query.contains("key=")) {
                    key = URLDecoder.decode(query.split("key=")[1].split("&")[0], StandardCharsets.UTF_8);
                }
                if (key == null) {
                    sendJsonResponse(exchange, 400, "{\"success\":false}");
                    return;
                }
                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM categories WHERE category_key=? OR label=?")) {
                    ps.setString(1, key);
                    ps.setString(2, key);
                    int count = ps.executeUpdate();
                    System.out.println("🗑️ Category deleted from MySQL: " + key);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"deleted\":" + count + ",\"message\":\"Category deleted from MySQL DB ✓\"}");
                } catch (Exception e) {
                    System.err.println("❌ Category Delete Error: " + e.getMessage());
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            }
        }
    }

    static class OrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                try (Connection con = getConnection();
                     Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM orders ORDER BY id DESC")) {
                    List<String> list = new ArrayList<>();
                    while (rs.next()) {
                        int orderId = rs.getInt("id");
                        String itemsJson = "[]";
                        try (PreparedStatement ips = con.prepareStatement("SELECT * FROM order_items WHERE order_id=?")) {
                            ips.setInt(1, orderId);
                            try (ResultSet irs = ips.executeQuery()) {
                                List<String> itemsList = new ArrayList<>();
                                while (irs.next()) {
                                    itemsList.add(String.format("{\"id\":%d,\"name\":\"%s\",\"price\":%.2f,\"quantity\":%d}",
                                            irs.getInt("product_id"),
                                            escapeJson(irs.getString("product_name")),
                                            irs.getDouble("price"),
                                            irs.getInt("quantity")));
                                }
                                itemsJson = "[" + String.join(",", itemsList) + "]";
                            }
                        }
                        list.add(String.format("{\"id\":%d,\"number\":\"%s\",\"total\":%.2f,\"status\":\"%s\",\"date\":\"%s\",\"shipping\":{\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"address\":\"%s\",\"city\":\"%s\",\"pincode\":\"%s\",\"paymentMethod\":\"%s\"},\"items\":%s}",
                                orderId,
                                escapeJson(rs.getString("order_number")),
                                rs.getDouble("total_amount"),
                                escapeJson(rs.getString("order_status")),
                                rs.getTimestamp("order_date"),
                                escapeJson(rs.getString("customer_name")),
                                escapeJson(rs.getString("customer_email")),
                                escapeJson(rs.getString("customer_phone")),
                                escapeJson(rs.getString("shipping_address")),
                                escapeJson(rs.getString("city")),
                                escapeJson(rs.getString("pincode")),
                                escapeJson(rs.getString("payment_method")),
                                itemsJson));
                    }
                    sendJsonResponse(exchange, 200, "[" + String.join(",", list) + "]");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseJsonSimple(body);
                String name = data.getOrDefault("name", "Customer");
                String email = data.getOrDefault("email", "");
                String phone = data.getOrDefault("phone", "");
                String address = data.getOrDefault("address", "");
                String city = data.getOrDefault("city", "");
                String state = data.getOrDefault("state", "Gujarat");
                String pincode = data.getOrDefault("pincode", "");
                String paymentMethod = data.getOrDefault("paymentMethod", "Cash on Delivery");
                double total = Double.parseDouble(data.getOrDefault("total", "0"));

                String orderNum = data.get("orderNumber");
                if (orderNum == null || orderNum.trim().isEmpty()) {
                    orderNum = data.get("order_number");
                }
                if (orderNum == null || orderNum.trim().isEmpty()) {
                    orderNum = "ORD-" + System.currentTimeMillis();
                } else {
                    orderNum = orderNum.trim();
                }

                try (Connection con = getConnection()) {
                    String sql = "INSERT INTO orders (order_number, customer_name, customer_email, customer_phone, shipping_address, city, state, pincode, payment_method, total_amount, order_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Processing')";
                    try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, orderNum);
                        ps.setString(2, name);
                        ps.setString(3, email);
                        ps.setString(4, phone);
                        ps.setString(5, address);
                        ps.setString(6, city);
                        ps.setString(7, state);
                        ps.setString(8, pincode);
                        ps.setString(9, paymentMethod);
                        ps.setDouble(10, total);
                        ps.executeUpdate();
                        int orderId = 0;
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) orderId = rs.getInt(1);
                        }

                        // Insert into order_items
                        String itemsStr = data.get("items");
                        if (itemsStr != null && orderId > 0) {
                            String[] itemRows = itemsStr.split("\\|\\|");
                            for (String row : itemRows) {
                                if (row.trim().isEmpty()) continue;
                                String[] parts = row.split("::");
                                if (parts.length >= 4) {
                                    String pId = parts[0];
                                    String pName = parts[1];
                                    double pPrice = Double.parseDouble(parts[2]);
                                    int pQty = Integer.parseInt(parts[3]);
                                    double pTotal = pPrice * pQty;
                                    String itemSql = "INSERT INTO order_items (order_id, product_id, product_name, price, quantity, total) VALUES (?, ?, ?, ?, ?, ?)";
                                    try (PreparedStatement ips = con.prepareStatement(itemSql)) {
                                        ips.setInt(1, orderId);
                                        ips.setString(2, pId);
                                        ips.setString(3, pName);
                                        ips.setDouble(4, pPrice);
                                        ips.setInt(5, pQty);
                                        ips.setDouble(6, pTotal);
                                        ips.executeUpdate();
                                    } catch(Exception ignored){}
                                }
                            }
                        }
                        System.out.println("📦 Order saved in MySQL: " + orderNum + " (ID: " + orderId + ", Total: " + total + ")");
                        sendJsonResponse(exchange, 200, String.format("{\"success\":true,\"id\":%d,\"orderNumber\":\"%s\",\"message\":\"Order saved in MySQL DB ✓\"}", orderId, orderNum));
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Order Save Error: " + e.getMessage());
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseJsonSimple(body);
                String idStr = data.get("id");
                String orderNum = data.get("order_number");
                if (orderNum == null) orderNum = data.get("orderNumber");
                String status = data.get("status");

                if (status == null || status.trim().isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Status is required\"}");
                    return;
                }

                int numId = 0;
                try {
                    if (idStr != null) {
                        String digits = idStr.replaceAll("[^0-9]", "");
                        if (!digits.isEmpty()) numId = Integer.parseInt(digits);
                    }
                } catch (Exception ignored) {}

                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement("UPDATE orders SET order_status=? WHERE id=? OR order_number=? OR order_number=? OR order_number LIKE ?")) {
                    ps.setString(1, status.trim());
                    ps.setInt(2, numId);
                    ps.setString(3, idStr != null ? idStr.trim() : "");
                    ps.setString(4, orderNum != null ? orderNum.trim() : "");
                    ps.setString(5, "%" + (numId > 0 ? String.valueOf(numId) : (idStr != null ? idStr.trim() : "---")) + "%");
                    int count = ps.executeUpdate();
                    System.out.println("🔄 Updated order status to '" + status + "' in MySQL (rows: " + count + ")");
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"updated\":" + count + ",\"message\":\"Order status updated in MySQL DB ✓\"}");
                } catch (Exception e) {
                    System.err.println("❌ Order Status Update Error: " + e.getMessage());
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            }
        }
    }

    static class ContactHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseJsonSimple(body);
                String name = data.get("name");
                String email = data.get("email");
                String subject = data.getOrDefault("subject", "General Inquiry");
                String message = data.get("message");

                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement("INSERT INTO contact_messages (name, email, subject, message, status) VALUES (?, ?, ?, ?, 'Unread')")) {
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, subject);
                    ps.setString(4, message);
                    ps.executeUpdate();
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Contact inquiry saved in MySQL DB ✓\"}");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("GET".equalsIgnoreCase(method)) {
                try (Connection con = getConnection();
                     Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM contact_messages ORDER BY id DESC")) {
                    List<String> list = new ArrayList<>();
                    while (rs.next()) {
                        String statusVal = "Unread";
                        try { statusVal = rs.getString("status"); } catch(Exception ignored){}
                        if (statusVal == null || statusVal.isEmpty()) statusVal = "Unread";
                        list.add(String.format("{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"subject\":\"%s\",\"message\":\"%s\",\"status\":\"%s\",\"created_at\":\"%s\"}",
                                rs.getInt("id"),
                                escapeJson(rs.getString("name")),
                                escapeJson(rs.getString("email")),
                                escapeJson(rs.getString("subject")),
                                escapeJson(rs.getString("message")),
                                escapeJson(statusVal),
                                rs.getTimestamp("created_at")));
                    }
                    sendJsonResponse(exchange, 200, "[" + String.join(",", list) + "]");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseJsonSimple(body);
                int id = Integer.parseInt(data.getOrDefault("id", "0"));
                String status = data.getOrDefault("status", "Read");

                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement("UPDATE contact_messages SET status=? WHERE id=?")) {
                    ps.setString(1, status);
                    ps.setInt(2, id);
                    int count = ps.executeUpdate();
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"updated\":" + count + ",\"message\":\"Status updated ✓\"}");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                String idStr = null;
                if (query != null && query.contains("id=")) {
                    idStr = query.split("id=")[1].split("&")[0];
                }
                if (idStr == null) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"ID required\"}");
                    return;
                }
                try (Connection con = getConnection();
                     PreparedStatement ps = con.prepareStatement("DELETE FROM contact_messages WHERE id=?")) {
                    ps.setInt(1, Integer.parseInt(idStr));
                    int count = ps.executeUpdate();
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"deleted\":" + count + "}");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            }
        }
    }

    static class ReviewsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                String pId = null;
                if (query != null && query.contains("productId=")) {
                    pId = query.split("productId=")[1].split("&")[0];
                }

                try (Connection con = getConnection()) {
                    String rawPid = (pId != null && !pId.isEmpty()) ? URLDecoder.decode(pId, StandardCharsets.UTF_8).trim() : null;
                    String sql;
                    if (rawPid != null && !rawPid.isEmpty()) {
                        sql = "SELECT * FROM reviews WHERE product_id=? OR product_id=? OR product_id=? ORDER BY id DESC";
                    } else {
                        sql = "SELECT * FROM reviews ORDER BY id DESC";
                    }
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        if (rawPid != null && !rawPid.isEmpty()) {
                            String numOnly = rawPid.replaceAll("[^0-9]", "");
                            int intId = 0;
                            try { if (!numOnly.isEmpty()) intId = Integer.parseInt(numOnly); } catch (Exception ignored) {}
                            String pPadded = (intId > 0) ? String.format("P%03d", intId) : rawPid;

                            ps.setString(1, rawPid);
                            ps.setString(2, numOnly.isEmpty() ? rawPid : numOnly);
                            ps.setString(3, pPadded);
                        }
                        try (ResultSet rs = ps.executeQuery()) {
                            List<String> list = new ArrayList<>();
                            while (rs.next()) {
                                String commentVal = "";
                                try { commentVal = rs.getString("comment"); } catch (Exception ignored) {}
                                if (commentVal == null || commentVal.isEmpty()) {
                                    try { commentVal = rs.getString("review_text"); } catch (Exception ignored) {}
                                }
                                if (commentVal == null) commentVal = "";

                                String statusVal = "Approved";
                                try { statusVal = rs.getString("status"); } catch (Exception ignored) {}
                                if (statusVal == null || statusVal.isEmpty()) statusVal = "Approved";

                                String titleVal = "";
                                try { titleVal = rs.getString("title"); } catch (Exception ignored) {}
                                if (titleVal == null) titleVal = "";

                                String pNameVal = "";
                                try { pNameVal = rs.getString("product_name"); } catch (Exception ignored) {}
                                if (pNameVal == null) pNameVal = "";

                                String uEmailVal = "";
                                try { uEmailVal = rs.getString("user_email"); } catch (Exception ignored) {}
                                if (uEmailVal == null) uEmailVal = "";

                                list.add(String.format("{\"id\":%d,\"productId\":\"%s\",\"productName\":\"%s\",\"userName\":\"%s\",\"userEmail\":\"%s\",\"rating\":%d,\"title\":\"%s\",\"comment\":\"%s\",\"status\":\"%s\",\"created_at\":\"%s\"}",
                                        rs.getInt("id"),
                                        escapeJson(rs.getString("product_id")),
                                        escapeJson(pNameVal),
                                        escapeJson(rs.getString("user_name")),
                                        escapeJson(uEmailVal),
                                        rs.getInt("rating"),
                                        escapeJson(titleVal),
                                        escapeJson(commentVal),
                                        escapeJson(statusVal),
                                        rs.getTimestamp("created_at")));
                            }
                            sendJsonResponse(exchange, 200, "[" + String.join(",", list) + "]");
                        }
                    }
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseJsonSimple(body);
                String productId = data.getOrDefault("productId", "P001");
                String productName = data.getOrDefault("productName", "Product");
                String userName = data.getOrDefault("userName", "Customer");
                String userEmail = data.getOrDefault("userEmail", "");
                int rating = 5;
                try { rating = Integer.parseInt(data.getOrDefault("rating", "5")); } catch(Exception ignored){}
                String title = data.getOrDefault("title", "");
                String comment = data.getOrDefault("comment", data.getOrDefault("text", ""));
                String status = data.getOrDefault("status", "Approved");

                try (Connection con = getConnection()) {
                    try (PreparedStatement checkPs = con.prepareStatement("INSERT IGNORE INTO products (id, name, brand, price) VALUES (?, ?, 'Radiant Skin', 299.00)")) {
                        checkPs.setString(1, productId);
                        checkPs.setString(2, productName);
                        checkPs.executeUpdate();
                    } catch (Exception ignored) {}

                    String sql = "INSERT INTO reviews (product_id, product_name, user_name, user_email, rating, title, comment, review_text, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, productId);
                        ps.setString(2, productName);
                        ps.setString(3, userName);
                        ps.setString(4, userEmail);
                        ps.setInt(5, rating);
                        ps.setString(6, title);
                        ps.setString(7, comment);
                        ps.setString(8, comment);
                        ps.setString(9, status);
                        ps.executeUpdate();
                    } catch (SQLException se) {
                        String fallbackSql = "INSERT INTO reviews (product_id, user_name, rating, comment) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement ps = con.prepareStatement(fallbackSql)) {
                            ps.setString(1, productId);
                            ps.setString(2, userName);
                            ps.setInt(3, rating);
                            ps.setString(4, comment);
                            ps.executeUpdate();
                        }
                    }

                    // Recalculate average rating & reviews_count for this product
                    recalcProductRating(con, productId);

                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Review posted in MySQL DB ✓\"}");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseJsonSimple(body);
                int id = Integer.parseInt(data.getOrDefault("id", "0"));
                String status = data.getOrDefault("status", "Approved");

                try (Connection con = getConnection()) {
                    String pId = "";
                    try (PreparedStatement ps = con.prepareStatement("SELECT product_id FROM reviews WHERE id=?")) {
                        ps.setInt(1, id);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) pId = rs.getString("product_id");
                        }
                    }

                    try (PreparedStatement ps = con.prepareStatement("UPDATE reviews SET status=? WHERE id=?")) {
                        ps.setString(1, status);
                        ps.setInt(2, id);
                        ps.executeUpdate();
                    }

                    if (!pId.isEmpty()) recalcProductRating(con, pId);

                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Review status updated ✓\"}");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                String idStr = null;
                if (query != null && query.contains("id=")) {
                    idStr = query.split("id=")[1].split("&")[0];
                }
                if (idStr == null) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"ID required\"}");
                    return;
                }
                int id = Integer.parseInt(idStr);
                try (Connection con = getConnection()) {
                    String pId = "";
                    try (PreparedStatement ps = con.prepareStatement("SELECT product_id FROM reviews WHERE id=?")) {
                        ps.setInt(1, id);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) pId = rs.getString("product_id");
                        }
                    }

                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM reviews WHERE id=?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }

                    if (!pId.isEmpty()) recalcProductRating(con, pId);

                    sendJsonResponse(exchange, 200, "{\"success\":true,\"deleted\":1,\"message\":\"Review deleted ✓\"}");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            }
        }

        private static void recalcProductRating(Connection con, String productId) {
            try {
                double avgRating = 1.0;
                int count = 0;
                try (PreparedStatement ps = con.prepareStatement("SELECT AVG(rating) as avg_r, COUNT(*) as cnt FROM reviews WHERE product_id=? AND status='Approved'")) {
                    ps.setString(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            count = rs.getInt("cnt");
                            if (count > 0) avgRating = rs.getDouble("avg_r");
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE products SET rating=?, reviews_count=? WHERE id=? OR id=?")) {
                    ps.setDouble(1, Math.round(avgRating * 10.0) / 10.0);
                    ps.setInt(2, count);
                    ps.setString(3, productId);
                    ps.setString(4, productId.replaceAll("[^0-9]", ""));
                    ps.executeUpdate();
                }
            } catch (Exception ignored) {}
        }
    }

    static class ReportsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try (Connection con = getConnection()) {
                double totalRevenue = 0;
                int totalOrders = 0;
                int deliveredCount = 0;
                int shippedCount = 0;
                int processingCount = 0;
                int cancelledCount = 0;

                try (Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT order_status, COUNT(*) as cnt, SUM(total_amount) as sum_amt FROM orders GROUP BY order_status")) {
                    while (rs.next()) {
                        String st = rs.getString("order_status");
                        int c = rs.getInt("cnt");
                        double a = rs.getDouble("sum_amt");
                        totalOrders += c;
                        if (!"Cancelled".equalsIgnoreCase(st)) {
                            totalRevenue += a;
                        }
                        if ("Delivered".equalsIgnoreCase(st)) deliveredCount = c;
                        else if ("Shipped".equalsIgnoreCase(st)) shippedCount = c;
                        else if ("Processing".equalsIgnoreCase(st)) processingCount = c;
                        else if ("Cancelled".equalsIgnoreCase(st)) cancelledCount = c;
                    }
                }

                int totalUsers = 0;
                try (Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role != 'admin'")) {
                    if (rs.next()) totalUsers = rs.getInt(1);
                }

                int totalProducts = 0;
                try (Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
                    if (rs.next()) totalProducts = rs.getInt(1);
                }

                int totalReviews = 0;
                double avgSystemRating = 5.0;
                try (Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*), AVG(rating) FROM reviews WHERE status='Approved'")) {
                    if (rs.next()) {
                        totalReviews = rs.getInt(1);
                        if (totalReviews > 0) avgSystemRating = rs.getDouble(2);
                    }
                }

                // Top 5 best selling products
                List<String> topSelling = new ArrayList<>();
                try (Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT product_name, SUM(quantity) as qty, SUM(total) as rev FROM order_items GROUP BY product_name ORDER BY qty DESC LIMIT 5")) {
                    while (rs.next()) {
                        topSelling.add(String.format("{\"name\":\"%s\",\"quantity\":%d,\"revenue\":%.2f}",
                                escapeJson(rs.getString("product_name")),
                                rs.getInt("qty"),
                                rs.getDouble("rev")));
                    }
                }

                String json = String.format("{\"totalRevenue\":%.2f,\"totalOrders\":%d,\"deliveredOrders\":%d,\"shippedOrders\":%d,\"processingOrders\":%d,\"cancelledOrders\":%d,\"totalUsers\":%d,\"totalProducts\":%d,\"totalReviews\":%d,\"avgRating\":%.1f,\"topSelling\":[%s]}",
                        totalRevenue, totalOrders, deliveredCount, shippedCount, processingCount, cancelledCount, totalUsers, totalProducts, totalReviews, avgSystemRating, String.join(",", topSelling));

                sendJsonResponse(exchange, 200, json);
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.trim().isEmpty()) path = "/index.html";

            String relPath = path.startsWith("/") ? path.substring(1) : path;
            
            // Candidate paths for frontend files
            File file = new File("../" + relPath);
            if (!file.exists() || file.isDirectory()) {
                file = new File(relPath);
            }
            if (!file.exists() || file.isDirectory()) {
                file = new File("radiant-skin-frontend/" + relPath);
            }
            if (!file.exists() || file.isDirectory()) {
                file = new File("radiant skin/radiant-skin-frontend/" + relPath);
            }

            if (!file.exists() || file.isDirectory()) {
                sendJsonResponse(exchange, 404, "{\"error\":\"File not found: " + escapeJson(relPath) + "\"}");
                return;
            }

            String contentType = "text/html; charset=UTF-8";
            if (path.endsWith(".css")) contentType = "text/css; charset=UTF-8";
            else if (path.endsWith(".js")) contentType = "application/javascript; charset=UTF-8";
            else if (path.endsWith(".json")) contentType = "application/json; charset=UTF-8";
            else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (path.endsWith(".png")) contentType = "image/png";
            else if (path.endsWith(".svg")) contentType = "image/svg+xml";
            else if (path.endsWith(".webp")) contentType = "image/webp";
            else if (path.endsWith(".avif")) contentType = "image/avif";
            else if (path.endsWith(".ico")) contentType = "image/x-icon";

            exchange.getResponseHeaders().set("Content-Type", contentType);
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }
    }
}
