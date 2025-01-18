import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TeacherHandler {

    public static void handleTeacherOperations(Connection conn) {
        String[] options = {"Add Project", "Delete Project", "Query Project", "View All Projects", "Edit Project", "View Statistics", "Export Data", "Exit"};

        JFrame frame = new JFrame("Teacher Dashboard");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full window mode
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize()); // Set to full screen size

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add empty border for padding
        panel.setBackground(new Color(240, 240, 240)); // Light gray background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        UIButtonFactory buttonFactory = new UIButtonFactory();

        for (int i = 0; i < options.length - 1; i++) {
            JButton button = buttonFactory.createButton(options[i]);
            button.addActionListener(e -> {
                switch (button.getText()) {
                    case "Add Project" -> addProject(conn);
                    case "Delete Project" -> deleteProject(conn);
                    case "Query Project" -> queryProject(conn); // Ensure this method is here
                    case "View All Projects" -> DBHelper.viewProjects(conn);
                    case "Edit Project" -> editProject(conn);
                    case "View Statistics" -> viewStatistics(conn);
                    case "Export Data" -> DataExporter.exportData(conn, "teacher");
                }
            });
            gbc.gridy = i;
            panel.add(button, gbc);
        }

        JButton exitButton = buttonFactory.createButton("Exit");
        exitButton.addActionListener(e -> frame.dispose());
        gbc.gridy = options.length - 1;
        panel.add(exitButton, gbc);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    public static void queryProject(Connection conn) {
        // Add the queryProject method here
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

    public static void addProject(Connection conn) {
        JTextField projectNameField = new JTextField();
        JTextField supervisorNameField = new JTextField();
        JTextField remainingMembersField = new JTextField();
        Object[] message = {
                "Project Name:", projectNameField,
                "Supervisor Name:", supervisorNameField,
                "Remaining Team Members:", remainingMembersField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Add Project", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String projectName = projectNameField.getText().trim();
            String supervisorName = supervisorNameField.getText().trim();
            int remainingMembers;
            try {
                remainingMembers = Integer.parseInt(remainingMembersField.getText().trim());
                if (remainingMembers < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid number for remaining team members.");
                return;
            }

            try {
                String sql = "INSERT INTO Projects (project_name, supervisor_name, remaining_team_members) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, projectName);
                    pstmt.setString(2, supervisorName);
                    pstmt.setInt(3, remainingMembers);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Project added successfully.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error adding project: " + e.getMessage());
            }
        }
    }

    public static void deleteProject(Connection conn) {
        JTextField projectIdField = new JTextField();
        Object[] message = { "Project ID:", projectIdField };

        int option = JOptionPane.showConfirmDialog(null, message, "Delete Project", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int projectId = Integer.parseInt(projectIdField.getText().trim());

            try {
                String unsetProjectSql = "UPDATE Students SET selected_project_id = NULL WHERE selected_project_id = ?";
                try (PreparedStatement unsetStmt = conn.prepareStatement(unsetProjectSql)) {
                    unsetStmt.setInt(1, projectId);
                    unsetStmt.executeUpdate();
                }

                String deleteProjectSql = "DELETE FROM Projects WHERE project_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteProjectSql)) {
                    deleteStmt.setInt(1, projectId);
                    int rowsAffected = deleteStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Project deleted successfully.");
                    } else {
                        JOptionPane.showMessageDialog(null, "No project found with the given ID.");
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error deleting project: " + e.getMessage());
            }
        }
    }

    public static void editProject(Connection conn) {
        String projectId = JOptionPane.showInputDialog("Enter the Project ID to edit:");
        if (projectId != null && !projectId.trim().isEmpty()) {
            JTextField projectNameField = new JTextField();
            JTextField supervisorNameField = new JTextField();
            JTextField remainingMembersField = new JTextField();
            Object[] message = {
                    "New Project Name (leave blank to keep unchanged):", projectNameField,
                    "New Supervisor Name (leave blank to keep unchanged):", supervisorNameField,
                    "New Remaining Team Members (leave blank to keep unchanged):", remainingMembersField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Edit Project", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String newName = projectNameField.getText().trim();
                String newSupervisor = supervisorNameField.getText().trim();
                String newRemaining = remainingMembersField.getText().trim();

                StringBuilder sql = new StringBuilder("UPDATE Projects SET ");
                boolean firstField = true;

                if (!newName.isEmpty()) {
                    sql.append("project_name = '").append(newName).append("'");
                    firstField = false;
                }
                if (!newSupervisor.isEmpty()) {
                    if (!firstField) sql.append(", ");
                    sql.append("supervisor_name = '").append(newSupervisor).append("'");
                }
                if (!newRemaining.isEmpty()) {
                    if (!firstField) sql.append(", ");
                    sql.append("remaining_team_members = ").append(newRemaining);
                }

                sql.append(" WHERE project_id = ").append(projectId.trim());

                try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Project updated successfully.");
                    } else {
                        JOptionPane.showMessageDialog(null, "No project found with the given ID.");
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Error editing project: " + e.getMessage());
                }
            }
        }
    }

    public static void viewStatistics(Connection conn) {
        try {
            String sql = "SELECT Students.student_name, Students.student_id, Students.major, " +
                    "GROUP_CONCAT(Projects.project_name SEPARATOR ', ') AS projects " +
                    "FROM Students " +
                    "LEFT JOIN Projects ON Projects.project_id = Students.selected_project_id " +
                    "GROUP BY Students.student_id";

            try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
                StringBuilder result = new StringBuilder();
                while (rs.next()) {
                    result.append(String.format("Student Name: %s, Student ID: %s, Major: %s, Projects: %s\n",
                            rs.getString("student_name"), rs.getString("student_id"), rs.getString("major"),
                            rs.getString("projects") != null ? rs.getString("projects") : "Not enrolled"));
                }
                JOptionPane.showMessageDialog(null, result.length() > 0 ? result.toString() : "No statistics available.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error viewing statistics: " + e.getMessage());
        }
    }

    static class UIButtonFactory {
        private final Color buttonColor = new Color(70, 130, 180);
        private final Color hoverColor = new Color(60, 179, 113);

        public JButton createButton(String text) {
            JButton button = new JButton(text);
            button.setBackground(buttonColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            button.setFont(new Font("Helvetica", Font.PLAIN, 14));

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
