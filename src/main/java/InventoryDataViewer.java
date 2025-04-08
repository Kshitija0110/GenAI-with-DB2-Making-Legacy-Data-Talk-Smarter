import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class InventoryDataViewer {
    
    // Use the same connection URL as your other applications
    private static final String JDBC_URL = "jdbc:db2://localhost:25000/USER";
    
    public static void main(String[] args) {
        Connection connection = null;
        Statement statement = null;
        
        try {
            // Load the DB2 JDBC driver
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            
            // Setup connection properties
            Properties props = new Properties();
            props.setProperty("user", "DELL");
            props.setProperty("password", "Kshitu@2211");
            
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(JDBC_URL, props);
            statement = connection.createStatement();
            
            // Basic query to display all inventory items
            System.out.println("\n--- INVENTORY ITEMS ---");
            ResultSet inventoryRS = statement.executeQuery("SELECT * FROM INVENTORY ORDER BY ID");
            displayResultSet(inventoryRS);
            inventoryRS.close();
            
            // Query to display items by category
            System.out.println("\n\n--- ITEMS BY CATEGORY ---");
            ResultSet categoryRS = statement.executeQuery(
                "SELECT CATEGORY, COUNT(*) AS ITEM_COUNT, SUM(STOCK) AS TOTAL_STOCK, " +
                "AVG(PRICE) AS AVG_PRICE " +
                "FROM INVENTORY " +
                "GROUP BY CATEGORY " +
                "ORDER BY ITEM_COUNT DESC");
            displayResultSet(categoryRS);
            categoryRS.close();
            
            // Query to display low stock items (less than 10)
            System.out.println("\n\n--- LOW STOCK ITEMS (LESS THAN 10) ---");
            ResultSet lowStockRS = statement.executeQuery(
                "SELECT ID, NAME, CATEGORY, STOCK, SUPPLIER " +
                "FROM INVENTORY " +
                "WHERE STOCK < 10 " +
                "ORDER BY STOCK");
            displayResultSet(lowStockRS);
            lowStockRS.close();
            
            // Query to display high-value items (over $500)
            System.out.println("\n\n--- HIGH VALUE ITEMS (OVER $500) ---");
            ResultSet highValueRS = statement.executeQuery(
                "SELECT ID, NAME, CATEGORY, STOCK, PRICE, STOCK * PRICE AS TOTAL_VALUE " +
                "FROM INVENTORY " +
                "WHERE PRICE > 500 " +
                "ORDER BY TOTAL_VALUE DESC");
            displayResultSet(highValueRS);
            highValueRS.close();
            
        } catch (ClassNotFoundException e) {
            System.err.println("DB2 JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL exception occurred:");
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
                System.out.println("Database resources closed.");
            } catch (SQLException e) {
                System.err.println("Failed to close database resources.");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Utility method to display a ResultSet in a formatted table
     */
    private static void displayResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // Print column headers
        for (int i = 1; i <= columnCount; i++) {
            System.out.printf("%-20s", metaData.getColumnName(i));
        }
        System.out.println();
        
        // Print separator line
        for (int i = 1; i <= columnCount; i++) {
            System.out.print("--------------------");
        }
        System.out.println();
        
        // Print data rows
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String value = rs.getString(i);
                System.out.printf("%-20s", value == null ? "NULL" : value);
            }
            System.out.println();
        }
    }
}