public class Player {
	
	private int userId;
	private int count = 0;
	private String username;
	private String password;
	private int balance;
	private int totalCardValue;
	private Card[] hand;
	private int currentBet;
	private int handSize = 0;
	
	Player(String username, String password){
		this.userId = count++;
		this.username = username;
		this.password =  password;
		this.balance = 0;
        this.totalCardValue = 0;
        this.currentBet = 0;
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
		// waiting for table class
		
	}
	
	public void leaveTable() {
		// waiting for table class
		this.handSize = 0;
        for (int i = 0; i < hand.length; i++) {
            hand[i] = null;
        }
        this.currentBet = 0;
        this.totalCardValue = 0;
	}
	
	public int bet(int money) {
		
		if (money <= 0) {
            return 0;
        }
        if (money > balance) {
        	return 0;
        }
        balance = balance - money;
        currentBet = currentBet + money;
        return money;
        
	}
	
	public void hit(Shoe shoe) {
		
		Card newCard = shoe.dealCard();
	    hand[handSize++] = newCard;
	    
	    int total = 0;
	    int aces = 0;

	    for (int i = 0; i < handSize; i++) {
	        Card c = hand[i];
	        if (c == null) {
	        	continue;
	        }
	        total = total + c.getCardValue();
	        if (c.getRank() == Rank.Ace) {
	            aces = aces +1;
	        }
	    }

	    while (aces > 0 && total + 10 <= 21) {
	        total += 10;
	        aces = aces -1;
	    }

	    this.totalCardValue = total;
	    
	}
	
	public int stand() {
		return this.totalCardValue;
	}
	
	public void doubleDown(int bet, Shoe shoe) {
		
		int placed = bet(bet); 
        if (placed > 0) {
            hit(shoe);        
            stand();   
        } else {
            break;
        }
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