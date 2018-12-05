package com.earnix.eo.gui.correlation;

import java.awt.Font;
import java.util.List;

/**
 * @author Taras Maslov
 * 12/3/2018
 */
class Zoom {
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
