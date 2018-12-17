package com.earnix.eo.gui.correlation;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Correlation matrix component. Consist of proportionally resizing correlation matrix and temperature scale pane.
 * Provides presentation customization settings. Does not provide correlations calculation functionality.
 * <br/>
 * There are two display modes of correlation matrix. If there is enough space for cell to take equal or
 * more then {@code 16} pixels (is customizable with {@link #setCompactCellSize(int)}), correlations are displayed as ovals, where oval radius
 * depends on correlation square absolute value, and fill color depends on square correlation sign.
 * If there is not enough space - square correlations are displayed as rectangles with indication based on fill color.
 * {@link #setPositiveColor(Color)} is used for positive correlations and {@link #setNegativeColor(Color)} for
 * negative ones.
 * <br/>
 * Highlight feature covers row and column of specific value after click on title. Zooming feature displays
 * a region of correlation grid with vertical and horizontal labels. Value of correlation in cell may observed with tooltip.
 * <br/>
 * Each presentational setting of matrix can be customized in this component. For example, {@link #setGridLinesWidth(float)}.
 * Beware of background color of this component. It is used as backgound of grid, zoom, and as interpolated color of cells.
 * <br/>
 * Correlations should be calculated with following methods depending on data types:
 * <br/>
 * Numeric with numeric - <a href="https://en.wikipedia.org/wiki/Pearson_correlation_coefficient">Pearson correlation coefficient</a>;
 * <br/>
 * Nominal with nominal - <a href="https://en.wikipedia.org/wiki/Cram%C3%A9r%27s_V">Cramér's V</a>;
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

	/**
	 * Grid border width.
	 */
	private int gridBorderWidth = 2;

	/**
	 * Grid border color.
	 */
	private Color gridBorderColor = new Color(0x0);

	/**
	 * Color used to indicate positive correlations. Is mixed with background color.
	 */
	private Color positiveColor = new Color(0xfa7b64);

	/**
	 * Color used to indicate negative correlations. Is mixed with background color.
	 */
	private Color negativeColor = new Color(0x274184);

	/**
	 * Background color of cell and title tooltips.
	 */
	private Color toolTipBackgroundColor = new Color(0xfdfdfd);

	/**
	 * Border width of ellipse (in non-compact mode).
	 */
	private float ellipseStrokeWidth = 1;

	/**
	 * Border color of ellipse (in non-compact mode).
	 */
	private Color ellipseStrokeColor = new Color(0x111111);

	/**
	 * Cell and title tooltip's border color.
	 */
	private Color toolTipBorderColor = new Color(0xc9d1d1);

	/**
	 * Cell and title tooltip's border width.
	 */
	private int toolTipBorderWidth = 1;

	/**
	 * Cell and and title tooltip's text color.
	 */
	private Color toolTipTextColor = new Color(0x32383d);

	/**
	 * Width of zoom border.
	 */
	private float zoomBorderWidth = 0.5f;

	/**
	 * Color of zoom border.
	 */
	private Color zoomBorderColor = new Color(0x0);

	/**
	 * Width of border which highlights currently zoomed cells in main grid..
	 */
	private float zoomSelectionBorderWidth = 2f;

	/**
	 * Color of border which highlights currently zoomed cells in main grid.
	 */
	private Color zoomSelectionBorderColor = new Color(0x0);

	/**
	 * Grid lines width (main and zoom).
	 */
	private Color gridLinesColor = new Color(0x7F000000, true);

	/**
	 * Grid lines width (main and zoom).
	 */
	private float gridLinesWidth = 0.3f;

	/**
	 * Cell and title tooltip's text font size.
	 */
	private float tooltipFontSize = 20f;

	/**
	 * Cell and title tooltip's padding (between text and border).
	 */
	private int tooltipPadding = 20;

	/**
	 * Color to use for highlight lines. Should be partially transparent to since completely covers data cells in compact mode.
	 */
	private Color highlightColor = new Color(0xB2e3d7b4, true);

	/**
	 * Font to display labels. Only font family is taken into account, because size is calculated depending on component size.
	 * Also is used as tooltip's font.
	 */
	private Font labelsFont = new Font("Tahoma", Font.PLAIN, 22);

	/**
	 * Labels color (in main grid and in zoom).
	 */
	private Color labelsColor = new Color(0x0);

	/**
	 * If cell is smaller - compact display mode will be applied.
	 * {@see com.earnix.eo.gui.correlation.CorrelationMatrixGrid#createCell(int, int)}
	 */
	private int compactCellSize = 16;

	/**
	 * Margin between grid and it's parent (this component).
	 */
	private int gridMargin = 20;

	/**
	 * Width of temperature scale's gradient.
	 */
	private float temperatureScaleGradientWidth = 20;

	/**
	 * Font to use for temperature scale's labels.
	 */
	private float temperatureScaleFontSize = 15;

	/**
	 * Amount of temperature scale labels.
	 */
	private int temperatureScaleLabelsCount = 10;

	/**
	 * Top and bottom temperature scale insets
	 */
	private int temperatureScaleVerticalMargin = 5;

	private final CorrelationMatrixGrid grid;
	private final TemperatureScale temperatureScalePanel;

	/**
	 * Creates correlation matrix component.
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

		setLayout(new GridBagLayout());

		// placing grid in the center
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.weightx = 1.0;
		constraints.insets = new Insets(gridMargin, gridMargin, gridMargin, gridMargin);
		constraints.anchor = GridBagConstraints.CENTER;
		grid = new CorrelationMatrixGrid(this);
		grid.setBackground(getBackground());
		add(grid, constraints);

		// placing temperature scale on the right side.
		temperatureScalePanel = new TemperatureScale(this);
		temperatureScalePanel.setBackground(getBackground());
		constraints.gridx = 1;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.weightx = 0;
		constraints.insets = new Insets(temperatureScaleVerticalMargin, 0, temperatureScaleVerticalMargin, 0);
		add(temperatureScalePanel, constraints);
	}

	/**
	 * Returns amount of rows in correlations table
	 *
	 * @return number of rows
	 */
	public int length()
	{
		return titles.size();
	}

	// region Accessors

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
		if (highlightColor.getAlpha() == 255)
		{
			throw new IllegalArgumentException("Highlight color should have opacity component");
		}
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

	public int getGridBorderWidth()
	{
		return gridBorderWidth;
	}

	public CorrelationMatrix setGridBorderWidth(int gridBorderWidth)
	{
		this.gridBorderWidth = gridBorderWidth;
		return this;
	}

	public Color getGridBorderColor()
	{
		return gridBorderColor;
	}

	public CorrelationMatrix setGridBorderColor(Color gridBorderColor)
	{
		this.gridBorderColor = gridBorderColor;
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

	public float getTemperatureScaleGradientWidth()
	{
		return temperatureScaleGradientWidth;
	}

	public CorrelationMatrix setTemperatureScaleGradientWidth(float temperatureScaleGradientWidth)
	{
		this.temperatureScaleGradientWidth = temperatureScaleGradientWidth;
		return this;
	}

	public float getTemperatureScaleFontSize()
	{
		return temperatureScaleFontSize;
	}

	public CorrelationMatrix setTemperatureScaleFontSize(float temperatureScaleFontSize)
	{
		this.temperatureScaleFontSize = temperatureScaleFontSize;
		return this;
	}

	public int getTemperatureScaleLabelsCount()
	{
		return temperatureScaleLabelsCount;
	}

	public CorrelationMatrix setTemperatureScaleLabelsCount(int temperatureScaleLabelsCount)
	{
		this.temperatureScaleLabelsCount = temperatureScaleLabelsCount;
		return this;
	}

	public int getTemperatureScaleVerticalMargin()
	{
		return temperatureScaleVerticalMargin;
	}

	public CorrelationMatrix setTemperatureScaleVerticalMargin(int temperatureScaleVerticalMargin)
	{
		this.temperatureScaleVerticalMargin = temperatureScaleVerticalMargin;
		return this;
	}

	// endregion

	public CorrelationMatrixGrid getGrid()
	{
		return grid;
	}

	public TemperatureScale getTemperatureScalePanel()
	{
		return temperatureScalePanel;
	}
}
