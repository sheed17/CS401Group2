public class Player {
	
	private int userId;
	private String username;
	private String password;
	private int balance;
	private int totalCardValue;
	private Card[] hand;
	private int currentBet;
	
	Player(String username, String password){
		this.username = username;
		this.password =  password;
	}
	
	public void logIn(String u, String p) {
		if (u == this.username && p == this.password) {
			return;
		}
		break;
	}
	
	public void logout() {
		
	}
	
	public void joinTable(Table table) {
		
	}
	
	public void leaveTable() {
		
	}
	
	public int bet(int money) {
		
	}
	
	public void hit() {
		
	}
	
	public void stand() {
		
	}
	
	public void doubleDown(int bet) {
		
	}
	
	public void split() {
		
	}
	
	public int getUserId() {
		return this.userId;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public int getBalance() {
		return this.balance;
	}
	
	public int getTotalCardValue() {
		return this.totalCardValue;
	}
	
	public int getCurrentBet() {
		return this.currentBet;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setBalance(int balance) {
		this.balance = balance;
	}
	
	public void setTotalCardValue(int tcv) {
		this.totalCardValue = tcv;
	}
	
	public void setCurrentBet(int currentBet) {
		this.currentBet = currentBet;
	}
	
}