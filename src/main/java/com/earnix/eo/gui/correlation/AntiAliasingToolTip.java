package com.earnix.eo.gui.correlation;

import javax.swing.*;
import java.awt.*;

/**
 * Custom tooltip used in matrix grid component.
 * Purpose: to enable anti-aliasing.
 * <br/>
 *
 * @see com.earnix.eo.gui.correlation.CorrelationMatrixGrid#createToolTip()
 */
public class AntiAliasingToolTip extends JToolTip
{
	/**
	 * Enables anti-aliasing.
	 * {@inheritDoc}
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(g);
	}
}
