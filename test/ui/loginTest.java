package ui;

import org.junit.Test;
import static org.junit.Assert.*;

public class loginTest {
    
    @Test
    public void testLoginClassExists() {
        try {
            login loginFrame = new login();
            assertNotNull("Login class should be instantiable", loginFrame);
            loginFrame.dispose();
        } catch (Exception e) {
            fail("Login class could not be instantiated: " + e.getMessage());
        }
    }
    
    @Test
    public void testDatabaseConnection() {
        db.db dbInstance = new db.db();
        assertNotNull("Database class should exist", dbInstance);
    }
    
    @Test
    public void testLoginFrameDisplays() {
        login loginFrame = new login();
        assertTrue("Login frame should be visible", loginFrame.isVisible());
        loginFrame.dispose();
    }
}