import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentHandler {
    // Method to handle student operations and create the main UI
    public static void handleStudentOperations(Connection conn) {
        String[] options = {"Display All Projects", "Query Project", "Select Project", "Cancel Selected Project", "Export Data", "Exit"};
        JFrame frame = new JFrame("Student Dashboard");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
// Create a button factory instance
        UIButtonFactory buttonFactory = new UIButtonFactory();
        // Adding buttons to the panel
        for (int i = 0; i < options.length - 1; i++) {
            JButton button = buttonFactory.createButton(options[i]);
            button.addActionListener(e -> {
                switch (button.getText()) {
                    case "Display All Projects" -> DBHelper.viewProjects(conn);
                    case "Query Project" -> queryProject(conn);
                    case "Select Project" -> selectProject(conn);
                    case "Cancel Selected Project" -> cancelSelectedProject(conn);
                    case "Export Data" -> DataExporter.exportData(conn, "student");
                }
            });
            gbc.gridy = i;
            panel.add(button, gbc);
        }
        // Adding exit button
        JButton exitButton = buttonFactory.createButton("Exit");
        exitButton.addActionListener(e -> frame.dispose());
        gbc.gridy = options.length - 1;
        panel.add(exitButton, gbc);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
    public static void selectProject(Connection conn) {
        JTextField projectIdField = new JTextField();
        JTextField studentIdField = new JTextField();
        JTextField studentNameField = new JTextField();
        JTextField majorField = new JTextField();
        Object[] message = {
                "Project ID:", projectIdField,
                "Student ID:", studentIdField,
                "Student Name:", studentNameField,
                "Major:", majorField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Select Project", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int projectId = Integer.parseInt(projectIdField.getText().trim());
            String studentId = studentIdField.getText().trim();
            String studentName = studentNameField.getText().trim();
            String major = majorField.getText().trim();

            try {
                String checkAvailabilitySql = "SELECT remaining_team_members FROM Projects WHERE project_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkAvailabilitySql)) {
                    checkStmt.setInt(1, projectId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt("remaining_team_members") > 0) {
                            String updateStudentSql = "UPDATE Students SET selected_project_id = ? WHERE student_id = ?";
                            try (PreparedStatement updateStudentStmt = conn.prepareStatement(updateStudentSql)) {
                                updateStudentStmt.setInt(1, projectId);
                                updateStudentStmt.setString(2, studentId);
                                updateStudentStmt.executeUpdate();
                            }

                            String updateProjectSql = "UPDATE Projects SET remaining_team_members = remaining_team_members - 1 WHERE project_id = ?";
                            try (PreparedStatement updateProjectStmt = conn.prepareStatement(updateProjectSql)) {
                                updateProjectStmt.setInt(1, projectId);
                                updateProjectStmt.executeUpdate();
                            }

                            String insertOrUpdateStudentSql = "INSERT INTO Students (student_id, student_name, major, selected_project_id) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE selected_project_id = ?";
                            try (PreparedStatement insertOrUpdateStudentStmt = conn.prepareStatement(insertOrUpdateStudentSql)) {
                                insertOrUpdateStudentStmt.setString(1, studentId);
                                insertOrUpdateStudentStmt.setString(2, studentName);
                                insertOrUpdateStudentStmt.setString(3, major);
                                insertOrUpdateStudentStmt.setInt(4, projectId);
                                insertOrUpdateStudentStmt.setInt(5, projectId);
                                insertOrUpdateStudentStmt.executeUpdate();
                            }

                            JOptionPane.showMessageDialog(null, "Project selected successfully.");
                        } else {
                            JOptionPane.showMessageDialog(null, "No remaining slots in the selected project.");
                        }
                    }
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error selecting project: " + e.getMessage());
            }
        }
    }


    public static void cancelSelectedProject(Connection conn) {
        JTextField studentIdField = new JTextField();
        JTextField projectIdField = new JTextField();
        Object[] message = { "Student ID:", studentIdField, "Project ID:", projectIdField };

        int option = JOptionPane.showConfirmDialog(null, message, "Cancel Selected Project", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String studentId = studentIdField.getText().trim();
            int projectId = Integer.parseInt(projectIdField.getText().trim());

            try {
                String checkProjectSql = "SELECT selected_project_id FROM Students WHERE student_id = ? AND selected_project_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkProjectSql)) {
                    checkStmt.setString(1, studentId);
                    checkStmt.setInt(2, projectId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            String updateStudentSql = "UPDATE Students SET selected_project_id = NULL WHERE student_id = ? AND selected_project_id = ?";
                            String updateProjectSql = "UPDATE Projects SET remaining_team_members = remaining_team_members + 1 WHERE project_id = ?";

                            try (PreparedStatement updateStudentStmt = conn.prepareStatement(updateStudentSql);
                                 PreparedStatement updateProjectStmt = conn.prepareStatement(updateProjectSql)) {
                                updateStudentStmt.setString(1, studentId);
                                updateStudentStmt.setInt(2, projectId);
                                updateStudentStmt.executeUpdate();
                                updateProjectStmt.setInt(1, projectId);
                                updateProjectStmt.executeUpdate();
                                JOptionPane.showMessageDialog(null, "Project selection cancelled successfully.");
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "No matching project found for the given student.");
                        }
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error cancelling project selection: " + e.getMessage());
            }
        }
    }
    public static void queryProject(Connection conn) {
        String searchTerm = JOptionPane.showInputDialog("Enter project name (case-sensitive):");
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            try {
                String sql = "SELECT * FROM Projects WHERE project_name = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, searchTerm.trim());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        StringBuilder result = new StringBuilder();
                        while (rs.next()) {
                            result.append(String.format("Project ID: %d, Name: %s, Supervisor: %s, Remaining Members: %d\n",
                                    rs.getInt("project_id"), rs.getString("project_name"), rs.getString("supervisor_name"), rs.getInt("remaining_team_members")));
                        }
                        JOptionPane.showMessageDialog(null, result.length() > 0 ? result.toString() : "No projects found.");
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error querying projects: " + e.getMessage());
            }
        }
    }


    // Modern UIButtonFactory for creating styled buttons
    static class UIButtonFactory {
        private final Color buttonColor = new Color(70, 130, 180); // Steel blue
        private final Color hoverColor = new Color(60, 179, 113); // Medium sea green
        private final Font font = new Font("Helvetica", Font.BOLD, 16);

        JButton createButton(String text) {
            JButton button = new JButton(text);
            button.setBackground(buttonColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
            button.setFont(font);
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(hoverColor);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(buttonColor);
                }
            });
            return button;
        }
    }
}
