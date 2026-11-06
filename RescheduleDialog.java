import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/** "Reschedule appointment" dialog. The same dialog (with a dentist box) is used for Rebook. */
final class RescheduleDialog extends JDialog {
    private final Appointment appt;
    private final boolean rebook;
    private final DateField dateField =
            new DateField(LocalDate.now(), LocalDate.now().plusYears(Store.MAX_YEARS_AHEAD));
    private final JTextField timeField = Theme.field(10);
    private final JComboBox<String> dentistBox = new JComboBox<>(Store.DENTISTS);
    private Appointment result;

    /** Moves the appointment to a new date / time. Returns true when it changed. */
    static boolean reschedule(Component parent, Appointment a) {
        RescheduleDialog d = new RescheduleDialog(parent, a, false);
        d.setVisible(true);
        return d.result != null;
    }

    /** Creates a new Pending copy of an archived appointment. Returns it, or null when cancelled. */
    static Appointment rebook(Component parent, Appointment a) {
        RescheduleDialog d = new RescheduleDialog(parent, a, true);
        d.setVisible(true);
        return d.result;
    }

    private RescheduleDialog(Component parent, Appointment a, boolean rebook) {
        super(parent == null ? null : SwingUtilities.getWindowAncestor(parent),
                rebook ? "Rebook appointment" : "Reschedule appointment", ModalityType.APPLICATION_MODAL);
        this.appt = a;
        this.rebook = rebook;

        dentistBox.setSelectedItem(a.dentist);
        timeField.setText(a.timeText());
        if (!rebook) dateField.setDate(a.date);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 18, 6, 18));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 0, 5, 18);
        int row = 0;
        row = addRow(form, c, row, "Patient:", bold(a.patientName()));
        row = addRow(form, c, row, "Dentist:", rebook ? dentistBox : bold(a.dentist));
        row = addRow(form, c, row, "Current schedule:", bold(a.dateText() + "  " + a.timeText()));
        row = addRow(form, c, row, rebook ? "New date (MM/dd/yyyy):" : "New date (MM/dd/yyyy):", dateField);
        addRow(form, c, row, "New time (" + Store.hoursText() + "):", timeField);

        JButton ok = new JButton("OK");
        ok.addActionListener(e -> submit());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(ok);
        buttons.add(cancel);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private static JLabel bold(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }

    private static int addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridy = row;
        c.gridx = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        p.add(bold(label), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        p.add(field, c);
        return row + 1;
    }

    private void submit() {
        LocalDate d = dateField.getDate();
        int minutes = Util.parseTime(timeField.getText(), null);
        String dentist = rebook ? (String) dentistBox.getSelectedItem() : appt.dentist;
        String problem = Store.validateSlot(d, minutes, dentist, appt.patientKey(), rebook ? -1 : appt.id);
        if (problem != null) {
            Dialogs.warn(this, problem);
            return;
        }
        if (!rebook && d.equals(appt.date) && minutes == appt.minutes) {
            Dialogs.info(this, "The new schedule is the same as the current one.", "Reschedule");
            return;
        }
        if (rebook) {
            result = Store.add(appt.copyFor(dentist, d, minutes));
        } else {
            appt.date = d;
            appt.minutes = minutes;
            result = appt;
        }
        dispose();
    }
}
