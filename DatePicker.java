import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/** Small modal calendar. Days outside [min, max] are disabled. */
final class DatePicker extends JDialog {
    private static final String[] DAYS = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};

    private final LocalDate min, max;
    private final LocalDate today = LocalDate.now();
    private final LocalDate selected;
    private LocalDate result;
    private int year, month;
    private boolean adjusting;

    private final JComboBox<String> monthBox = new JComboBox<>();
    private final JSpinner yearSpin;
    private final JPanel grid = new JPanel(new GridLayout(0, 7, 3, 3));
    private final int minYear, maxYear;

    /** Returns the chosen date, or null when cancelled. */
    static LocalDate choose(Component parent, LocalDate initial, LocalDate min, LocalDate max) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        DatePicker p = new DatePicker(owner, initial, min, max);
        p.setLocationRelativeTo(parent);
        p.setVisible(true);
        return p.result;
    }

    private DatePicker(Window owner, LocalDate initial, LocalDate min, LocalDate max) {
        super(owner, "Select date", ModalityType.APPLICATION_MODAL);
        this.min = min;
        this.max = max;
        this.selected = initial;
        this.minYear = min != null ? min.getYear() : 1900;
        this.maxYear = max != null ? max.getYear() : 2100;

        LocalDate view = initial != null ? initial : today;
        if (min != null && view.isBefore(min)) view = min;
        if (max != null && view.isAfter(max)) view = max;
        year = Math.max(minYear, Math.min(maxYear, view.getYear()));
        month = view.getMonthValue();

        for (int m = 1; m <= 12; m++) monthBox.addItem(Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        yearSpin = new JSpinner(new SpinnerNumberModel(year, minYear, maxYear, 1));
        yearSpin.setEditor(new JSpinner.NumberEditor(yearSpin, "#"));

        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        prev.addActionListener(e -> shift(-1));
        next.addActionListener(e -> shift(1));
        monthBox.addActionListener(e -> {
            if (adjusting) return;
            month = monthBox.getSelectedIndex() + 1;
            rebuild();
        });
        yearSpin.addChangeListener(e -> {
            if (adjusting) return;
            year = (Integer) yearSpin.getValue();
            rebuild();
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        top.add(prev);
        top.add(monthBox);
        top.add(yearSpin);
        top.add(next);

        grid.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 10));
        grid.setPreferredSize(new Dimension(300, 230));

        JButton todayBtn = new JButton("Today");
        todayBtn.setEnabled(inRange(today));
        todayBtn.addActionListener(e -> { result = today; dispose(); });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        bottom.add(todayBtn);
        bottom.add(cancel);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        sync();
        rebuild();
        setResizable(false);
        pack();
    }

    private boolean inRange(LocalDate d) {
        return (min == null || !d.isBefore(min)) && (max == null || !d.isAfter(max));
    }

    private void sync() {
        adjusting = true;
        monthBox.setSelectedIndex(month - 1);
        yearSpin.setValue(year);
        adjusting = false;
    }

    private void shift(int delta) {
        LocalDate d = LocalDate.of(year, month, 1).plusMonths(delta);
        if (d.getYear() < minYear || d.getYear() > maxYear) return;
        year = d.getYear();
        month = d.getMonthValue();
        sync();
        rebuild();
    }

    private void rebuild() {
        grid.removeAll();
        for (String d : DAYS) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(Theme.font(Font.BOLD, 12f));
            l.setForeground(Theme.TEAL_DARK);
            grid.add(l);
        }
        YearMonth ym = YearMonth.of(year, month);
        int lead = ym.atDay(1).getDayOfWeek().getValue() % 7;   // Sunday = 0
        int cells = 0;
        for (int i = 0; i < lead; i++, cells++) grid.add(new JLabel());
        for (int day = 1; day <= ym.lengthOfMonth(); day++, cells++) {
            LocalDate d = ym.atDay(day);
            JButton b = new JButton(String.valueOf(day));
            b.setMargin(new Insets(2, 2, 2, 2));
            b.setFocusPainted(false);
            b.setFont(Theme.font(Font.PLAIN, 13f));
            b.setEnabled(inRange(d));
            if (d.equals(selected)) {
                b.setBackground(Theme.TEAL);
                b.setForeground(Color.WHITE);
            } else if (d.equals(today)) {
                b.setBorder(new LineBorder(Theme.TEAL, 2));
            }
            b.addActionListener(e -> { result = d; dispose(); });
            grid.add(b);
        }
        for (; cells < 42; cells++) grid.add(new JLabel());
        grid.revalidate();
        grid.repaint();
    }
}
