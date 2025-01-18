import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ProjectSelectionManager {

    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/projecttopicmanagement", "root", "YOUR_DB_PASSWORD"); // Establish database connection
            DBHelper.createTables(conn); // Create tables if they don't exist
            SwingUtilities.invokeLater(() -> new MainInterface(conn).setVisible(true)); // Invoke Main Interface UI
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage()); // Display error message
        }
    }
}
