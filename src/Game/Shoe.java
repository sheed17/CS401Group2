import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Shoe {
    private final int numDecks = 6; 
    private List<Card> cards; 
    private final int shuffleThreshold; 
    private int cardsRemaining; 
    private final Dealer dealer; 
  
    public Shoe(Dealer dealer) {
        this.dealer = dealer;
        Random random = new Random();
        this.shuffleThreshold = random.nextInt(31) + 20; // 31 possible values (0-30) + 20 offset = 20-50
        
        initializeDecks();
        shuffleDecks();
    }
    
    //////////MIGHT CHANGE LATER ON
    private void initializeDecks() {
        cards = new ArrayList<>();
        
        // Loop for the six-deck setup 
        for (int i = 0; i < numDecks; i++) {
            // Loop through all Ranks and Suits
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(new Card(rank, suit));
                }
            }
        }
        this.cardsRemaining = cards.size(); // 312 cards
    }

    public void shuffleDecks() {
        Collections.shuffle(cards); 
        this.cardsRemaining = cards.size(); // Reset card count to 312
        
    }

}
