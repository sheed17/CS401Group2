package src.Game;
import java.io.File;
import java.util.Scanner;

public class Player {
	
	private int userId;
	private static int count = 0;
	private String username;
	private String password;
	private int balance;
	private int totalCardValue;
	// put 12 because mathematically there is no way that with more than 11 you can
	// still stand and with the 12 you bust
	private Card[][] hands = new Card[4][12];
	private int[] handBets = new int[4];
	private int numHands = 1;
	private int currentBet;
	private int activeHandIndex = 0;
	private int[] handSizes = new int[4];
	private int[] handTotals = new int[4];
	private Table currentTable;
	
	Player(String username, String password){
		this.userId = count++;
		this.username = username;
		this.password =  password;
		this.balance = 0;
        this.totalCardValue = 0;
        this.currentBet = 0;
	}
	
	public boolean logIn(String u, String p) {
	//CHANGE FILE NAME WHEN WE DEFINE THAT	
	// I ASSUMED THAT THE FILE HAS THIS FORMAT
	// USER,PASSWORD,BALANCE
		File file = new File("UserData.txt");   
		
		if (file.exists() == false) {
		    return false;
		}
		
		boolean found = false;
		
		try {
		    Scanner scanner = new Scanner(file);
		    while (scanner.hasNextLine()) {
		        String line = scanner.nextLine().trim();
		        if (line.isEmpty()) {
		            continue;
		        }
		
		        String[] userData = line.split(",");
		        if (userData.length != 3) {
		            break;
		        }
		
		        String fileUser = userData[0].trim();
		        String filePass = userData[1].trim();
		        String fileBalance = userData[2].trim();
		        int fB = Integer.parseInt(fileBalance);
		
		        if (fileUser.isBlank() || filePass.isBlank()) {
		            break;
		        }
		
		        if (u.equals(fileUser) && p.equals(filePass)) {
		            this.username = fileUser;
		            this.password = filePass;
		            this.balance = fB;
		            found = true;
		            break;
		        }
		    }
		    scanner.close();
		} catch (Exception e) {
			//error reading the file
		    return false;
		}
		
		if (found == false) {
		    // incorrect credentials
			this.username = null;
	        this.password = null;
	        this.balance = 0;
			return false;
		}
		
		return true;
	}

	
	public void logout() {
		// real logic of the logout is in the client-server part, in here im only
		// resetting the stuff to 0 and updating the balance in the txt file
		
		File file = new File("UserData.txt");
	    if (!file.exists()) {
	        return;
	    }

	    try {
	        Scanner scanner = new Scanner(file);
	        StringBuilder sb = new StringBuilder();

	        while (scanner.hasNextLine()) {
	            String line = scanner.nextLine().trim();

	            if (line.isEmpty()) {
	                sb.append("\n");
	                continue;
	            }
	            String[] parts = line.split(",");
	            if (parts.length != 3) {
	                sb.append(line).append("\n");
	                continue;
	            }
	            String fileUser = parts[0].trim();
	            String filePass = parts[1].trim();

	            // if it finds the user, it needs to update the balance
	            if (fileUser.equals(this.username) && filePass.equals(this.password)) {
	                sb.append(fileUser).append(",").append(filePass).append(",").append(this.balance).append("\n");
	            } else {
	                sb.append(line).append("\n");
	            }
	        }
	        scanner.close();

	        java.io.FileWriter fileUpdated = new java.io.FileWriter(file, false); 
	        fileUpdated.write(sb.toString());
	        fileUpdated.close();

	    } catch (Exception e) {
	        // error updating the txt file
	    }
	    
		this.balance = 0;
	    this.totalCardValue = 0;
	    this.currentBet = 0;
	    
	    for (int i = 0; i < hands.length; i++) {
            handSizes[i] = 0;
            handBets[i] = 0;
            handTotals[i] = 0;
            for (int j = 0; j < hands[i].length; j++) {
                hands[i][j] = null;
            }
        }
        numHands = 1;
        activeHandIndex = 0;
	}
	
	public void joinTable(Table table) {
		if (table == null) {
	        return;
	    }

	    // leave another table before joining another one
	    if (currentTable != null && currentTable != table) {
	        currentTable.removePlayer(this);
	    }

	    boolean joined = table.addPlayer(this);
	    if (joined) {
	        currentTable = table;
	        // resetting stats
	        for (int i = 0; i < hands.length; i++) {
	            handSizes[i] = 0;
	            handBets[i] = 0;
	            handTotals[i] = 0;
	            for (int j = 0; j < hands[i].length; j++) {
	                hands[i][j] = null;
	            }
	        }
	        numHands = 1;
	        activeHandIndex = 0;
	        currentBet = 0;
	        totalCardValue = 0;
	    }
		
	}
	
	public void leaveTable() {
		if (currentTable != null) {
	        currentTable.removePlayer(this);
	        currentTable = null;
	    }
	    
		// resetting all to 0, similar as logout
		for (int i = 0; i < hands.length; i++) {
            handSizes[i] = 0;
            handBets[i] = 0;
            handTotals[i] = 0;
            for (int j = 0; j < hands[i].length; j++) {
                hands[i][j] = null;
            }
        }
        numHands = 1;
        activeHandIndex = 0;
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
        handBets[activeHandIndex] = handBets[activeHandIndex] + money;
        return money;
        
	}
	
	public void hit(Shoe shoe) {
		
		int handSize = handSizes[activeHandIndex];

        if (handSize >= hands[activeHandIndex].length) {
            // hand full, cannot hit
            return;
        }

        Card newCard = shoe.dealCard();
        hands[activeHandIndex][handSize] = newCard;
        handSizes[activeHandIndex] = handSize + 1;

        // recheck total for this hand
        int total = 0;
        int aces = 0;

        for (int i = 0; i < handSizes[activeHandIndex]; i++) {
            Card c = hands[activeHandIndex][i];
            if (c == null) continue;

            total += c.getCardValue(); 
            if (c.getRank() == Rank.Ace) {
                aces = aces + 1;
            }
        }

        while (aces > 0 && total + 10 <= 21) {
            total = total + 10;
            aces = aces - 1;
        }

        handTotals[activeHandIndex] = total;
        this.totalCardValue = total; 
	    
	}
	
	public int stand() {
		return handTotals[activeHandIndex];
	}
	
	public void doubleDown(int bet, Shoe shoe) {
		
		int placed = bet(bet); 
        if (placed > 0) {
            hit(shoe);        
        } else {
            // message of not enough balance or smth like that
        }
	}
	
	public void split(Shoe shoe) {
		// max hands
        if (numHands >= 4) {
            return;
        }
        // cannot split with other number of cards different to 2
        if (handSizes[activeHandIndex] != 2) {
            return;
        }

        Card first = hands[activeHandIndex][0];
        Card second = hands[activeHandIndex][1];

        if (first == null || second == null) {
            return;
        }

        if (first.getRank() != second.getRank()) {
            return; 
        }

        // not enough money 
        int betForHand = handBets[activeHandIndex];
        if (betForHand <= 0 || balance < betForHand) {
            return; 
        }
        balance = balance - betForHand;
        currentBet = currentBet - betForHand;

        int newIndex = numHands;
        numHands++;

        hands[newIndex][0] = second;
        handSizes[newIndex] = 1;

        hands[activeHandIndex][1] = null;
        handSizes[activeHandIndex] = 1;
        handBets[newIndex] = betForHand;

        // dealing the next cards
        if (handSizes[activeHandIndex] < hands[activeHandIndex].length) {
            Card c1 = shoe.dealCard();
            hands[activeHandIndex][handSizes[activeHandIndex] + 1] = c1;
        }
        int total1 = 0;
        int aces1 = 0;
        for (int i = 0; i < handSizes[activeHandIndex]; i++) {
            Card c = hands[activeHandIndex][i];
            if (c == null) {
            	continue;
            }
            total1 += c.getCardValue();
            if (c.getRank() == Rank.Ace) {
            	aces1 = aces1 + 1;
            }
        }
        while (aces1 > 0 && total1 + 10 <= 21) {
            total1 = total1 + 10;
            aces1 = aces1 - 1;
        }
        handTotals[activeHandIndex] = total1;
        this.totalCardValue = total1;

        // new split hand
        if (handSizes[newIndex] < hands[newIndex].length) {
            Card c2 = shoe.dealCard();
            hands[newIndex][handSizes[newIndex]++] = c2;
        }
        int total2 = 0;
        int aces2 = 0;
        for (int i = 0; i < handSizes[newIndex]; i++) {
            Card c = hands[newIndex][i];
            if (c == null) continue;
            total2 += c.getCardValue();
            if (c.getRank() == Rank.Ace) {
            	aces2 = aces2 + 1;
            }
        }
        while (aces2 > 0 && total2 + 10 <= 21) {
        	total2 = total2 + 10;
        	aces2 = aces2 - 1;
        }
        handTotals[newIndex] = total2;

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
	
	public int getNumHands() {
		return this.numHands;
	}
	
	public void setNumHands(int numHands) {
		this.numHands = numHands;
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
	
	@Override
	public String toString() {
	    return "Player ID: " + userId + ", Username: " + username + ", Balance: " + balance;
	}

}