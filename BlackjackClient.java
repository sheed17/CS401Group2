import java.net.*;
import java.util.*;

import javax.swing.JOptionPane;

import java.io.*;

public class BlackjackClient
{
	private static boolean loggedIn = false;
	private Socket socket;
	private ObjectInputStream objectIn;
	private ObjectOutputStream objectOut;

	public BlackjackClient() throws IOException
	{
		this.socket = new Socket("localhost", 52904);
		this.objectOut = new ObjectOutputStream(socket.getOutputStream());
		objectOut.flush();
		this.objectIn = new ObjectInputStream(socket.getInputStream());
	}
	
	
	public static void main(String[] args) throws IOException
	{
		Scanner userInput = new Scanner(System.in);
		// Used for receiving command line user input
		
		BlackjackClient client = new BlackjackClient();
		// Creates a new client object to be passed into the GUI (not needed for now)
		// Might not be needed in general, commenting out for now as it messes with the method references
		
		int logInChoice;
		// used to determine if the user is returning or new and then calls the corresponding function
		
		//GameGUI newGUI = new GameGUI(client);
		
		do 
		{
			System.out.println("Welcome to the Blackjack Game!"
					+ "\nEnter 1 to log in or 2 to register a new account");
			logInChoice = userInput.nextInt();
			userInput.nextLine();
		
		} while (logInChoice != 1 && logInChoice != 2);
		
		switch (logInChoice)
		{
			case 1: client.logIn(userInput); break;
			case 2: client.createNewUser(userInput); break;
		}
		client.mainMenu();
	}
	
	public synchronized void send(BlackjackMessage message)
	{
		try
		{
			objectOut.writeObject(message);
			objectOut.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public synchronized BlackjackMessage receive()
	{
		try {
            return (BlackjackMessage) objectIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
	}
	
	public void logIn(Scanner userInput)
	{
		while (!loggedIn)
		{
			System.out.println("Enter your username");
			String username = userInput.nextLine();
			
			System.out.println("Enter your password");
			String password = userInput.nextLine();
			
			send(new BlackjackMessage(MessageEnum.LOGIN,username,password));
			
			BlackjackMessage message = receive();
			if (message.getStatus().equals("Success"))
				loggedIn=true;
			System.out.println(message.getText());
		}
	}
	
	public void createNewUser(Scanner userInput)
	{
		while (!loggedIn)
		{
			System.out.println("Enter your username");
			String username = userInput.nextLine();
			
			System.out.println("Enter your password");
			String password = userInput.nextLine();
			
			System.out.println("You entered: \nUsername: " + username + "\nPassword: " + password + "\nIs this okay? (Y/N)");
			String confirmation = userInput.nextLine().trim();
			
			if (confirmation.equalsIgnoreCase("Y"))
			{
				send(new BlackjackMessage(MessageEnum.NEWUSER,username,password));
				BlackjackMessage serverResponse = receive();
				if (serverResponse.getStatus().equals("Success"))
				{
					loggedIn = true;
				}
				System.out.println(serverResponse.getText());
			}
			
		}
		// Luis' idea: User enters username and password. Gets asked for confirmation since its a new account. If
		// the user is sure of their details, a message is sent to the server with the details. Not implemented yet, but the server
		// should search the user data files and make sure the username is unique before registering their details. If its unique, 
		// server returns a message saying that they've been registered successfully, otherwise a message is sent back saying that they
		// gotta choose a different username. 
	}
	
	public void mainMenu()
	{
		while (loggedIn)
		{
			String[] commands = {"Update Balance",
					"View Tables",
				 	"Log Out"};
		 
			int choice;
			Scanner scan = new Scanner(System.in);
		 
			do 
			{
				System.out.println("Welcome to the main menu. Please select an option. (1-" + commands.length + ")");
				for (int i = 0; i < commands.length; i++)
				{
					System.out.println(i+1 + ": " + commands[i]);
				}
				choice = scan.nextInt() - 1;
				scan.nextLine();
		 
				switch (choice) 
				{
					case 0: updateBalance(); break;
					case 1: displayTables(); break;
					case 2: break;
					default:  // do nothing
				}
			 
			} while (choice != commands.length-1);
			System.exit(0);
			// Menu from the professor 
		}
	}
	
	public void updateBalance()
	{
		send(new BlackjackMessage(MessageEnum.UPDATEBALANCE, "add", "150"));
		BlackjackMessage serverResponse = receive();
		System.out.println(serverResponse.getText());
		// Basic example of sending a message from the client to the server
	}
	
	public void displayTables()
	{
		System.out.println("These are the tables (No they're not)");
		// Needs actual GUI Implementation
	}
}