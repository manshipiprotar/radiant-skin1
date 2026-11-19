import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Database Configuration (તમારા MySQL મુજબ બદલી શકાય છે)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/radiant_skin?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; // જો પાસવ

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 1. ફોર્મમાંથી ડેટા લેવો
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String city = request.getParameter("city");
        String password = request.getParameter("password");

        // Validation
        if (name == null || email == null || password == null || name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            renderMessage(out, false, "Please fill all required fields!", "register.html");
            return;
        }

        Connection con = null;
        PreparedStatement ps = null;

        try {
            // 2. MySQL Driver Load કરવો
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 3. Database Connection બનાવવું
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            // 4. SQL Query (Data Insert કરવો)
            String sql = "INSERT INTO users (name, email, password, phone, city) VALUES (?, ?, ?, ?, ?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.setString(3, password);
            ps.setString(4, phone != null ? phone.trim() : "");
            ps.setString(5, city != null ? city.trim() : "");

            // 5. Query Execute કરવી
            int rowsInserted = ps.executeUpdate();

            if (rowsInserted > 0) {
                renderMessage(out, true, "Registration Successful! Welcome, " + name + " 🎉", "login.html");
            } else {
                renderMessage(out, false, "Registration Failed! Please try again.", "login.html?tab=register");
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            renderMessage(out, false, "MySQL JDBC Driver not found! (lib folder માં mysql-connector jar ઉમેરો)", "login.html?tab=register");
        } catch (SQLException e) {
            e.printStackTrace();
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Duplicate entry")) {
                renderMessage(out, false, "This email (" + email + ") is already registered!", "login.html?tab=register");
            } else {
                renderMessage(out, false, "Database Error: " + errorMsg + " (કૃપા કરીને database.sql રન કરો)", "login.html?tab=register");
            }
        } catch (Exception e) {
            e.printStackTrace();
            renderMessage(out, false, "Error: " + e.getMessage(), "login.html?tab=register");
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void renderMessage(PrintWriter out, boolean success, String message, String redirectUrl) {
        String bgColor = success ? "#4E8A7A" : "#B96B78";
        String icon = success ? "✓" : "⚠";
        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Registration Status</title>");
        out.println("<style>");
        out.println("body { font-family: 'Segoe UI', Tahoma, sans-serif; background: #FAF7F2; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }");
        out.println(".card { background: white; padding: 40px; border-radius: 16px; box-shadow: 0 10px 30px rgba(0,0,0,0.08); text-align: center; max-width: 420px; }");
        out.println(".badge { display: inline-flex; width: 60px; height: 60px; border-radius: 50%; background: " + bgColor + "; color: white; font-size: 30px; align-items: center; justify-content: center; margin-bottom: 20px; }");
        out.println("h2 { color: #2B1E20; margin-bottom: 10px; }");
        out.println("p { color: #7A6064; font-size: 15px; line-height: 1.5; margin-bottom: 25px; }");
        out.println(".btn { display: inline-block; padding: 12px 28px; background: #2B1E20; color: white; text-decoration: none; border-radius: 30px; font-weight: 500; transition: 0.2s; }");
        out.println(".btn:hover { background: #4E8A7A; }");
        out.println("</style>");
        out.println("<script>");
        if (success) {
            out.println("setTimeout(function(){ window.location.href = '" + redirectUrl + "'; }, 2500);");
        }
        out.println("</script>");
        out.println("</head><body>");
        out.println("<div class='card'>");
        out.println("<div class='badge'>" + icon + "</div>");
        out.println("<h2>" + (success ? "Registration Successful" : "Registration Notice") + "</h2>");
        out.println("<p>" + message + "</p>");
        out.println("<a class='btn' href='" + redirectUrl + "'>" + (success ? "Go to Login" : "Try Again") + "</a>");
        out.println("</div></body></html>");
    }
}