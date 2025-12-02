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

        String rankStr;

        switch (rank) {
            case Ace:   rankStr = "A"; break;
            case Jack:  rankStr = "J"; break;
            case Queen: rankStr = "Q"; break;
            case King:  rankStr = "K"; break;
            default:    rankStr = String.valueOf(getCardValue());
        }

        String suitStr = "";
        switch (suit) {
            case Spades:   suitStr = "♠"; break;
            case Hearts:   suitStr = "♥"; break;
            case Diamonds: suitStr = "♦"; break;
            case Clubs:    suitStr = "♣"; break;
        }

        return rankStr + "(" + suitStr + ")";
    }

}
