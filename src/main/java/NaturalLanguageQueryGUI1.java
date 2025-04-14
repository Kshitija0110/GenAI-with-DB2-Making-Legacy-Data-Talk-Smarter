import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class NaturalLanguageQueryGUI1 extends JFrame {
    private JTextArea queryInput;
    private JTextArea sqlDisplay;
    private JTable resultTable;
    private JLabel statusLabel;
    private Connection connection;
    private NaturalLanguageQueryExecutor queryExecutor;
    private JButton executeButton;

    public NaturalLanguageQueryGUI1() {
        queryExecutor = new NaturalLanguageQueryExecutor();
        initializeUI();
        connectToDatabase();
    }

    private void initializeUI() {
        setTitle("DB2 NLP GenAI Query Assistant");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(25, 25, 112),
                        getWidth(), getHeight(), new Color(72, 61, 139));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(10, 10));
        
        // Title Panel
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Input Panel
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // Results Panel
        JPanel resultsPanel = createResultsPanel();
        mainPanel.add(resultsPanel, BorderLayout.SOUTH);
         resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Status Label
        statusLabel = new JLabel("Ready to process queries...");
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setLocationRelativeTo(null);
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("DB2 Natural Language Query Assistant");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Powered by GenAI");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitleLabel.setForeground(Color.LIGHT_GRAY);
        
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitleLabel);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        return titlePanel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.setLayout(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Query input area
        queryInput = new JTextArea(3, 40);
        queryInput.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane queryScroll = new JScrollPane(queryInput);
        queryScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            "Enter your question in natural language",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 12),
            Color.BLACK
        ));

        // SQL Display area
        sqlDisplay = new JTextArea(3, 40);
        sqlDisplay.setEditable(false);
        sqlDisplay.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane sqlScroll = new JScrollPane(sqlDisplay);
        sqlScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            "Generated SQL Query",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 12),
            Color.BLACK
        ));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);

        JButton generateButton = new JButton("Generate Query");
    styleButton(generateButton);
    generateButton.addActionListener(e -> generateQuery());
        
    executeButton = new JButton("Execute Query");
    styleButton(executeButton);
    executeButton.setEnabled(false); // Disabled by default
    executeButton.addActionListener(e -> executeQuery());
        
        JButton clearButton = new JButton("Clear");
        styleButton(clearButton);
        clearButton.addActionListener(e -> clearAll());
        buttonPanel.add(generateButton);
        buttonPanel.add(executeButton);
        buttonPanel.add(clearButton);

        // Arrange components
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.add(queryScroll, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        inputPanel.add(topPanel, BorderLayout.NORTH);
        inputPanel.add(sqlScroll, BorderLayout.CENTER);

        return inputPanel;
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
    }

    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setOpaque(false);
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        resultTable = new JTable();
        resultTable.setFillsViewportHeight(true);
        // resultTable.setBackground(Color.BLACK);
        // resultTable.setFont(new Font("Arial", Font.PLAIN, 12));
        resultTable.setBackground(Color.WHITE);
    resultTable.setForeground(Color.BLACK);
    resultTable.setFont(new Font("Arial", Font.PLAIN, 12));
    resultTable.setGridColor(Color.GRAY);
    resultTable.setShowGrid(true);
    
    // Set header appearance
    resultTable.getTableHeader().setBackground(new Color(70, 130, 180));
    resultTable.getTableHeader().setForeground(Color.BLACK);
    resultTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane tableScroll = new JScrollPane(resultTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            "Query Results",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 12),
            Color.BLACK
        ));
        
        resultsPanel.add(tableScroll, BorderLayout.CENTER);
        return resultsPanel;
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            Properties props = new Properties();
            props.setProperty("user", "DELL");
            props.setProperty("password", "Kshitu@2211");
            connection = DriverManager.getConnection("jdbc:db2://localhost:25000/USER", props);
            statusLabel.setText("Connected to database successfully");
        } catch (Exception e) {
            statusLabel.setText("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

private void generateQuery() {
    String naturalQuery = queryInput.getText().trim();
    if (naturalQuery.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a question", "Input Required", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        statusLabel.setText("Generating SQL query...");
        String sqlQuery = queryExecutor.generateSqlQuery(naturalQuery);
        
        if (sqlQuery == null || sqlQuery.isEmpty()) {
            statusLabel.setText("Failed to generate SQL query");
            return;
        }

        sqlDisplay.setText(sqlQuery);
        statusLabel.setText("SQL query generated. Click Execute to run the query.");
        executeButton.setEnabled(true);
    } catch (Exception e) {
        statusLabel.setText("Error generating query: " + e.getMessage());
        e.printStackTrace();
    }
}

// private static void displayResultSet(ResultSet rs) throws SQLException {
//         ResultSetMetaData metaData = rs.getMetaData();
//         int columnCount = metaData.getColumnCount();
        
//         // Print column headers
//         for (int i = 1; i <= columnCount; i++) {
//             System.out.printf("%-20s", metaData.getColumnName(i));
//         }
//         System.out.println();
        
//         // Print separator line
//         for (int i = 1; i <= columnCount; i++) {
//             System.out.print("--------------------");
//         }
//         System.out.println();
        
//         // Print data rows
//         while (rs.next()) {
//             for (int i = 1; i <= columnCount; i++) {
//                 String value = rs.getString(i);
//                 System.out.printf("%-20s", value == null ? "NULL" : value);
//             }
//             System.out.println();
//         }
//     }
private void displayResultSet(ResultSet rs) throws SQLException {
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();

    // Create vector of column names
    Vector<String> columnNames = new Vector<>();
    for (int i = 1; i <= columnCount; i++) {
        columnNames.add(metaData.getColumnName(i));
    }

    // Create vector of row data
    Vector<Vector<Object>> data = new Vector<>();
    while (rs.next()) {
        Vector<Object> row = new Vector<>();
        for (int i = 1; i <= columnCount; i++) {
            Object value = rs.getObject(i);
            row.add(value == null ? "NULL" : value.toString());
        }
        data.add(row);
    }

    // Update the table on the EDT
    SwingUtilities.invokeLater(() -> {
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        resultTable.setModel(model);

        // Auto-resize columns
        for (int i = 0; i < columnCount; i++) {
            resultTable.getColumnModel().getColumn(i).setPreferredWidth(150);
        }

        // Update status
        statusLabel.setText("Query executed successfully. Found " + data.size() + " rows.");
        
        // Ensure table is visible
        resultTable.revalidate();
        resultTable.repaint();
    });
}

private void executeQuery() {
    String sqlQuery = sqlDisplay.getText().trim();
    if (sqlQuery.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No SQL query to execute", "Query Required", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        statusLabel.setText("Executing query...");
        Statement stmt = connection.createStatement();
        
        ResultSet res = stmt.executeQuery(sqlQuery);
        displayResultSet(res);
            res.close();
        stmt.close();
    } catch (SQLException e) {
        statusLabel.setText("Error executing query: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Error executing query: " + e.getMessage(), 
            "Query Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}

// private void displayResults(ResultSet rs) throws SQLException {
//     ResultSetMetaData metaData = rs.getMetaData();
//     int columnCount = metaData.getColumnCount();

//     // Create vector of column names
//     Vector<String> columnNames = new Vector<>();
//     for (int i = 1; i <= columnCount; i++) {
//         columnNames.add(metaData.getColumnName(i));
//     }

//     // Create vector of row data
//     Vector<Vector<Object>> data = new Vector<>();
//     while (rs.next()) {
//         Vector<Object> row = new Vector<>();
//         for (int i = 1; i <= columnCount; i++) {
//             Object value = rs.getObject(i);
//             row.add(value == null ? "NULL" : value.toString());
//         }
//         data.add(row);
//     }

//     // Create and set the table model on the EDT
//     SwingUtilities.invokeLater(() -> {
//         try {
//             DefaultTableModel model = new DefaultTableModel(data, columnNames) {
//                 @Override
//                 public boolean isCellEditable(int row, int column) {
//                     return false;
//                 }
//             };
//             resultTable.setModel(model);

//             // Auto-resize columns
//             for (int i = 0; i < columnCount; i++) {
//                 int width = 100; // minimum width
//                 resultTable.getColumnModel().getColumn(i).setPreferredWidth(width);
//             }
            
//             // Ensure table is visible
//             resultTable.revalidate();
//             resultTable.repaint();
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     });
// }

    // private void displayResults(ResultSet rs) throws SQLException {
    //     ResultSetMetaData metaData = rs.getMetaData();
    //     int columnCount = metaData.getColumnCount();

    //     // Create column headers
    //     Vector<String> columnNames = new Vector<>();
    //     for (int i = 1; i <= columnCount; i++) {
    //         columnNames.add(metaData.getColumnName(i));
    //     }

    //     // Create data vectors
    //     Vector<Vector<Object>> data = new Vector<>();
    //     while (rs.next()) {
    //         Vector<Object> row = new Vector<>();
    //         for (int i = 1; i <= columnCount; i++) {
    //             row.add(rs.getObject(i));
    //         }
    //         data.add(row);
    //     }

    //     // Create and set table model
    //     DefaultTableModel model = new DefaultTableModel(data, columnNames);
    //     resultTable.setModel(model);
    // }

    private void clearAll() {
        queryInput.setText("");
        sqlDisplay.setText("");
        clearResults();
        executeButton.setEnabled(false);
        statusLabel.setText("Ready to process queries...");
    }

    private void clearResults() {
        resultTable.setModel(new DefaultTableModel());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            NaturalLanguageQueryGUI1 gui = new NaturalLanguageQueryGUI1();
            gui.setVisible(true);
        });
    }
}