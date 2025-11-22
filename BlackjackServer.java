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
						if (message.getStatus().equals("add"))
							updateBalance(Integer.parseInt(message.getText()), "add", userIndex);
						else 
							updateBalance(Integer.parseInt(message.getText()), "subtract", userIndex);
						
						objectOut.writeObject(new BlackjackMessage(MessageEnum.UPDATEBALANCE, "Success", "User Balance has been updated.\nBalance: " + getBalance()));
					}
					else if (message.getType() == MessageEnum.VIEWPROFILE && loggedIn)
					{
						objectOut.writeObject(new BlackjackMessage(MessageEnum.VIEWPROFILE, "Success", userData.get(userIndex)));
						// Not sure how this will work honestly, current idea is that the server simply returns the string 
						// with the user data and then the GUI will split it using comma and display each field in a JPanel.
					}
					else if (message.getType() == MessageEnum.UPDATEPASSWORD && loggedIn)
					{
						updatePassword(message.getText(), userIndex);
						objectOut.writeObject(new BlackjackMessage(MessageEnum.UPDATEPASSWORD, "Success", "Password has been updated."));
						// Simply calls updatepassword with the new password and then sends a message back to the client notifying them that it is updated.
					}
					else if (message.getType() == MessageEnum.UPDATEUSERNAME && loggedIn)
					{
						updateUsername(message.getText(), userIndex);
						objectOut.writeObject(new BlackjackMessage(MessageEnum.UPDATEUSERNAME, "Success", "Username has been updated."));
						// calls updateUsername with the new username and then sends a message back to the client notifying them that it has been updated. 
					}
					else if (message.getType() == MessageEnum.LOGOUT && loggedIn)
					{
						connected = false;
						objectOut.writeObject(new BlackjackMessage(MessageEnum.LOGOUT, "success", "You are now logged out"));
						// This may need more functionality i'm not really sure what else i'd add though
					}
					
				
				}
			}
			catch (ClassNotFoundException | IOException e)
			{
				// not sure what to put here.
			}
		}
		
		public void addNewUser(String username, String password)
		{
			String newUser = username + "," + password + ",50";			
			userData.add(newUser);
			userIndex = userData.size()-1;
			saveUserData();
		}
		// Takes the username and password plus a default balance of 50 and creates a newUser string which is then added to the userData array list.
		// This string is in the format username,password,balance which tracks with the format in userData.txt and this is for easy string splitting and reading. 
		// the userIndex is also initialized to the size of the userData array list minus 1 for future reference.
		
		public int searchUser(String username, String password, MessageEnum purpose)
		{
			if (purpose == MessageEnum.LOGIN)
			{
				for (int index = 0; index < userData.size(); index++)
				{
					String[] userInfo = userData.get(index).split(",");
					if (userInfo[0].equals(username) && userInfo[1].equals(password))
					{
						userIndex = index;
						return index;
					}
				}
				return -1;
				// Iterates through the user Data array list and only returns the index if there exists an index that contains the corresponding username and password
				// Otherwise, if the for loop iterates through the entire array list without finding the corresponding info, then -1 is returned as it means one of the fields was wrong 
			}
			// If this is used to log in then both the username and password must be validated, and so this branch only returns a proper index
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
				// -1 indicating that it is not a unique username;
			}
			return -1;		
		}

		
		public void updateBalance(int amount, String operation, int userIndex)
		{
			String[] userInfo = userData.get(userIndex).split(",");
			// Grabs the user data from the arrayList and splits the string into the three data fields
			int userBalance = Integer.parseInt(userInfo[2]);
			//Index 2 corresponds to the user balance since the format is username, password, and then balance
			if (operation.equalsIgnoreCase("add"))
				userBalance += amount;
			else
				userBalance -= amount;
			// Depending on the operation argument, the server will either add the balance or subtract it 
			
			userInfo[2] = Integer.toString(userBalance);
			// replace index 2 of the user info with the updated balance
			String userInfoString = userInfo[0] + "," + userInfo[1] + "," + userInfo[2];
			// Assemble the user info string with the new information
			userData.set(userIndex, userInfoString);
			saveUserData();
			// Set the updated userInfo string into the index it was grabbed from. 
		}
		
		
		public void updateUsername(String username, int userIndex)
		{
			String[] userInfo = userData.get(userIndex).split(",");
			userInfo[0] = username;
			String userInfoString = userInfo[0] + "," + userInfo[1] + "," + userInfo[2];
			userData.set(userIndex, userInfoString);
			saveUserData();
		}
		// replaces the username field in userData with the new username and then saves it back to userData array list
		// before calling saveUserData() to write it to the UserData file.
		
		public void updatePassword(String password, int userIndex)
		{
			String[] userInfo = userData.get(userIndex).split(",");
			userInfo[1] = password;
			String userInfoString = userInfo[0] + "," + userInfo[1] + "," + userInfo[2];
			userData.set(userIndex, userInfoString);
			saveUserData();
		}
		// replaces the password field in userData with the new password and then saves it back to userData array list
		// before calling saveUserData() to write it to the UserData file.
		
		public String getBalance()
		{
			String[] userInfo = userData.get(userIndex).split(",");
			return userInfo[2];
		}
		// Used for the updateBalance method 
		
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
	// This method will read from the userData.text file and save every line within it into an arrayList
	// This arrayList will be used for searching through usernames, passwords, etc,
	
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
	// Method used for simply grabbing every string from the userData array list and then saving it to a string which is then
	// written back into UserData.txt for safe keeping
}
