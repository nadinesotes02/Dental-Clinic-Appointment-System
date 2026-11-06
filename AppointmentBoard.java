import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Booking form used by staff (personal information, services, medical alert, dentist). */
final class AppointmentBoard extends BaseFrame {
    private final JTextField first = Theme.field(16);
    private final JTextField last = Theme.field(16);
    private final JTextField age = Theme.field(4);
    private final JTextField contact = Theme.field(16);
    private final JTextField time = Theme.field(6);
    private final JComboBox<String> gender = new JComboBox<>(new String[]{"GENDER", "Male", "Female"});
    private final JComboBox<String> ampm = new JComboBox<>(new String[]{"Select", "AM", "PM"});
    private final JComboBox<String> dentist = new JComboBox<>();
    private final DateField birth =
            new DateField(LocalDate.now().minusYears(120), LocalDate.now());
    private final DateField apptDate =
            new DateField(LocalDate.now(), LocalDate.now().plusYears(Store.MAX_YEARS_AHEAD));
    private final JList<Store.Service> services = new JList<>(Store.SERVICES.toArray(new Store.Service[0]));
    private final JLabel total = new JLabel("Total: " + Util.money(0));
    private final JTextArea note = Theme.area(true);
    private final JTextArea medical = Theme.area(true);

    AppointmentBoard(BaseFrame parent) {
        super("Appointment Board", parent, 1250, 760);
        dentist.addItem("Select Dentist");
        for (String d : Store.DENTISTS) dentist.addItem(d);
        age.setEditable(false);
        age.setBackground(new Color(236, 242, 243));
        age.setToolTipText("Calculated from the birthdate");
        services.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        services.setCellRenderer(new ServiceRenderer());
        services.setFixedCellHeight(24);
        services.addListSelectionListener(e -> updateTotal());
        birth.addChangeListener(this::updateAge);
        buildUi();
    }

    // ---------------------------------------------------------------- layout

    private void buildUi() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(Theme.CYAN);
        main.setBorder(new EmptyBorder(16, 22, 16, 22));
        GridBagConstraints c = new GridBagConstraints();

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 26));
        titleBox.setBackground(Theme.TEAL);
        titleBox.add(Theme.heading("APPOINTMENT  BOARD", 34f));
        header.add(titleBox, BorderLayout.WEST);
        Theme.FlatButton back = new Theme.FlatButton("Back", Theme.TEAL_DARK, Color.WHITE);
        back.setPreferredSize(new Dimension(104, 32));
        back.addActionListener(e -> goBack());
        JPanel backBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        backBox.setBackground(Theme.PAPER);
        backBox.add(back);
        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backWrap.setOpaque(false);
        backWrap.add(backBox);
        header.add(backWrap, BorderLayout.EAST);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        main.add(header, c);

        c.gridy = 1;
        c.gridwidth = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(18, 0, 0, 18);
        c.gridx = 0;
        c.weightx = 0.30;
        main.add(buildPersonal(), c);
        c.gridx = 1;
        c.weightx = 0.38;
        main.add(buildDetails(), c);
        c.gridx = 2;
        c.weightx = 0.32;
        c.insets = new Insets(18, 0, 0, 0);
        main.add(buildMedical(), c);

        JScrollPane page = new JScrollPane(main);
        page.setBorder(null);
        page.getViewport().setBackground(Theme.CYAN);
        page.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(page);
    }

    private JPanel card(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(Theme.TEAL);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        return p;
    }

    private static void put(JPanel p, JComponent c, int maxWidth) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(maxWidth, c.getPreferredSize().height));
        p.add(c);
    }

    private static void gap(JPanel p, int h) {
        p.add(Box.createVerticalStrut(h));
    }

    private JPanel buildPersonal() {
        JPanel p = card(null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        put(p, Theme.heading("PERSONAL INFORMATION", 24f), 600);
        gap(p, 10);
        put(p, Theme.caption("FIRST NAME"), 400);
        gap(p, 3);
        put(p, first, 300);
        gap(p, 10);
        put(p, Theme.caption("LAST NAME"), 400);
        gap(p, 3);
        put(p, last, 300);
        gap(p, 10);
        put(p, gender, 160);
        gap(p, 10);
        put(p, Theme.caption("AGE"), 400);
        gap(p, 3);
        put(p, age, 70);
        gap(p, 10);
        put(p, Theme.caption("BIRTHDATE"), 400);
        gap(p, 3);
        put(p, birth, 220);
        gap(p, 10);
        put(p, Theme.caption("CONTACT NUMBER"), 400);
        gap(p, 3);
        put(p, contact, 300);
        gap(p, 10);
        put(p, Theme.caption("APPOINTMENT DATE"), 400);
        gap(p, 3);
        put(p, apptDate, 220);
        gap(p, 10);
        put(p, Theme.caption("APPOINTMENT TIME   (clinic hours " + Store.hoursText() + ")"), 400);
        gap(p, 3);
        JPanel tr = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tr.setOpaque(false);
        tr.add(time);
        tr.add(Box.createHorizontalStrut(8));
        tr.add(ampm);
        put(p, tr, 300);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildDetails() {
        JPanel p = card(new BorderLayout(0, 8));
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        put(top, Theme.heading("APPOINTMENT DETAILS", 24f), 700);
        gap(top, 10);
        put(top, Theme.caption("SERVICE TYPE   (hold Ctrl to pick several)"), 700);
        gap(top, 3);
        JScrollPane sp = Theme.scroll(services);
        sp.setPreferredSize(new Dimension(300, 13 * 24 + 4));
        put(top, sp, 1000);
        gap(top, 6);
        total.setForeground(Color.WHITE);
        total.setFont(Theme.font(Font.BOLD, 15f));
        put(top, total, 700);
        gap(top, 8);
        p.add(top, BorderLayout.NORTH);

        JPanel notePanel = new JPanel(new BorderLayout(0, 6));
        notePanel.setOpaque(false);
        notePanel.add(Theme.heading("NOTE", 24f), BorderLayout.NORTH);
        JScrollPane ns = Theme.scroll(note);
        ns.setPreferredSize(new Dimension(300, 120));
        notePanel.add(ns, BorderLayout.CENTER);
        p.add(notePanel, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildMedical() {
        JPanel p = card(new BorderLayout(0, 10));
        JPanel title = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        title.setOpaque(false);
        title.add(Theme.heading("MEDICAL ALERT", 24f));
        title.add(Theme.heading("OPTIONAL", 18f));
        p.add(title, BorderLayout.NORTH);

        JPanel centre = new JPanel(new BorderLayout(0, 14));
        centre.setOpaque(false);
        JScrollPane ms = Theme.scroll(medical);
        ms.setPreferredSize(new Dimension(280, 200));
        centre.add(ms, BorderLayout.NORTH);

        JPanel lower = new JPanel();
        lower.setOpaque(false);
        lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));
        put(lower, Theme.caption("DENTIST"), 400);
        gap(lower, 3);
        put(lower, dentist, 220);
        gap(lower, 30);

        Theme.FlatButton book = new Theme.FlatButton("BOOK", Theme.GREEN, Color.WHITE);
        book.setPreferredSize(new Dimension(130, 38));
        book.addActionListener(e -> book());
        Theme.FlatButton clear = new Theme.FlatButton("CLEAR ALL", Theme.RED, Color.WHITE);
        clear.setPreferredSize(new Dimension(130, 38));
        clear.addActionListener(e -> clearAll());
        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 10));
        buttons.setOpaque(false);
        buttons.add(book);
        buttons.add(clear);
        buttons.setPreferredSize(new Dimension(130, 86));
        put(lower, buttons, 130);
        centre.add(lower, BorderLayout.CENTER);
        p.add(centre, BorderLayout.CENTER);
        return p;
    }

    private static final class ServiceRenderer implements ListCellRenderer<Store.Service> {
        private final JPanel row = new JPanel(new BorderLayout(8, 0));
        private final JLabel name = new JLabel();
        private final JLabel price = new JLabel();

        ServiceRenderer() {
            row.setBorder(new EmptyBorder(0, 6, 0, 8));
            name.setFont(Theme.font(Font.PLAIN, 13f));
            price.setFont(Theme.font(Font.PLAIN, 12f));
            row.add(name, BorderLayout.CENTER);
            row.add(price, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Store.Service> list, Store.Service s,
                                                      int index, boolean selected, boolean focus) {
            name.setText(s.name);
            price.setText(Util.money(s.price));
            row.setBackground(selected ? Theme.TEAL : Color.WHITE);
            name.setForeground(selected ? Color.WHITE : Color.BLACK);
            price.setForeground(selected ? Color.WHITE : new Color(80, 80, 80));
            return row;
        }
    }

    // ----------------------------------------------------------------- logic

    private void updateTotal() {
        int sum = 0;
        for (Store.Service s : services.getSelectedValuesList()) sum += s.price;
        total.setText("Total: " + Util.money(sum));
    }

    private void updateAge() {
        LocalDate b = birth.getDate();
        LocalDate today = LocalDate.now();
        if (b != null && !b.isAfter(today)) age.setText(String.valueOf(Util.ageOn(b, today)));
        else age.setText("");
    }

    private void fail(String msg, JComponent focus) {
        Dialogs.warn(this, msg);
        focus.requestFocusInWindow();
    }

    private void book() {
        String fn = first.getText().trim();
        String ln = last.getText().trim();
        if (!Util.validName(fn)) {
            fail("Enter a valid first name (letters only).", first);
            return;
        }
        if (!Util.validName(ln)) {
            fail("Enter a valid last name (letters only).", last);
            return;
        }
        if (gender.getSelectedIndex() == 0) {
            fail("Select the patient's gender.", gender);
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate b = birth.getDate();
        if (b == null || b.isAfter(today)) {
            fail("Enter a valid birthdate (MM/dd/yyyy) that is not in the future.", birth.getTextField());
            return;
        }
        int years = Util.ageOn(b, today);
        if (years > 120) {
            fail("Please check the birthdate - the age would be over 120.", birth.getTextField());
            return;
        }
        String phone = contact.getText().trim();
        if (!Util.validPhone(phone)) {
            fail("Enter a valid contact number (7 to 15 digits, e.g. 09171234567).", contact);
            return;
        }
        LocalDate d = apptDate.getDate();
        if (d == null) {
            fail("Enter a valid appointment date (MM/dd/yyyy).", apptDate.getTextField());
            return;
        }
        String ap = ampm.getSelectedIndex() == 0 ? null : (String) ampm.getSelectedItem();
        int minutes = Util.parseTime(time.getText(), ap);
        if (minutes < 0) {
            fail("Enter a valid appointment time such as 1:30 and choose AM or PM.", time);
            return;
        }
        List<Store.Service> chosen = services.getSelectedValuesList();
        if (chosen.isEmpty()) {
            fail("Select at least one service.", services);
            return;
        }
        if (dentist.getSelectedIndex() == 0) {
            fail("Select a dentist.", dentist);
            return;
        }
        String dentistName = (String) dentist.getSelectedItem();

        Appointment a = new Appointment();
        a.firstName = fn;
        a.lastName = ln;
        a.gender = (String) gender.getSelectedItem();
        a.age = years;
        a.birthDate = b;
        a.contact = phone;
        a.dentist = dentistName;
        for (Store.Service s : chosen) a.services.add(s.name);
        a.date = d;
        a.minutes = minutes;
        a.note = note.getText().trim();
        a.medical = medical.getText().trim();
        a.status = Appointment.PENDING;

        String problem = Store.validateSlot(d, minutes, dentistName, a.patientKey(), -1);
        if (problem != null) {
            Dialogs.warn(this, problem);
            return;
        }
        Store.add(a);
        Dialogs.persist(this);
        Dialogs.info(this, "Appointment booked!\n\n"
                + "Appointment ID: " + a.id + "\n"
                + "Patient: " + a.patientName() + "\n"
                + "Dentist: " + a.dentist + "\n"
                + "Schedule: " + a.dateText() + " at " + a.timeText() + "\n"
                + "Services: " + a.servicesText() + "\n"
                + "Total: " + Util.money(a.total()) + "\n\n"
                + "Status: Pending (waiting for the admin's approval)", "Booked");
        clearAll();
    }

    private void clearAll() {
        first.setText("");
        last.setText("");
        gender.setSelectedIndex(0);
        age.setText("");
        birth.clear();
        contact.setText("");
        apptDate.clear();
        time.setText("");
        ampm.setSelectedIndex(0);
        services.clearSelection();
        note.setText("");
        medical.setText("");
        dentist.setSelectedIndex(0);
        updateTotal();
        first.requestFocusInWindow();
    }
}
