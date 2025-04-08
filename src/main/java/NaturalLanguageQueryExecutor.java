
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

import org.json.JSONObject;

public class NaturalLanguageQueryExecutor {
    
    // Database connection URL
    private static final String JDBC_URL = "jdbc:db2://localhost:25000/USER";
    
    // Hugging Face API endpoint
    private static final String HF_API_ENDPOINT = "https://kshitu-genai.hf.space/get_answer";
    
    // Schema information to include in prompts
    private static final String SCHEMA_INFO = 
        "You are a DB2 SQL expert. Convert the following question to a valid DB2 SQL query for the INVENTORY table.\n\n" +
        "IMPORTANT: Return the SQL query as a single line without any line breaks or newlines.\n\n" +
        "Table schema:\n" +
        "```sql\n" +
        "CREATE TABLE INVENTORY (\n" +
        "    ID INTEGER NOT NULL PRIMARY KEY,\n" + 
        "    NAME VARCHAR(50) NOT NULL,\n" + 
        "    CATEGORY VARCHAR(30),\n" + 
        "    STOCK INTEGER,\n" + 
        "    PRICE DECIMAL(10,2),\n" + 
        "    SUPPLIER VARCHAR(50),\n" + 
        "    ADDED_DATE DATE,\n" + 
        "    LAST_UPDATED DATE\n" +
        ");\n" +
        "```\n\n" +
        "The table contains categories: 'Electronics', 'Clothing', 'Home Appliances', 'Footwear', 'Beauty', 'Kitchen Appliances'.\n" +
        "Products include: Laptops, Smartphones, T-shirts, Washing Machines, Headphones, Sneakers, Refrigerators, etc.\n\n" +
        "Return ONLY a valid DB2 SQL query without any explanation. Question: ";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;
        Statement statement = null;
        
        try {
            // Load the DB2 JDBC driver and establish connection
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            
            // Setup connection properties
            Properties props = new Properties();
            props.setProperty("user", "DELL");
            props.setProperty("password", "Kshitu@2211");
            
            System.out.println("Connecting to DB2 database...");
            connection = DriverManager.getConnection(JDBC_URL, props);
            statement = connection.createStatement();
            
            System.out.println("\n✅ Connected to DB2 database");
            System.out.println("----------------------------------------");
            System.out.println("🤖 Natural Language to SQL Query Executor");
            System.out.println("----------------------------------------");
            System.out.println("Ask questions about your inventory in natural language");
            System.out.println("Type 'exit' to quit the application");
            System.out.println("----------------------------------------");
            
            while (true) {
                System.out.print("\n Enter your question: ");
                String userQuery = scanner.nextLine().trim();
                
                if (userQuery.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting the application...");
                    break;
                }
                
                if (userQuery.isEmpty()) {
                    System.out.println("Please enter a valid question.");
                    continue;
                }
                
                System.out.println("\n Generating SQL query...");
                
                // Create instance and send the natural language query to Hugging Face and get SQL query
                NaturalLanguageQueryExecutor executor = new NaturalLanguageQueryExecutor();
                String sqlQuery = executor.generateSqlQuery(userQuery);
                
                if (sqlQuery == null || sqlQuery.isEmpty()) {
                    System.out.println(" Failed to generate a SQL query. Please try a different question.");
                    continue;
                }
                
                System.out.println("\n Generated SQL query:");
                System.out.println("----------------------------------------");
                System.out.println(sqlQuery);
                System.out.println("----------------------------------------");
                
                System.out.print("Do you want to execute this query? (y/n): ");
                String execute = scanner.nextLine().trim().toLowerCase();
                
                if (execute.equals("y")) {
                    System.out.println("\n Executing query...");
                    
                    try {
                        boolean isResultSet = statement.execute(sqlQuery);
                        
                        if (isResultSet) {
                            ResultSet resultSet = statement.getResultSet();
                            System.out.println("\n Query Results:");
                            System.out.println("----------------------------------------");
                            displayResultSet(resultSet);
                            resultSet.close();
                        } else {
                            int updateCount = statement.getUpdateCount();
                            System.out.println("\n Query executed successfully. Rows affected: " + updateCount);
                        }
                    } catch (SQLException e) {
                        System.out.println("\n Error executing query: " + e.getMessage());
                        System.out.println("Please try a different question or rephrase your query.");
                    }
                } else {
                    System.out.println("Query execution cancelled.");
                }
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("DB2 JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL exception occurred:");
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (scanner != null) scanner.close();
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
     * Sends a natural language query to the Hugging Face endpoint and gets a SQL query
     */
    public String generateSqlQuery(String naturalLanguageQuery) {
        try {
            URL url = new URL(HF_API_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            // Prepare the full prompt with schema information
            String fullPrompt = SCHEMA_INFO + naturalLanguageQuery;
            
            // Create the JSON payload
            String jsonInputString = "{\"question\": \"" + escapeJson(fullPrompt) + "\"}";
            
            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            // Read the response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                // Parse the JSON response to extract the SQL query
                JSONObject jsonResponse = new JSONObject(response.toString());
                String sqlQuery = jsonResponse.getString("answer").trim();
                
                return sqlQuery;
            } else {
                System.err.println("Error: API request failed with status code " + responseCode);
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.err.println("Error response: " + response.toString());
                return null;
            }
            
        } catch (IOException e) {
            System.err.println("Error connecting to Hugging Face API:");
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Escape special characters in JSON strings
     */
    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
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
        int rowCount = 0;
        while (rs.next()) {
            rowCount++;
            for (int i = 1; i <= columnCount; i++) {
                String value = rs.getString(i);
                System.out.printf("%-20s", value == null ? "NULL" : value);
            }
            System.out.println();
        }
        
        System.out.println("----------------------------------------");
        System.out.println("Total rows: " + rowCount);
    }
}