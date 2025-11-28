package src.Game;
public class Card {
    private final Rank rank;  
    private final Suit suit; 

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }
// Getters
    public Rank getRank() {
        return rank;
    }
    public Suit getSuit() {
        return suit;
    }
    public int getCardValue() {
        return rank.getValue();
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}
