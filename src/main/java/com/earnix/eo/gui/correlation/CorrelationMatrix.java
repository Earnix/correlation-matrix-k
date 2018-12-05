package com.earnix.eo.gui.correlation;

//import com.earnix.eo.datatypes.DataType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CorrelationMatrix extends JPanel {

    @Getter final List<String> titles;

    @Getter final double[][] data;

    @Getter final double[][] dataSqr;
    
    @Getter final List<CellType> dataTypes;

    @Getter @Setter
    Color positiveColor = new Color(0xfa7b64);

    @Getter @Setter
    Color negativeColor = new Color(0x274184);

    @Getter @Setter
    Color highlightColor = new Color(0xB2e3d7b4, true);

    @Getter @Setter
    Font labelsFont = new Font("Tahoma", Font.PLAIN, 22);

    @Getter @Setter
    int compactCellSize = 16;

    @Getter @Setter
    Color linesColor = new Color(0x7F000000, true);
    
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

        val layout = new BorderLayout();
        layout.setHgap(20);
        setLayout(layout);
        val graph = new CorrelationMatrixGraph(this);
        add(graph, BorderLayout.CENTER);

        val legend = new TemperatureScalePanel(this);
        add(legend, BorderLayout.EAST);
        
        setBackground(Color.WHITE);
    }

    public int length() {
        return titles.size();
    }
}
