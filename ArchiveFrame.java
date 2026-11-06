import javax.swing.*;
import java.awt.*;

/** Archive: finished (Done) and Cancelled appointments. Can restore or rebook them. */
final class ArchiveFrame extends BaseFrame {
    private final ApptTable table = new ApptTable();
    private final JLabel count = new JLabel(" ");

    ArchiveFrame(BaseFrame parent) {
        super("Archive", parent, 1180, 720);

        JPanel side = new JPanel();
        side.setBackground(Theme.TEAL);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(30, 60, 24, 60));
        side.setPreferredSize(new Dimension(290, 100));
        JLabel title = Theme.heading("ARCHIVE", 40f);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(title);
        side.add(Box.createVerticalStrut(34));
        side.add(sideButton("Unarchive", this::unarchive));
        side.add(Box.createVerticalStrut(16));
        side.add(sideButton("Refresh", this::reload));
        side.add(Box.createVerticalStrut(16));
        side.add(sideButton("Rebook", this::rebook));
        side.add(Box.createVerticalStrut(16));
        side.add(sideButton("Back", this::goBack));
        side.add(Box.createVerticalStrut(24));
        count.setForeground(Color.WHITE);
        count.setFont(Theme.font(Font.BOLD, 13f));
        count.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(count);

        JPanel root = new JPanel(new BorderLayout());
        root.add(side, BorderLayout.WEST);
        root.add(Theme.scroll(table), BorderLayout.CENTER);
        setContentPane(root);
        reload();
    }

    private JButton sideButton(String text, Runnable r) {
        Theme.FlatButton b = new Theme.FlatButton(text, Theme.TEAL_DARK, Color.WHITE);
        b.setFont(Theme.font(Font.BOLD, 16f));
        b.setPreferredSize(new Dimension(190, 36));
        b.setMaximumSize(new Dimension(190, 36));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> r.run());
        return b;
    }

    private void reload() {
        table.setData(Store.query(a -> a.archived, true));
        count.setText(table.getRowCount() + " archived");
    }

    private void unarchive() {
        Appointment a = table.selectedOne();
        if (a == null) {
            Dialogs.warn(this, "Select an appointment to restore.");
            return;
        }
        if (!Dialogs.confirm(this, "Restore this appointment to the main list?", "Unarchive")) return;
        String problem = Store.findConflict(a.dentist, a.date, a.minutes, a.patientKey(), a.id);
        if (problem != null) {
            Dialogs.warn(this, "This appointment cannot be restored:\n" + problem);
            return;
        }
        // A finished visit comes back approved; a cancelled one needs approving again.
        a.status = Appointment.DONE.equals(a.status) ? Appointment.APPROVED : Appointment.PENDING;
        a.archived = false;
        Dialogs.persist(this);
        reload();
        Dialogs.info(this, "Appointment restored as " + a.status + ".", "Unarchive");
    }

    private void rebook() {
        Appointment a = table.selectedOne();
        if (a == null) {
            Dialogs.warn(this, "Select an appointment to rebook.");
            return;
        }
        Appointment copy = RescheduleDialog.rebook(this, a);
        if (copy == null) return;
        Dialogs.persist(this);
        reload();
        Dialogs.info(this, "New appointment #" + copy.id + " booked for " + copy.patientName() + " on "
                + copy.dateText() + " at " + copy.timeText() + ".\nStatus: Pending (needs approval).", "Rebooked");
    }
}
