/*
 * JUnit Test for admin_dashboard.java
 * Place this file inside: test/ui/AdminDashboardTest.java
 */

package ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class admin_dashboardTest {

    @Test
    public void testDashboardCreated() {
        admin_dashboard ad = new admin_dashboard();

        assertNotNull(ad);
        assertEquals("admin_dashboard", ad.getClass().getSimpleName());
    }

    @Test
    public void testDashboardVisible() {
        admin_dashboard ad = new admin_dashboard();
        ad.setVisible(true);

        assertTrue(ad.isVisible());

        ad.dispose();
    }

    @Test
    public void testCloseOperation() {
        admin_dashboard ad = new admin_dashboard();

        assertEquals(javax.swing.JFrame.EXIT_ON_CLOSE,
                ad.getDefaultCloseOperation());
    }

    @Test
    public void testWindowResizable() {
        admin_dashboard ad = new admin_dashboard();

        assertFalse(ad.isResizable());
    }

    @Test
    public void testWindowSize() {
        admin_dashboard ad = new admin_dashboard();

        assertTrue(ad.getWidth() > 0);
        assertTrue(ad.getHeight() > 0);
    }

    @Test
    public void testTitleNotNull() {
        admin_dashboard ad = new admin_dashboard();

        assertNotNull(ad.getTitle());
    }
}