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
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class GenAISQLAssistant extends JFrame {
    private JTextArea inputArea;
    private JTextArea sqlArea;
    private JTable resultTable;
    private JLabel statusLabel;

    public GenAISQLAssistant() {
        setTitle("GenAI SQL Assistant");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Load background image
        Image bgImage = Toolkit.getDefaultToolkit().getImage("C:\\Users\\SHUBHANGI\\OneDrive\\Desktop\\AML\\GenAI-with-DB2-Making-Legacy-Data-Talk-Smarter\\src\\main\\java\\bg.jpg");
        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new BorderLayout());

        // Main panel on the right
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(30, 30, 30, 50));

        JLabel title = new JLabel("🧠 GenAI SQL Assistant");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
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
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(createLabeledBox("🧾 Generated SQL", sqlArea));

        // Buttons moved to LEFT
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        JButton generateBtn = createStyledButton("Generate & Run", new Color(0, 100, 100));
        JButton clearBtn = createStyledButton("Clear", new Color(0, 100, 100));

        generateBtn.addActionListener(e -> generateAndRunSQL());
        clearBtn.addActionListener(e -> clearAll());

        buttonPanel.add(generateBtn);
        buttonPanel.add(clearBtn);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(buttonPanel); // Add to panel after SQL box

        // Table for Results
        resultTable = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(resultTable);
        tableScrollPane.setPreferredSize(new Dimension(1100, 220));
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("📊 Query Output Table"));

        // Status Label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.WHITE);
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

    private void generateAndRunSQL() {
        String naturalQuery = inputArea.getText().trim();
        if (naturalQuery.isEmpty()) {
            statusLabel.setText("⚠ Please enter a natural language query.");
            return;
        }

        // Simulate LLM SQL generation
        String generatedSQL = "SELECT * FROM employees WHERE department = 'Sales';";
        sqlArea.setText(generatedSQL);

        // Run SQL
        runSQL(generatedSQL);
    }

    private void runSQL(String sql) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:db2://localhost:25000/MYDB", "DELL", "Kshitu@2211");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            ResultSetMetaData metaData = rs.getMetaData();
            DefaultTableModel model = new DefaultTableModel();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            resultTable.setModel(model);
            statusLabel.setText("✅ Query executed successfully.");

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            statusLabel.setText("❌ DB Error: " + e.getMessage());
        }
    }

    private void clearAll() {
        inputArea.setText("");
        sqlArea.setText("");
        resultTable.setModel(new DefaultTableModel());
        statusLabel.setText("🧹 Cleared.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GenAISQLAssistant app = new GenAISQLAssistant();
            app.setVisible(true);
        });
    }

    // Custom Panel with Background Image
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