import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class InventoryDbCreator {
    
    // Use the same connection URL used in your other programs
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
            
            // Drop existing inventory table if it exists
            try {
                statement.executeUpdate("DROP TABLE INVENTORY");
                System.out.println("Dropped existing INVENTORY table");
            } catch (SQLException e) {
                System.out.println("INVENTORY table does not exist yet");
            }
            
            // Create INVENTORY table
            String createInventoryTable = 
                "CREATE TABLE INVENTORY (" +
                "ID INTEGER NOT NULL PRIMARY KEY, " +
                "NAME VARCHAR(50) NOT NULL, " +
                "CATEGORY VARCHAR(30), " +
                "STOCK INTEGER, " +
                "PRICE DECIMAL(10,2), " +
                "SUPPLIER VARCHAR(50), " +
                "ADDED_DATE DATE, " +
                "LAST_UPDATED DATE)";
            
            statement.executeUpdate(createInventoryTable);
            System.out.println("Created INVENTORY table");
            
            // Insert data into INVENTORY
            String[] inventoryInserts = {
                "INSERT INTO INVENTORY VALUES (1, 'Laptop', 'Electronics', 10, 1200.00, 'Dell', '2024-04-01', '2024-04-05')",
                "INSERT INTO INVENTORY VALUES (2, 'Smartphone', 'Electronics', 5, 899.99, 'Samsung', '2024-04-03', '2024-04-06')",
                "INSERT INTO INVENTORY VALUES (3, 'T-shirt', 'Clothing', 50, 25.00, 'FashionCo', '2024-03-20', '2024-04-03')",
                "INSERT INTO INVENTORY VALUES (4, 'Washing Machine', 'Home Appliances', 4, 499.99, 'LG', '2024-03-28', '2024-04-07')",
                "INSERT INTO INVENTORY VALUES (5, 'Headphones', 'Electronics', 20, 199.99, 'Sony', '2024-04-02', '2024-04-05')",
                "INSERT INTO INVENTORY VALUES (6, 'Sneakers', 'Footwear', 30, 75.00, 'Adidas', '2024-03-25', '2024-04-04')",
                "INSERT INTO INVENTORY VALUES (7, 'Refrigerator', 'Home Appliances', 6, 1200.00, 'Whirlpool', '2024-03-18', '2024-04-01')",
                "INSERT INTO INVENTORY VALUES (8, 'Gaming Console', 'Electronics', 7, 499.99, 'Microsoft', '2024-04-05', '2024-04-06')",
                "INSERT INTO INVENTORY VALUES (9, 'Perfume', 'Beauty', 40, 60.00, 'Chanel', '2024-03-30', '2024-04-02')",
                "INSERT INTO INVENTORY VALUES (10, 'Coffee Maker', 'Kitchen Appliances', 10, 149.99, 'Philips', '2024-04-01', '2024-04-06')"
            };
            
            for (String sql : inventoryInserts) {
                statement.executeUpdate(sql);
            }
            System.out.println("Inserted data into INVENTORY table");
            
            System.out.println("Inventory database setup completed successfully!");
            
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
}