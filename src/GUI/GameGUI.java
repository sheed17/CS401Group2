package src.GUI;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import src.Client.BlackjackClient;
import src.Server.MessageEnum;

public class GameGUI
{
	private final JFrame GameFrame = new JFrame("Multiplayer Blackjack Game");
	private final JPanel GamePanel = new JPanel (new GridLayout(3,2,10,10));
	BlackjackClient client;
	
	public GameGUI(BlackjackClient client)
	{
		this.client = client;
		GameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		GameFrame.setLocationRelativeTo(null);
		GameFrame.addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	            if (confirmLogout()) {
	                // optional: client.logout() or client.disconnect();
	                GameFrame.dispose();
	                System.exit(0); // close app completely
	            }
	        }
	    });
	}
	
	public void logInGUI()
	{
		int choice; 
		String[] options = {"Log In", "Create Account"};
		
		choice = JOptionPane.showOptionDialog(GameFrame, "Please log in or create a new account",
				"Multiplayer Blackjack Game", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[options.length - 1]);
		switch (choice)			
		{	
			case 0: logIn(); break;
			case 1: createNewUser(); break;
		}
		// test the two windows that work currently as soon as the functionality for the server is fully done
	}
	
	public void logIn()
	{
			
		JLabel namePrompt = new JLabel("Enter your username");
		JTextField nameField = new JTextField(10);	
		GamePanel.add(namePrompt);
		GamePanel.add(nameField);
		
		JLabel passwordPrompt = new JLabel("Enter your password");
		JTextField passwordField = new JTextField(10);
		
		GamePanel.add(passwordPrompt);
		GamePanel.add(passwordField);
		
		JButton logInButton = new JButton("Log In");
		
		logInButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String username = nameField.getText();
				String password = passwordField.getText();
				String serverResponse = client.logIn(username, password);
				
				if (serverResponse.equals("Failure"))
				{
					JOptionPane.showMessageDialog(null, "Wrong username/password", 
							"Login Error", 
							JOptionPane.ERROR_MESSAGE);
				}
				else
					mainMenu();
			}
		});

		GameFrame.add(GamePanel, BorderLayout.CENTER);
		GameFrame.add(logInButton, BorderLayout.SOUTH);
		GameFrame.pack();
		GameFrame.setVisible(true);

	}

	// The GUI part of this works so far
	
	public void createNewUser()
	{
		JLabel namePrompt = new JLabel("Enter your username");
		JTextField nameField = new JTextField(15);
		
		GamePanel.add(namePrompt);
		GamePanel.add(nameField);
		
		JLabel passwordPrompt = new JLabel("Enter your password");
		JTextField passwordField = new JTextField(15);
		
		GamePanel.add(passwordPrompt);
		GamePanel.add(passwordField);
		
		JButton newUserButton = new JButton("Create Account");
		
		newUserButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String username = nameField.getText();
				String password = passwordField.getText();
				String serverResponse = client.createNewUser(username, password);
				
				if (serverResponse.equals("Failure"))
				{
					JOptionPane.showMessageDialog(null, "Username is already in use", 
							"Login Error", 
							JOptionPane.ERROR_MESSAGE);
				}
				else
					mainMenu();
			}
		});
		
		GameFrame.add(GamePanel, BorderLayout.CENTER);
		GameFrame.add(newUserButton, BorderLayout.SOUTH);
		GameFrame.pack();
		GameFrame.setVisible(true);
	}
	// GUI for a new account 
	
	public void mainMenu()
	{
		GameFrame.getContentPane().removeAll();

	    // Build main menu panel
	    JPanel menuPanel = new JPanel(new GridLayout(1, 3, 10, 10));
	    

	    JButton viewProfileButton = new JButton("View Profile");
	    JButton viewTablesButton = new JButton("View Tables");
	    JButton logOutButton = new JButton("Log Out");

	    viewProfileButton.addActionListener(e -> viewProfile());
	    viewTablesButton.addActionListener(e -> viewTables());
	    logOutButton.addActionListener(e -> logOut());

	    menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

	    menuPanel.add(viewProfileButton);
	    menuPanel.add(viewTablesButton);
	    menuPanel.add(logOutButton);

	    GameFrame.add(menuPanel, BorderLayout.CENTER);

	    GameFrame.pack();
	    GameFrame.setLocationRelativeTo(null); // keep it centered if you like
	    GameFrame.revalidate();
	    GameFrame.repaint();
	}
	
	public void viewProfile()
	{
		JOptionPane.showMessageDialog(null, "You have pressed View Profile", 
				"Main Menu", 
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void viewTables()
	{
		JOptionPane.showMessageDialog(null, "You have pressed View Tables", 
				"Main Menu", 
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void logOut()
	{		
		if (confirmLogout()) {
			String serverResponse = client.logOut();
			JOptionPane.showMessageDialog(null, serverResponse);
	        GameFrame.dispose();   // close the GUI window
	        System.exit(0);        // terminate the app
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