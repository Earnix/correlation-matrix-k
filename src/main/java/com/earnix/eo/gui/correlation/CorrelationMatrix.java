package com.earnix.eo.gui.correlation;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Objects;

/**
 * Correlation matrix component. Consist of proportionally resizing correlation matrix and temperature scale pane.
 * Provides presentation customization settings. Does not provide correlations calculation functionality.
 * <br>
 * There are two display modes of correlation matrix. If there is enough space for cell to take equal or
 * more then {@code 16} pixels (is customizable with {@link #setCompactCellSize(int)}), correlations are displayed as ovals, where oval radius
 * depends on correlation square absolute value, and fill color depends on square correlation sign.
 * If there is not enough space - square correlations are displayed as rectangles with indication based on fill color.
 * {@link #setPositiveColor(Color)} is used for positive correlations and {@link #setNegativeColor(Color)} for
 * negative ones.
 * <br>
 * Highlight feature covers row and column of specific value after click on title. Zooming feature displays
 * a region of correlation grid with vertical and horizontal labels. Value of correlation in cell may observed with tooltip.
 * <br>
 * Each presentational setting of matrix can be customized in this component. For example, {@link #setGridLinesWidth(float)}.
 * Beware of background color of this component. It is used as backgound of grid, zoom, and as interpolated color of cells.
 * <br>
 * Correlations should be calculated with following methods depending on data types:
 * <br>
 * Numeric with numeric - <a href="https://en.wikipedia.org/wiki/Pearson_correlation_coefficient">Pearson correlation coefficient</a>;
 * <br>
 * Nominal with nominal - <a href="https://en.wikipedia.org/wiki/Cram%C3%A9r%27s_V">Cramér's V</a>;
 * <br>
 * Numeric with nominal - <a href="https://researchbasics.education.uconn.edu/anova_regression_and_chi-square/">ANOVA (ANalysis Of VAriance)</a>;
 */
public class CorrelationMatrix extends JPanel
{
	final List<String> titles;
	final double[][] correlations;
	final double[][] correlationsSqr;
	final List<RowType> dataTypes;

	/**
	 * Maximum amount of cells (in square) to be displayed in zoom.
	 * In case if there is not enough data rows - maximum available amount will be displayed.
	 */
	int zoomLength = 5;

	/**
	 * Grid border width.
	 */
	int gridBorderWidth = 2;

	/**
	 * Grid border color.
	 */
	Color gridBorderColor = new Color(0x0);

	/**
	 * Color used to indicate positive correlations. Is mixed with background color.
	 */
	Color positiveColor = new Color(0xfa7b64);

	/**
	 * Color used to indicate negative correlations. Is mixed with background color.
	 */
	Color negativeColor = new Color(0x274184);

	/**
	 * Background color of cell and title tooltips.
	 */
	Color toolTipBackgroundColor = new Color(0xfdfdfd);

	/**
	 * Border width of ellipse (in non-compact mode).
	 */
	float ellipseStrokeWidth = 1;

	/**
	 * Border color of ellipse (in non-compact mode).
	 */
	Color ellipseStrokeColor = new Color(0x111111);

	/**
	 * Cell and title tooltip's border color.
	 */
	Color toolTipBorderColor = new Color(0xc9d1d1);

	/**
	 * Cell and title tooltip's border width.
	 */
	int toolTipBorderWidth = 1;

	/**
	 * Cell and and title tooltip's text color.
	 */
	Color toolTipTextColor = new Color(0x32383d);

	/**
	 * Width of zoom border.
	 */
	float zoomBorderWidth = 0.5f;

	/**
	 * Color of zoom border.
	 */
	Color zoomBorderColor = new Color(0x0);

	/**
	 * Width of border which highlights currently zoomed cells in main grid..
	 */
	float zoomSelectionBorderWidth = 2f;

	/**
	 * Color of border which highlights currently zoomed cells in main grid.
	 */
	Color zoomSelectionBorderColor = new Color(0x0);

	/**
	 * Grid lines width (main and zoom).
	 */
	Color gridLinesColor = new Color(0x7F000000, true);

	/**
	 * Grid lines width (main and zoom).
	 */
	float gridLinesWidth = 0.3f;

	/**
	 * Cell and title tooltip's text font size.
	 */
	float tooltipFontSize = 20f;

	/**
	 * Cell and title tooltip's padding (between text and border).
	 */
	int tooltipPadding = 20;

	/**
	 * Color to use for highlight lines. Should be partially transparent to since completely covers data cells in compact mode.
	 */
	Color highlightColor = new Color(0xB2e3d7b4, true);

	/**
	 * Font to display labels. Only font family is taken into account, because size is calculated depending on component size.
	 * Also is used as tooltip's font.
	 */
	Font labelsFont = new Font("Tahoma", Font.PLAIN, 22);

	/**
	 * Labels color (in main grid and in zoom).
	 */
	Color labelsColor = new Color(0x0);

	/**
	 * If cell is smaller - compact display mode will be applied.
	 *
	 * @see com.earnix.eo.gui.correlation.CorrelationMatrixGrid#createCell(int, int)
	 */
	int compactCellSize = 16;

	/**
	 * Margin between grid and it's parent (this component).
	 */
	int gridMargin = 20;

	/**
	 * Width of temperature scale's gradient.
	 */
	float temperatureScaleGradientWidth = 20;

	/**
	 * Font to use for temperature scale's labels.
	 */
	float temperatureScaleFontSize = 15;

	/**
	 * Amount of temperature scale labels.
	 */
	int temperatureScaleLabelsCount = 10;

	/**
	 * Top and bottom temperature scale insets
	 */
	int temperatureScaleVerticalMargin = 5;

	final CorrelationMatrixGrid grid;
	final TemperatureScale temperatureScalePanel;

	/**
	 * Creates correlation matrix component.
	 *
	 * @param dataTypes types of data rows.
	 * @param titles data rows titles to display
	 * @param correlations two-dimensional array with correlation values. {@code NaN} means absence of correlation.
	 * @param correlationsSqr two-dimensional array with square correlation values. {@code NaN} means absence of correlation.
	 * @see com.earnix.eo.gui.correlation.RowType
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

	/**
	 * Returns correlation value (square) for given cell coordinates. Source - {@link #correlationsSqr}.
	 *
	 * @param i row index
	 * @param j column index
	 * @return correlation square value
	 */
	double getValue(int i, int j)
	{
		double value;
		if (correlations[i][j] < 0)
		{
			value = -correlationsSqr[i][j];
		}
		else
		{
			value = correlationsSqr[i][j];
		}
		return value;
	}

	// region Accessors

	/**
	 * @return Types of data rows
	 */
	public List<String> getTitles()
	{
		return this.titles;
	}

	/**
	 * @return Two-dimensional array with correlation values. {@code NaN} means absence of correlation.
	 */
	public double[][] getCorrelations()
	{
		return this.correlations;
	}

	/**
	 * @return Two-dimensional array with square correlation values. {@code NaN} means absence of correlation.
	 */
	public double[][] getCorrelationsSqr()
	{
		return this.correlationsSqr;
	}

	/**
	 * @return Types of data rows.
	 */
	public List<RowType> getDataTypes()
	{
		return this.dataTypes;
	}

	/**
	 * @return Color used to indicate positive correlations. Is mixed with background color.
	 */
	public Color getPositiveColor()
	{
		return this.positiveColor;
	}

	/**
	 * @param positiveColor Color used to indicate positive correlations. Is mixed with background color.
	 */
	public void setPositiveColor(Color positiveColor)
	{
		this.positiveColor = Objects.requireNonNull(positiveColor);
	}

	/**
	 * @return Color used to indicate negative correlations. Is mixed with background color.
	 */
	public Color getNegativeColor()
	{
		return this.negativeColor;
	}

	/**
	 * @param negativeColor Color used to indicate negative correlations. Is mixed with background color.
	 */
	public void setNegativeColor(Color negativeColor)
	{
		this.negativeColor = Objects.requireNonNull(negativeColor);
	}

	/**
	 * @return If cell is smaller - compact display mode will be applied.
	 * @see com.earnix.eo.gui.correlation.CorrelationMatrixGrid#createCell(int, int)
	 */
	public int getCompactCellSize()
	{
		return this.compactCellSize;
	}

	/**
	 * @param compactCellSize If cell is smaller - compact display mode will be applied.
	 * @see com.earnix.eo.gui.correlation.CorrelationMatrixGrid#createCell(int, int)
	 */
	public void setCompactCellSize(int compactCellSize)
	{
		this.compactCellSize = compactCellSize;
	}

	/**
	 * @return Grid lines width (main and zoom).
	 */
	public Color getGridLinesColor()
	{
		return this.gridLinesColor;
	}

	/**
	 * @param gridLinesColor Grid lines width (main and zoom).
	 * @return {@code this}
	 */
	public CorrelationMatrix setGridLinesColor(Color gridLinesColor)
	{
		this.gridLinesColor = Objects.requireNonNull(gridLinesColor);
		return this;
	}

	/**
	 * @return Color to use for highlight lines. Should be partially transparent to since completely covers data cells in compact mode.
	 */
	public Color getHighlightColor()
	{
		return this.highlightColor;
	}

	/**
	 * @param highlightColor Color to use for highlight lines. Should be partially transparent to since completely covers data cells in compact mode.
	 */
	public void setHighlightColor(Color highlightColor)
	{
		Objects.requireNonNull(highlightColor);
		if (highlightColor.getAlpha() == 255)
		{
			throw new IllegalArgumentException("Highlight color should have opacity component");
		}
		this.highlightColor = highlightColor;
	}

	/**
	 * @return Font to display labels. Only font family is taken into account, because size is calculated depending on component size.
	 * Also is used as tooltip's font.
	 */
	public Font getLabelsFont()
	{
		return this.labelsFont;
	}

	/**
	 * @param labelsFont Font to display labels. Only font family is taken into account, because size is calculated depending on component size.
	 * Also is used as tooltip's font.
	 */
	public void setLabelsFont(Font labelsFont)
	{
		this.labelsFont = Objects.requireNonNull(labelsFont);
	}

	/**
	 * @return Grid lines width (main and zoom).
	 */
	public float getGridLinesWidth()
	{
		return gridLinesWidth;
	}

	/**
	 * @param gridLinesWidth Grid lines width (main and zoom).
	 * @return {@code this}
	 */
	public CorrelationMatrix setGridLinesWidth(float gridLinesWidth)
	{
		this.gridLinesWidth = gridLinesWidth;
		return this;
	}

	/**
	 * @return Background color of cell and title tooltips.
	 */
	public Color getToolTipBackgroundColor()
	{
		return toolTipBackgroundColor;
	}

	/**
	 * @param toolTipBackgroundColor Background color of cell and title tooltips.
	 * @return {@code this}
	 */
	public CorrelationMatrix setToolTipBackgroundColor(Color toolTipBackgroundColor)
	{
		this.toolTipBackgroundColor = Objects.requireNonNull(toolTipBackgroundColor);
		return this;
	}

	/**
	 * @return Border width of ellipse (in non-compact mode).
	 */
	public float getEllipseStrokeWidth()
	{
		return ellipseStrokeWidth;
	}

	/**
	 * @param ellipseStrokeWidth Border width of ellipse (in non-compact mode).
	 * @return {@code this}
	 */
	public CorrelationMatrix setEllipseStrokeWidth(float ellipseStrokeWidth)
	{
		this.ellipseStrokeWidth = ellipseStrokeWidth;
		return this;
	}

	/**
	 * @return Border color of ellipse (in non-compact mode).
	 */
	public Color getEllipseStrokeColor()
	{
		return ellipseStrokeColor;
	}

	/**
	 * @param ellipseStrokeColor Border color of ellipse (in non-compact mode).
	 * @return {@code this}
	 */
	public CorrelationMatrix setEllipseStrokeColor(Color ellipseStrokeColor)
	{
		this.ellipseStrokeColor = Objects.requireNonNull(ellipseStrokeColor);
		return this;
	}

	/**
	 * @return Cell and title tooltip's border color.
	 */
	public Color getToolTipBorderColor()
	{
		return toolTipBorderColor;
	}

	/**
	 * @param toolTipBorderColor Cell and title tooltip's border color.
	 * @return {@code this}
	 */
	public CorrelationMatrix setToolTipBorderColor(Color toolTipBorderColor)
	{
		this.toolTipBorderColor = Objects.requireNonNull(toolTipBorderColor);
		return this;
	}

	/**
	 * @return Cell and title tooltip's border width.
	 */
	public int getToolTipBorderWidth()
	{
		return toolTipBorderWidth;
	}

	/**
	 * @param toolTipBorderWidth Cell and title tooltip's border width.
	 * @return {@code this}
	 */
	public CorrelationMatrix setToolTipBorderWidth(int toolTipBorderWidth)
	{
		this.toolTipBorderWidth = toolTipBorderWidth;
		return this;
	}

	/**
	 * @return Cell and and title tooltip's text color.
	 */
	public Color getToolTipTextColor()
	{
		return toolTipTextColor;
	}

	/**
	 * @param toolTipTextColor Cell and and title tooltip's text color.
	 * @return {@code this}
	 */
	public CorrelationMatrix setToolTipTextColor(Color toolTipTextColor)
	{
		this.toolTipTextColor = Objects.requireNonNull(toolTipTextColor);
		return this;
	}

	/**
	 * @return Width of zoom border.
	 */
	public float getZoomBorderWidth()
	{
		return zoomBorderWidth;
	}

	/**
	 * @param zoomBorderWidth Width of zoom border.
	 * @return {@code this}
	 */
	public CorrelationMatrix setZoomBorderWidth(float zoomBorderWidth)
	{
		this.zoomBorderWidth = zoomBorderWidth;
		return this;
	}

	/**
	 * @return Color of zoom border.
	 */
	public Color getZoomBorderColor()
	{
		return zoomBorderColor;
	}

	/**
	 * @param zoomBorderColor Color of zoom border.
	 * @return {@code this}
	 */
	public CorrelationMatrix setZoomBorderColor(Color zoomBorderColor)
	{
		this.zoomBorderColor = Objects.requireNonNull(zoomBorderColor);
		return this;
	}

	/**
	 * @return Width of border which highlights currently zoomed cells in main grid..
	 */
	public float getZoomSelectionBorderWidth()
	{
		return zoomSelectionBorderWidth;
	}

	/**
	 * @param zoomSelectionBorderWidth Width of border which highlights currently zoomed cells in main grid.
	 * @return {@code this}
	 */
	public CorrelationMatrix setZoomSelectionBorderWidth(float zoomSelectionBorderWidth)
	{
		this.zoomSelectionBorderWidth = zoomSelectionBorderWidth;
		return this;
	}

	/**
	 * @return Color of border which highlights currently zoomed cells in main grid.
	 */
	public Color getZoomSelectionBorderColor()
	{
		return zoomSelectionBorderColor;
	}

	/**
	 * @param zoomSelectionBorderColor Color of border which highlights currently zoomed cells in main grid.
	 * @return {@code this}
	 */
	public CorrelationMatrix setZoomSelectionBorderColor(Color zoomSelectionBorderColor)
	{
		this.zoomSelectionBorderColor = Objects.requireNonNull(zoomSelectionBorderColor);
		return this;
	}

	/**
	 * @return Grid border width.
	 */
	public int getGridBorderWidth()
	{
		return gridBorderWidth;
	}

	/**
	 * @param gridBorderWidth Grid border width.
	 * @return {@code this}
	 */
	public CorrelationMatrix setGridBorderWidth(int gridBorderWidth)
	{
		this.gridBorderWidth = gridBorderWidth;
		return this;
	}

	/**
	 * @return Grid border color.
	 */
	public Color getGridBorderColor()
	{
		return gridBorderColor;
	}

	/**
	 * @param gridBorderColor Grid border color.
	 * @return {@code this}
	 */
	public CorrelationMatrix setGridBorderColor(Color gridBorderColor)
	{
		this.gridBorderColor = Objects.requireNonNull(gridBorderColor);
		return this;
	}

	/**
	 * @return Labels color (in main grid and in zoom).
	 */
	public Color getLabelsColor()
	{
		return labelsColor;
	}

	/**
	 * @param labelsColor Labels color (in main grid and in zoom).
	 * @return {@code this}
	 */
	public CorrelationMatrix setLabelsColor(Color labelsColor)
	{
		this.labelsColor = Objects.requireNonNull(labelsColor);
		return this;
	}

	/**
	 * @return Margin between grid and it's parent (this component).
	 */
	public int getGridMargin()
	{
		return gridMargin;
	}

	/**
	 * @param gridMargin Margin between grid and it's parent (this component).
	 * @return {@code this}
	 */
	public CorrelationMatrix setGridMargin(int gridMargin)
	{
		this.gridMargin = gridMargin;
		return this;
	}

	/**
	 * @return Cell and title tooltip's text font size.
	 */
	public float getTooltipFontSize()
	{
		return tooltipFontSize;
	}

	/**
	 * @param tooltipFontSize Cell and title tooltip's text font size.
	 * @return {@code this}
	 */
	public CorrelationMatrix setTooltipFontSize(float tooltipFontSize)
	{
		this.tooltipFontSize = tooltipFontSize;
		return this;
	}

	/**
	 * @return Cell and title tooltip's padding (between text and border).
	 */
	public int getTooltipPadding()
	{
		return tooltipPadding;
	}

	/**
	 * @param tooltipPadding Cell and title tooltip's padding (between text and border).
	 * @return {@code this}
	 */
	public CorrelationMatrix setTooltipPadding(int tooltipPadding)
	{
		this.tooltipPadding = tooltipPadding;
		return this;
	}

	/**
	 * @return Maximum amount of cells (in square) to be displayed in zoom.
	 * In case if there is not enough data rows - maximum available amount will be displayed.
	 */
	public int getZoomLength()
	{
		return zoomLength;
	}

	/**
	 * @param zoomLength Maximum amount of cells (in square) to be displayed in zoom.
	 * In case if there is not enough data rows - maximum available amount will be displayed.
	 * @return {@code this}
	 */
	public CorrelationMatrix setZoomLength(int zoomLength)
	{
		this.zoomLength = zoomLength;
		return this;
	}

	/**
	 * @return Width of temperature scale's gradient.
	 */
	public float getTemperatureScaleGradientWidth()
	{
		return temperatureScaleGradientWidth;
	}

	/**
	 * @param temperatureScaleGradientWidth Width of temperature scale's gradient.
	 * @return {@code this}
	 */
	public CorrelationMatrix setTemperatureScaleGradientWidth(float temperatureScaleGradientWidth)
	{
		this.temperatureScaleGradientWidth = temperatureScaleGradientWidth;
		return this;
	}

	/**
	 * @return Font to use for temperature scale's labels.
	 */
	public float getTemperatureScaleFontSize()
	{
		return temperatureScaleFontSize;
	}

	/**
	 * @param temperatureScaleFontSize Font to use for temperature scale's labels.
	 * @return {@code this}
	 */
	public CorrelationMatrix setTemperatureScaleFontSize(float temperatureScaleFontSize)
	{
		this.temperatureScaleFontSize = temperatureScaleFontSize;
		return this;
	}

	/**
	 * @return Amount of temperature scale labels.
	 */
	public int getTemperatureScaleLabelsCount()
	{
		return temperatureScaleLabelsCount;
	}

	/**
	 * @param temperatureScaleLabelsCount Amount of temperature scale labels.
	 * @return {@code this}
	 */
	public CorrelationMatrix setTemperatureScaleLabelsCount(int temperatureScaleLabelsCount)
	{
		this.temperatureScaleLabelsCount = temperatureScaleLabelsCount;
		return this;
	}

	/**
	 * @return Top and bottom temperature scale insets
	 */
	public int getTemperatureScaleVerticalMargin()
	{
		return temperatureScaleVerticalMargin;
	}

	/**
	 * @param temperatureScaleVerticalMargin Top and bottom temperature scale insets
	 * @return {@code this}
	 */
	public CorrelationMatrix setTemperatureScaleVerticalMargin(int temperatureScaleVerticalMargin)
	{
		this.temperatureScaleVerticalMargin = temperatureScaleVerticalMargin;
		return this;
	}

	// endregion
}
