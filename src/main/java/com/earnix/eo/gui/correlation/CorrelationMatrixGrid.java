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
	 * Number of cells (square side) which are included in zoom by default.
	 * If data consist of less rows - all of them will be included.
	 */
	private static final int ZOOM_LENGTH = 5;

	/**
	 * A proportion of oval in cell.
	 */
	private final float CIRCLE_HEIGHT_PROPORTION = 0.8f;

	/**
	 * A proportion of label in title cell (height).
	 */
	private final float LABEL_HEIGHT_PROPORTION = 0.8f;

	/**
	 * Coefficient which restricts oval from squeezing too much.
	 */
	private static final float SQUEEZE_COEFFICIENT = 0.8f;

	/**
	 * Maximum label display length in characters.
	 */
	private static final int LABEL_ABBREVIATION_LENGTH = 64;

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

	private double cellsHeight;
	private double cellsWidth;
	private BasicStroke ellipseStroke;

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
		setBorder(BorderFactory.createLineBorder(matrix.getBorderColor(), matrix.getBorderWidth()));
	}

	// region Painting methods

	/**
	 * Paints this component (data cells, column titles, highlights and zoom) into given graphical context.
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

		cellsWidth = getWidth() - matrix.getBorderWidth() * 2;
		cellsHeight = getHeight() - matrix.getBorderWidth() * 2;

		// translating 
		g2d.translate(matrix.getBorderWidth(), matrix.getBorderWidth());

		double labelMargin = (1 - LABEL_HEIGHT_PROPORTION) * cellSize / 2;

		// Painting cells and highlights. 
		// In case of compact mode - highlights are drawn over cells since sells are 
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

		// Painting vertical grid lines
		g2d.setColor(matrix.getGridLinesColor());
		g2d.setStroke(new BasicStroke(matrix.getGridLinesWidth()));
		for (int i = 1; i <= matrix.length(); i++)
		{
			int x = (int) Math.ceil(cellsWidth - cellSize * i);
			g2d.drawLine(x, 0, x, (int) Math.ceil(cellsHeight));
		}

		// drawing horizontal grid lines
		for (int i = 1; i <= matrix.length() - 1; i++)
		{
			int y = (int) Math.ceil(i * cellSize);
			g2d.drawLine(0, y, (int) Math.ceil(cellsWidth), y);
		}

		// drawing rows titles
		g2d.setColor(matrix.getLabelsColor());
		g2d.setFont(labelsFont);
		for (int i = 0; i < matrix.length(); i++)
		{
			String label = abbreviate(matrix.getTitles().get(i));
			g2d.drawString(label, (int) labelMargin, (int) ((i + 1) * cellSize - labelMargin));
		}

		// painting zoom
		paintZoom(g2d);

		g2d.translate(-matrix.getBorderWidth(), -matrix.getBorderWidth());
	}

	/**
	 * If highlight is currently active, paints highlight lines.
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
			g2d.fillRect(0, (int) (j * cellSize), (int) cellsWidth, (int) Math.round(cellSize));
			g2d.fillRect((int) (cellsWidth - (matrix.length() - i) * cellSize), 0, (int) Math.round(cellSize),
					(int) cellsHeight);
		}
	}

	/**
	 * Creates data cell model for given data coordinates
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
	 * Paints correlation matrix cell into give graphical context
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
	 * Paints given cell into given graphical context
	 *
	 * @param g2d component's graphical context
	 * @param cell cell model
	 */
	private void paintCell(Graphics2D g2d, Cell cell)
	{
		// NaN cell should
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

		// interpolating fill color between main (positive or negative) and white
		double interpolation = Math.abs(cell.value);

		g2d.setColor(interpolateColor(fillColor, matrix.getBackground(), interpolation));

		// drawing cell
		if (!cell.compact)
		{
			AffineTransform currentTransform = g2d.getTransform();
			AffineTransform nextTransform = (AffineTransform) currentTransform.clone();
			nextTransform.rotate(rotation, cell.x + cell.size / 2, cell.y + cell.size / 2);
			g2d.setTransform(nextTransform);
			int x = (int) Math.ceil(cell.x + (cell.size - radiusX) / 2);
			int y = (int) Math.ceil(cell.y + margin);

			g2d.fillOval(x, y, (int) Math.ceil(radiusX), (int) Math.ceil(radiusY));
			g2d.setColor(matrix.getEllipseStrokeColor());
			g2d.setStroke(ellipseStroke);
			g2d.drawOval(x, y, (int) Math.ceil(radiusX), (int) Math.ceil(radiusY));
			g2d.setTransform(currentTransform);
		}
		else
		{
			g2d.fillRect((int) Math.ceil(cell.x), (int) Math.ceil(cell.y), (int) Math.ceil(cell.size),
					(int) Math.ceil(cell.size));
		}
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
		zoom.length = Math.min(ZOOM_LENGTH, matrix.length());
		zoom.i = Math.min(Math.max(coordinates.i - zoom.length / 2, 0), matrix.length() - zoom.length);
		zoom.j = Math.min(Math.max(coordinates.j - zoom.length / 2, 0), matrix.length() - zoom.length);
		zoom.zoomSelectionSize = zoom.length * cellSize;

		// zoom cell size should take 1/4 of space
		zoom.cellsSize = cellsHeight / 4;
		zoom.cellSize = zoom.cellsSize / zoom.length;

		zoom.labelsMargin = zoom.cellSize * (1 - LABEL_HEIGHT_PROPORTION) / 2;
		zoom.font = matrix.getFont().deriveFont((float) zoom.cellSize * LABEL_HEIGHT_PROPORTION);

		// preparing, measuring and abbreviating horizontal labels
		zoom.horizontalLabels = new ArrayList<>(matrix.getTitles().subList(zoom.j, zoom.j + zoom.length));
		zoom.horizontalLabelsWidth = getLabelsWidth(zoom.horizontalLabels, zoom.font) + zoom.labelsMargin * 2;
		double maxHorizontalLabelsWidth = cellsWidth - zoom.cellsSize - 2 * zoom.labelsMargin;
		if (zoom.horizontalLabelsWidth > maxHorizontalLabelsWidth)
		{
			abbreviate(zoom.horizontalLabels, zoom.font, maxHorizontalLabelsWidth);
			zoom.horizontalLabelsWidth = maxHorizontalLabelsWidth;
		}

		zoom.verticalLabels = new ArrayList<>(matrix.getTitles().subList(zoom.i, zoom.i + zoom.length));
		zoom.verticalLabelsWidth = getLabelsWidth(zoom.verticalLabels, zoom.font) + zoom.labelsMargin * 2;
		double maxVerticalLabelsWidth = cellsHeight - zoom.cellsSize;

		if (zoom.verticalLabelsWidth > maxVerticalLabelsWidth)
		{
			abbreviate(zoom.verticalLabels, zoom.font, maxVerticalLabelsWidth);
			zoom.verticalLabelsWidth = maxVerticalLabelsWidth;
		}

		zoom.cellsSize = zoom.cellSize * zoom.length;
		zoom.width = zoom.horizontalLabelsWidth + zoom.cellsSize;
		zoom.height = zoom.verticalLabelsWidth + zoom.cellsSize;
		zoom.x = cellsWidth - zoom.width;
		zoom.y = 0;

		return zoom;
	}

	/**
	 * Paints given zoom model into given graphical context
	 *
	 * @param g2d graphical context
	 */
	private void paintZoom(Graphics2D g2d)
	{
		if (zoom != null)
		{
			// drawing zoom selection border
			g2d.setColor(matrix.getZoomSelectionBorderColor());
			g2d.setStroke(new BasicStroke(matrix.getZoomSelectionBorderWidth()));
			g2d.drawRect((int) (cellsWidth - (matrix.length() - zoom.i) * cellSize), (int) (zoom.j * cellSize),
					(int) zoom.zoomSelectionSize, (int) zoom.zoomSelectionSize);

			// drawing zoom area
			g2d.setStroke(new BasicStroke(matrix.getZoomBorderWidth()));
			g2d.setColor(matrix.getZoomBorderColor());
			g2d.setBackground(matrix.getBackground());
			g2d.setFont(zoom.font);
			g2d.clearRect((int) zoom.x, (int) zoom.y, (int) zoom.width, (int) zoom.height);
			g2d.drawRect((int) zoom.x, (int) zoom.y, (int) zoom.width, (int) zoom.height);

			// drawing cells in zoom area
			g2d.setStroke(ellipseStroke);
			for (int l = 0; l < zoom.length; l++)
			{
				for (int m = 0; m < zoom.length; m++)
				{
					int i = zoom.i + l;
					int j = zoom.j + m;
					if (i != j) // skipping diagonal
					{
						int x = (int) (zoom.x + zoom.width - zoom.cellsSize + l * zoom.cellSize);
						int y = (int) (zoom.y + zoom.height - zoom.cellsSize + m * zoom.cellSize);

						// creating and painting  cell for zoom grid
						Cell cell = new Cell();
						cell.x = x;
						cell.y = y;
						cell.compact = isCompact();
						cell.size = zoom.cellSize;
						cell.value = getValue(i, j);
						paintCell(g2d, cell);
					}
				}
			}

			// drawing grid in zoom area
			g2d.setColor(matrix.getGridLinesColor());
			g2d.setStroke(new BasicStroke(matrix.getGridLinesWidth()));
			for (int k = 0; k < zoom.length; k++)
			{
				int y = (int) (zoom.y + zoom.height - k * zoom.cellSize);
				// horizontal lines
				g2d.drawLine((int) zoom.x, (int) (zoom.y + y - zoom.cellSize), (int) (zoom.width + zoom.x),
						(int) (zoom.y + y - zoom.cellSize));
				// vertical lines
				int x = (int) (zoom.x + zoom.width - zoom.cellsSize + zoom.cellSize * k);
				g2d.drawLine(x, (int) zoom.y, x, (int) (zoom.y + zoom.height));
			}


			// drawing labels in zoom area
			g2d.setColor(matrix.getLabelsColor());
			for (int l = 0; l < zoom.length; l++)
			{
				// painting horizontal label
				String label = abbreviate(zoom.horizontalLabels.get(l));
				g2d.drawString(label, (int) (zoom.x + zoom.labelsMargin),
						(int) (zoom.height - zoom.cellsSize + zoom.cellSize * (l + 1) - zoom.labelsMargin));

				// painting vertical label
				label = abbreviate(zoom.verticalLabels.get(l));
				AffineTransform oldTransform = g2d.getTransform();
				AffineTransform transform = (AffineTransform) oldTransform.clone();
				int vx = (int) (zoom.x + zoom.horizontalLabelsWidth + (l + 1) * zoom.cellSize);
				int vy = (int) (zoom.verticalLabelsWidth);
				transform.rotate(-Math.PI / 2, vx, vy);
				g2d.setTransform(transform);
				g2d.drawString(label, (int) (vx + zoom.labelsMargin), (int) (vy - zoom.labelsMargin));
				g2d.setTransform(oldTransform);
			}
		}
	}

	private double getLabelsWidth(List<String> labels, Font font)
	{
		return labels.stream().mapToDouble(title -> getFontMetrics(font).stringWidth(abbreviate(title))).max()
				.orElseThrow(IllegalStateException::new);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		Optional<CellIndex> optionalCellCoordinates = detectCell(e.getX(), e.getY());
		if (optionalCellCoordinates.isPresent())
		{
			zoom = createZoom(optionalCellCoordinates.get());
		}
		else if (e.getX() < getWidth() - cellSize * matrix.length() - matrix.getBorderWidth())
		{
			// label is pressed
			int ij = (int) (e.getY() / cellSize);
			highlightIndex = new CellIndex(ij, ij);
		}
		else
		{
			highlightIndex = null;
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		zoom = null;
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	/**
	 * Handles mouse dragging.
	 * If currently displaying zoom
	 *
	 * @param e
	 */
	@Override
	public void mouseDragged(MouseEvent e)
	{
		boolean repaint = false;
		if (zoom != null)
		{
			Optional<CellIndex> cellIndex = detectCell(e.getX(), e.getY());
			if (cellIndex.isPresent())
			{
				zoom = createZoom(cellIndex.get());
				repaint = true;
			}
		}

		if (highlightIndex != null)
		{
			if (e.getX() < getWidth() - cellSize * matrix.length() - matrix.getBorderWidth())
			{
				int ij = (int) (e.getY() / cellSize);
				highlightIndex = new CellIndex(ij, ij);
			}
			else
			{
				highlightIndex = null;
			}
			repaint = true;
		}
		if (repaint)
		{
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	/**
	 * Returns tooltip text with column title, if mouse if over label.
	 * Returns tooltip with correlations and both columns titles if mouse is over data cell
	 *
	 * @param event mouse event which triggered this tooltip
	 * @return tooltip text
	 */
	@Override
	public String getToolTipText(MouseEvent event)
	{
		repaint();
		Optional<CellIndex> optionalCellCoordinates = detectCell(event.getX(), event.getY());

		if (optionalCellCoordinates.isPresent())
		{
			// mouse is above data cell
			CellIndex cellCoordinates = optionalCellCoordinates.get();
			int i = cellCoordinates.i;
			int j = cellCoordinates.j;

			// creating HTML text displaying coordinates
			String text = "<html>";
			RowType dataTypeI = matrix.getDataTypes().get(i);
			RowType dataTypeJ = matrix.getDataTypes().get(j);
			text += matrix.getTitles().get(i) + "<br/>";
			text += matrix.getTitles().get(j) + "<br/>";
			if (dataTypeI == RowType.NUMERIC && (dataTypeJ == RowType.NUMERIC))
			{  // Numeric vs. Numeric
				text += "Pearson's R\u00B2 = " + formatCorrelationValue(matrix.getCorrelationsSqr()[i][j]) + "<br/>";
				text += "Pearson's R = " + formatCorrelationValue(matrix.getCorrelations()[i][j]);
			}
			else if (dataTypeI == RowType.NOMINAL && dataTypeJ == RowType.NOMINAL)
			{ // Nominal vs. Nominal

				text += "Cramer's V = " + formatCorrelationValue(matrix.getCorrelationsSqr()[i][j]);
			}
			else
			{ // others (assume Numeric vs. Nominal)
				text += "ANOVA R\u00B2 = " + formatCorrelationValue(matrix.getCorrelationsSqr()[i][j]);
			}
			text += "</html>";
			return text;
		}
		else if (event.getX() < (getWidth() - cellSize * matrix.length() - matrix.getBorderWidth()))
		{
			int i = (int) (event.getY() / cellSize);
			return "<html><p>" + matrix.getTitles().get(i) + "</p></html>";
		}
		{
			return super.getToolTipText(event);
		}
	}

	/**
	 * Creates custom tooltip for this correlation matrix grid, with respect to tooltip presentational properties specified in {@link CorrelationMatrix},
	 * like padding and border. Using custom subclass of tooltip to provide enable anti-aliasing
	 *
	 * @return created tooltip.
	 */
	@Override
	public JToolTip createToolTip()
	{
		JToolTip tooltip = new CorrelationToolTip();
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

	/**
	 * Calculates preferred size for this component taking into account available parent size
	 * (with respect to {@link TemperatureScalePanel}) and required proportion.
	 * <br/>
	 * Firstly, checks if component will fit horizontally (with free space above and below), or vertically (with free space on the right and on the left side).
	 * Then, using synthetically calculated label's cell proportion calculate matching dimension.
	 * Afterwards, set's components {@link #cellSize}, which will be used during painting.
	 * Font sizing not always is proportional, so at the final step this method checks how accurate labels are matching their space and modifies
	 * {@link #cellSize} according to result (and sets components {@link #labelsFont}, which will be used during painting).
	 *
	 * @return the preferred size for this correlation matrix grid
	 */
	@Override
	public Dimension getPreferredSize()
	{
		double availableWidth =
				matrix.getWidth() - matrix.getTemperatureScalePanel().getDefinedWidth() - matrix.getGridMargin() * 2;
		double availableHeight = matrix.getHeight() - matrix.getGridMargin() * 2;
		double borders = matrix.getBorderWidth() * 2;

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

	/**
	 * Returns correlation value (square) for given cell coordinates.
	 *
	 * @param i row index
	 * @param j column index
	 * @return correlation value
	 */
	private double getValue(int i, int j)
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
	 * Returns whether matrix must be displayed in compact mode (square correlation cells).
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
	 * @param x x coordinate ong component
	 * @param y y coordinate on component
	 * @return Cell indexes if there is a cell on given on given coordinates, {@link Optional#empty()} otherwise.
	 */
	private Optional<CellIndex> detectCell(int x, int y)
	{
		double cellsStart = getWidth() - cellSize * matrix.length() - matrix.getBorderWidth();
		if (x > cellsStart)
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
	 * Creates string for double data value, keeping 4 decimal places.
	 * In case of {@link Double#NaN}, returns "N/A".
	 *
	 * @param value value to format
	 * @return formatted value as {@link String}
	 */
	private String formatCorrelationValue(double value)
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
	 * Checks whether given width is enough for given labels,
	 * if they will be rendered with given font. If not - required number of latters is removed from the end,
	 * and 3 more letter at the end are replaced with "...".
	 *
	 * @param labels labels to check and replace
	 * @param labelsFont font
	 * @param maxWidth maximum allowed width for given labels
	 */
	void abbreviate(List<String> labels, Font labelsFont, double maxWidth)
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
	 * Abbreviates given label by removing all symbols after length defined by {@link #LABEL_ABBREVIATION_LENGTH}.
	 * The last 3 symbols which last will be replaced with dots for presentational purpose.
	 * <br/>
	 * The same label will be returned if it's length is below {@link #LABEL_ABBREVIATION_LENGTH}
	 *
	 * @param label the label to abbreviate if needed
	 * @return the same label or label with length equal to {@link #LABEL_ABBREVIATION_LENGTH} and 3 dots at the end
	 */
	private static String abbreviate(String label, int index)
	{
		if (label.length() > index + 1)
		{
			return label.substring(0, index - 2) + "...";
		}
		else
		{
			return label;
		}
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
}
