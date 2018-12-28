package com.earnix.eo.gui.correlation;

import javax.swing.BorderFactory;
import javax.swing.JToolTip;
import javax.swing.border.CompoundBorder;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Custom tooltip used in matrix grid component. Uses presentational properties, stored in {@link CorrelationMatrix}.
 * Renders with anti-aliasing.
 *
 * @see com.earnix.eo.gui.correlation.CorrelationMatrixGrid#createToolTip()
 */
class GridToolTip extends JToolTip
{

	/**
	 * Creates new tooltip for correlation matrix.
	 *
	 * @param matrix {@link CorrelationMatrix} to get properties from.
	 */
	GridToolTip(CorrelationMatrix matrix)
	{
		setFont(matrix.labelsFont.deriveFont(matrix.tooltipFontSize));
		setForeground(matrix.toolTipTextColor);
		setBackground(matrix.toolTipBackgroundColor);
		int padding = matrix.tooltipPadding;
		CompoundBorder border = new CompoundBorder(
				BorderFactory.createLineBorder(matrix.toolTipBorderColor, matrix.toolTipBorderWidth),
				BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		setBorder(border);
	}

	/**
	 * Enables anti-aliasing.
	 * {@inheritDoc}
	 */
	@SuppressWarnings("Duplicates")
	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(g);
	}
}
