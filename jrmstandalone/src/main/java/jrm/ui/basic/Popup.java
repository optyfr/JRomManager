package jrm.ui.basic;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

/**
 * Utility interface providing popup menu attachment functionality for Swing components.
 * <p>
 * This interface defines a static helper method that registers mouse listeners on a component
 * to display a {@link JPopupMenu} on both right-click and platform-specific popup trigger events.
 * </p>
 *
 * @see JPopupMenu
 */
public interface Popup {
    /**
     * Attaches a popup menu to the specified component.
     * <p>
     * Registers a {@link MouseAdapter} that shows the popup menu when the platform-specific
     * popup trigger is detected (typically right-click on desktop platforms).
     * </p>
     *
     * @param component the {@link Component} to attach the popup menu to
     * @param popup the {@link JPopupMenu} to display
     */
    public static void addPopup(final Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(final MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }
}
