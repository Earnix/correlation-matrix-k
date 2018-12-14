package com.earnix.eo.gui.correlation;

import java.awt.*;
import java.util.List;

/**
 * Represents correlation matrix zoom model with pre-calculated rendering properties like coordinates and sizes.
 */
class Zoom
{
	double labelsMargin;
	int length;

	int i;
	int j;

	double zoomSelectionSize;

	double cellSize;
	Font font;
	List<String> horizontalLabels;
	double horizontalLabelsWidth;

	List<String> verticalLabels;
	double verticalLabelsWidth;

	double cellsSize;
	double width;
	double height;

	double x, y;
}
