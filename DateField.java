import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/** Text box (MM/dd/yyyy) with a calendar button beside it. */
final class DateField extends JPanel {
    private final JTextField text = Theme.field(10);
    private final LocalDate min, max;

    DateField(LocalDate min, LocalDate max) {
        super(new BorderLayout(4, 0));
        this.min = min;
        this.max = max;
        setOpaque(false);
        text.setToolTipText("MM/dd/yyyy");

        JButton pick = new JButton(new CalendarIcon());
        pick.setFocusPainted(false);
        pick.setMargin(new Insets(2, 4, 2, 4));
        pick.setToolTipText("Choose from calendar");
        pick.addActionListener(e -> {
            LocalDate d = DatePicker.choose(this, getDate(), min, max);
            if (d != null) setDate(d);
        });
        add(text, BorderLayout.CENTER);
        add(pick, BorderLayout.EAST);
    }

    /** The date typed or chosen, or null when the text is not a valid MM/dd/yyyy date. */
    LocalDate getDate() {
        return Util.parseDate(text.getText());
    }

    void setDate(LocalDate d) {
        text.setText(Util.fmtDate(d));
    }

    void clear() {
        text.setText("");
    }

    JTextField getTextField() {
        return text;
    }

    void addChangeListener(Runnable r) {
        Theme.onChange(text, r);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Component c : getComponents()) c.setEnabled(enabled);
    }

    private static final class CalendarIcon implements Icon {
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }

        @Override
        public void paintIcon(Component c, Graphics g0, int x, int y) {
            Graphics2D g = (Graphics2D) g0.create();
            g.translate(x, y);
            g.setColor(Color.WHITE);
            g.fillRect(1, 3, 14, 12);
            g.setColor(Theme.TEAL_DARK);
            g.drawRect(1, 3, 13, 11);
            g.fillRect(1, 3, 14, 4);
            g.fillRect(4, 1, 2, 3);
            g.fillRect(10, 1, 2, 3);
            g.setColor(new Color(90, 90, 90));
            for (int r = 0; r < 2; r++) {
                for (int col = 0; col < 3; col++) g.fillRect(3 + col * 4, 8 + r * 3, 2, 2);
            }
            g.dispose();
        }
    }
}
