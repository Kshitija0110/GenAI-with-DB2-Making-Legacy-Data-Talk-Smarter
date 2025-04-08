import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Db2JdbcTest {
      
    // Use correct port 25000 as found in services file
    private static final String JDBC_URL = "jdbc:db2://localhost:25000/USER";
    
    public static void main(String[] args) {
        Connection connection = null;

        try {
            // Load the DB2 JDBC driver
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            
            // Create properties for connection
            Properties props = new Properties();
            props.setProperty("user", "DELL");
            props.setProperty("password", "Kshitu@2211");
            
            // Try without security mechanism property
            // Or try different security mechanism values:
            // props.setProperty("securityMechanism", "3"); // USER_ONLY security
            
            System.out.println("Attempting to connect to: " + JDBC_URL);
            connection = DriverManager.getConnection(JDBC_URL, props);
            
            // Check if the connection is valid
            if (connection != null && !connection.isClosed()) {
                System.out.println("Connection to DB2 database is successful.");
            } else {
                System.out.println("Failed to connect to DB2 database.");
            }

        } catch (ClassNotFoundException e) {
            System.err.println("DB2 JDBC Driver not found. Include the driver in your classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL exception occurred while trying to connect to the DB2 database.");
            e.printStackTrace();
        } finally {
            // Close the connection
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Failed to close the connection.");
                e.printStackTrace();
            }
        }
    }
}