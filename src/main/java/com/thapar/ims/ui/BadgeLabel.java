package com.thapar.ims.ui;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Rounded pill badge for category tags, status tags, and modes.
 */
public class BadgeLabel extends JLabel {

    private Color badgeBackground = new Color(241, 245, 249);
    private Color badgeForeground = new Color(51, 65, 85);
    private Color badgeBorder = new Color(226, 232, 240);

    public BadgeLabel(String text) {
        super(text);
        setFont(new Font("SansSerif", Font.BOLD, 11));
        setHorizontalAlignment(CENTER);
        setOpaque(false);
    }

    public BadgeLabel(String text, Color bg, Color fg, Color border) {
        this(text);
        setBadgeColors(bg, fg, border);
    }

    public void setBadgeColors(Color bg, Color fg, Color border) {
        this.badgeBackground = bg;
        this.badgeForeground = fg;
        this.badgeBorder = border;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.stringWidth(getText()) + 18;
        int h = 24;
        return new Dimension(w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background
        g2.setColor(badgeBackground);
        g2.fillRoundRect(0, 0, w, h, h, h);

        // Border if any
        if (badgeBorder != null) {
            g2.setColor(badgeBorder);
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
        }

        // Text
        g2.setColor(badgeForeground);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (w - fm.stringWidth(getText())) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
