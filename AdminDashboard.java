import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Admin home: approve, cancel, reschedule, archive. */
final class AdminDashboard extends BaseFrame {
    private final ApptTable table = new ApptTable();
    private final JLabel detail = bar(Theme.BAR_BLUE, Color.WHITE);
    private final JLabel stats = bar(Theme.BAR_CYAN, new Color(0, 51, 51));

    AdminDashboard() {
        super("Admin", null, 1180, 720);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.TEAL);
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 20, 22));

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Theme.heading("ADMIN DASHBOARD", 46f), BorderLayout.WEST);
        Theme.FlatButton logout = new Theme.FlatButton("LOGOUT", Theme.LIGHT_BTN, Theme.TEAL_DARK);
        logout.setFont(Theme.font(Font.BOLD, 15f));
        logout.setPreferredSize(new Dimension(112, 34));
        logout.addActionListener(e -> logout());
        JPanel lw = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        lw.setOpaque(false);
        lw.add(logout);
        header.add(lw, BorderLayout.EAST);

        // button column
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(side("APPROVE", Theme.GREEN, this::approve));
        buttons.add(Box.createVerticalStrut(18));
        buttons.add(side("APPROVE VIEW", Theme.TEAL_DARK, () -> open(new ApprovedViewFrame(this))));
        buttons.add(Box.createVerticalStrut(46));
        buttons.add(side("CANCEL", Theme.RED, this::cancel));
        buttons.add(Box.createVerticalStrut(18));
        buttons.add(side("CANCEL VIEW", Theme.TEAL_DARK, () -> new CancelledDialog(this).setVisible(true)));
        buttons.add(Box.createVerticalStrut(30));
        buttons.add(side("ARCHIVE", Theme.BLUE, () -> open(new ArchiveFrame(this))));
        buttons.add(Box.createVerticalStrut(18));
        buttons.add(side("RESCHEDULE", Theme.TEAL_DARK, this::reschedule));
        buttons.add(Box.createVerticalStrut(18));
        buttons.add(side("REFRESH", Theme.TEAL_DARK, this::reload));

        JPanel center = new JPanel(new BorderLayout(20, 0));
        center.setOpaque(false);
        center.add(buttons, BorderLayout.WEST);
        center.add(Theme.scroll(table), BorderLayout.CENTER);

        JPanel bars = new JPanel(new GridLayout(2, 1, 0, 14));
        bars.setOpaque(false);
        bars.add(detail);
        bars.add(stats);

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(bars, BorderLayout.SOUTH);
        setContentPane(root);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateDetail();
        });
        reload();
    }

    private static JLabel bar(Color bg, Color fg) {
        JLabel l = new JLabel(" ");
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setFont(Theme.font(Font.BOLD, 14f));
        l.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        l.setPreferredSize(new Dimension(100, 50));
        return l;
    }

    private JButton side(String text, Color bg, Runnable r) {
        Theme.FlatButton b = new Theme.FlatButton(text, bg, Color.WHITE);
        b.setPreferredSize(new Dimension(248, 38));
        b.setMaximumSize(new Dimension(248, 38));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> r.run());
        return b;
    }

    @Override
    protected void refreshData() {
        reload();
    }

    @Override
    protected void closeRequested() {
        logout();
    }

    // ----------------------------------------------------------------- data

    private void reload() {
        table.setData(Store.query(a -> !a.archived && a.isActive(), false));
        int pending = Store.count(a -> !a.archived && Appointment.PENDING.equals(a.status));
        int approved = Store.count(a -> !a.archived && Appointment.APPROVED.equals(a.status));
        int cancelled = Store.count(a -> Appointment.CANCELLED.equals(a.status));
        int done = Store.count(a -> Appointment.DONE.equals(a.status));
        LocalDate today = LocalDate.now();
        int todays = Store.count(a -> !a.archived && a.isActive() && a.date.equals(today));
        stats.setText("Pending: " + pending + "     Approved: " + approved + "     Cancelled: " + cancelled
                + "     Done: " + done + "     Today: " + todays);
        updateDetail();
    }

    private void updateDetail() {
        List<Appointment> sel = table.selected();
        String text;
        if (sel.isEmpty()) {
            text = "Select an appointment to see its details.";
        } else if (sel.size() > 1) {
            text = sel.size() + " appointments selected.";
        } else {
            Appointment a = sel.get(0);
            text = "#" + a.id + "   " + a.patientName() + "   |   " + a.servicesText()
                    + "   |   Total: " + Util.money(a.total()) + "   |   " + a.contact;
        }
        detail.setText(text);
        detail.setToolTipText(text);
    }

    // -------------------------------------------------------------- actions

    private static String noteText(Appointment a) {
        String n = a.combinedNote();
        return n.isEmpty() ? "(no note)" : n;
    }

    private void approve() {
        List<Appointment> sel = table.selected();
        if (sel.isEmpty()) {
            Dialogs.warn(this, "Select an appointment to approve.");
            return;
        }
        int n = 0;
        Appointment last = null;
        for (Appointment a : sel) {
            if (Appointment.PENDING.equals(a.status)) {
                a.status = Appointment.APPROVED;
                last = a;
                n++;
            }
        }
        if (n == 0) {
            Dialogs.info(this, sel.size() == 1 ? "That appointment is already approved."
                    : "The selected appointments are already approved.", "Approve");
            return;
        }
        Dialogs.persist(this);
        reload();
        if (n == 1) {
            Dialogs.info(this, "Appointment Approved!\n\nPatient note:\n" + noteText(last), "Approved");
        } else {
            Dialogs.info(this, n + " appointments approved.", "Approved");
        }
    }

    private void cancel() {
        List<Appointment> sel = table.selected();
        if (sel.isEmpty()) {
            Dialogs.warn(this, "Select an appointment to cancel.");
            return;
        }
        if (!Dialogs.confirm(this, "Cancel " + sel.size() + " appointment(s)?\n"
                + "They will be moved to the archive.", "Cancel appointment")) return;
        for (Appointment a : sel) {
            a.status = Appointment.CANCELLED;
            a.archived = true;
        }
        Dialogs.persist(this);
        reload();
        Dialogs.info(this, sel.size() == 1 ? "Appointment cancelled." : sel.size() + " appointments cancelled.");
    }

    private void reschedule() {
        List<Appointment> sel = table.selected();
        if (sel.size() != 1) {
            Dialogs.warn(this, "Select one appointment to reschedule.");
            return;
        }
        Appointment a = sel.get(0);
        if (RescheduleDialog.reschedule(this, a)) {
            Dialogs.persist(this);
            reload();
            Dialogs.info(this, "Appointment rescheduled to " + a.dateText() + " at " + a.timeText() + ".");
        }
    }
}
