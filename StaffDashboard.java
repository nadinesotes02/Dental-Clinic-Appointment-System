import javax.swing.*;
import java.awt.*;

/** Staff home: book an appointment or look at the list. */
final class StaffDashboard extends BaseFrame {

    StaffDashboard() {
        super("Staff", null, 1180, 720);

        JPanel side = new JPanel();
        side.setBackground(Theme.TEAL);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(26, 32, 26, 32));
        side.setPreferredSize(new Dimension(310, 100));

        JLabel title = Theme.heading("STAFF DASHBOARD", 26f);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(title);
        side.add(Box.createVerticalStrut(46));
        side.add(sideButton("Appointment", Theme.TEAL_DARK, () -> open(new AppointmentBoard(this))));
        side.add(Box.createVerticalStrut(18));
        side.add(sideButton("View List", Theme.TEAL_DARK, () -> open(new AppointmentListFrame(this))));
        side.add(Box.createVerticalStrut(18));
        side.add(sideButton("Logout", Theme.LIGHT_BTN, this::logout, Theme.TEAL_DARK));

        JPanel photo = new JPanel(new BorderLayout());
        photo.setBackground(Theme.TEAL);
        photo.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 14));
        photo.add(new ImagePanel("staff.jpg", "Welcome to the clinic"), BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.TEAL);
        root.add(side, BorderLayout.WEST);
        root.add(photo, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JButton sideButton(String text, Color bg, Runnable r) {
        return sideButton(text, bg, r, Color.WHITE);
    }

    private JButton sideButton(String text, Color bg, Runnable r, Color fg) {
        Theme.FlatButton b = new Theme.FlatButton(text, bg, fg);
        b.setFont(Theme.font(Font.BOLD, 16f));
        b.setPreferredSize(new Dimension(240, 44));
        b.setMaximumSize(new Dimension(240, 44));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> r.run());
        return b;
    }

    @Override
    protected void closeRequested() {
        logout();
    }
}
