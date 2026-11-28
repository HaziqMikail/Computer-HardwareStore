package computerhardwarestore.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Loads product images from the classpath (bundled "images/...") or from a file path (admin uploads), scaled to fit
 * a box while keeping the aspect ratio. Missing or unreadable images become a "No image" placeholder.
 */
public final class ImageLoader {
    private static final Map<String, ImageIcon> CACHE = new HashMap<>();

    private ImageLoader() {}

    public static ImageIcon load(String path, int width, int height) {
        String key = path + "@" + width + "x" + height;
        ImageIcon icon = CACHE.get(key);
        if (icon == null) {
            icon = create(path, width, height);
            CACHE.put(key, icon);
        }
        return icon;
    }

    private static ImageIcon create(String path, int width, int height) {
        BufferedImage source = read(path);
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (source == null) {
                g.setColor(new Color(235, 235, 235));
                g.fillRect(0, 0, width, height);
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                FontMetrics fm = g.getFontMetrics();
                String text = "No image";
                g.drawString(text, (width - fm.stringWidth(text)) / 2, height / 2 + fm.getAscent() / 2);
            } else {
                double scale = Math.min((double) width / source.getWidth(), (double) height / source.getHeight());
                int w = Math.max(1, (int) Math.round(source.getWidth() * scale));
                int h = Math.max(1, (int) Math.round(source.getHeight() * scale));
                g.drawImage(source, (width - w) / 2, (height - h) / 2, w, h, null);
            }
        } finally {
            g.dispose();
        }
        return new ImageIcon(out);
    }

    private static BufferedImage read(String path) {
        if (path == null || path.isBlank()) return null;
        try {
            URL url = ImageLoader.class.getClassLoader().getResource(path);
            if (url != null) return ImageIO.read(url);
            File file = new File(path);
            return file.isFile() ? ImageIO.read(file) : null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
