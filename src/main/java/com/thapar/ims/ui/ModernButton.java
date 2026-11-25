package com.thapar.ims.ui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern flat button with rounded corners, hover/press animations,
 * cross-platform color rendering (fixes macOS Aqua button bugs),
 * and optional count badge.
 */
public class ModernButton extends JButton {

    public enum Variant {
        PRIMARY,    // Deep Green #166534
        ACCENT,     // Emerald #059669
        SECONDARY,  // White / Slate Outline
        WARNING,    // Amber #B45309
        DANGER      // Red #DC2626
    }

    private final Variant variant;
    private boolean hovered = false;
    private boolean pressed = false;
    private String countBadge = null;
    private boolean active = false;

    public ModernButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        setFont(new Font("SansSerif", Font.BOLD, 12));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    hovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    pressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    public void setCountBadge(Integer count) {
        this.countBadge = (count != null && count >= 0) ? String.valueOf(count) : null;
        repaint();
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        int extraWidth = (countBadge != null) ? 36 : 0;
        return new Dimension(Math.max(d.width + 24 + extraWidth, 80), 36);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 8;

        Color bg;
        Color fg;
        Color border;

        if (!isEnabled()) {
            bg = new Color(241, 245, 249);
            fg = new Color(148, 163, 184);
            border = new Color(226, 232, 240);
        } else {
            switch (variant) {
                case PRIMARY:
                    if (pressed) bg = new Color(20, 83, 45);
                    else if (hovered) bg = new Color(22, 101, 52).brighter();
                    else bg = new Color(22, 101, 52);
                    fg = Color.WHITE;
                    border = bg;
                    break;
                case ACCENT:
                    if (pressed) bg = new Color(4, 120, 87);
                    else if (hovered) bg = new Color(5, 150, 105).brighter();
                    else bg = new Color(5, 150, 105);
                    fg = Color.WHITE;
                    border = bg;
                    break;
                case WARNING:
                    if (active) {
                        bg = new Color(180, 83, 9);
                        fg = Color.WHITE;
                        border = bg;
                    } else {
                        bg = hovered ? new Color(254, 243, 199) : new Color(255, 251, 235);
                        fg = new Color(180, 83, 9);
                        border = new Color(245, 158, 11);
                    }
                    break;
                case DANGER:
                    if (pressed) bg = new Color(185, 28, 28);
                    else if (hovered) bg = new Color(254, 226, 226);
                    else bg = Color.WHITE;
                    fg = new Color(220, 38, 38);
                    border = new Color(239, 68, 68);
                    break;
                case SECONDARY:
                default:
                    if (active) {
                        bg = new Color(22, 101, 52);
                        fg = Color.WHITE;
                        border = bg;
                    } else {
                        bg = hovered ? new Color(241, 245, 249) : Color.WHITE;
                        fg = new Color(30, 41, 59);
                        border = new Color(203, 213, 225);
                    }
                    break;
            }
        }

        // Fill background
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // Draw border
        g2.setColor(border);
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        // Paint text & badge
        FontMetrics fm = g2.getFontMetrics(getFont());
        String text = getText();
        int textWidth = fm.stringWidth(text);
        int totalContentWidth = textWidth;

        int badgeWidth = 0;
        int badgeHeight = 18;
        if (countBadge != null) {
            FontMetrics bfm = g2.getFontMetrics(new Font("SansSerif", Font.BOLD, 10));
            badgeWidth = Math.max(18, bfm.stringWidth(countBadge) + 10);
            totalContentWidth += 8 + badgeWidth;
        }

        int startX = (w - totalContentWidth) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();

        g2.setFont(getFont());
        g2.setColor(fg);
        g2.drawString(text, startX, textY);

        if (countBadge != null) {
            int badgeX = startX + textWidth + 8;
            int badgeY = (h - badgeHeight) / 2;

            Color badgeBg = (variant == Variant.PRIMARY || active) ? new Color(255, 255, 255, 60) : new Color(226, 232, 240);
            Color badgeFg = (variant == Variant.PRIMARY || active) ? Color.WHITE : new Color(51, 65, 85);

            if (variant == Variant.WARNING && !active) {
                badgeBg = new Color(180, 83, 9);
                badgeFg = Color.WHITE;
            }

            g2.setColor(badgeBg);
            g2.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 10, 10);

            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics bfm = g2.getFontMetrics();
            int bTextX = badgeX + (badgeWidth - bfm.stringWidth(countBadge)) / 2;
            int bTextY = badgeY + (badgeHeight - bfm.getHeight()) / 2 + bfm.getAscent();
            g2.setColor(badgeFg);
            g2.drawString(countBadge, bTextX, bTextY);
        }

        g2.dispose();
    }
}
