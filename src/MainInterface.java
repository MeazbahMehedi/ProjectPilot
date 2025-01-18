import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MainInterface extends JFrame {

    private Connection connection;

    public MainInterface(Connection connection) {
        this.connection = connection; // Initialize connection

        // Setup modern UI elements
        setTitle("Project Topic Selection Management System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set modern color palette
        Color backgroundColor = new Color(240, 240, 240); // Light gray
        Color buttonColor = new Color(70, 130, 180); // Steel blue
        Color accentColor = new Color(60, 179, 113); // Medium sea green

        // Use modern sans-serif font
        Font headerFont = new Font("Helvetica", Font.BOLD, 16);
        Font bodyFont = new Font("Helvetica", Font.PLAIN, 14);

        // Modern layout with card-based design
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(backgroundColor);
        getContentPane().add(mainPanel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Set padding
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally

        // Add modern button with icon and hover effect
        JButton teacherButton = createModernButton("Teacher", buttonColor, accentColor);
        teacherButton.setFont(headerFont);
        teacherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AuthHandler.authenticateTeacher(connection); // Authenticate teacher
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(teacherButton, gbc); // Add teacher button to panel

        JButton studentButton = createModernButton("Student", buttonColor, accentColor);
        studentButton.setFont(headerFont);
        studentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AuthHandler.authenticateStudent(connection); // Authenticate student
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 0;
        mainPanel.add(studentButton, gbc); // Add student button to panel

        JButton exportButton = createModernButton("Export Data", buttonColor, accentColor);
        exportButton.setFont(headerFont);
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String role = JOptionPane.showInputDialog("Enter role (teacher/student):").trim().toLowerCase();
                if (role.equals("teacher") || role.equals("student")) {
                    DataExporter.exportData(connection, role); // Export data based on role
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid role entered.");
                }
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(exportButton, gbc); // Add export button to panel

        JButton exitButton = createModernButton("Exit", buttonColor, accentColor);
        exitButton.setFont(headerFont);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    connection.close(); // Close connection
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                System.exit(0); // Exit application
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(exitButton, gbc); // Add exit button to panel
    }

    // Create a modern button with hover effects
    private JButton createModernButton(String text, Color buttonColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setBackground(buttonColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFont(new Font("Helvetica", Font.PLAIN, 14)); // Set button font

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor); // Change button color on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(buttonColor); // Reset button color when not hovered
            }
        });

        return button; // Return styled button
    }

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/projecttopicmanagement", "root", "YOUR_DB_PASSWORD");
            SwingUtilities.invokeLater(() -> new MainInterface(connection).setVisible(true)); // Start main interface
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }
    }
}
