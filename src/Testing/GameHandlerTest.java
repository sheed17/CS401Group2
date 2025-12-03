package src.Testing;

import org.junit.jupiter.api.*;

import src.Game.GameHandler;
import src.Game.Player;
import src.Game.Table;

import static org.junit.jupiter.api.Assertions.*;

public class GameHandlerTest {

    private GameHandler handler;
    private Player p1;
    private Player p2;

    @BeforeEach
    void setup() {
        handler = new GameHandler();
        p1 = new Player("Lebron", "password");
        p2 = new Player("Steph", "password123");

        handler.join(p1);
        handler.join(p2);
    }

    @Test
    void testJoinAddsPlayers() {
        Table table = handler.getTable();
        assertEquals(2, table.getPlayers().size());
        assertTrue(table.getPlayers().contains(p1));
        assertTrue(table.getPlayers().contains(p2));
    }

    @Test
    void testPlaceBet() {
        boolean ok = handler.placeBet(p1, 100);
        assertTrue(ok);
        assertEquals(100, p1.getCurrentBet());
    }

    @Test
    void testStartRound() {
        handler.placeBet(p1, 100);
        handler.placeBet(p2, 100);
        
        handler.startRound();

        assertTrue(p1.getHandSizes()[0] > 0); 
        assertTrue(p2.getHandSizes()[0] > 0);
    }

    @Test
    void testHitOnTurn() {
        handler.placeBet(p1, 50);
        handler.startRound();

        int before = p1.getTotalCardValue();
        handler.hit(p1);
        int after = p1.getTotalCardValue();

        assertTrue(after > before);
    }

    @Test
    void testStandEndsTurn() {
        handler.placeBet(p1, 50);
        handler.placeBet(p2, 50);
        handler.startRound();

        handler.stand(p1);

        String state = handler.buildState(p2);

        assertTrue(state.contains("Current turn: Damian Lillard"));
    }

    @Test
    void testFinishRound() {
        handler.placeBet(p1, 50);
        handler.startRound();

        handler.stand(p1); 

        String state = handler.buildState(p1);

        assertTrue(state.contains("ROUND RESULTS"));
        assertFalse(state.contains("IN PROGRESS"));
    }
}
