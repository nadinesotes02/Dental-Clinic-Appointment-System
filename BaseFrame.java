import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Common behaviour for every window: same icon, same size handling, and a simple
 * "open child / go back to parent" navigation that keeps the window position.
 */
abstract class BaseFrame extends JFrame {
    protected final BaseFrame parent;

    BaseFrame(String title, BaseFrame parent, int width, int height) {
        super(title);
        this.parent = parent;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setIconImage(Theme.toothIcon(64));
        setSize(width, height);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { closeRequested(); }
        });
    }

    /** Called when a child window closes and this one becomes visible again. */
    protected void refreshData() {
    }

    /** Hides this window and shows {@code next} in the same place. */
    protected void open(BaseFrame next) {
        next.setBounds(getBounds());
        next.setExtendedState(getExtendedState());
        next.setVisible(true);
        setVisible(false);
    }

    protected void goBack() {
        if (parent == null) return;
        parent.setBounds(getBounds());
        parent.setExtendedState(getExtendedState());
        parent.refreshData();
        parent.setVisible(true);
        dispose();
    }

    protected void logout() {
        if (!Dialogs.confirm(this, "Log out of the system?", "Logout")) return;
        open(new LoginFrame());
        dispose();
    }

    /** The window's X button. Child windows go back, the top window asks before exiting. */
    protected void closeRequested() {
        if (parent != null) {
            goBack();
        } else if (Dialogs.confirm(this, "Exit the application?", "Exit")) {
            dispose();
            System.exit(0);
        }
    }
}
