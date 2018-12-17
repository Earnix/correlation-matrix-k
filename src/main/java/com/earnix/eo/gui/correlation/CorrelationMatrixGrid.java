package com.earnix.eo.gui.correlation;


import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Correlation matrix grid component.
 * <br/>
 * Visually includes data cells, titles column, highlights (if active), zoom (if active) and tooltip (if active).
 * Component is assumed to be used only within (@link {@link CorrelationMatrix}) as parent.
 * Presentation parameters are stored in {@link CorrelationMatrix}.
 * Two main entry points are {@link #getPreferredSize()}, which calculates main sizing value (@link {@link #cellSize}),
 * and {@link #paintComponent(Graphics)}, which paints all active visual element's, calculating their properties before.
 */
@SuppressWarnings("FieldCanBeLocal")
public class CorrelationMatrixGrid extends JPanel implements MouseListener, MouseMotionListener
{
	/**
	 * A proportion of oval in cell.
	 */
	private final float CIRCLE_HEIGHT_PROPORTION = 0.7f;

	/**
	 * A proportion of label in title cell (height).
	 */
	private final float LABEL_HEIGHT_PROPORTION = 0.7f;

	/**
	 * Coefficient which restricts oval from squeezing too much.
	 */
	private static final float SQUEEZE_COEFFICIENT = 0.8f;

	/**
	 * Maximum label display length in characters.
	 */
	private static final int LABEL_ABBREVIATION_LENGTH = 64;

	/**
	 * How much of component's height should take zoom area grid.
	 */
	private static final float ZOOM_CELLS_PROPORTION = 0.25f;

	/**
	 * Link to main component.
	 */
	private final CorrelationMatrix matrix;

	// region Current presentational state

	/**
	 * Current data cell size, depends on component preferred size
	 */
	private double cellSize;

	/**
	 * Current font for title labels. Size depends on component's preferred size, for family see {@link CorrelationMatrix#labelsFont}.
	 */
	private Font labelsFont;

	/**
	 * Current component's height without borders
	 */
	double cellsHeight;

	/**
	 * Current component's width without borders
	 */
	double cellsWidth;

	private BasicStroke ellipseStroke;
	private final BasicStroke zoomSelectionBorderStroke;
	private final BasicStroke zoomBorderStroke;

	/**
	 * If highlight is active, contains indexes of cell which triggered it, {@code null} otherwise.
	 */
	private /* Nullable */ CellIndex highlightIndex;

	/**
	 * If zoom is active, represents current zoom model, {@code null} otherwise.
	 */
	private /* Nullable */ Zoom zoom;

	// endregion

	/**
	 * Creates correlation grid component, see
	 * {@link CorrelationMatrix#CorrelationMatrix(java.util.List, java.util.List, double[][], double[][])}.
	 */
	CorrelationMatrixGrid(CorrelationMatrix matrix)
	{
		this.matrix = matrix;
		addMouseListener(this);
		addMouseMotionListener(this);
		setOpaque(false);
		ToolTipManager.sharedInstance().registerComponent(this);

		// caching common values
		ellipseStroke = new BasicStroke(matrix.getEllipseStrokeWidth());
		zoomSelectionBorderStroke = new BasicStroke(matrix.getZoomSelectionBorderWidth());
		zoomBorderStroke = new BasicStroke(matrix.getZoomBorderWidth());
		setBorder(BorderFactory.createLineBorder(matrix.getGridBorderColor(), matrix.getGridBorderWidth()));
	}

	// region Painting methods

	/**
	 * Paints this component (data cells, column titles, highlights and zoom with its own cells) into given
	 * graphical context. Calculates all required sizes and coordinates depending on current {@link #labelsFont} and
	 * {@link #cellSize} properties (as well as current bounds of component). Font size and cell size are set by
	 * {@link #getPreferredSize()}.
	 *
	 * @param g graphical context
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintComponent(g);

		// translating to be not aware of border during calculations
		g2d.translate(matrix.getGridBorderWidth(), matrix.getGridBorderWidth());
		cellsWidth = getWidth() - matrix.getGridBorderWidth() * 2;
		cellsHeight = getHeight() - matrix.getGridBorderWidth() * 2;

		// Painting cells and highlights. 
		// In case of compact mode - highlights are drawn over cells since cells are 
		// not assumed to be transparent.
		if (isCompact())
		{
			paintCells(g2d);
			paintHighlights(g2d);
		}
		else
		{
			paintHighlights(g2d);
			paintCells(g2d);
		}

		// painting vertical grid lines
		g2d.setColor(matrix.getGridLinesColor());
		g2d.setStroke(new BasicStroke(matrix.getGridLinesWidth()));
		for (int i = 1; i <= matrix.length(); i++)
		{
			int x = ceil(cellsWidth - cellSize * i);
			g2d.drawLine(x, 0, x, ceil(cellsHeight));
		}

		// painting horizontal grid lines
		for (int i = 1; i <= matrix.length() - 1; i++)
		{
			int y = ceil(i * cellSize);
			g2d.drawLine(0, y, ceil(cellsWidth), y);
		}

		// painting rows titles
		double labelMargin = (1 - LABEL_HEIGHT_PROPORTION) * cellSize / 2;
		g2d.setColor(matrix.getLabelsColor());
		g2d.setFont(labelsFont);
		for (int i = 0; i < matrix.length(); i++)
		{
			String label = abbreviate(matrix.getTitles().get(i));
			int x = ceil(labelMargin);
			int y = ceil((i + 1) * cellSize - labelMargin);
			g2d.drawString(label, x, y);
		}

		// painting zoom
		paintZoom(g2d);

		g2d.translate(-matrix.getGridBorderWidth(), -matrix.getGridBorderWidth());
	}

	/**
	 * If highlight is currently active - paints highlight lines, does nothing otherwise.
	 *
	 * @param g2d graphical context
	 */
	private void paintHighlights(Graphics2D g2d)
	{
		if (highlightIndex != null)
		{
			int i = highlightIndex.i;
			int j = highlightIndex.j;
			g2d.setColor(matrix.getHighlightColor());

			// painting horizontal highlight
			int x = 0;
			int y = ceil(j * cellSize);
			int width = ceil(cellsWidth);
			int height = ceil(cellSize - matrix.getGridLinesWidth() / 2);
			g2d.fillRect(x, y, width, height);

			// painting vertical highlight
			x = ceil(cellsWidth - (matrix.length() - i) * cellSize);
			y = 0;
			width = ceil(cellSize - matrix.getGridLinesWidth() / 2);
			height = ceil(cellsHeight);
			g2d.fillRect(x, y, width, height);
		}
	}

	/**
	 * Creates and paints grid cells content into give graphical context.
	 * Skips diagonal cells and cells over the diagonal in not compact mode.
	 *
	 * @param g2d component's graphical context
	 */
	private void paintCells(Graphics2D g2d)
	{
		g2d.setStroke(ellipseStroke);
		for (int i = 0; i < matrix.length(); i++)
		{
			for (int j = 0; j < matrix.length(); j++)
			{
				if (j > i || (isCompact() && j != i))
				{
					Cell cell = createCell(i, j);
					paintCell(g2d, cell);
				}
			}
		}
	}

	/**
	 * Paints given cell content into given graphical context. Depending on current display mode
	 * (@link {@link #isCompact()}), cell is draws as rotated squeezed ellipse or just rectangle.
	 * Figure is filled with interpolated color. Cells with {@code NaN} values are skipped.
	 *
	 * @param g2d component's graphical context
	 * @param cell cell model
	 */
	private void paintCell(Graphics2D g2d, Cell cell)
	{
		// NaN cell should not be displayed
		if (Double.isNaN(cell.value))
		{
			return;
		}

		// preparing shape properties
		double margin = cell.size * (1 - CIRCLE_HEIGHT_PROPORTION) / 2;
		double radiusY = cell.size - margin * 2;
		double radiusX = radiusY * (1.0 - Math.abs(cell.value) * SQUEEZE_COEFFICIENT);
		Color fillColor;
		double rotation;
		if (cell.value > 0)
		{
			fillColor = matrix.getPositiveColor();
			rotation = Math.PI / 4;
		}
		else
		{
			fillColor = matrix.getNegativeColor();
			rotation = -Math.PI / 4;
		}
		// interpolating (mixing) fill color between main color (positive or negative) and background color
		g2d.setColor(interpolateColor(fillColor, matrix.getBackground(), Math.abs(cell.value)));

		// Ceiling coordinates to avoid resize flickering and painting cell
		if (!cell.compact)
		{
			// ...as squeezed rotated oval with fill and stroke.
			AffineTransform oldTransform = g2d.getTransform();
			AffineTransform transform = (AffineTransform) oldTransform.clone();
			// rotating context around cell's center
			transform.rotate(rotation, cell.x + cell.size / 2, cell.y + cell.size / 2);
			g2d.setTransform(transform);
			int x = ceil(cell.x + (cell.size - radiusX) / 2);
			int y = ceil(cell.y + margin);
			g2d.fillOval(x, y, ceil(radiusX), ceil(radiusY));
			g2d.setColor(matrix.getEllipseStrokeColor());
			g2d.setStroke(ellipseStroke);
			g2d.drawOval(x, y, ceil(radiusX), ceil(radiusY));
			g2d.setTransform(oldTransform);
		}
		else
		{
			/// ...as filled rectangle.
			g2d.fillRect(ceil(cell.x), ceil(cell.y), ceil(cell.size), ceil(cell.size));
		}
	}

	/**
	 * If zoom is active - paints current zoom model into given graphical context.
	 * Does nothing otherwise.
	 *
	 * @param g2d graphical context
	 */
	private void paintZoom(Graphics2D g2d)
	{
		if (zoom != null)
		{
			// painting zoom selection border in main grid
			g2d.setColor(matrix.getZoomSelectionBorderColor());
			g2d.setStroke(zoomSelectionBorderStroke);
			int selectionX = ceil(cellsWidth - (matrix.length() - zoom.i) * cellSize);
			int selectionY = ceil(zoom.j * cellSize);
			g2d.drawRect(selectionX, selectionY, ceil(zoom.zoomSelectionSize), ceil(zoom.zoomSelectionSize));

			// clearing zoom area
			g2d.setBackground(matrix.getBackground());
			g2d.setFont(zoom.font);
			int x = ceil(zoom.x);
			int y = ceil(zoom.y);
			int width = ceil(zoom.width);
			int height = ceil(zoom.height);
			g2d.clearRect(x, y, width, height);

			// painting cells in zoom area
			g2d.setStroke(ellipseStroke);
			double cellsStartX = zoom.x + zoom.width - zoom.cellsSize;
			double cellsStartY = zoom.y + zoom.height - zoom.cellsSize;
			for (int l = 0; l < zoom.length; l++)
			{
				for (int m = 0; m < zoom.length; m++)
				{
					int i = zoom.i + l;
					int j = zoom.j + m;
					if (i != j) // skipping diagonal
					{
						// creating and painting zoom grid cell
						Cell cell = new Cell();
						cell.x = (cellsStartX + l * zoom.cellSize);
						cell.y = (cellsStartY + m * zoom.cellSize);
						cell.compact = isCompact();
						cell.size = zoom.cellSize;
						cell.value = getValue(i, j);
						paintCell(g2d, cell);
					}
				}
			}

			// painting grid in zoom area
			g2d.setColor(matrix.getGridLinesColor());
			g2d.setStroke(new BasicStroke(matrix.getGridLinesWidth()));
			for (int k = 0; k < zoom.length; k++)
			{
				// horizontal lines
				double lineY = zoom.y + zoom.height - k * zoom.cellSize;
				int x1 = ceil(zoom.x);
				int y1 = ceil(zoom.y + lineY - zoom.cellSize);
				int x2 = ceil(zoom.width + zoom.x);
				int y2 = ceil(zoom.y + lineY - zoom.cellSize);
				g2d.drawLine(x1, y1, x2, y2);

				// vertical lines
				x1 = ceil(zoom.x + zoom.width - zoom.cellsSize + zoom.cellSize * k);
				y1 = ceil(zoom.y);
				x2 = x1;
				y2 = ceil(zoom.y + zoom.height);
				g2d.drawLine(x1, y1, x2, y2);
			}

			// painting labels in zoom area grid
			g2d.setColor(matrix.getLabelsColor());
			for (int l = 0; l < zoom.length; l++)
			{
				// painting horizontal label
				String label = abbreviate(zoom.horizontalLabels.get(l));
				int labelX = ceil(zoom.x + zoom.labelsMargin);
				int labelY = ceil(zoom.height - zoom.cellsSize + zoom.cellSize * (l + 1) - zoom.labelsMargin);
				g2d.drawString(label, labelX, labelY);

				// painting vertical label
				label = abbreviate(zoom.verticalLabels.get(l));
				AffineTransform oldTransform = g2d.getTransform();
				AffineTransform transform = (AffineTransform) oldTransform.clone();
				double rotationX = zoom.x + zoom.horizontalLabelsWidth + (l + 1) * zoom.cellSize;
				@SuppressWarnings("SuspiciousNameCombination")
				double rotationY = zoom.verticalLabelsWidth;
				transform.rotate(-Math.PI / 2, rotationX, rotationY);
				g2d.setTransform(transform);
				g2d.drawString(label, ceil(rotationX + zoom.labelsMargin), ceil(rotationY - zoom.labelsMargin));
				g2d.setTransform(oldTransform);
			}

			// painting zoom area border
			g2d.setStroke(zoomBorderStroke);
			g2d.setColor(matrix.getZoomBorderColor());
			g2d.drawRect(x, y, width, height);
		}
	}

	// endregion
	
	/**
	 * Creates correlation cell model with pre-calculated coordinates for given data coordinates.
	 *
	 * @param i row index
	 * @param j column index
	 * @return cell model
	 */
	Cell createCell(int i, int j)
	{
		Cell cell = new Cell();
		cell.x = cellsWidth - (matrix.length() - i) * cellSize;
		cell.y = j * cellSize;
		cell.value = getValue(i, j);
		cell.size = cellSize;
		cell.compact = isCompact();
		return cell;
	}

	/**
	 * Creates zoom model with pre-calculated coordinates for it's components.
	 *
	 * @param coordinates the coordinates of cell which was active during zoom initiation.
	 * @return {@link Zoom} model
	 */
	Zoom createZoom(CellIndex coordinates)
	{
		Zoom zoom = new Zoom();

		// zooming defined amount of cells, or less, if there is no so much cells.
		zoom.length = Math.min(matrix.getZoomLength(), matrix.length());
		zoom.zoomSelectionSize = zoom.length * cellSize;

		// trying to zoom area with initiator cell at center, shifting if is not possible
		zoom.i = Math.min(Math.max(coordinates.i - zoom.length / 2, 0), matrix.length() - zoom.length);
		zoom.j = Math.min(Math.max(coordinates.j - zoom.length / 2, 0), matrix.length() - zoom.length);

		// entry point for sizes calculation is the proportion of zoom grid with main grid's height
		zoom.cellsSize = cellsHeight * ZOOM_CELLS_PROPORTION;
		zoom.cellSize = zoom.cellsSize / zoom.length;

		// gathering, measuring and abbreviating (if needed) horizontal labels
		zoom.labelsMargin = zoom.cellSize * (1 - LABEL_HEIGHT_PROPORTION) / 2;
		zoom.font = matrix.getFont().deriveFont((float) zoom.cellSize * LABEL_HEIGHT_PROPORTION);
		zoom.horizontalLabels = new ArrayList<>(matrix.getTitles().subList(zoom.j, zoom.j + zoom.length));
		zoom.horizontalLabelsWidth = getLabelsWidth(zoom.horizontalLabels, zoom.font) + zoom.labelsMargin * 2;

		// ensuring labels to fit component's size
		double maxHorizontalLabelsWidth = cellsWidth - zoom.cellsSize - 2 * zoom.labelsMargin;
		if (zoom.horizontalLabelsWidth > maxHorizontalLabelsWidth)
		{
			abbreviate(zoom.horizontalLabels, zoom.font, maxHorizontalLabelsWidth);
			zoom.horizontalLabelsWidth = maxHorizontalLabelsWidth;
		}

		// gathering, measuring and abbreviating (if needed) vertical labels
		zoom.verticalLabels = new ArrayList<>(matrix.getTitles().subList(zoom.i, zoom.i + zoom.length));
		zoom.verticalLabelsWidth = getLabelsWidth(zoom.verticalLabels, zoom.font) + zoom.labelsMargin * 2;

		// ensuring labels to fit component's size
		double maxVerticalLabelsWidth = cellsHeight - zoom.cellsSize;
		if (zoom.verticalLabelsWidth > maxVerticalLabelsWidth)
		{
			abbreviate(zoom.verticalLabels, zoom.font, maxVerticalLabelsWidth);
			zoom.verticalLabelsWidth = maxVerticalLabelsWidth;
		}

		zoom.width = zoom.horizontalLabelsWidth + zoom.cellsSize;
		zoom.height = zoom.verticalLabelsWidth + zoom.cellsSize;
		zoom.x = cellsWidth - zoom.width;
		zoom.y = 0;

		return zoom;
	}

	/**
	 * Calculates preferred size for this component taking into account available parent size
	 * (with respect to {@link TemperatureScale}) and required proportion.
	 * <br/>
	 * Firstly, checks if component will fit horizontally (with free space above and below), or vertically (with free space on the right and on the left side).
	 * Then, using synthetically calculated label's cell proportion calculate matching dimension.
	 * Afterwards, set's components {@link #cellSize}, which will be used during painting.
	 * Font sizing not always is proportional, so at the final step this method checks how accurate labels are matching their space and modifies
	 * {@link #cellSize} according to the result (and sets component's {@link #labelsFont}, which will be used during painting).
	 *
	 * @return the preferred size for this correlation matrix grid
	 */
	@Override
	public Dimension getPreferredSize()
	{
		double availableWidth = matrix.getWidth() - matrix.getTemperatureScalePanel().getPreferredSize().width
				- matrix.getGridMargin() * 2;
		double availableHeight = matrix.getHeight() - matrix.getGridMargin() * 2;
		double borders = matrix.getGridBorderWidth() * 2;

		// calculating title cell proportion for synthetic  font height


		double testHeight = Math.min(availableHeight - borders, availableWidth - borders);
		double testFontHeight = (testHeight / matrix.length());
		double testLabelWidth = getLabelsWidth(matrix.getTitles(), matrix.getFont().deriveFont((float) testFontHeight));
		double testLabelMargin = testFontHeight * (1 - LABEL_HEIGHT_PROPORTION) / 2;
		double labelCellProportion = (testLabelWidth + testLabelMargin * 2) / (testFontHeight + testLabelMargin * 2);

		boolean horizontalFit;
		if (availableHeight > availableWidth)
		{
			// free size is vertical rectangle, but grid is always horizontal one
			horizontalFit = true;
		}
		else
		{
			// Let's assume that component's size is exact as available size (may be not in reality).
			// Reason - to check if there would be enough (or more) space for labels if so.
			// If enough - the grid is fitting vertically or exactly, and cell size becomes known.

			double labelsWidth = availableWidth - availableHeight;
			double requiredLabelsWidth = ((availableHeight - borders) / matrix.length()) * labelCellProportion;
			horizontalFit = !(labelsWidth > requiredLabelsWidth);
		}

		if (horizontalFit)
		{
			// In case of horizontal fit width is known, but height is unknown. 
			// Let's calculate it using system of equations:
			//
			// (height - borders) / length = label cell height
			// label cell width / label cell height = label cell proportion
			// width = label cell width + height
			//
			// as result:
			//
			// label cell height = (width - border * 2) / (length + label cell proportion)

			cellSize = (availableWidth - borders) / (matrix.length() + labelCellProportion);
		}
		else
		{
			cellSize = (availableHeight - borders) / matrix.length();
		}

		labelsFont = matrix.getFont().deriveFont((float) cellSize * LABEL_HEIGHT_PROPORTION);

		// Now label width becomes an entry point for calculations.
		// Correcting cell size if label is too long because of not proportional font scaling.
		double labelWidth = getLabelsWidth(matrix.getTitles(), labelsFont);
		double labelMargins = cellSize * (1 - LABEL_HEIGHT_PROPORTION);
		double error = availableWidth - borders - cellSize * matrix.length() - labelWidth - labelMargins;
		// to get less jerking - only reducing cell, not enlarging 
		cellSize = Math.min(cellSize + error / matrix.length(), cellSize);
		labelMargins = cellSize * (1 - LABEL_HEIGHT_PROPORTION);

		// calculating sizes based on cell size
		if (horizontalFit)
		{
			int width = (int) availableWidth;
			int height = (int) (matrix.length() * cellSize + borders);
			return new Dimension(width, height);
		}
		else
		{
			int width = (int) (cellSize * matrix.length() + labelWidth + labelMargins + borders);
			int height = (int) availableHeight;
			return new Dimension(width, height);
		}
	}

	// region Interactivity
	
	/**
	 * Detects if mouse was pressed on data cell, title cell or elsewhere.
	 * In case of title cell - enables highlights (store title's index).
	 * In case of data cell - creates zoom model.
	 * Removes current highlight otherwise. Requests component's repaint.
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(MouseEvent e)
	{
		Optional<CellIndex> optionalZoomIndex = detectCell(e.getX(), e.getY());
		if (optionalZoomIndex.isPresent())
		{
			zoom = createZoom(optionalZoomIndex.get());
		}
		else
		{
			Optional<CellIndex> optionalHighlightIndex = detectTitleCell(e.getX(), e.getY());
			highlightIndex = optionalHighlightIndex.orElse(null);
		}
		repaint();
	}

	/**
	 * I zoom is currently active - removes it's model and triggers component's repaint.
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (zoom != null)
		{
			zoom = null;
			repaint();
		}
	}

	/**
	 * Detects if mouse was dragged with active zoom or highlight feature.
	 * If so - check whether feature's data coordinates were changed.
	 * If so - updates its model and triggers repaint.
	 * If mouse was dragged outside feature's cells - disables it.
	 * {@inheritDoc}
	 */
	@Override
	public void mouseDragged(MouseEvent e)
	{
		boolean repaint = false;
		if (zoom != null)
		{
			// updating or removing current zoom if needed

			Optional<CellIndex> optionalIndex = detectCell(e.getX(), e.getY());
			if (optionalIndex.isPresent())
			{
				CellIndex nextZoomIndex = optionalIndex.get();
				if (nextZoomIndex.i != zoom.i || nextZoomIndex.j != zoom.j)
				{
					zoom = createZoom(optionalIndex.get());
					repaint = true;
				}
			}
			else
			{
				// mouse was dragged away the data cells area
				zoom = null;
				repaint = true;
			}
		}

		if (highlightIndex != null)
		{
			// updating or removing current highlight if needed

			Optional<CellIndex> optionalIndex = detectTitleCell(e.getX(), e.getY());
			if (optionalIndex.isPresent())
			{
				CellIndex index = optionalIndex.get();
				if (index.j != highlightIndex.i && index.j != highlightIndex.j)
				{
					highlightIndex = index;
					repaint = true;
				}
			}
			else
			{
				// mouse was dragged away the titles cells are
				highlightIndex = null;
				repaint = true;
			}
		}

		if (repaint)
		{
			repaint();
		}
	}

	/**
	 * Returns tooltip text with column title, if mouse if over the row title label.
	 * Returns tooltip with correlations and titles of correlating rows if mouse is over data cell.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public String getToolTipText(MouseEvent event)
	{
		Optional<CellIndex> optionalIndex = detectCell(event.getX(), event.getY());

		if (optionalIndex.isPresent())
		{
			// mouse is above the data cell
			CellIndex index = optionalIndex.get();
			int i = index.i;
			int j = index.j;
			RowType rowTypeI = matrix.getDataTypes().get(i);
			RowType rowTypeJ = matrix.getDataTypes().get(j);

			// creating HTML text which describes rows and correlations
			String result = "<html>";
			result += matrix.getTitles().get(i) + "<br/>";
			result += matrix.getTitles().get(j) + "<br/>";

			// detecting type of correlation
			if (rowTypeI == RowType.NUMERIC && rowTypeJ == RowType.NUMERIC)
			{
				// correlation of two numeric values
				result += "Pearson's R\u00B2 = " + formatCorrelationValue(matrix.getCorrelationsSqr()[i][j]) + "<br/>";
				result += "Pearson's R = " + formatCorrelationValue(matrix.getCorrelations()[i][j]);
			}
			else if (rowTypeI == RowType.NOMINAL && rowTypeJ == RowType.NOMINAL)
			{
				// correlation of numeric and nominal value
				result += "Cramer's V = " + formatCorrelationValue(matrix.getCorrelationsSqr()[i][j]);
			}
			else
			{
				// correlation of two nominal values
				result += "ANOVA R\u00B2 = " + formatCorrelationValue(matrix.getCorrelationsSqr()[i][j]);
			}

			result += "</html>";
			return result;
		}
		else
		{
			Optional<CellIndex> titleIndex = detectTitleCell(event.getX(), event.getY());
			if (titleIndex.isPresent())
			{
				return "<html><p>" + matrix.getTitles().get(titleIndex.get().i) + "</p></html>";
			}
		}
		return super.getToolTipText(event);
	}

	/**
	 * Creates custom tooltip for this correlation matrix grid, with respect to tooltip presentational properties specified in {@link CorrelationMatrix},
	 * like padding and border. Uses custom subclass of tooltip ({@link AntiAliasingToolTip}) to provide it's rendering anti-aliasing.
	 *
	 * @return created tooltip.
	 */
	@Override
	public JToolTip createToolTip()
	{
		JToolTip tooltip = new AntiAliasingToolTip();
		tooltip.setFont(matrix.getFont().deriveFont(matrix.getTooltipFontSize()));
		tooltip.setForeground(matrix.getToolTipTextColor());
		tooltip.setBackground(matrix.getToolTipBackgroundColor());
		int padding = matrix.getTooltipPadding();
		CompoundBorder border = new CompoundBorder(
				BorderFactory.createLineBorder(matrix.getToolTipBorderColor(), matrix.getToolTipBorderWidth()),
				BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		tooltip.setBorder(border);
		return tooltip;
	}

	// endregion
	
	/**
	 * Returns correlation value (square) for given cell coordinates. Source - {@link CorrelationMatrix#correlationsSqr}.
	 *
	 * @param i row index
	 * @param j column index
	 * @return correlation square value
	 */
	double getValue(int i, int j)
	{
		double value;
		if (matrix.getCorrelations()[i][j] < 0)
		{
			value = -matrix.getCorrelationsSqr()[i][j];
		}
		else
		{
			value = matrix.getCorrelationsSqr()[i][j];
		}
		return value;
	}

	/**
	 * Returns the graphical width (in pixels) of graphically longest label of given ones for given font.
	 * Abbreviates labels with {@link #abbreviate(java.lang.String)} before measuring.
	 *
	 * @param labels labels to measure
	 * @param font font to use during measuring
	 * @return width in pixels of graphically longest label
	 */
	private double getLabelsWidth(List<String> labels, Font font)
	{
		return labels.stream().mapToDouble(title -> getFontMetrics(font).stringWidth(abbreviate(title))).max()
				.orElseThrow(IllegalStateException::new);
	}

	/**
	 * Returns whether matrix must be displayed in compact mode (correlation cells as filled rectangles).
	 * {@see com.earnix.eo.gui.correlation.CorrelationMatrix#compactCellSize}
	 *
	 * @return {@code true} if matrix must be displayed in compact mode
	 */
	boolean isCompact()
	{
		return cellSize < matrix.getCompactCellSize();
	}

	/**
	 * Detects if given coordinates correspond to the grid cell with correlation data
	 * and returns cell indexes if so.
	 *
	 * @param x x coordinate on component
	 * @param y y coordinate on component
	 * @return Cell indexes if there is a cell on given on given coordinates, {@link Optional#empty()} otherwise.
	 */
	private Optional<CellIndex> detectCell(int x, int y)
	{
		double cellsStart = getWidth() - cellSize * matrix.length() - matrix.getGridBorderWidth();
		if (x > cellsStart && y > matrix.getGridBorderWidth() && y < matrix.getWidth() - matrix.getGridBorderWidth())
		{
			int i = (int) ((x - cellsStart) / cellSize);
			int j = (int) (y / cellSize);
			if (i < matrix.length() && j < matrix.length())
			{
				return Optional.of(new CellIndex(i, j));
			}
		}
		return Optional.empty();
	}

	/**
	 * Detects if given coordinates correspond to the grid cell with row title
	 * and returns cell indexes of data representing row correlation with itself.
	 *
	 * @param x x coordinate ong component
	 * @param y y coordinate on component
	 * @return If coordinates match title label - cell indexes of data representing row correlation with itself,
	 * {@link Optional#empty()} otherwise.
	 */
	private Optional<CellIndex> detectTitleCell(int x, int y)
	{
		// taking border into account
		boolean xMatches = x < getWidth() - cellSize * matrix.length() - matrix.getGridBorderWidth();
		boolean yMatches = y > matrix.getGridBorderWidth() && y < getHeight() - matrix.getGridBorderWidth();
		if (xMatches && yMatches)
		{
			int ij = (int) ((y - matrix.getGridBorderWidth()) / cellSize);
			return Optional.of(new CellIndex(ij, ij));
		}
		else
		{
			return Optional.empty();
		}
	}

	/**
	 * Creates string for double data value, keeping 4 decimal places.
	 * In case of {@link Double#NaN}, returns "N/A".
	 *
	 * @param value value to format
	 * @return formatted value as {@link String}
	 */
	String formatCorrelationValue(double value)
	{
		if (Double.isNaN(value))
		{
			return "N/A";
		}
		else
		{
			return String.format("%.4f", value);
		}
	}

	/**
	 * Checks whether given width is enough for given labels in case they are rendered with given font.
	 * If not enough - required number of letters is removed from the end, and 3 more letter at the end are replaced
	 * with "...".
	 *
	 * @param labels labels to check and replace with modified if needed
	 * @param labelsFont font to use while measuring
	 * @param maxWidth maximum allowed width for given labels
	 */
	private void abbreviate(List<String> labels, Font labelsFont, double maxWidth)
	{
		for (int i = 0; i < labels.size(); i++)
		{
			String label = labels.get(i);
			TextLayout layout = new TextLayout(label, labelsFont,
					getGraphics().getFontMetrics().getFontRenderContext());
			TextHitInfo hit = layout.hitTestChar((float) maxWidth, 0);
			int charIndex = hit.getCharIndex();
			labels.set(i, abbreviate(label, charIndex));
		}
	}

	/**
	 * Abbreviates given label by removing all symbols after length defined by {@link #LABEL_ABBREVIATION_LENGTH}.
	 * The last 3 symbols which last will be replaced with dots for presentational purpose.
	 * <br/>
	 * The same label will be returned if it's length is below {@link #LABEL_ABBREVIATION_LENGTH}
	 *
	 * @param label the label to abbreviate if needed
	 * @return the same label or label with length equal to {@link #LABEL_ABBREVIATION_LENGTH} and 3 dots at the end
	 */
	private static String abbreviate(String label)
	{
		return abbreviate(label, LABEL_ABBREVIATION_LENGTH);
	}

	/**
	 * Abbreviates given label by removing all symbols after given length.
	 * The last 3 symbols which last will be replaced with dots for presentational purpose.
	 * <br/>
	 * The same label will be returned if it's length is less or equal given one.
	 *
	 * @param label the label to abbreviate if needed
	 * @return the same label or label with length equal to the given length and 3 dots at the end.
	 */
	static String abbreviate(String label, int length)
	{
		if (label.length() > length)
		{
			return label.substring(0, length - 3) + "...";
		}
		else
		{
			return label;
		}
	}

	/**
	 * Returns equal or next larger {@code int} value for given {@code double}.
	 *
	 * @param value given value
	 * @return equal or next larger {@code int}
	 */
	static int ceil(double value)
	{
		return (int) Math.ceil(value);
	}

	/**
	 * Interpolates (mixes) given colors by applying proportional addition of color components.
	 *
	 * @param color1 the first color to mix
	 * @param color2 the second color to mix
	 * @param interpolation the proportion of first color in resulting one
	 * @return the resulting mixed color
	 */
	static Color interpolateColor(Color color1, Color color2, double interpolation)
	{
		int red = (int) (color1.getRed() * interpolation + color2.getRed() * (1 - interpolation));
		int green = (int) (color1.getGreen() * interpolation + color2.getGreen() * (1 - interpolation));
		int blue = (int) (color1.getBlue() * interpolation + color2.getBlue() * (1 - interpolation));
		return new Color(red, green, blue);
	}

	/**
	 * Required implementation of {@link MouseListener}.
	 * {@inheritDoc}
	 */
	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	/**
	 * Required implementation of {@link MouseMotionListener}.
	 * {@inheritDoc}
	 */
	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	/**
	 * Required implementation of {@link MouseMotionListener}.
	 * {@inheritDoc}
	 */
	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	/**
	 * Required implementation of {@link MouseMotionListener}.
	 * {@inheritDoc}
	 */
	@Override
	public void mouseMoved(MouseEvent e)
	{
	}
}
