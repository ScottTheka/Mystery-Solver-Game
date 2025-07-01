import javax.swing.*; // Swing GUI toolkit
import java.awt.*; // Layouts and color settings
import java.awt.event.*; // For button click events
import java.io.*; // File I/O
import java.sql.*; // SQLite database connectivity
import java.util.Scanner; // For reading from file

/**
 * Java Detective Game
 * A GUI mystery game using Swing. Clues and suspects are loaded from external .txt files.
 * This version includes SQLite support for saving player profiles and progress.
 */
public class DetectiveGame extends JFrame implements ActionListener {

    // GUI components
    private JTextArea displayArea; 
    private JButton startButton, cluesButton, suspectsButton, accuseButton, exitButton, saveButton; // All main buttons
    private String detectiveName; 
    private Connection conn; // SQLite database connection

    // Constructor to initialize the game window and components
    public DetectiveGame(String detectiveName) {
        this.detectiveName = detectiveName;
        connectDatabase(); 
        savePlayerProfile(); 

        setTitle("🕵️ Java Detective"); 
        setDefaultCloseOperation(EXIT_ON_CLOSE); 
        setSize(600, 400);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout(10, 10)); 
        getContentPane().setBackground(new Color(245, 245, 245)); 

        // Text area for displaying messages to the player
        displayArea = new JTextArea("Welcome, Detective " + detectiveName + "! Press 'Start Case' to begin.\n");
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        displayArea.setEditable(false); 
        displayArea.setBackground(new Color(255, 250, 240)); 
        displayArea.setForeground(new Color(33, 33, 33)); 
        displayArea.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        add(new JScrollPane(displayArea), BorderLayout.CENTER); 

        // Panel for holding buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 6, 10, 10)); 
        buttonPanel.setBackground(new Color(70, 130, 180)); 

        // Initialize buttons
        startButton = new JButton("Start Case");
        cluesButton = new JButton("View Clues");
        suspectsButton = new JButton("Question Suspects");
        accuseButton = new JButton("Make Accusation");
        saveButton = new JButton("Save Notes");
        exitButton = new JButton("Exit");

        // Add buttons to the panel with styling and event listeners
        for (JButton btn : new JButton[] { startButton, cluesButton, suspectsButton, accuseButton, saveButton, exitButton }) {
            btn.setBackground(Color.WHITE); 
            btn.setForeground(new Color(25, 25, 112)); 
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12)); 
            btn.setFocusPainted(false); 
            btn.setBorder(BorderFactory.createLineBorder(new Color(30, 144, 255))); 
            btn.addActionListener(this); 
            buttonPanel.add(btn);
        }

        add(buttonPanel, BorderLayout.SOUTH); 
        setVisible(true); 
    }

    // Connects to the SQLite database and creates tables if they don't exist
    private void connectDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:detective_game.db"); 
            Statement stmt = conn.createStatement();
            // Create tables for player and suspects
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS players (name TEXT PRIMARY KEY, progress TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS suspects (name TEXT, questioned BOOLEAN)");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage()); 
        }
    }

    
    private void savePlayerProfile() {
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO players (name, progress) VALUES (?, ?)");
            ps.setString(1, detectiveName); 
            ps.setString(2, "Not Started"); 
            ps.executeUpdate(); 
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving profile: " + e.getMessage());
        }
    }

    // Updates the player's progress in the database
    private void updateProgress(String progress) {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE players SET progress = ? WHERE name = ?");
            ps.setString(1, progress);
            ps.setString(2, detectiveName);
            ps.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating progress: " + e.getMessage());
        }
    }

    // Handles all button click actions
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) {
            // Show crime intro text
            displayArea.setText("\uD83D\uDCCD Crime Scene: A priceless painting was stolen from the museum.\n" +
                    "Detective " + detectiveName + ", you must find out who did it!\n");
            updateProgress("Started");
            getContentPane().setBackground(new Color(245, 245, 245));

        } else if (e.getSource() == cluesButton) {
            // Show clues from clues.txt
            File clueFile = new File("resource/clues.txt");
            if (clueFile.exists()) {
                displayArea.setText(readFileContent(clueFile.getAbsolutePath()));
            } else {
                displayArea.setText("❌ clues.txt NOT found!\nLooking in:\n" + clueFile.getAbsolutePath());
            }

        } else if (e.getSource() == suspectsButton) {
            // Let player question a suspect
            String[] suspects = { "Zwelibanzi Ntanzi", "Thembelani Tshaka", "Tevin Monayi" };
            String suspect = (String) JOptionPane.showInputDialog(this,
                    "Who do you want to question?",
                    "Question Suspects",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    suspects,
                    suspects[0]);

            if (suspect != null) {
                // Display custom clue
                displayArea.setText("You questioned " + suspect + " and found a clue related to them!\n");
                if ("Zwelibanzi Ntanzi".equals(suspect)) {
                    displayArea.append("Clue: Zwelibanzi had access to the museum's security systems.\n");
                } else if ("Thembelani Tshaka".equals(suspect)) {
                    displayArea.append("Clue: Thembelani was spotted out of town during the theft.\n");
                } else if ("Tevin Monayi".equals(suspect)) {
                    displayArea.append("Clue: Tevin claims he was attending a lecture at the university that evening.\n");
                }
                updateProgress("Questioned: " + suspect);
            }

        } else if (e.getSource() == accuseButton) {
            // Allow player to make accusation
            String[] options = { "Zwelibanzi Ntanzi", "Thembelani Tshaka", "Tevin Monayi" };
            int choice = JOptionPane.showOptionDialog(this,
                    "Who do you accuse?",
                    "Make Accusation",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == 0) {
                // Player made correct accusation
                displayArea.setText("✅ Correct! Zwelibanzi Ntanzi disabled the cameras and stole the painting.");
                getContentPane().setBackground(new Color(144, 238, 144)); // Green
                updateProgress("Solved");
                JOptionPane.showMessageDialog(this, "🎉 Congratulations, Detective " + detectiveName + "! You solved the case!");
            } else if (choice >= 0) {
                // Incorrect suspect
                displayArea.setText("❌ Wrong choice! The real thief got away. Try again.");
                updateProgress("Wrong Accusation");
                getContentPane().setBackground(new Color(255, 182, 193)); // Pink
                JOptionPane.showMessageDialog(this, "😞 Wrong suspect! Give it another shot, Detective " + detectiveName + ".");
            }

        } else if (e.getSource() == saveButton) {
            // Save notes to file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Investigation Log");
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                try {
                    File fileToSave = fileChooser.getSelectedFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave));
                    writer.write(displayArea.getText());
                    writer.close();
                    JOptionPane.showMessageDialog(this, "✅ Notes saved to:\n" + fileToSave.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "⚠️ Error saving file:\n" + ex.getMessage());
                }
            }

        } else if (e.getSource() == exitButton) {
            // Exit the application
            JOptionPane.showMessageDialog(this, "Thanks for playing! Goodbye, Detective " + detectiveName + ".");
            System.exit(0);
        }
    }

    // Reads a file and returns its content as a String
    private String readFileContent(String filename) {
        StringBuilder content = new StringBuilder();
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
        } catch (FileNotFoundException e) {
            content.append("❌ Error reading file: ").append(filename);
        }
        return content.toString();
    }

    // Launch the game by prompting for detective name
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String name = JOptionPane.showInputDialog(null,
                    "Enter your name, Detective:",
                    "Detective Login",
                    JOptionPane.PLAIN_MESSAGE);

            if (name == null || name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "You must enter a name to play!");
                System.exit(0);
            } else {
                new DetectiveGame(name.trim()); 
            }
        });
    }
}
