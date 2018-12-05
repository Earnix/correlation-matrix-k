package com.earnix.eo.gui.correlation;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

/**
 * @author Taras Maslov
 * 11/26/2018
 */
public class TemperatureScalePanel extends JPanel {

    private static final int GRADIENT_WIDTH = 20;
    private static final int LABELS_COUNT = 10;
    private static final int LABELS_WIDTH = 20;
    private static final int LABELS_MARGIN = 5;
    private static final int FONT_SIZE = 12;
    
    private final CorrelationMatrix matrix;

    TemperatureScalePanel(CorrelationMatrix matrix) {
        this.matrix = matrix;
        setPreferredSize(new Dimension(getDefinedWidth(), 0));
        setBackground(Color.WHITE);
    }

    private int getDefinedWidth() {
        return GRADIENT_WIDTH + LABELS_WIDTH + LABELS_MARGIN * 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // drawing gradient rect
        Point2D.Double gradientEnd = new Point2D.Double();
        gradientEnd.setLocation(0, getHeight());
        float[] gradientFractions = new float[]{0, 0.5f, 1};
        Color[] gradientColors = new Color[]{matrix.getPositiveColor(), Color.WHITE, matrix.getNegativeColor()};
        Paint paint = new LinearGradientPaint(0, 0, 0, getHeight(), gradientFractions, gradientColors);
        g2d.setPaint(paint);
        g2d.fillRect(0, 0, GRADIENT_WIDTH, getHeight());

        // drawing labels
        g2d.setColor(Color.black);
        g2d.setFont(matrix.getFont().deriveFont((float) FONT_SIZE));
        double current = 1.0;
        float step = 2 / (float) LABELS_COUNT;
        int heightStep = getHeight() / LABELS_COUNT;
        for (int i = 0; i < LABELS_COUNT; i++) {
            g2d.drawString(String.format("%.1f", current), GRADIENT_WIDTH + LABELS_MARGIN, i * heightStep);
            current -= step;
        }

    }
}
