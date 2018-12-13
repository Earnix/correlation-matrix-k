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

	int startI;
	int startJ;

	double zoomSelectionSize;

	double cellSize; // todo expose
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
