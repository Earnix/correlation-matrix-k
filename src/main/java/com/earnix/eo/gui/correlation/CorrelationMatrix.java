package com.earnix.eo.gui.correlation;

//import com.earnix.eo.datatypes.DataType;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

/**
 * @author Taras Maslov
 * 11/26/2018
 */
public class CorrelationMatrix extends JPanel {

    public CorrelationMatrix(Object[] dataTypes, List<String> titles, double[][] data, double[][] dataSqr) {

        setLayout(new BorderLayout());
        add(new CorrelationMatrixGraph(dataTypes, titles, data, dataSqr), BorderLayout.CENTER);

        CorrelateionMatrixLegend legend = new CorrelateionMatrixLegend();
        legend.setPreferredSize(new Dimension(40, 40));
        legend.setMinimumSize(new Dimension(40, 40));
        add(new CorrelateionMatrixLegend(), BorderLayout.EAST);
    }
}
