package com.earnix.eo.gui.correlation;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

/**
 * Informative temperature scale. Purpose - to improve visual understanding of correlation
 * by color of the cell in {@link CorrelationMatrixGrid}. Main parameters - {@link CorrelationMatrix#positiveColor} and
 * {@link CorrelationMatrix#negativeColor}. Supposed to be used only as part of {@link CorrelationMatrix}.
 */
class TemperatureScale extends JPanel
{
	static final int LABELS_MARGIN = 5;

	private final CorrelationMatrix matrix;

	private Font font;

	/**
	 * Creates new temperature scale for given {@link CorrelationMatrix}.
	 */
	TemperatureScale(CorrelationMatrix matrix)
	{
		this.matrix = matrix;
		setOpaque(false);
		font = matrix.getLabelsFont().deriveFont(matrix.getTemperatureScaleFontSize());
	}

	/**
	 * Paints temperature scale gradient (from positive to negative color) and labels, representing corresponding correlations.
	 * {@inheritDoc}
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(g);

		// painting gradient rect
		float gradientWidth = matrix.getTemperatureScaleGradientWidth();
		Point2D.Double gradientEnd = new Point2D.Double();
		gradientEnd.setLocation(0, getHeight());
		float[] gradientFractions = new float[] { 0, 0.5f, 1 };
		Color[] gradientColors = new Color[] { matrix.getPositiveColor(), Color.WHITE, matrix.getNegativeColor() };
		Paint paint = new LinearGradientPaint(0, 0, 0, getHeight(), gradientFractions, gradientColors);
		g2d.setPaint(paint);
		g2d.fillRect(0, 0, (int) gradientWidth, getHeight());

		// painting labels
		g2d.setColor(Color.black);
		g2d.setFont(font);
		double current = 1.0;
		int labelsCount = matrix.getTemperatureScaleLabelsCount();
		float step = 2 / (float) labelsCount;
		int heightStep = getHeight() / labelsCount;
		g2d.setColor(matrix.getLabelsColor());
		for (int i = 0; i < labelsCount; i++)
		{
			g2d.drawString(String.format("%.1f", current), gradientWidth + LABELS_MARGIN, i * heightStep);
			current -= step;
		}
	}

	/**
	 * Calculates preferred size for this temperature scale. It depends on label's width for font set in {@link CorrelationMatrix#temperatureScaleFontSize} and
	 * {@link CorrelationMatrix#temperatureScaleGradientWidth}. Component's preferred height is parent's height.
	 */
	@Override
	public Dimension getPreferredSize()
	{
		float labelsWidth = getFontMetrics(font).stringWidth("-0.0");
		int width = (int) Math.ceil(matrix.getTemperatureScaleGradientWidth() + labelsWidth + LABELS_MARGIN * 2);
		return new Dimension(width, matrix.getHeight() - matrix.getTemperatureScaleVerticalMargin() * 2);
	}
}
