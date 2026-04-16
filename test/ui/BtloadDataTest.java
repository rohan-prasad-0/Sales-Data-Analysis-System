/*
 * JUnit Test for BtloadData.java
 * Place this file inside: test/ui/BtloadDataTest.java
 */

package ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BtloadDataTest {

    @Test
    public void testPanelCreated() {
        BtloadData panel = new BtloadData();

        assertNotNull(panel);
        assertEquals("BtloadData", panel.getClass().getSimpleName());
    }

    @Test
    public void testPanelSize() {
        BtloadData panel = new BtloadData();

        assertTrue(panel.getPreferredSize().width > 0);
        assertTrue(panel.getPreferredSize().height > 0);
    }

    @Test
    public void testPanelVisible() {
        BtloadData panel = new BtloadData();
        panel.setVisible(true);

        assertTrue(panel.isVisible());
    }

    @Test
    public void testPanelEnabled() {
        BtloadData panel = new BtloadData();

        assertTrue(panel.isEnabled());
    }

    @Test
    public void testPanelHasComponents() {
        BtloadData panel = new BtloadData();

        assertTrue(panel.getComponentCount() > 0);
    }
}