package com.thapar.ims.ui;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Text field with modern styling, placeholder support,
 * focus accent border, and fixed height to prevent stretching.
 */
public class PlaceholderTextField extends JTextField {

    private String placeholder = "";
    private boolean isFocused = false;

    private static final Color BORDER_NORMAL = new Color(203, 213, 225);
    private static final Color BORDER_FOCUS = new Color(5, 150, 105);
    private static final Color TEXT_COLOR = new Color(15, 23, 42);
    private static final Color PLACEHOLDER_COLOR = new Color(148, 163, 184);

    public PlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;
        setFont(new Font("SansSerif", Font.PLAIN, 13));
        setForeground(TEXT_COLOR);
        setBackground(Color.WHITE);
        setCaretColor(TEXT_COLOR);

        updateBorder();

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                updateBorder();
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                updateBorder();
                repaint();
            }
        });
    }

    public PlaceholderTextField() {
        this("");
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    private void updateBorder() {
        Color borderCol = isFocused ? BORDER_FOCUS : BORDER_NORMAL;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderCol, isFocused ? 2 : 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, 36);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 36);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(60, 36);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getText().isEmpty() && !placeholder.isEmpty() && !isFocused) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(PLACEHOLDER_COLOR);
            g2.setFont(getFont());
            int paddingLeft = getInsets().left;
            int textY = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(placeholder, paddingLeft, textY);
            g2.dispose();
        }
    }
}
