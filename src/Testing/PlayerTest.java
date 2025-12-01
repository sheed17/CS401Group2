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
        // This is the same file name used inside Player
        userDataFile = new File("UserData.txt");

        // Write a fresh file before each test
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
        Player p = new Player("x", "y"); // constructor values don't matter; login will overwrite
        boolean result = p.logIn("alice", "1234");

        assertTrue("Login should succeed for valid credentials", result);
        assertEquals(100, p.getBalance());
    }

}
