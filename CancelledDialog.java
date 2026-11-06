import javax.swing.*;
import java.awt.*;

/** "Cancelled Appointments" window opened from the admin dashboard. */
final class CancelledDialog extends JDialog {

    CancelledDialog(Component parent) {
        super(parent == null ? null : SwingUtilities.getWindowAncestor(parent),
                "Cancelled Appointments", ModalityType.APPLICATION_MODAL);
        setIconImage(Theme.toothIcon(64));
        ApptTable table = new ApptTable();
        table.setData(Store.query(a -> Appointment.CANCELLED.equals(a.status), true));

        JLabel count = new JLabel(table.getRowCount() + " cancelled appointment(s)");
        count.setFont(Theme.font(Font.BOLD, 13f));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        bottom.add(count, BorderLayout.WEST);
        bottom.add(close, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(Theme.scroll(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setSize(960, 520);
        setLocationRelativeTo(parent);
    }
}
