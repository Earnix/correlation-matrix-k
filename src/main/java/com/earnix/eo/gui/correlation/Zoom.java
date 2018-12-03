package com.earnix.eo.gui.correlation;

import java.awt.Font;
import java.util.List;

/**
 * @author Taras Maslov
 * 12/3/2018
 */
public class Zoom {
    int zoomLength;

    int zoomStartI;
    int zoomStartJ;

    double zoomSelectionSize;

    double zoomCellSize; // todo expose
    Font font;
    List<String> horizontalLabels;
    double horizontalLabelsWidth;

    List<String> verticalLabels;
    double verticalLabelsWidth;

    double zoomCellsSize;
    double zoomWidth;
    double zoomHeight;
    
    double zoomX, zoomY;
}
