import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class NaturalLanguageQueryGUI extends JFrame {
    private JTextArea inputArea;
    private JTextArea sqlArea;
    private JTable resultTable;
    private JLabel statusLabel;
    private Connection connection;
    private NaturalLanguageQueryExecutor queryExecutor;
    private JButton generateBtn;
    private JButton executeBtn;

    public NaturalLanguageQueryGUI() {
        queryExecutor = new NaturalLanguageQueryExecutor();
        initializeUI();
        connectToDatabase();
    }

    private void initializeUI() {
        setTitle("GenAI SQL Assistant");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Load background image
        // Note: You may need to change the path to match your actual image location
        Image bgImage = Toolkit.getDefaultToolkit().getImage("D:\\Db2_Inventory_Manager\\src\\main\\java\\bg.jpg");
        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new BorderLayout());

        // Main panel on the right
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(30, 30, 30, 50));

        JLabel title = new JLabel("🧠 GenAI SQL Assistant");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.BLACK);
        title.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightPanel.add(title);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Input Area
        inputArea = new JTextArea(5, 40);
        configureTextArea(inputArea);
        rightPanel.add(createLabeledBox("📝 Natural Language Query", inputArea));

        // SQL Area
        sqlArea = new JTextArea(3, 40);
        configureTextArea(sqlArea);
        sqlArea.setEditable(false);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(createLabeledBox("🧾 Generated SQL", sqlArea));

        // Buttons moved to LEFT
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        generateBtn = createStyledButton("Generate Query", new Color(0, 100, 100));
        executeBtn = createStyledButton("Execute Query", new Color(0, 100, 100));
        executeBtn.setEnabled(false); // Disabled by default
        JButton clearBtn = createStyledButton("Clear", new Color(0, 100, 100));

        generateBtn.addActionListener(e -> generateQuery());
        executeBtn.addActionListener(e -> executeQuery());
        clearBtn.addActionListener(e -> clearAll());

        buttonPanel.add(generateBtn);
        buttonPanel.add(executeBtn);
        buttonPanel.add(clearBtn);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(buttonPanel); // Add to panel after SQL box

        // Table for Results
        resultTable = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(resultTable);
        tableScrollPane.setPreferredSize(new Dimension(1100, 220));
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("📊 Query Output Table"));

        // Style the table
        resultTable.setFillsViewportHeight(true);
        resultTable.setBackground(Color.WHITE);
        resultTable.setForeground(Color.BLACK);
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resultTable.setGridColor(Color.LIGHT_GRAY);
        resultTable.setShowGrid(true);
        resultTable.getTableHeader().setBackground(new Color(0, 100, 100));
        resultTable.getTableHeader().setForeground(Color.BLACK);
        resultTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Status Label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

        // Add all to background panel
        bgPanel.add(statusLabel, BorderLayout.NORTH);
        bgPanel.add(rightPanel, BorderLayout.CENTER);
        bgPanel.add(tableScrollPane, BorderLayout.SOUTH);

        setContentPane(bgPanel);
    }

    private void configureTextArea(JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    private JPanel createLabeledBox(String labelText, JTextArea area) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.WHITE);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(5, 5));
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        panel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            Properties props = new Properties();
            props.setProperty("user", "DELL");
            props.setProperty("password", "Kshitu@2211");
            connection = DriverManager.getConnection("jdbc:db2://localhost:25000/USER", props);
            statusLabel.setText("✅ Connected to database successfully");
        } catch (Exception e) {
            statusLabel.setText("❌ Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateQuery() {
        String naturalQuery = inputArea.getText().trim();
        if (naturalQuery.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a question", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            statusLabel.setText("🔄 Generating SQL query...");
            String sqlQuery = queryExecutor.generateSqlQuery(naturalQuery);
            
            if (sqlQuery == null || sqlQuery.isEmpty()) {
                statusLabel.setText("❌ Failed to generate SQL query");
                return;
            }

            sqlArea.setText(sqlQuery);
            statusLabel.setText("✅ SQL query generated. Click Execute to run the query.");
            executeBtn.setEnabled(true);
        } catch (Exception e) {
            statusLabel.setText("❌ Error generating query: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeQuery() {
        String sqlQuery = sqlArea.getText().trim();
        if (sqlQuery.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No SQL query to execute", "Query Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            statusLabel.setText("🔄 Executing query...");
            Statement stmt = connection.createStatement();
            
            ResultSet res = stmt.executeQuery(sqlQuery);
            displayResultSet(res);
            res.close();
            stmt.close();
        } catch (SQLException e) {
            statusLabel.setText("❌ Error executing query: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error executing query: " + e.getMessage(), 
                "Query Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

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
            statusLabel.setText("✅ Query executed successfully. Found " + data.size() + " rows.");
            
            // Ensure table is visible
            resultTable.revalidate();
            resultTable.repaint();
        });
    }

    private void clearAll() {
        inputArea.setText("");
        sqlArea.setText("");
        resultTable.setModel(new DefaultTableModel());
        executeBtn.setEnabled(false);
        statusLabel.setText("🧹 Ready to process queries...");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            NaturalLanguageQueryGUI gui = new NaturalLanguageQueryGUI();
            gui.setVisible(true);
        });
    }

    // Custom Panel with Background Image from GenAISQLAssistant
    static class BackgroundPanel extends JPanel {
        private final Image bg;

        public BackgroundPanel(Image img) {
            this.bg = img;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    }
}