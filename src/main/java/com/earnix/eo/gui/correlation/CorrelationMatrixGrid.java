package com.earnix.eo.gui.correlation;


import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Correlation matrix table componentm which includes data cells, titles column, highlights (if active) and zoom (if active).
 * Component is supposed to keep its proportion with {@link #getPreferredSize()} and is assumed to be used only within (@link {@link CorrelationMatrix}) as parent.
 * Presentation parameters are stored in {@link CorrelationMatrix}
 *
 * @author Taras Maslov
 * 11/22/2018
 */

public class CorrelationMatrixGrid extends JPanel implements MouseListener, MouseMotionListener
{
	/**
	 * Number of cells which are zoomed by default. Less cells will be included if data has less rows.
	 */
	private static final int ZOOM_LENGTH = 5;

	/**
	 * A proportion of circle in cell
	 */
	private final float CIRCLE_HEIGHT_PROPORTION = 0.8f;

	/**
	 * A proportion of label in title cell
	 */
	private final float LABEL_HEIGHT_PROPORTION = 0.7f;

	private static final double SQUEEZE_COEFFICIENT = 0.8;

	/**
	 * Maximum label display length in characters
	 */
	private static final int LABEL_ABBREVIATION_LENGTH = 64;

	private CorrelationMatrix matrix;

	// region Current presentational state

	/**
	 * Current data cell size, depends on component preferred size
	 */
	private double cellSize;

	/**
	 * Current font for title labels. Size depends on component's preferred size, for family see {@link CorrelationMatrix#labelsFont}
	 */
	private Font labelsFont;

	private /* Nullable */ CellCoordinates zoomCoordinates;
	private /* Nullable */ CellCoordinates highlightCoordinates;

	// endregion

	/**
	 * Creates new grid component.
	 */
	CorrelationMatrixGrid(CorrelationMatrix matrix)
	{
		this.matrix = matrix;
		addMouseListener(this);
		addMouseMotionListener(this);
		setOpaque(false);
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	/**
	 * Paints this component (data cells, column titles, highlights and zoom) into given graphical context
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

		double labelMargin = (1 - LABEL_HEIGHT_PROPORTION) * cellSize / 2;
		g2d.setFont(labelsFont);

		// Drawing cells and highlights. 
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

		// drawing vertical grid lines
		g2d.setColor(matrix.getGridLinesColor());
		g2d.setStroke(new BasicStroke(matrix.getGridLinesWidth()));
		for (int i = 1; i <= matrix.length(); i++)
		{
			int x = (int) (getWidth() - cellSize * i);
			g2d.drawLine(x, 0, x, getHeight() - 1);
		}

		// drawing horizontal grid lines
		for (int i = 0; i <= matrix.length() - 1; i++)
		{
			g2d.drawLine(0, (int) (i * cellSize), getWidth() - 1, (int) (i * cellSize));
		}

		// drawing titles
		g2d.setColor(matrix.getLabelsColor());
		for (int i = 0; i < matrix.length(); i++)
		{
			g2d.drawString(abbreviate(matrix.getTitles().get(i)), (int) labelMargin,
					(int) ((i + 1) * cellSize - labelMargin));
		}

		// drawing zoom
		if (zoomCoordinates != null)
		{
			Zoom zoom = createZoom(zoomCoordinates);
			paintZoom(zoom, g2d);
		}

		// border
		g2d.setStroke(new BasicStroke(matrix.getBorderWidth()));
		g2d.setColor(matrix.getBorderColor());
		g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		g2d.dispose();
	}

	/**
	 * If highlight is currenly active, paint highlight lines
	 *
	 * @param g2d graphical context
	 */
	private void paintHighlights(Graphics2D g2d)
	{
		if (highlightCoordinates != null)
		{
			int i = highlightCoordinates.i;
			int j = highlightCoordinates.j;
			g2d.setColor(matrix.getHighlightColor());
			g2d.fillRect(0, (int) (j * cellSize), getWidth(), (int) Math.round(cellSize));
			g2d.fillRect((int) (getWidth() - (matrix.length() - i) * cellSize), 0, (int) Math.round(cellSize),
					getHeight());
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
	 * Creates data cell model for given data coordinates
	 *
	 * @param i row index
	 * @param j column index
	 * @return cell model
	 */
	Cell createCell(int i, int j)
	{
		Cell cell = new Cell();
		cell.x = getWidth() - (matrix.length() - i) * cellSize;
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
		double margin = ((cell.size - cell.size * CIRCLE_HEIGHT_PROPORTION) / 2);
		double radiusY = cell.size - margin * 2;
		double radiusX =
				(cell.size - margin * 2 /* radius margin */) * (1.0 - Math.abs(cell.value) * SQUEEZE_COEFFICIENT);
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
			g2d.fillOval((int) (cell.x - radiusX / 2 + cell.size / 2), (int) (cell.y + margin), (int) radiusX,
					(int) radiusY);
			g2d.setColor(matrix.getEllipseStrokeColor());
			g2d.setStroke(new BasicStroke(matrix.getEllipseStrokeWidth()));
			g2d.drawOval((int) (cell.x - radiusX / 2 + cell.size / 2), (int) (cell.y + margin), (int) radiusX,
					(int) radiusY);
			g2d.setTransform(currentTransform);

		}
		else
		{
			g2d.fillRect((int) cell.x, (int) cell.y, (int) cell.size, (int) cell.size);
		}
	}

	/**
	 * Creates zoom model with pre-calculated coordinates for it's components.
	 *
	 * @param initiatorCellCoordinates the coordinates of cell which was active during zoom initiation.
	 * @return {@link Zoom} model
	 */
	Zoom createZoom(CellCoordinates initiatorCellCoordinates)
	{
		int i = initiatorCellCoordinates.i;
		int j = initiatorCellCoordinates.j;

		Zoom zoom = new Zoom();
		zoom.length = Math.min(ZOOM_LENGTH, matrix.length());

		zoom.i = Math.min(Math.max(i - zoom.length / 2, 0), matrix.length() - zoom.length);
		zoom.j = Math.min(Math.max(j - zoom.length / 2, 0), matrix.length() - zoom.length);

		zoom.zoomSelectionSize = zoom.length * cellSize;
		// zoom cell size should take 1/4 of space
		zoom.cellsSize = getHeight() / 4;
		zoom.cellSize = zoom.cellsSize / zoom.length;
		zoom.labelsMargin = zoom.cellSize * (1 - LABEL_HEIGHT_PROPORTION) / 2;
		zoom.font = matrix.getFont().deriveFont((float) zoom.cellSize * LABEL_HEIGHT_PROPORTION);

		zoom.horizontalLabels = matrix.getTitles().subList(zoom.j, zoom.j + zoom.length);
		zoom.horizontalLabelsWidth =
				getLabelsWidth(zoom.horizontalLabels, zoom.font) + zoom.cellSize * (1 - LABEL_HEIGHT_PROPORTION);

		zoom.verticalLabels = matrix.getTitles().subList(zoom.i, zoom.i + zoom.length);
		zoom.verticalLabelsWidth =
				getLabelsWidth(zoom.verticalLabels, zoom.font) + zoom.cellSize * (1 - LABEL_HEIGHT_PROPORTION);

		zoom.cellsSize = zoom.cellSize * zoom.length;
		zoom.width = zoom.horizontalLabelsWidth + zoom.cellsSize;
		zoom.height = zoom.verticalLabelsWidth + zoom.cellsSize;
		zoom.x = getWidth() - zoom.width;
		zoom.y = 0;

		return zoom;
	}

	/**
	 * Paints given zoom model into given graphical context
	 *
	 * @param zoom zoom model
	 * @param g2d graphical context
	 */
	private void paintZoom(Zoom zoom, Graphics2D g2d)
	{
		// drawing zoom selection border
		g2d.setColor(matrix.getZoomSelectionBorderColor());
		g2d.setStroke(new BasicStroke(matrix.getZoomSelectionBorderWidth()));
		g2d.drawRect((int) (getWidth() - (matrix.length() - zoom.i) * cellSize), (int) (zoom.j * cellSize),
				(int) zoom.zoomSelectionSize, (int) zoom.zoomSelectionSize);

		// drawing zoom area
		g2d.setStroke(new BasicStroke(matrix.getZoomBorderWidth()));
		g2d.setColor(matrix.getZoomBorderColor());
		g2d.setBackground(matrix.getBackground());
		g2d.setFont(zoom.font);
		g2d.clearRect((int) zoom.x, (int) zoom.y, (int) zoom.width, (int) zoom.height);
		g2d.drawRect((int) zoom.x, (int) zoom.y, (int) zoom.width, (int) zoom.height);

		// drawing cells in zoom area
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
			String label = zoom.horizontalLabels.get(l);
			g2d.drawString(label, (int) (zoom.x + zoom.labelsMargin),
					(int) (zoom.height - zoom.cellsSize + zoom.cellSize * (l + 1) - zoom.labelsMargin));
			AffineTransform transform = new AffineTransform();
			int vx = (int) (zoom.x + zoom.horizontalLabelsWidth + (l + 1) * zoom.cellSize);
			int vy = (int) (zoom.verticalLabelsWidth);
			transform.rotate(-Math.PI / 2, vx, vy);
			g2d.setTransform(transform);
			g2d.drawString(zoom.verticalLabels.get(l), (int) (vx + zoom.labelsMargin), (int) (vy - zoom.labelsMargin));
			g2d.setTransform(new AffineTransform());
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
		Optional<CellCoordinates> optionalCellCoordinates = detectCell(e.getX(), e.getY());
		if (optionalCellCoordinates.isPresent())
		{
			zoomCoordinates = optionalCellCoordinates.get();
		}
		else if (e.getX() < getWidth() - cellSize * matrix.length())
		{
			// label is pressed
			int ij = (int) (e.getY() / cellSize);
			highlightCoordinates = new CellCoordinates(ij, ij);
		}
		else
		{
			highlightCoordinates = null;
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		zoomCoordinates = null;
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
		AtomicBoolean doRepaint = new AtomicBoolean();
		if (zoomCoordinates != null)
		{
			detectCell(e.getX(), e.getY()).ifPresent(coordinates -> {
				zoomCoordinates = coordinates;
				doRepaint.set(true);
			});
		}

		if (highlightCoordinates != null)
		{
			if (e.getX() < getWidth() - cellSize * matrix.length())
			{
				int ij = (int) (e.getY() / cellSize);
				highlightCoordinates = new CellCoordinates(ij, ij);
			}
			else
			{
				highlightCoordinates = null;
			}
			doRepaint.set(true);
		}
		if (doRepaint.get())
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
		Optional<CellCoordinates> optionalCellCoordinates = detectCell(event.getX(), event.getY());

		if (optionalCellCoordinates.isPresent())
		{
			// mouse is above data cell
			CellCoordinates cellCoordinates = optionalCellCoordinates.get();
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
		else if (event.getX() < (getWidth() - cellSize * matrix.length()))
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
		float fontHeight = (float) (availableHeight / matrix.length());
		double fontWidth = getLabelsWidth(matrix.getTitles(), matrix.getFont().deriveFont(fontHeight));
		double margin = fontHeight * (1 - LABEL_HEIGHT_PROPORTION) / 2;
		double labelCellProportion = (fontWidth + margin * 2) / (fontHeight + margin * 2);

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
			double requiredLabelsWidth = (availableHeight / matrix.length()) * labelCellProportion;
			horizontalFit = !(labelsWidth > requiredLabelsWidth);
		}

		if (horizontalFit)
		{
			// In case of horizontal fit width is known, but height is unknown. 
			// Let's calculate it using system of equations:
			//
			// height / length = label cell height
			// title cell width / label cell height = label cell proportion
			// width = label cell width + height
			//
			// as result:
			//
			// label cell height = width / (length + label cell proportion)

			cellSize = availableWidth / (matrix.length() + labelCellProportion);
		}
		else
		{
			cellSize = availableHeight / matrix.length();
		}
		labelsFont = matrix.getFont().deriveFont((float) cellSize * LABEL_HEIGHT_PROPORTION);

		// correcting cell size if label is too long because of not proportional font scaling
		double labelWidth = getLabelsWidth(matrix.getTitles(), labelsFont);
		double error =
				availableWidth - cellSize * matrix.length() - labelWidth - (cellSize * (1 - LABEL_HEIGHT_PROPORTION));

		cellSize = Math.min(cellSize + error / matrix.length(), availableHeight / matrix.length());

		if (horizontalFit)
		{
			return new Dimension((int) availableWidth, (int) (matrix.length() * cellSize));
		}
		else
		{
			return new Dimension((int) (availableHeight + labelWidth + cellSize * (1 - LABEL_HEIGHT_PROPORTION)),
					(int) availableHeight);
		}
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
	private Optional<CellCoordinates> detectCell(int x, int y)
	{
		double cellsStart = getWidth() - cellSize * matrix.length();
		if (x > cellsStart)
		{
			int i = (int) ((x - cellsStart) / cellSize);
			int j = (int) (y / cellSize);
			if (i < matrix.length() && j < matrix.length())
			{
				return Optional.of(new CellCoordinates(i, j));
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
		if (label.length() > LABEL_ABBREVIATION_LENGTH)
		{
			return label.substring(0, LABEL_ABBREVIATION_LENGTH - 3) + "...";
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
