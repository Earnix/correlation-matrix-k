package com.earnix.eo.gui.correlation;

/**
 * Represents pre-calculated correlation cell model.
 */
class Cell
{
	/**
	 * Horizontal location on grid.
	 */
	double x;

	/**
	 * Vertical location on grid.
	 */
	double y;

	/**
	 * Width and height.
	 */
	double size;

	/**
	 * Presentation mode. Square if compact, oval otherwise.
	 */
	boolean compact;

	/**
	 * Correlation value.
	 */
	double value;
}
