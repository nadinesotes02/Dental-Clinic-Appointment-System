import javax.swing.*;

/**
 * Dental Clinic Appointment System - entry point.
 *
 * Build:  javac -d out src/*.java
 * Run:    java -cp out Main
 */
public class Main {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "Unexpected error: " + e, "Error", JOptionPane.ERROR_MESSAGE));
        });
        SwingUtilities.invokeLater(() -> {
            try {
                // Same "Java" look as the original screens on every computer.
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to the default look and feel
            }
            Store.load();
            new LoginFrame().setVisible(true);
        });
    }
}
