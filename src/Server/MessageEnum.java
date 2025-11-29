package src.Server;
public enum MessageEnum
{
	NEWUSER, // Used to register a new user account 
	LOGIN, // Server will validate user credentials and log the person in or tell them credentials are wrong
	LOGOUT, // Server will sign the user out of the system. Only works if the user is logged in beforehand.
	VIEWPROFILE, // Server will return the user's information which will be displayed via the GUI
	UPDATEBALANCE, // After a round of blackjack is done, this will be sent to the server automatically. Status field will indicate if you add or subtract the value
	UPDATEUSERNAME, // Update the user's username in the text file. First make sure that it is unique
	UPDATEPASSWORD // Update the user's password in the text file. No uniqueness is needed but display the password back to user and have them confirm it. 
}