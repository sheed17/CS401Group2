import java.io.Serializable;

public class BlackjackMessage implements Serializable
{
	protected MessageEnum type;
	protected String status;
	// not sure if status is necessary for the blackjack project
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
}