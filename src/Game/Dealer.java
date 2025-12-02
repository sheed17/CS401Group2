package src.Game;

import java.util.ArrayList;
import java.util.List;

public class Dealer {
	
	private static int count = 0;
    private int dealerId;
    private List<Card> hand;
    private int totalCardValue;

    public Dealer() {
        this.dealerId = ++count;
        this.hand = new ArrayList<>();
        this.totalCardValue = 0;
    }

    public void resetHand() {
        hand.clear();
        totalCardValue = 0;
    }

    public void hit(Shoe shoe) {
        Card card = shoe.dealCard();
        if (card != null) {
            hand.add(card);
            totalCardValue = calculateHandValue();
        }
    }

    public void playDealerHand(Shoe shoe) {
        while (calculateHandValue() < 17) {
            hit(shoe);
        }
    }

    private int calculateHandValue() {
        int value = 0;
        int aces = 0;

        for (Card c : hand) {
            value += c.getCardValue();
            if (c.getRank() == Rank.Ace) aces++;
        }

		// Upgrade some aces from 1 to 11
		while (aces > 0 && value + 10 <= 21) {
    		value += 10;
    		aces--;
		}

        return value;
    }

    public int getTotalCardValue() {
        return totalCardValue;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void removeCard(int index) {
        if (index >= 0 && index < hand.size()) {
            hand.remove(index);
            totalCardValue = calculateHandValue();
        }
    }
    
    public int getDealerId() {
    	return dealerId;
    }

    @Override
    public String toString() {
        return "Dealer Hand: " + hand + " | Total: " + totalCardValue;
    }
}


