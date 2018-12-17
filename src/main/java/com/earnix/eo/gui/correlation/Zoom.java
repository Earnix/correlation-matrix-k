package com.earnix.eo.gui.correlation;

import java.awt.*;
import java.util.List;

/**
 * Represents correlation matrix zoom model with pre-calculated rendering properties like coordinates and sizes.
 */
class Zoom
{
	/**
	 * Margins of zoom grid title labels.
	 */
	double labelsMargin;

	/**
	 * Width and height of zoom grid in cells.
	 */
	int length;

	/**
	 * Horizontal index of cell which initiated zoom.
	 */
	int i;

	/**
	 * Vertical index of cell which initiated zoom.
	 */
	int j;

	/**
	 * Width and height of zoom selection area in pixels.
	 */
	double zoomSelectionSize;

	/**
	 * Cell width and height in zoom grid.
	 */
	double cellSize;

	/**
	 * Font to use while painting zoom labels.
	 */
	Font font;

	/**
	 * Horizontal zoom labels.
	 */
	List<String> horizontalLabels;

	/**
	 * Horizontal zoom labels width in pixels.
	 */
	double horizontalLabelsWidth;

	/**
	 * Vertical zoom labels.
	 */
	List<String> verticalLabels;

	/**
	 * Vertical zoom labels width (on screen - height) in pixels.
	 */
	double verticalLabelsWidth;

	/**
	 * Width and height of zoom grid in pixels.
	 */
	double cellsSize;

	/**
	 * Width of zoom area (including grid and labels) in pixels.
	 */
	double width;

	/**
	 * Height of zoom area (including grid and labels) in pixels.
	 */
	double height;

	/**
	 * Horizontal location of zoom area on main grid component.
	 */
	double x;

	/**
	 * Vertical location of zoom area on main grid component.
	 */
	double y;
}
