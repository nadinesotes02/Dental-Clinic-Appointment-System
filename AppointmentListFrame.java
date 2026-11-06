import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Locale;

/** Staff "View List": every appointment, with search and status filter. */
final class AppointmentListFrame extends BaseFrame {
    private final ApptTable table = new ApptTable();
    private final JTextField search = Theme.field(18);
    private final JComboBox<String> filter =
            new JComboBox<>(new String[]{"All", "Pending", "Approved", "Done", "Cancelled"});
    private final JTextArea detail = Theme.area(false);
    private final JLabel count = new JLabel(" ");

    AppointmentListFrame(BaseFrame parent) {
        super("Appointment List", parent, 1180, 720);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Theme.TEAL);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Theme.heading("APPOINTMENT LIST", 36f), BorderLayout.WEST);
        Theme.FlatButton back = new Theme.FlatButton("Back", Theme.TEAL_DARK, Color.WHITE);
        back.setPreferredSize(new Dimension(110, 36));
        back.addActionListener(e -> goBack());
        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        backWrap.setOpaque(false);
        backWrap.add(back);
        header.add(backWrap, BorderLayout.EAST);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.add(Theme.caption("SEARCH"));
        bar.add(search);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(Theme.caption("STATUS"));
        bar.add(filter);
        Theme.FlatButton refresh = new Theme.FlatButton("Refresh", Theme.TEAL_DARK, Color.WHITE);
        refresh.setPreferredSize(new Dimension(100, 32));
        refresh.addActionListener(e -> reload());
        bar.add(refresh);
        count.setForeground(Color.WHITE);
        count.setFont(Theme.font(Font.BOLD, 13f));
        bar.add(Box.createHorizontalStrut(12));
        bar.add(count);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(bar, BorderLayout.CENTER);

        JScrollPane details = Theme.scroll(detail);
        details.setPreferredSize(new Dimension(100, 120));

        root.add(top, BorderLayout.NORTH);
        root.add(Theme.scroll(table), BorderLayout.CENTER);
        root.add(details, BorderLayout.SOUTH);
        setContentPane(root);

        Theme.onChange(search, this::reload);
        filter.addActionListener(e -> reload());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail();
        });
        reload();
    }

    @Override
    protected void refreshData() {
        reload();
    }

    private void reload() {
        String q = search.getText().trim().toLowerCase(Locale.ROOT);
        String st = (String) filter.getSelectedItem();
        List<Appointment> rows = Store.query(a -> {
            if (!"All".equals(st) && !a.status.equals(st)) return false;
            if (q.isEmpty()) return true;
            String hay = (a.id + " " + a.patientName() + " " + a.dentist + " " + a.servicesText() + " "
                    + a.status + " " + a.dateText()).toLowerCase(Locale.ROOT);
            return hay.contains(q);
        }, false);
        table.setData(rows);
        count.setText(rows.size() + " appointment(s)");
        showDetail();
    }

    private void showDetail() {
        Appointment a = table.selectedOne();
        detail.setText(a == null ? "Select an appointment to see its details." : Util.details(a));
        detail.setCaretPosition(0);
    }
}
