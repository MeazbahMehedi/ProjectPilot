import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class DataExporter {

    // Method to export data in chosen format (CSV or JSON)
    public static void exportData(Connection conn, String role) {
        String[] options = {"CSV", "JSON"};
        int choice = JOptionPane.showOptionDialog(null, "Select export format", "Export Data", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == -1) return; // If user cancels the dialog, exit method

        String format = options[choice].toLowerCase();
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (format.equals("csv")) {
                exportToCSV(conn, filePath, role);
            } else {
                exportToJSON(conn, filePath, role);
            }
        }
    }

    // Export data to CSV format
    private static void exportToCSV(Connection conn, String filePath, String role) {
        String query = role.equals("student") ? "SELECT * FROM Students" : "SELECT * FROM Projects";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query);
             FileWriter fw = new FileWriter(filePath + ".csv")) {

            int columnCount = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                fw.append(rs.getMetaData().getColumnName(i)).append(i < columnCount ? "," : "\n");
            }
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    fw.append(rs.getString(i)).append(i < columnCount ? "," : "\n");
                }
            }
            JOptionPane.showMessageDialog(null, "Data exported successfully to " + filePath + ".csv");
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(null, "Error exporting data: " + e.getMessage());
        }
    }
    // Export data to JSON format
    private static void exportToJSON(Connection conn, String filePath, String role) {
        String query = role.equals("student") ? "SELECT * FROM Students" : "SELECT * FROM Projects";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query);
             FileWriter fw = new FileWriter(filePath + ".json")) {

            // Use Gson to convert ResultSet to JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(resultSetToJson(rs));
            fw.write(json); // Write JSON to file
            JOptionPane.showMessageDialog(null, "Data exported successfully to " + filePath + ".json");
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(null, "Error exporting data: " + e.getMessage());
        }
    }

    // Convert ResultSet to JSON-friendly structure
    private static Object resultSetToJson(ResultSet rs) throws SQLException {
        var metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        var list = new ArrayList<HashMap<String, Object>>();
        while (rs.next()) {
            var map = new HashMap<String, Object>();
            for (int i = 1; i <= columnCount; i++) {
                map.put(metaData.getColumnName(i), rs.getObject(i));
            }
            list.add(map);
        }
        return list; // Return the list of maps
    }
}
