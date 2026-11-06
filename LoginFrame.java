import javax.swing.*;
import java.awt.*;

/** Login screen: image on the left, name / password on the right. */
final class LoginFrame extends BaseFrame {
    private final JTextField nameField = Theme.field(20);
    private final JPasswordField passField = new JPasswordField(20);
    private final JLabel error = new JLabel(" ");

    LoginFrame() {
        super("Dental Clinic Appointment System - Login", null, 1100, 680);
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(new ImagePanel("login.jpg", "DENTAL CLINIC"));
        root.add(buildForm());
        setContentPane(root);
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.PAPER);

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel t1 = new JLabel("DENTAL CLINIC");
        t1.setFont(Theme.font(Font.ITALIC, 44f));
        t1.setForeground(Theme.INK);
        t1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t2 = new JLabel("APPOINTMENT SYSTEM");
        t2.setFont(Theme.font(Font.ITALIC, 28f));
        t2.setForeground(Theme.INK);
        t2.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 0));
        t2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator line = new JSeparator();
        line.setForeground(new Color(110, 130, 150));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 0));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        passField.setFont(Theme.font(Font.PLAIN, 14f));
        passField.setBorder(nameField.getBorder());
        char defaultEcho = passField.getEchoChar();

        JCheckBox show = new JCheckBox("Show password");
        show.setOpaque(false);
        show.setFont(Theme.font(Font.PLAIN, 12f));
        show.addActionListener(e -> passField.setEchoChar(show.isSelected() ? (char) 0 : defaultEcho));

        error.setForeground(Theme.RED);
        error.setFont(Theme.font(Font.BOLD, 12f));

        Theme.FlatButton login = new Theme.FlatButton("LOGIN", Theme.INDIGO, Color.WHITE);
        login.setFont(Theme.font(Font.BOLD, 16f));
        login.setPreferredSize(new Dimension(170, 50));
        login.setMaximumSize(new Dimension(170, 50));
        login.addActionListener(e -> doLogin());

        addRow(form, new JLabel("NAME"), 12);
        addRow(form, nameField, 280);
        form.add(Box.createVerticalStrut(18));
        addRow(form, new JLabel("PASSWORD"), 12);
        addRow(form, passField, 280);
        form.add(Box.createVerticalStrut(6));
        addRow(form, show, 280);
        form.add(Box.createVerticalStrut(4));
        addRow(form, error, 320);
        form.add(Box.createVerticalStrut(14));
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(login);

        box.add(t1);
        box.add(t2);
        box.add(Box.createVerticalStrut(10));
        box.add(line);
        box.add(Box.createVerticalStrut(34));
        box.add(form);
        box.setPreferredSize(new Dimension(480, 440));
        outer.add(box);

        getRootPane().setDefaultButton(login);
        return outer;
    }

    private static void addRow(JPanel p, JComponent c, int maxWidth) {
        if (c instanceof JLabel && !(c.getFont().isItalic())) {
            c.setFont(Theme.font(Font.PLAIN, 15f));
        }
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(maxWidth < 100 ? 400 : maxWidth, c.getPreferredSize().height));
        p.add(c);
        p.add(Box.createVerticalStrut(4));
    }

    private void doLogin() {
        String user = nameField.getText().trim();
        String pass = new String(passField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            showError("Enter your name and password.");
            return;
        }
        Store.User u = Store.authenticate(user, pass);
        if (u == null) {
            showError("Invalid name or password.");
            passField.setText("");
            passField.requestFocusInWindow();
            return;
        }
        BaseFrame next;
        switch (u.role) {
            case ADMIN:
                next = new AdminDashboard();
                break;
            case STAFF:
                next = new StaffDashboard();
                break;
            default:
                next = new DentistDashboard(u.dentist);
                break;
        }
        open(next);
        dispose();
    }

    private void showError(String msg) {
        error.setText(msg);
    }
}
