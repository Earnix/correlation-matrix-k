package com.earnix.eo.gui.correlation;

//import com.earnix.eo.datatypes.DataType;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Objects;

/**
 * @author Taras Maslov
 * 11/26/2018
 */
public class CorrelationMatrix extends JPanel {

    @Getter
    private final List<String> titles;

    @Getter
    private final double[][] data;

    @Getter
    private final double[][] dataSqr;

    @Getter @Setter
    private Color color1 = new Color(0xfa7b64);

    @Getter @Setter
    private Color color2 = new Color(0x274184);

    @Getter @Setter
    private Font labelsFont = new Font("Tahoma", Font.PLAIN, 22);

    @Getter @Setter
    private int compactSize = 500;

    /**
     * @param dataTypes
     * @param titles
     * @param data
     * @param dataSqr
     */
    public CorrelationMatrix(Object[] dataTypes, List<String> titles, double[][] data, double[][] dataSqr) {
        this.titles = Objects.requireNonNull(titles);
        this.data = Objects.requireNonNull(data);
        this.dataSqr = Objects.requireNonNull(dataSqr);

        if (dataTypes.length != titles.size() || titles.size() != data.length || data.length != dataSqr.length) {
            throw new IllegalArgumentException();
        }

        setLayout(new BorderLayout());
        val graph = new CorrelationMatrixGraph(this);
        add(graph, BorderLayout.CENTER);

        val legend = new CorrelateionMatrixLegend(this);
        legend.setPreferredSize(new Dimension(40, 40));
        legend.setMinimumSize(new Dimension(40, 40));
        add(legend, BorderLayout.EAST);
    }

    int length() {
        return titles.size();
    }
}
