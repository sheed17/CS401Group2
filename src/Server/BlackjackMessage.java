package src.Server;
import java.io.Serializable;

public class BlackjackMessage implements Serializable
{
	protected MessageEnum type;
	
	protected String status;
	// Status will be used as an additional text field for messages such as
	// add new user and log in which require two String variables for username and password.
	// In those cases, status will contain the username, while text has the password.
	
	protected String text;
	
	public BlackjackMessage(MessageEnum type, String status, String text)
	{
		this.type = type;
		this.status = status;
		this.text = text;
	}
	
	public MessageEnum getType()
	{
		return type;
	}
	
	public String getText()
	{
		return text;
	}
	
	public String getStatus()
	{
		return status;
	}
}