package gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Shows the Radiant Glow logo picture.
 * The pictures are saved in the "images" folder 
 */
// [INHERITANCE] LogoPanel extends JPanel
public class LogoPanel extends JPanel {
    private Image image;
    public LogoPanel(String fileName, int width, int height) {   // used in the login form and the dashboard header
        image = loadImage(fileName);
        if (image != null) {
            // image smoothing: resize the picture once, the smooth way (SCALE_SMOOTH)
            image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMaximumSize(size);       // the panel never grows, so the picture is never stretched
        setOpaque(false);
    }

    // [POLYMORPHISM - OVERRIDING] we replace JPanel's paintComponent() to draw our picture
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);     // [INHERITANCE] run JPanel's version first
        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }

    // Loads a picture from the images folder. Returns null if the file is missing.
    public static Image loadImage(String fileName) {
        if (LogoPanel.class.getResource("/images/" + fileName) == null) {
            System.out.println("Picture not found: " + fileName);
            return null;
        }
        ImageIcon icon = new ImageIcon(LogoPanel.class.getResource("/images/" + fileName));
        return icon.getImage();
    }
}
