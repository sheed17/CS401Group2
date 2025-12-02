package src.GUI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

import src.Client.BlackjackClient;

public class GameGUI {

    private final JFrame GameFrame = new JFrame("Multiplayer Blackjack Game");
    private final JPanel GamePanel = new JPanel(new GridLayout(3, 2, 10, 10));
    private final BlackjackClient client;
    private javax.swing.Timer refreshTimer;

    // Action buttons (for enabling/disabling)
    private JButton startRoundBtn;
    private JButton hitBtn;
    private JButton standBtn;
    private JButton doubleBtn;
    private JButton splitBtn;
    private JButton betButton;

    // Timer UI
    private JLabel timerLabel;
    private Timer turnTimer;
    private int timeRemaining;
    private String currentUsername; // set on login

    public GameGUI(BlackjackClient client) {
        this.client = client;

        GameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        GameFrame.setLocationRelativeTo(null);
        GameFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (confirmLogout()) {
                    GameFrame.dispose();
                    System.exit(0);
                }
            }
        });
    }

    // ==========================================================
    //                     LOGIN SCREENS
    // ==========================================================

    public void logInGUI() {
        String[] options = {"Log In", "Create Account"};
        int choice = JOptionPane.showOptionDialog(
                GameFrame,
                "Please log in or create a new account",
                "Multiplayer Blackjack Game",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            logIn();
        } else if (choice == 1) {
            createNewUser();
        }
    }

    public void logIn() {
        GamePanel.removeAll();

        JLabel namePrompt = new JLabel("Username:");
        JTextField nameField = new JTextField(12);

        JLabel passwordPrompt = new JLabel("Password:");
        JTextField passwordField = new JTextField(12);

        GamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GamePanel.add(namePrompt);
        GamePanel.add(nameField);
        GamePanel.add(passwordPrompt);
        GamePanel.add(passwordField);

        JButton logInButton = new JButton("Log In");

        logInButton.addActionListener(e -> runAsync(() -> {
            String username = nameField.getText().trim();
            String password = passwordField.getText().trim();
            String serverResponse = client.logIn(username, password);

            if (serverResponse.equals("Failure")) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(GameFrame,
                                "Wrong username/password",
                                "Login Error",
                                JOptionPane.ERROR_MESSAGE));
            } else {
                // Save username locally for turn display
                this.currentUsername = username;
                SwingUtilities.invokeLater(this::mainMenu);
            }
        }));

        GameFrame.getContentPane().removeAll();
        GameFrame.add(GamePanel, BorderLayout.CENTER);
        GameFrame.add(logInButton, BorderLayout.SOUTH);
        GameFrame.pack();
        GameFrame.setLocationRelativeTo(null);
        GameFrame.setVisible(true);
    }

    public void createNewUser() {
        GamePanel.removeAll();

        JLabel namePrompt = new JLabel("Choose a username:");
        JTextField nameField = new JTextField(15);

        JLabel passwordPrompt = new JLabel("Choose a password:");
        JTextField passwordField = new JTextField(15);

        GamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GamePanel.add(namePrompt);
        GamePanel.add(nameField);
        GamePanel.add(passwordPrompt);
        GamePanel.add(passwordField);

        JButton createButton = new JButton("Create Account");

        createButton.addActionListener(e -> runAsync(() -> {
            String username = nameField.getText().trim();
            String password = passwordField.getText().trim();

            String serverResponse = client.createNewUser(username, password);

            if (serverResponse.equals("Failure")) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(GameFrame,
                                "Username already taken!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE));
            } else {
                this.currentUsername = username;
                SwingUtilities.invokeLater(this::mainMenu);
            }
        }));

        GameFrame.getContentPane().removeAll();
        GameFrame.add(GamePanel, BorderLayout.CENTER);
        GameFrame.add(createButton, BorderLayout.SOUTH);
        GameFrame.pack();
        GameFrame.setLocationRelativeTo(null);
        GameFrame.setVisible(true);
    }

    // ==========================================================
    //                     MAIN MENU
    // ==========================================================

    public void mainMenu() {
        GameFrame.getContentPane().removeAll();

        JPanel menuPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton viewProfileButton = new JButton("View Profile");
        JButton joinTableButton = new JButton("Join Blackjack Table");
        JButton logOutButton = new JButton("Log Out");

        viewProfileButton.addActionListener(e -> viewProfile());
        joinTableButton.addActionListener(e -> showGameTable());
        logOutButton.addActionListener(e -> logOut());

        menuPanel.add(viewProfileButton);
        menuPanel.add(joinTableButton);
        menuPanel.add(logOutButton);

        GameFrame.add(menuPanel, BorderLayout.CENTER);
        GameFrame.pack();
        GameFrame.setLocationRelativeTo(null);
        GameFrame.revalidate();
        GameFrame.repaint();
    }

    public void viewProfile() {
        runAsync(() -> {
            String userData = client.viewProfile();
            if (userData == null || userData.isBlank()) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(GameFrame,
                                "No profile data.",
                                "Profile",
                                JOptionPane.INFORMATION_MESSAGE));
                return;
            }

            String[] userInfo = userData.split(",");
            if (userInfo.length < 3) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(GameFrame,
                                "Invalid profile data.",
                                "Profile",
                                JOptionPane.ERROR_MESSAGE));
                return;
            }

            String details =
                    "Username: " + userInfo[0] + "\n" +
                    "Password: " + userInfo[1] + "\n" +
                    "Balance:  " + userInfo[2];

            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(GameFrame, details, "Profile", JOptionPane.INFORMATION_MESSAGE));
        });
    }

    // ==========================================================
    //                     BLACKJACK TABLE
    // ==========================================================

    public void showGameTable() {
        GameFrame.getContentPane().removeAll();

        JTextArea gameState = new JTextArea(18, 55);
        gameState.setEditable(false);
        gameState.setFont(new Font("Monospaced", Font.PLAIN, 15));
        gameState.setMargin(new Insets(10, 10, 10, 10));

        // Join table first and show initial state
        runAsync(() -> {
            String joinState = client.joinTable();
            updateGameState(gameState, joinState);
        });

        // Controls
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        startRoundBtn = new JButton("Start Round");
        hitBtn = new JButton("Hit");
        standBtn = new JButton("Stand");
        doubleBtn = new JButton("Double");
        splitBtn = new JButton("Split");
        JButton backBtn = new JButton("Return to Menu");

        // Bet controls
        JPanel betPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        betPanel.add(new JLabel("Bet Amount:"));
        JTextField betField = new JTextField("10", 6);
        betButton = new JButton("Place Bet");
        betPanel.add(betField);
        betPanel.add(betButton);

        // Timer label in center
        timerLabel = new JLabel("Waiting for round...");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Disable game actions until betting/round started
        disableActionButtons();
        startRoundBtn.setEnabled(false);

        betButton.addActionListener(e -> runAsync(() -> {
            try {
                int amount = Integer.parseInt(betField.getText().trim());
                String resp = client.placeBet(amount);
                updateGameState(gameState, resp);
                // If bet succeeded, enable "Start Round"
                SwingUtilities.invokeLater(() -> startRoundBtn.setEnabled(true));
            } catch (NumberFormatException ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(GameFrame,
                                "Invalid bet amount.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE));
            }
        }));

        startRoundBtn.addActionListener(e -> runAsync(() -> {
            String resp = client.startRound();
            updateGameState(gameState, resp);
            // Round started -> enable controls, but we'll also manage via turn logic
            SwingUtilities.invokeLater(() -> {
                hitBtn.setEnabled(true);
                standBtn.setEnabled(true);
                doubleBtn.setEnabled(true);
                splitBtn.setEnabled(true);
                startRoundBtn.setEnabled(false);
            });
        }));

        hitBtn.addActionListener(e -> runAsync(() -> {
            String resp = client.hit();
            updateGameState(gameState, resp);
        }));

        standBtn.addActionListener(e -> runAsync(() -> {
            String resp = client.stand();
            updateGameState(gameState, resp);
            // After standing, will usually move turn; timer logic will handle buttons
        }));

        doubleBtn.addActionListener(e -> runAsync(() -> {
            String resp = client.doubleDown();
            updateGameState(gameState, resp);
        }));

        splitBtn.addActionListener(e -> runAsync(() -> {
            String resp = client.split();
            updateGameState(gameState, resp);
        }));

        backBtn.addActionListener(e -> {
            stopTurnTimer();
            mainMenu();
        });

        controls.add(startRoundBtn);
        controls.add(hitBtn);
        controls.add(standBtn);
        controls.add(doubleBtn);
        controls.add(splitBtn);
        controls.add(backBtn);

        topPanel.add(betPanel, BorderLayout.NORTH);
        topPanel.add(timerLabel, BorderLayout.CENTER);
        topPanel.add(controls, BorderLayout.SOUTH);

        GameFrame.add(topPanel, BorderLayout.NORTH);
        GameFrame.add(new JScrollPane(gameState), BorderLayout.CENTER);

        GameFrame.pack();
        GameFrame.setLocationRelativeTo(null);
        GameFrame.revalidate();
        GameFrame.repaint();
        
        refreshTimer = new javax.swing.Timer(1000, ev -> {
            runAsync(() -> {
                String text = client.getGameState();
                if (text != null) {
                    updateGameState(gameState, text);
                }
            });
        });
        refreshTimer.start();
        
        backBtn.addActionListener(e -> {
            stopTurnTimer();
            if (refreshTimer != null) {
                refreshTimer.stop();
                refreshTimer = null;
            }
            mainMenu();
        });

    }

    private void disableActionButtons() {
        if (hitBtn != null) hitBtn.setEnabled(false);
        if (standBtn != null) standBtn.setEnabled(false);
        if (doubleBtn != null) doubleBtn.setEnabled(false);
        if (splitBtn != null) splitBtn.setEnabled(false);
    }

    private void stopTurnTimer() {
        if (turnTimer != null) {
            turnTimer.stop();
            turnTimer = null;
        }
    }

    // ==========================================================
    //                     UTILITY
    // ==========================================================

    private void runAsync(Runnable task) {
        new Thread(task).start();
    }

    private void updateGameState(JTextArea gameState, String text) {
        SwingUtilities.invokeLater(() -> {
            gameState.setText(text != null ? text : "");

            // Simple color coding if your gameState text ever includes these words.
            if (text != null && text.contains("YOU WIN")) {
                gameState.setForeground(new Color(0, 150, 0));
            } else if (text != null &&
                    (text.contains("YOU LOSE") || text.contains("BUST"))) {
                gameState.setForeground(Color.RED.darker());
            } else if (text != null && text.contains("Push")) {
                gameState.setForeground(new Color(200, 120, 0));
            } else {
                gameState.setForeground(Color.BLACK);
            }

            // --- TIMER + TURN LOGIC ---

            if (timerLabel == null || text == null) {
                return;
            }

            boolean roundInProgress = text.contains("Round status: IN PROGRESS");
            String currentTurn = extractCurrentTurn(text);
            boolean myTurn = roundInProgress &&
                             currentUsername != null &&
                             currentUsername.equals(currentTurn);

            updateTurnTimer(roundInProgress, myTurn, currentTurn);

            // Optional: enable/disable action buttons based on turn
            if (roundInProgress && myTurn) {
                if (hitBtn != null) hitBtn.setEnabled(true);
                if (standBtn != null) standBtn.setEnabled(true);
                if (doubleBtn != null) doubleBtn.setEnabled(true);
                if (splitBtn != null) splitBtn.setEnabled(true);
            } else {
                disableActionButtons();
            }
        });
    }

    private String extractCurrentTurn(String text) {
        String marker = "Current turn:";
        int idx = text.indexOf(marker);
        if (idx == -1) return null;
        int start = idx + marker.length();
        int end = text.indexOf('\n', start);
        if (end == -1) end = text.length();
        return text.substring(start, end).trim();
    }

    private void updateTurnTimer(boolean roundInProgress, boolean myTurn, String currentTurnPlayer) {
        if (!roundInProgress || currentTurnPlayer == null || currentTurnPlayer.equals("None")) {
            stopTurnTimer();
            timerLabel.setText("No active turn.");
            return;
        }

        // countdown of your turn
        if (!myTurn) {
            stopTurnTimer();
            timerLabel.setText("Waiting for " + currentTurnPlayer + "...");
            return;
        }

        if (turnTimer != null) {
            return;
        }

        // 10 second timer
        timeRemaining = 10;
        timerLabel.setText("Your turn: " + timeRemaining + "s");

        turnTimer = new Timer(1000, e -> {
            timeRemaining--;
            if (timeRemaining <= 0) {
                timerLabel.setText("Your turn timed out.");
                stopTurnTimer();
            } else {
                timerLabel.setText("Your turn: " + timeRemaining + "s");
            }
        });
        turnTimer.start();
    }


    public void logOut() {
        if (confirmLogout()) {
            runAsync(() -> {
                String msg = client.logOut();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(GameFrame, msg);
                    GameFrame.dispose();
                    System.exit(0);
                });
            });
        }
    }

    private boolean confirmLogout() {
        int option = JOptionPane.showConfirmDialog(
                GameFrame,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return option == JOptionPane.YES_OPTION;
    }
}

