package src.GUI;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import src.Client.BlackjackClient;

public class GameGUI
{
	private final JFrame GameFrame = new JFrame("Multiplayer Blackjack Game");
	private final JPanel GamePanel = new JPanel (new GridLayout(3,2,10,10));
	BlackjackClient client;
	
	public GameGUI(BlackjackClient client)
	{
		this.client = client;
		GameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GameFrame.setLocationRelativeTo(null);
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
		JOptionPane.showMessageDialog(null, "You are now in the main Menu", 
				"Log In Success", 
				JOptionPane.INFORMATION_MESSAGE);
	}
}