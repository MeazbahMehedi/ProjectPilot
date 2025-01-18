import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class AuthHandler {

    // Method to authenticate teachers
    public static void authenticateTeacher(Connection conn) {
        JFrame frame = new JFrame("Teacher Authentication");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.white);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Set panel border

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Set padding
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally

        JLabel titleLabel = new JLabel("Teacher Authentication", JLabel.CENTER);
        titleLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.gridwidth = 2; // Span 2 columns
        panel.add(titleLabel, gbc); // Add title label to panel

        JLabel accountLabel = new JLabel("Account Number:");
        JTextField accountNumberField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        accountLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
        passwordLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
        gbc.gridwidth = 1; // Span 1 column

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(accountLabel, gbc); // Add account label to panel

        gbc.gridx = 1;
        panel.add(accountNumberField, gbc); // Add account number field to panel

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc); // Add password label to panel

        gbc.gridx = 1;
        panel.add(passwordField, gbc); // Add password field to panel

        UIButtonFactory buttonFactory = new UIButtonFactory();

        JButton loginButton = buttonFactory.createButton("Login");
        loginButton.addActionListener(e -> {
            // Get account number and password inputs
            String accountNumber = accountNumberField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            // Simple authentication check
            if (accountNumber.equals("001") && password.equals("001")) {
                TeacherHandler.handleTeacherOperations(conn); // Successful login
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Authentication failed. Please try again.");
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc); // Add login button to panel

        frame.getContentPane().add(panel); // Add panel to frame
        frame.setVisible(true); // Display frame
    }

    // Method to authenticate students
    public static void authenticateStudent(Connection conn) {
        JFrame frame = new JFrame("Student Authentication");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.white);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Set panel border

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Set padding
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally

        JLabel titleLabel = new JLabel("Student Authentication", JLabel.CENTER);
        titleLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.gridwidth = 2; // Span 2 columns
        panel.add(titleLabel, gbc); // Add title label to panel

        JLabel studentIdLabel = new JLabel("Student ID:");
        JTextField studentIdField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        studentIdLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
        passwordLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
        gbc.gridwidth = 1; // Span 1 column

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(studentIdLabel, gbc); // Add student ID label to panel

        gbc.gridx = 1;
        panel.add(studentIdField, gbc); // Add student ID field to panel

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc); // Add password label to panel

        gbc.gridx = 1;
        panel.add(passwordField, gbc); // Add password field to panel

        UIButtonFactory buttonFactory = new UIButtonFactory();

        JButton loginButton = buttonFactory.createButton("Login");
        loginButton.addActionListener(e -> {
            // Get student ID and password inputs
            String studentId = studentIdField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            // Simple authentication check
            if (studentId.equals("002") && password.equals("002")) {
                StudentHandler.handleStudentOperations(conn); // Successful login
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Authentication failed. Please try again.");
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc); // Add login button to panel

        frame.getContentPane().add(panel); // Add panel to frame
        frame.setVisible(true); // Display frame
    }

    // Factory class for creating modern buttons
    static class UIButtonFactory {
        private final Color buttonColor = new Color(70, 130, 180); // Steel blue
        private final Color hoverColor = new Color(60, 179, 113); // Medium sea green
        private final Font font = new Font("Helvetica", Font.BOLD, 16);

        // Method to create a styled button
        JButton createButton(String text) {
            JButton button = new JButton(text);
            button.setBackground(buttonColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
            button.setFont(font); // Set button font
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(hoverColor); // Change button color on hover
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(buttonColor); // Reset button color when not hovered
                }
            });
            return button;
        }
    }
}
