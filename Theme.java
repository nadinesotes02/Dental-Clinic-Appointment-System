import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Colours, fonts and small styled-component factories shared by every screen. */
final class Theme {
    private Theme() {}

    static final Color TEAL = new Color(0, 153, 153);
    static final Color TEAL_DARK = new Color(0, 102, 102);
    static final Color CYAN = new Color(0, 204, 204);
    static final Color GREEN = new Color(0, 180, 0);
    static final Color RED = new Color(220, 0, 0);
    static final Color BLUE = new Color(0, 40, 200);
    static final Color INDIGO = new Color(51, 51, 255);
    static final Color BAR_BLUE = new Color(64, 140, 255);
    static final Color BAR_CYAN = new Color(0, 204, 255);
    static final Color PAPER = new Color(238, 238, 238);
    static final Color LIGHT_BTN = new Color(226, 236, 241);
    static final Color INK = new Color(40, 40, 40);
    private static final Color BORDER = new Color(120, 130, 135);

    static final String FAMILY = pickFamily();

    private static String pickFamily() {
        try {
            Set<String> have = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            for (String w : new String[]{"Segoe UI", "Calibri", "Helvetica Neue", "Arial"}) {
                if (have.contains(w)) return w;
            }
        } catch (Throwable ignored) {
            // fall through
        }
        return Font.SANS_SERIF;
    }

    static Font font(int style, float size) {
        return new Font(FAMILY, style, 12).deriveFont(size);
    }

    // ------------------------------------------------------------------ labels

    static JLabel heading(String text, float size) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(font(Font.ITALIC, size));
        return l;
    }

    static JLabel caption(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(font(Font.PLAIN, 12f));
        return l;
    }

    // ----------------------------------------------------------------- buttons

    static FlatButton button(String text, Color bg) {
        return new FlatButton(text, bg, Color.WHITE);
    }

    /** Flat, colour-filled button (Swing's default look ignores button colours on some systems). */
    static final class FlatButton extends JButton {
        private final Color base;
        private boolean hover;

        FlatButton(String text, Color bg, Color fg) {
            super(text);
            this.base = bg;
            setForeground(fg);
            setFont(font(Font.BOLD, 13f));
            setContentAreaFilled(false);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setMargin(new Insets(4, 12, 4, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Color c;
            if (!isEnabled()) c = new Color(190, 195, 198);
            else if (getModel().isPressed()) c = base.darker();
            else if (hover) c = lighten(base);
            else c = base;
            g2.setColor(c);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (isFocusOwner() && isEnabled()) {
                g2.setColor(new Color(255, 255, 255, 190));
                g2.drawRect(2, 2, getWidth() - 5, getHeight() - 5);
            }
            g2.dispose();
            super.paintComponent(g);
        }

        private static Color lighten(Color c) {
            return new Color(Math.min(255, c.getRed() + 28), Math.min(255, c.getGreen() + 28), Math.min(255, c.getBlue() + 28));
        }
    }

    // ------------------------------------------------------------ input fields

    static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(font(Font.PLAIN, 14f));
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER), BorderFactory.createEmptyBorder(5, 6, 5, 6)));
        return f;
    }

    static JTextArea area(boolean editable) {
        JTextArea a = new JTextArea();
        a.setEditable(editable);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBackground(Color.WHITE);
        a.setFont(font(Font.PLAIN, 13f));
        a.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return a;
    }

    static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    static void onChange(JTextComponent c, Runnable r) {
        c.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { r.run(); }
            @Override public void removeUpdate(DocumentEvent e) { r.run(); }
            @Override public void changedUpdate(DocumentEvent e) { r.run(); }
        });
    }

    // ------------------------------------------------------------------ tables

    static void styleTable(JTable t) {
        t.setRowHeight(26);
        t.setShowGrid(true);
        t.setGridColor(new Color(200, 210, 215));
        t.setFont(font(Font.PLAIN, 13f));
        t.setSelectionBackground(new Color(184, 207, 229));
        t.setSelectionForeground(Color.BLACK);
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setFont(font(Font.BOLD, 13f));
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableRenderer r = new TableRenderer();
        t.setDefaultRenderer(Object.class, r);
        t.setDefaultRenderer(Integer.class, r);
        t.setDefaultRenderer(String.class, r);
    }

    private static final class TableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(table, value, sel, false, row, col);
            setHorizontalAlignment(LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            setToolTipText(value == null ? null : value.toString());
            if (!sel) {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(243, 249, 249));
                setForeground(Color.BLACK);
            }
            if ("Status".equals(table.getColumnName(col)) && value != null) {
                setFont(getFont().deriveFont(Font.BOLD));
                if (!sel) setForeground(statusColor(value.toString()));
            } else {
                setFont(table.getFont());
            }
            return this;
        }
    }

    static Color statusColor(String status) {
        if (Appointment.APPROVED.equals(status)) return new Color(0, 130, 40);
        if (Appointment.PENDING.equals(status)) return new Color(200, 110, 0);
        if (Appointment.CANCELLED.equals(status)) return new Color(190, 0, 0);
        if (Appointment.DONE.equals(status)) return new Color(0, 60, 170);
        return Color.BLACK;
    }

    // ------------------------------------------------------------------- art

    private static Path2D toothPath(double s) {
        Path2D p = new Path2D.Double();
        p.moveTo(.50 * s, .22 * s);
        p.curveTo(.62 * s, .10 * s, .90 * s, .12 * s, .88 * s, .38 * s);
        p.curveTo(.87 * s, .55 * s, .78 * s, .62 * s, .76 * s, .80 * s);
        p.curveTo(.74 * s, .95 * s, .62 * s, .95 * s, .60 * s, .80 * s);
        p.curveTo(.58 * s, .68 * s, .54 * s, .64 * s, .50 * s, .64 * s);
        p.curveTo(.46 * s, .64 * s, .42 * s, .68 * s, .40 * s, .80 * s);
        p.curveTo(.38 * s, .95 * s, .26 * s, .95 * s, .24 * s, .80 * s);
        p.curveTo(.22 * s, .62 * s, .13 * s, .55 * s, .12 * s, .38 * s);
        p.curveTo(.10 * s, .12 * s, .38 * s, .10 * s, .50 * s, .22 * s);
        p.closePath();
        return p;
    }

    static void paintTooth(Graphics2D g, double x, double y, double size, Color fill, Color outline) {
        AffineTransform oldT = g.getTransform();
        Stroke oldS = g.getStroke();
        g.translate(x, y);
        Path2D p = toothPath(size);
        g.setColor(fill);
        g.fill(p);
        g.setColor(outline);
        g.setStroke(new BasicStroke((float) Math.max(1.5, size * 0.035), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(p);
        g.setStroke(oldS);
        g.setTransform(oldT);
    }

    static Image toothIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(TEAL);
        g.fillRoundRect(0, 0, size, size, size / 4, size / 4);
        paintTooth(g, size * 0.1, size * 0.1, size * 0.8, Color.WHITE, TEAL_DARK);
        g.dispose();
        return img;
    }
}
