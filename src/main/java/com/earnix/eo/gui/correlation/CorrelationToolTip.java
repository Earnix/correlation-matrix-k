package com.earnix.eo.gui.correlation;

import javax.swing.*;
import java.awt.*;

/**
 * @author Taras Maslov
 * 12/10/2018
 */
public class CorrelationToolTip extends JToolTip
{
	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(g);
	}
}
