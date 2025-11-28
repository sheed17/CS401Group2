package src.GUI;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GameGUI
{
	private final JFrame GameFrame = new JFrame("CS401 Group 2 - Multiplayer Blackjack Game");
	private final JPanel GamePanel = new JPanel (new GridLayout(0,3,12,12));
	
	public GameGUI()
	{
		GameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void logInGUI()
	{
		int choice; 
		String[] options = {"Log In", "Create Account"};
		
		do
		{
			choice = JOptionPane.showOptionDialog(GameFrame, "Please log in or create a new account",
					"Multiplayer Blackjack Game", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, options, options[options.length - 1]);
			switch (choice)
			{	
				case 0: System.out.println("You chose log in"); logIn();
				case 1: System.out.println("You chose create new account"); break;
			}
		} while (choice != options.length-1);
		System.exit(0);
	} 
	
	public void logIn()
	{
		
	}
	
	public void createNewUser()
	{
		
	}
	// Simple GUI using a showOptionDialog that asks the user to log in if they are
	// an existing user or create a new account
}
