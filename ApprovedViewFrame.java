import javax.swing.*;
import java.awt.*;

/** Admin "Approve View": approved appointments with service / note / medical details. */
final class ApprovedViewFrame extends BaseFrame {
    private final ApptTable table = new ApptTable();
    private final JTextArea patient = Theme.area(false);
    private final JTextArea service = Theme.area(false);
    private final JTextArea note = Theme.area(false);
    private final JTextArea medical = Theme.area(false);

    ApprovedViewFrame(BaseFrame parent) {
        super("Approved View", parent, 1180, 720);

        // left column
        JPanel side = new JPanel();
        side.setBackground(Theme.TEAL);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        side.setPreferredSize(new Dimension(290, 100));
        JLabel title = Theme.heading("Approved View", 30f);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(title);
        side.add(Box.createVerticalStrut(26));
        side.add(sideButton("Refresh", this::reload));
        side.add(Box.createVerticalStrut(16));
        side.add(sideButton("Back", this::goBack));
        side.add(Box.createVerticalStrut(26));
        JLabel pl = Theme.caption("PATIENT");
        pl.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(pl);
        side.add(Box.createVerticalStrut(4));
        JScrollPane ps = Theme.scroll(patient);
        ps.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(ps);

        // bottom detail panel
        JPanel details = new JPanel(new GridLayout(1, 3, 22, 0));
        details.setBackground(Theme.TEAL_DARK);
        details.setBorder(BorderFactory.createEmptyBorder(14, 22, 18, 22));
        details.add(titled("SERVICE", service));
        details.add(titled("NOTE", note));
        details.add(titled("MEDICAL", medical));
        details.setPreferredSize(new Dimension(100, 270));

        JPanel bottomWrap = new JPanel(new BorderLayout());
        bottomWrap.setBackground(Theme.TEAL);
        bottomWrap.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        bottomWrap.add(details, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.add(Theme.scroll(table), BorderLayout.CENTER);
        right.add(bottomWrap, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout());
        root.add(side, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        setContentPane(root);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail();
        });
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

    private static JPanel titled(String caption, JTextArea area) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(Theme.heading(caption, 20f), BorderLayout.NORTH);
        p.add(Theme.scroll(area), BorderLayout.CENTER);
        return p;
    }

    @Override
    protected void refreshData() {
        reload();
    }

    private void reload() {
        table.setData(Store.query(a -> !a.archived && Appointment.APPROVED.equals(a.status), false));
        showDetail();
    }

    private void showDetail() {
        Appointment a = table.selectedOne();
        if (a == null) {
            patient.setText("");
            service.setText("");
            note.setText("");
            medical.setText("");
            return;
        }
        patient.setText(a.patientName() + "\n" + a.gender + ", " + a.age + " yrs\nContact: " + a.contact
                + "\nBirthdate: " + Util.fmtDate(a.birthDate) + "\nDentist: " + a.dentist);
        service.setText(Util.itemised(a));
        note.setText(a.note);
        medical.setText(a.medical);
        patient.setCaretPosition(0);
        service.setCaretPosition(0);
        note.setCaretPosition(0);
        medical.setCaretPosition(0);
    }
}
