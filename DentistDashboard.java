import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** One dentist's own schedule. The same screen is used for all three dentists. */
final class DentistDashboard extends BaseFrame {
    private final String dentist;
    private final ApptTable table = new ApptTable();
    private final JTextArea note = Theme.area(false);
    private final JTextArea receipt = Theme.area(false);
    private final JLabel todayBar = stat(new Color(0, 255, 204), new Color(0, 51, 51));
    private final JLabel upcomingBar = stat(new Color(0, 204, 153), new Color(0, 51, 51));
    private final JLabel doneBar = stat(Theme.TEAL_DARK, Color.WHITE);

    DentistDashboard(String dentist) {
        super("Dentist - " + dentist, null, 1180, 720);
        this.dentist = dentist;
        receipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        receipt.setLineWrap(false);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Theme.TEAL);
        root.setBorder(BorderFactory.createEmptyBorder(14, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Theme.heading("DENTIST DASHBOARD", 32f), BorderLayout.WEST);
        JLabel who = new JLabel("Dr. " + dentist);
        who.setForeground(Color.WHITE);
        who.setFont(Theme.font(Font.BOLD, 18f));
        header.add(who, BorderLayout.EAST);

        JScrollPane tableScroll = Theme.scroll(table);
        tableScroll.setPreferredSize(new Dimension(100, 300));

        root.add(header, BorderLayout.NORTH);
        root.add(tableScroll, BorderLayout.CENTER);
        root.add(buildBottom(), BorderLayout.SOUTH);
        setContentPane(root);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelected();
        });
        reload();
    }

    private static JLabel stat(Color bg, Color fg) {
        JLabel l = new JLabel(" ");
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setFont(Theme.font(Font.BOLD, 14f));
        l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        l.setPreferredSize(new Dimension(100, 40));
        return l;
    }

    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        bottom.setPreferredSize(new Dimension(100, 300));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;

        // left: buttons + statistics
        JPanel left = new JPanel(new BorderLayout(0, 18));
        left.setOpaque(false);
        Theme.FlatButton refresh = new Theme.FlatButton("Refresh", Theme.TEAL_DARK, Color.WHITE);
        refresh.setFont(Theme.font(Font.BOLD, 16f));
        refresh.setPreferredSize(new Dimension(120, 38));
        refresh.addActionListener(e -> reload());
        Theme.FlatButton done = new Theme.FlatButton("Done", Theme.TEAL_DARK, Color.WHITE);
        done.setFont(Theme.font(Font.BOLD, 16f));
        done.setPreferredSize(new Dimension(120, 38));
        done.addActionListener(e -> markDone());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(refresh);
        btnRow.add(done);
        left.add(btnRow, BorderLayout.NORTH);
        JPanel stats = new JPanel(new GridLayout(3, 1, 0, 12));
        stats.setOpaque(false);
        stats.add(todayBar);
        stats.add(upcomingBar);
        stats.add(doneBar);
        JPanel statsWrap = new JPanel(new BorderLayout());
        statsWrap.setOpaque(false);
        statsWrap.add(stats, BorderLayout.NORTH);
        left.add(statsWrap, BorderLayout.CENTER);

        // middle: note + log out
        JPanel mid = new JPanel(new BorderLayout(0, 6));
        mid.setOpaque(false);
        JPanel noteBox = new JPanel(new BorderLayout(0, 6));
        noteBox.setOpaque(false);
        noteBox.add(Theme.heading("Note:", 18f), BorderLayout.NORTH);
        noteBox.add(Theme.scroll(note), BorderLayout.CENTER);
        Theme.FlatButton logout = new Theme.FlatButton("LOG OUT", Theme.LIGHT_BTN, Theme.TEAL_DARK);
        logout.setFont(Theme.font(Font.BOLD, 16f));
        logout.setPreferredSize(new Dimension(130, 38));
        logout.addActionListener(e -> logout());
        JPanel lo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        lo.setOpaque(false);
        lo.add(logout);
        mid.add(noteBox, BorderLayout.CENTER);
        mid.add(lo, BorderLayout.SOUTH);

        // right: receipt
        JPanel rec = new JPanel(new BorderLayout(0, 6));
        rec.setOpaque(false);
        rec.add(Theme.heading("Receipt", 18f), BorderLayout.NORTH);
        rec.add(Theme.scroll(receipt), BorderLayout.CENTER);

        c.gridx = 0;
        c.weightx = 0.34;
        c.insets = new Insets(0, 0, 0, 22);
        bottom.add(left, c);
        c.gridx = 1;
        c.weightx = 0.28;
        bottom.add(mid, c);
        c.gridx = 2;
        c.weightx = 0.38;
        c.insets = new Insets(0, 0, 0, 0);
        bottom.add(rec, c);
        return bottom;
    }

    @Override
    protected void closeRequested() {
        logout();
    }

    private void reload() {
        LocalDate today = LocalDate.now();
        table.setData(Store.query(a -> a.dentist.equals(dentist) && !a.archived
                && Appointment.APPROVED.equals(a.status), false));
        int t = Store.count(a -> a.dentist.equals(dentist) && !a.archived
                && Appointment.APPROVED.equals(a.status) && !a.date.isAfter(today));
        int up = Store.count(a -> a.dentist.equals(dentist) && !a.archived
                && Appointment.APPROVED.equals(a.status) && a.date.isAfter(today));
        int d = Store.count(a -> a.dentist.equals(dentist) && Appointment.DONE.equals(a.status));
        todayBar.setText("Due today / overdue: " + t);
        upcomingBar.setText("Upcoming: " + up);
        doneBar.setText("Completed: " + d);
        showSelected();
    }

    private void showSelected() {
        List<Appointment> sel = table.selected();
        if (sel.size() == 1) {
            Appointment a = sel.get(0);
            String n = a.combinedNote();
            note.setText(n.isEmpty() ? "(no note)" : n);
            receipt.setText(Util.receipt(a));
        } else {
            note.setText("");
            receipt.setText("");
        }
        note.setCaretPosition(0);
        receipt.setCaretPosition(0);
    }

    private void markDone() {
        List<Appointment> sel = table.selected();
        if (sel.isEmpty()) {
            Dialogs.warn(this, "Select at least one appointment to mark as done.");
            return;
        }
        LocalDate today = LocalDate.now();
        for (Appointment a : sel) {
            if (a.date.isAfter(today)) {
                Dialogs.warn(this, "Appointment #" + a.id + " (" + a.patientName() + ") is scheduled for "
                        + a.dateText() + ".\nOnly appointments dated today or earlier can be marked as DONE.");
                return;
            }
        }
        if (!Dialogs.confirm(this, "Mark " + sel.size() + " appointment(s) as DONE and move them to the archive?",
                "Done")) return;
        StringBuilder receipts = new StringBuilder();
        for (Appointment a : sel) {
            a.status = Appointment.DONE;
            a.archived = true;
            if (receipts.length() > 0) receipts.append("\n\n");
            receipts.append(Util.receipt(a));
        }
        Dialogs.persist(this);
        reload();
        receipt.setText(receipts.toString());
        receipt.setCaretPosition(0);
    }
}
