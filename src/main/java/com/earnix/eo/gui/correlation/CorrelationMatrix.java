package com.earnix.eo.gui.correlation;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Objects;

/**
 * @author Taras Maslov
 * 11/26/2018
 */
public class CorrelationMatrix extends JPanel {

    private final List<String> titles;
    private final double[][] data;
    private final double[][] dataSqr;
    private final List<CellType> dataTypes;

    private Color positiveColor = new Color(0xfa7b64);
    private Color negativeColor = new Color(0x274184);
    private Color highlightColor = new Color(0xB2e3d7b4, true);
    private Font labelsFont = new Font("Tahoma", Font.PLAIN, 22);
    private int compactCellSize = 16;
    private Color linesColor = new Color(0x7F000000, true);
    
    /**
     * @param dataTypes
     * @param titles
     * @param data
     * @param dataSqr
     */
    public CorrelationMatrix(List<CellType> dataTypes, List<String> titles, double[][] data, double[][] dataSqr) {
        this.dataTypes = dataTypes;
        this.titles = Objects.requireNonNull(titles);
        this.data = Objects.requireNonNull(data);
        this.dataSqr = Objects.requireNonNull(dataSqr);

        if (dataTypes.size() != titles.size() || titles.size() != data.length || data.length != dataSqr.length) {
            throw new IllegalArgumentException();
        }

        BorderLayout layout = new BorderLayout();
        layout.setHgap(20);
        setLayout(layout);
        CorrelationMatrixGraph graph = new CorrelationMatrixGraph(this);
        add(graph, BorderLayout.CENTER);

        TemperatureScalePanel legend = new TemperatureScalePanel(this);
        add(legend, BorderLayout.EAST);
        
        setBackground(Color.WHITE);
    }

    public int length() {
        return titles.size();
    }

    public List<String> getTitles() {
        return this.titles;
    }

    public double[][] getData() {
        return this.data;
    }

    public double[][] getDataSqr() {
        return this.dataSqr;
    }

    public List<CellType> getDataTypes() {
        return this.dataTypes;
    }

    public Color getPositiveColor() {
        return this.positiveColor;
    }

    public Color getNegativeColor() {
        return this.negativeColor;
    }

    public Color getHighlightColor() {
        return this.highlightColor;
    }

    public Font getLabelsFont() {
        return this.labelsFont;
    }

    public int getCompactCellSize() {
        return this.compactCellSize;
    }

    public Color getLinesColor() {
        return this.linesColor;
    }

    public void setPositiveColor(Color positiveColor) {
        this.positiveColor = positiveColor;
    }

    public void setNegativeColor(Color negativeColor) {
        this.negativeColor = negativeColor;
    }

    public void setHighlightColor(Color highlightColor) {
        this.highlightColor = highlightColor;
    }

    public void setLabelsFont(Font labelsFont) {
        this.labelsFont = labelsFont;
    }

    public void setCompactCellSize(int compactCellSize) {
        this.compactCellSize = compactCellSize;
    }

    public void setLinesColor(Color linesColor) {
        this.linesColor = linesColor;
    }
}
