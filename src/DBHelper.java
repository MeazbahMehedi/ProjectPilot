import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHelper {

    // Method to create necessary tables if they don't exist
    public static void createTables(Connection conn) throws SQLException {
        String createProjectsTable = "CREATE TABLE IF NOT EXISTS Projects (" +
                "project_id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                "project_name TEXT NOT NULL, " +
                "remaining_team_members INTEGER NOT NULL, " +
                "supervisor_name TEXT NOT NULL)";

        String createStudentsTable = "CREATE TABLE IF NOT EXISTS Students (" +
                "student_id VARCHAR(10) PRIMARY KEY, " +
                "student_name TEXT NOT NULL, " +
                "major TEXT NOT NULL, " +
                "selected_project_id INTEGER, " +
                "FOREIGN KEY (selected_project_id) REFERENCES Projects(project_id))";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createProjectsTable); // Execute table creation for Projects
            stmt.execute(createStudentsTable); // Execute table creation for Students
        }
    }

    // Method to view all projects in the database
    public static void viewProjects(Connection conn) {
        try {
            String sql = "SELECT * FROM Projects";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                StringBuilder result = new StringBuilder();
                while (rs.next()) {
                    result.append(String.format("Project ID: %d, Name: %s, Supervisor: %s, Remaining Members: %d\n",
                            rs.getInt("project_id"), rs.getString("project_name"), rs.getString("supervisor_name"), rs.getInt("remaining_team_members")));
                }
                JOptionPane.showMessageDialog(null, result.length() > 0 ? result.toString() : "No projects found."); // Display projects
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error viewing projects: " + e.getMessage());
        }
    }

    // Method to update the statistics of students and their enrolled projects
    public static void updateStatistics(Connection conn) throws SQLException {
        String sql = "SELECT Students.student_name, Students.student_id, Students.major, " +
                "GROUP_CONCAT(Projects.project_name SEPARATOR ', ') AS projects " +
                "FROM Students " +
                "LEFT JOIN Projects ON FIND_IN_SET(Projects.project_id, Students.selected_project_id) > 0 " +
                "GROUP BY Students.student_name, Students.student_id, Students.major";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                result.append(String.format("Student Name: %s, Student ID: %s, Major: %s, Projects: %s\n",
                        rs.getString("student_name"), rs.getString("student_id"), rs.getString("major"),
                        rs.getString("projects") != null ? rs.getString("projects") : "Not enrolled"));
            }
            JOptionPane.showMessageDialog(null, result.length() > 0 ? result.toString() : "No statistics available."); // Display results
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error viewing statistics: " + e.getMessage());
        }
    }
}
