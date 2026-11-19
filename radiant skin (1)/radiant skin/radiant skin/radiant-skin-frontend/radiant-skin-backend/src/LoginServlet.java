import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/radiant_skin?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            renderMessage(out, false, "Please enter both email and password.", "login.html");
            return;
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, email.trim());
            ps.setString(2, password);

            rs = ps.executeQuery();

            if (rs.next()) {
                String userName = rs.getString("name");
                HttpSession session = request.getSession();
                session.setAttribute("userEmail", email);
                session.setAttribute("userName", userName);

                renderMessage(out, true, "Welcome back, " + userName + "! Login Successful ✓", "dashboard.html");
            } else {
                renderMessage(out, false, "Invalid email or password. Please check and try again.", "login.html");
            }

        } catch (Exception e) {
            e.printStackTrace();
            renderMessage(out, false, "Error: " + e.getMessage(), "login.html");
        } finally {
            try {
                if (rs != null) rs.close();
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
        out.println("<html><head><meta charset='UTF-8'><title>Login Status</title>");
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
            out.println("setTimeout(function(){ window.location.href = '" + redirectUrl + "'; }, 2000);");
        }
        out.println("</script>");
        out.println("</head><body>");
        out.println("<div class='card'>");
        out.println("<div class='badge'>" + icon + "</div>");
        out.println("<h2>" + (success ? "Login Successful" : "Login Notice") + "</h2>");
        out.println("<p>" + message + "</p>");
        out.println("<a class='btn' href='" + redirectUrl + "'>" + (success ? "Go to Dashboard" : "Try Again") + "</a>");
        out.println("</div></body></html>");
    }
}
