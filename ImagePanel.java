import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

/**
 * Shows a photo from images/&lt;file&gt; (cropped to fill). When the file is not there it paints a
 * clean tooth illustration instead, so the app always looks finished.
 */
final class ImagePanel extends JPanel {
    private final BufferedImage img;
    private final String caption;

    ImagePanel(String file, String caption) {
        this.img = load(file);
        this.caption = caption;
        setBackground(Color.WHITE);
    }

    private static BufferedImage load(String file) {
        try {
            File f = new File("images", file);
            if (f.isFile()) return ImageIO.read(f);
            URL u = ImagePanel.class.getResource("/images/" + file);
            if (u != null) return ImageIO.read(u);
        } catch (Exception ignored) {
            // use the drawn placeholder
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        if (img != null) {
            double s = Math.max(w / (double) img.getWidth(), h / (double) img.getHeight());
            int dw = (int) Math.ceil(img.getWidth() * s), dh = (int) Math.ceil(img.getHeight() * s);
            g.drawImage(img, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(244, 251, 252), 0, h, new Color(190, 226, 234)));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(255, 255, 255, 110));
            g.fillOval(-w / 3, h / 2, w * 4 / 3, w * 4 / 3);
            g.setColor(new Color(0, 153, 153, 28));
            g.fillOval(w / 2, -h / 6, w / 2 + 40, w / 2 + 40);

            double size = Math.min(w, h) * 0.42;
            double tx = (w - size) / 2, ty = h * 0.20;
            Theme.paintTooth(g, tx, ty, size, Color.WHITE, Theme.TEAL);

            g.setColor(Theme.TEAL_DARK);
            g.setFont(Theme.font(Font.BOLD | Font.ITALIC, Math.max(20f, Math.min(w, h) / 17f)));
            FontMetrics fm = g.getFontMetrics();
            int y = (int) (ty + size) + fm.getHeight() + 14;
            g.drawString(caption, (w - fm.stringWidth(caption)) / 2, y);

            g.setColor(new Color(0, 102, 102, 170));
            g.setFont(Theme.font(Font.PLAIN, 14f));
            fm = g.getFontMetrics();
            String sub = "Healthy smiles start here";
            g.drawString(sub, (w - fm.stringWidth(sub)) / 2, y + fm.getHeight() + 6);
        }
        g.dispose();
    }
}
