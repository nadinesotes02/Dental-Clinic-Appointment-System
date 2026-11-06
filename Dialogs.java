import javax.swing.*;
import java.awt.*;

/** Thin wrappers around JOptionPane so every message looks the same. */
final class Dialogs {
    private Dialogs() {}

    static void info(Component parent, String msg) {
        info(parent, msg, "Message");
    }

    static void info(Component parent, String msg, String title) {
        JOptionPane.showMessageDialog(parent, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    static void warn(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Please check", JOptionPane.WARNING_MESSAGE);
    }

    static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static boolean confirm(Component parent, String msg, String title) {
        return JOptionPane.showConfirmDialog(parent, msg, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Saves the data file and tells the user if that failed. */
    static void persist(Component parent) {
        if (!Store.save()) {
            error(parent, "Could not save the data file:\n" + Store.lastError());
        }
    }
}
