package com.earnix.eo.gui.correlation;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Correlation matrix component which consist of proportionally resizing correlation matrix and temperature scale pane.
 * Provides presentation customization settings. Does not provide calculation of correlations.
 * Correlations should be calculated with following methods depending on data types:
 * <br/>
 * Numeric with numeric - <a href="https://en.wikipedia.org/wiki/Pearson_correlation_coefficient">Pearson correlation coefficient</a>;
 * <br/>
 * Nominal with nominal - <a href="http://google.com">Cramér's V</a>;
 * <br/>
 * Numeric with nominal - <a href="https://researchbasics.education.uconn.edu/anova_regression_and_chi-square/">ANOVA (ANalysis Of VAriance)</a>;
 */
public class CorrelationMatrix extends JPanel
{

	private final List<String> titles;
	private final double[][] correlations;
	private final double[][] correlationsSqr;
	private final List<RowType> dataTypes;

	/**
	 * Maximum amount of cells (in square) to be displayed in zoom.
	 * In case if there is not enough data rows - maximum available amount will be displayed.
	 */
	private int zoomLength = 5;
	private float borderWidth = 2;
	private Color borderColor = new Color(0x0);
	private Color positiveColor = new Color(0xfa7b64);
	private Color negativeColor = new Color(0x274184);
	private Color toolTipBackgroundColor = new Color(0xfdfdfd);
	private float ellipseStrokeWidth = 1;
	private Color ellipseStrokeColor = new Color(0x111111);
	private Color toolTipBorderColor = new Color(0xc9d1d1);
	private int toolTipBorderWidth = 1;
	private Color toolTipTextColor = new Color(0x32383d);
	private float zoomBorderWidth = 0.5f;
	private Color zoomBorderColor = new Color(0x0);
	private float zoomSelectionBorderWidth = 2f;
	private Color zoomSelectionBorderColor = new Color(0x0);
	private Color gridLinesColor = new Color(0x7F000000, true);
	private float gridLinesWidth = 0.3f;
	private float tooltipFontSize = 20f;
	private int tooltipPadding = 20;

	/**
	 * Color to use for highlight lines. Should be partially transparent to since completely covers data cells in compact mode.
	 */
	private Color highlightColor = new Color(0xB2e3d7b4, true);
	private Font labelsFont = new Font("Tahoma", Font.PLAIN, 22);
	private Color labelsColor = Color.white;//new Color(0x0);
	private int compactCellSize = 16;
	private int gridMargin = 20;

	private final CorrelationMatrixGrid grid;
	private final TemperatureScalePanel temperatureScalePanel;

	/**
	 * Creates Main correlation matrix component
	 *
	 * @param dataTypes types of data rows. {@see com.earnix.eo.gui.correlation.DataType}
	 * @param titles data rows titles to display
	 * @param correlations two-dimensional array with correlation values. {@code NaN} means absence of correlation.
	 * @param correlationsSqr two-dimensional array with square correlation values. {@code NaN} means absence of correlation.
	 */
	public CorrelationMatrix(List<RowType> dataTypes, List<String> titles, double[][] correlations,
			double[][] correlationsSqr)
	{
		// setting initial data
		this.dataTypes = Objects.requireNonNull(dataTypes);
		this.titles = Objects.requireNonNull(titles);
		this.correlations = Objects.requireNonNull(correlations);
		this.correlationsSqr = Objects.requireNonNull(correlationsSqr);
		if (dataTypes.size() != titles.size() || titles.size() != correlations.length
				|| correlations.length != correlationsSqr.length)
		{
			throw new IllegalArgumentException();
		}

		setBackground(Color.RED);
		setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.weightx = 1.0;
		constraints.insets = new Insets(gridMargin, gridMargin, gridMargin, gridMargin);
		constraints.anchor = GridBagConstraints.CENTER;
		grid = new CorrelationMatrixGrid(this);
		grid.setBackground(this.getBackground());
		add(grid, constraints);

		temperatureScalePanel = new TemperatureScalePanel(this);
		temperatureScalePanel.setBackground(this.getBackground());
		constraints.gridx = 1;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.weightx = 0;
		constraints.insets = new Insets(0, 0, 0, 0);
		add(temperatureScalePanel, constraints);
	}

	public int length()
	{
		return titles.size();
	}

	public List<String> getTitles()
	{
		return this.titles;
	}

	public double[][] getCorrelations()
	{
		return this.correlations;
	}

	public double[][] getCorrelationsSqr()
	{
		return this.correlationsSqr;
	}

	public List<RowType> getDataTypes()
	{
		return this.dataTypes;
	}

	public Color getPositiveColor()
	{
		return this.positiveColor;
	}

	public Color getNegativeColor()
	{
		return this.negativeColor;
	}

	public Color getHighlightColor()
	{
		return this.highlightColor;
	}

	public Font getLabelsFont()
	{
		return this.labelsFont;
	}

	public int getCompactCellSize()
	{
		return this.compactCellSize;
	}

	public Color getGridLinesColor()
	{
		return this.gridLinesColor;
	}

	public void setPositiveColor(Color positiveColor)
	{
		this.positiveColor = positiveColor;
	}

	public void setNegativeColor(Color negativeColor)
	{
		this.negativeColor = negativeColor;
	}

	public void setHighlightColor(Color highlightColor)
	{
		this.highlightColor = highlightColor;
	}

	public void setLabelsFont(Font labelsFont)
	{
		this.labelsFont = labelsFont;
	}

	public void setCompactCellSize(int compactCellSize)
	{
		this.compactCellSize = compactCellSize;
	}

	public CorrelationMatrix setGridLinesColor(Color gridLinesColor)
	{
		this.gridLinesColor = gridLinesColor;
		return this;
	}

	public float getGridLinesWidth()
	{
		return gridLinesWidth;
	}

	public CorrelationMatrix setGridLinesWidth(float gridLinesWidth)
	{
		this.gridLinesWidth = gridLinesWidth;
		return this;
	}

	public Color getToolTipBackgroundColor()
	{
		return toolTipBackgroundColor;
	}

	public CorrelationMatrix setToolTipBackgroundColor(Color toolTipBackgroundColor)
	{
		this.toolTipBackgroundColor = toolTipBackgroundColor;
		return this;
	}

	public float getEllipseStrokeWidth()
	{
		return ellipseStrokeWidth;
	}

	public CorrelationMatrix setEllipseStrokeWidth(float ellipseStrokeWidth)
	{
		this.ellipseStrokeWidth = ellipseStrokeWidth;
		return this;
	}

	public Color getEllipseStrokeColor()
	{
		return ellipseStrokeColor;
	}

	public CorrelationMatrix setEllipseStrokeColor(Color ellipseStrokeColor)
	{
		this.ellipseStrokeColor = ellipseStrokeColor;
		return this;
	}

	public Color getToolTipBorderColor()
	{
		return toolTipBorderColor;
	}

	public CorrelationMatrix setToolTipBorderColor(Color toolTipBorderColor)
	{
		this.toolTipBorderColor = toolTipBorderColor;
		return this;
	}

	public int getToolTipBorderWidth()
	{
		return toolTipBorderWidth;
	}

	public CorrelationMatrix setToolTipBorderWidth(int toolTipBorderWidth)
	{
		this.toolTipBorderWidth = toolTipBorderWidth;
		return this;
	}

	public Color getToolTipTextColor()
	{
		return toolTipTextColor;
	}

	public CorrelationMatrix setToolTipTextColor(Color toolTipTextColor)
	{
		this.toolTipTextColor = toolTipTextColor;
		return this;
	}

	public float getZoomBorderWidth()
	{
		return zoomBorderWidth;
	}

	public CorrelationMatrix setZoomBorderWidth(float zoomBorderWidth)
	{
		this.zoomBorderWidth = zoomBorderWidth;
		return this;
	}

	public Color getZoomBorderColor()
	{
		return zoomBorderColor;
	}

	public CorrelationMatrix setZoomBorderColor(Color zoomBorderColor)
	{
		this.zoomBorderColor = zoomBorderColor;
		return this;
	}

	public float getZoomSelectionBorderWidth()
	{
		return zoomSelectionBorderWidth;
	}

	public CorrelationMatrix setZoomSelectionBorderWidth(float zoomSelectionBorderWidth)
	{
		this.zoomSelectionBorderWidth = zoomSelectionBorderWidth;
		return this;
	}

	public Color getZoomSelectionBorderColor()
	{
		return zoomSelectionBorderColor;
	}

	public CorrelationMatrix setZoomSelectionBorderColor(Color zoomSelectionBorderColor)
	{
		this.zoomSelectionBorderColor = zoomSelectionBorderColor;
		return this;
	}

	public float getBorderWidth()
	{
		return borderWidth;
	}

	public CorrelationMatrix setBorderWidth(float borderWidth)
	{
		this.borderWidth = borderWidth;
		return this;
	}

	public Color getBorderColor()
	{
		return borderColor;
	}

	public CorrelationMatrix setBorderColor(Color borderColor)
	{
		this.borderColor = borderColor;
		return this;
	}

	public Color getLabelsColor()
	{
		return labelsColor;
	}

	public CorrelationMatrix setLabelsColor(Color labelsColor)
	{
		this.labelsColor = labelsColor;
		return this;
	}

	public int getGridMargin()
	{
		return gridMargin;
	}

	public CorrelationMatrix setGridMargin(int gridMargin)
	{
		this.gridMargin = gridMargin;
		return this;
	}

	public float getTooltipFontSize()
	{
		return tooltipFontSize;
	}

	public CorrelationMatrix setTooltipFontSize(float tooltipFontSize)
	{
		this.tooltipFontSize = tooltipFontSize;
		return this;
	}

	public int getTooltipPadding()
	{
		return tooltipPadding;
	}

	public CorrelationMatrix setTooltipPadding(int tooltipPadding)
	{
		this.tooltipPadding = tooltipPadding;
		return this;
	}

	public int getZoomLength()
	{
		return zoomLength;
	}

	public CorrelationMatrix setZoomLength(int zoomLength)
	{
		this.zoomLength = zoomLength;
		return this;
	}

	// 

	public CorrelationMatrixGrid getGrid()
	{
		return grid;
	}

	public TemperatureScalePanel getTemperatureScalePanel()
	{
		return temperatureScalePanel;
	}
}
