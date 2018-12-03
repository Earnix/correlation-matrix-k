package com.earnix.eo.gui.correlation;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * @author Taras Maslov
 * 11/26/2018
 */
public class CorrelationMatrixLegend extends JComponent {


    private final CorrelationMatrix matrix;

    CorrelationMatrixLegend(CorrelationMatrix matrix) {
        this.matrix = matrix;
        setPreferredSize(new Dimension(40, 0));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setPaint(new GradientPaint(0, 0, matrix.getColor1(), getWidth(), getHeight(), matrix.getColor2()));

        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
