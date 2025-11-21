import java.net.*;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.*;

public class BlackjackServer
{
	private static ArrayList<String> userData = new ArrayList<String>();
	
	public static void main (String[] args) throws IOException
	{
		loadUserData();
		try (var listener = new ServerSocket(52904))
		{
			InetAddress localhost = InetAddress.getLocalHost(); 
			System.out.println("The server is now listening on " + localhost.getHostAddress().trim());
			var pool = Executors.newFixedThreadPool(10);
			while (true)
			{
				pool.execute(new clientHandler(listener.accept()));
			}
		}
	}
	
	private static class clientHandler implements Runnable
	{
		private Socket socket;
		// client socket
		private boolean loggedIn = false;
		// boolean used to determine if the user has successfully logged in
		private boolean connected;
		// boolean used for a while loop
		private int userIndex;
		// upon user logging in or creating a new account, the index where their data is located will be saved
		// to this variable which can then be utilized for the various modifier methods
		
		public clientHandler(Socket socket)
		{
			this.socket = socket;
		}
		
		
		public void run() 
		{
			try
			{
				connected = true;
				
				var objectOut = new ObjectOutputStream(socket.getOutputStream());
				objectOut.flush();
				// Object output stream
				
				var objectIn = new ObjectInputStream(socket.getInputStream());
				// Object Input stream

				System.out.println("Connection from:  " + socket);
				// Print out that someone has connected and the socket info

				while (connected)
				{
					BlackjackMessage message = (BlackjackMessage) objectIn.readObject();
					if (message.getType() == MessageEnum.LOGIN)
					{
						if (searchUser(message.getStatus(),message.getText(),MessageEnum.LOGIN) != -1)
						{
							addNewUser(message.getStatus(), message.getText());
							loggedIn = true;
							objectOut.writeObject(new BlackjackMessage(MessageEnum.LOGIN,"Success","You are now logged in"));
						}
						else
							objectOut.writeObject(new BlackjackMessage(MessageEnum.LOGIN,"Failure", "Wrong username/password"));
						// This works for command line. leave it alone
					}
					else if (message.getType() == MessageEnum.NEWUSER)
					{
						if (searchUser(message.getStatus(),message.getText(),MessageEnum.NEWUSER) != -1)
						{
							addNewUser(message.getStatus(), message.getText());
							objectOut.writeObject(new BlackjackMessage(MessageEnum.NEWUSER, "Success", "Successfully registered User."));
						}
						else 
							objectOut.writeObject(new BlackjackMessage(MessageEnum.NEWUSER, "Failure", "The username is already in use"));
						// Works for command line 
					}
					if (message.getType() == MessageEnum.UPDATEBALANCE && loggedIn)
					{
						updateBalance(150, userIndex);
					}
					else if (message.getType() == MessageEnum.VIEWPROFILE && loggedIn)
					{
						// viewProfile();
						// Debating on whether to keep this or not. Showing the user their profile
						// sounds like more of a GUI thing that simply extracts the information from 
						// the data files and then displays it.
					}
					else if (message.getType() == MessageEnum.UPDATEPASSWORD && loggedIn)
					{
						updatePassword(message.getText(), userIndex);
					}
					else if (message.getType() == MessageEnum.UPDATEUSERNAME && loggedIn)
					{
						updateUsername(message.getText(), userIndex);
					}
					else if (message.getType() == MessageEnum.LOGOUT && loggedIn)
					{
						connected = false;
						objectOut.writeObject(new BlackjackMessage(MessageEnum.LOGOUT, "success", "You are now logged out"));
					}
					
				
				}
			}
			catch (ClassNotFoundException | IOException e)
			{
				// not sure what to put here.
			}
		}
		
		
		// This method will read from the userData.text file and save every line within it into an arrayList
		// This arrayList will be used for searching through usernames, passwords, etc,
		
		public void addNewUser(String username, String password)
		{
			String newUser = username + "," + password + ",50";			
			// The three fields for user info are username, password and their balance. 
			// In this case, the default balance for new users will be 50 but in the end,
			// this is just a placeholder.
			userData.add(newUser);
			userIndex = userData.size()-1;
			saveUserData();
		}
		
		public int searchUser(String username, String password, MessageEnum purpose)
		{
			if (purpose == MessageEnum.LOGIN)
			{
				for (int index = 0; index < userData.size(); index++)
				{
					System.out.println(userData.get(index));
					String[] userInfo = userData.get(index).split(",");
					if (userInfo[0].equals(username) && userInfo[1].equals(password))
					{
						return index;
					}
				}
				return -1;
				// Iterates through the user Data array list and only returns true if there exists an index that contains the corresponding username and password
				// Otherwise, if the for loop iterates through the entire array list without finding the corresponding info, then failure is returned as it means one of the fields was wrong 
			}
			// If this is used to log in then both the username and password must be validated, and so this branch only returns success 
			// when both the username and password are correct.
			else if (purpose == MessageEnum.NEWUSER)
			{
				for (int index = 0; index < userData.size(); index++)
				{
					String[] userInfo = userData.get(index).split(",");
					if (userInfo[0].equals(username))
					{
						return -1;
					}
				}
				return 0;
				// Iterates through the user data array list comparing the username at each index with that of the argument. If the same username is found then it returns 
				// "failure" indicating that it is not a unique username;
			}
			return -1;		
		}

		
		public void updateBalance(int amount, int userIndex)
		{
			
		}
		// This method will update the user's balance upon the completion of a round of blackjack
		
		
		public void updateUsername(String username, int userIndex)
		{
			
		}
		
		public void updatePassword(String password, int userIndex)
		{
			
		}
		
	}
	
	public static void loadUserData()
	{
		try
		{
			File userTxt = new File("UserData.txt");
			if (userTxt.exists())
			{
				Scanner inFile = new Scanner(userTxt);
				while (inFile.hasNextLine())
				{
					String data = inFile.nextLine().trim();
					if (data != "")
					{
						userData.add(data);
					}
				}
				inFile.close();
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File was not found.");
		} // this works leave it alone 
	}
	
	public static void saveUserData()
	{

		String saveFile = ""; 
		for (int index=0; index<userData.size(); index++)
		{
			saveFile += userData.get(index) + "\n";
		}
		try
		{
			FileWriter outFile = new FileWriter("UserData.txt");
			outFile.write(saveFile);
			outFile.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
