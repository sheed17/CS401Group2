package src.Testing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import src.Game.Player;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class PlayerTest {

	private File userDataFile;

    @Before
    public void setUp() throws Exception {
        userDataFile = new File("UserData.txt");

        writeUserData(
            "alice,1234,100\n" +
            "bob,abcd,200\n"
        );
    }
    

    private void writeUserData(String content) throws IOException {
        try (FileWriter fw = new FileWriter(userDataFile, false)) {
            fw.write(content);
        }
    }
    
    @Test
    public void testLoginSuccess() {
        Player p = new Player("alice", "1234"); 
        boolean result = p.logIn("alice", "1234");

        assertTrue("Login should succeed for valid credentials", result);
        assertEquals("alice", p.getUsername());
        assertEquals(100, p.getBalance());
    }

}
