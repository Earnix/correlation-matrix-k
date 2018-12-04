package com.earnix.eo.gui.correlation;

import lombok.val;
import lombok.var;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;

/**
 * @author Taras Maslov
 * 11/26/2018
 */
public class CorrelationMatrixLegend extends JComponent {

    private final int GRADIENT_WIDTH = 20;
    private final int LABELS_COUNT = 10;
    private final int LABELS_WIDTH = 20;
    private final int LABELS_MARGIN = 5;
    
    private final CorrelationMatrix matrix;

    CorrelationMatrixLegend(CorrelationMatrix matrix) {
        this.matrix = matrix;
        setPreferredSize(new Dimension(getDefinedWidth(), 0));
    }

    private int getDefinedWidth() {
        return GRADIENT_WIDTH + LABELS_WIDTH + LABELS_MARGIN * 2;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        val gradientEnd = new Point2D.Double();
        gradientEnd.setLocation(0, getHeight());
        val paint = new LinearGradientPaint(0, 0, 0, getHeight(), new float[]{0, 0.5f, 1},
                new Color[]{matrix.getColor1(), Color.WHITE, matrix.getColor2()});
        g2d.setPaint(paint);

        g2d.fillRect(0, 0, GRADIENT_WIDTH, getHeight());

        g2d.setColor(Color.black);
        g2d.setFont(matrix.getFont().deriveFont(12f));

        var current = 1.0;
        val step = 2 / (float) LABELS_COUNT;
        val heightStep = getHeight() / LABELS_COUNT;
        for (int i = 0; i < LABELS_COUNT; i++) {
            g2d.drawString(String.format("%.2f", current), GRADIENT_WIDTH + LABELS_MARGIN, i * heightStep);
            current -= step;
        }
        
    }
}
