package com.earnix.eo.gui.correlation;


import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Correlation matrix table component.
 *
 * @author Taras Maslov
 * 11/22/2018
 */

public class CorrelationMatrixGrid extends JPanel implements MouseListener, MouseMotionListener
{

	private static final int ZOOM_LENGTH = 5;
	private final float CIRCLE_HEIGHT_PROPORTION = 0.8f;
	private final float LABEL_HEIGHT_PROPORTION = 0.7f;
	private static final double SQUEEZE_COEFFICIENT = 0.8;
	private static final int LABEL_ABBREVIATION_LENGTH = 64;

	private CorrelationMatrix matrix;

	private double cellSize;

	private Integer zoomJ;
	private Integer zoomI;
	private Integer highlightI;
	private Integer highlightJ;
	private Font labelsFont;

	/**
	 * Creates new grid component.
	 */
	CorrelationMatrixGrid(CorrelationMatrix matrix)
	{
		this.matrix = matrix;
		addMouseListener(this);
		addMouseMotionListener(this);
		setOpaque(true);
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	private boolean isCompact()
	{
		return cellSize < matrix.getCompactCellSize();
	}

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
		if (zoomI != null)
		{
			Zoom zoom = createZoom(g2d, zoomI, zoomJ);
			paintZoom(zoom, g2d);
		}

		// border
		g2d.setStroke(new BasicStroke(matrix.getBorderWidth()));
		g2d.setColor(matrix.getBorderColor());
		g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		g2d.dispose();
	}


	private void paintHighlights(Graphics2D g2d)
	{
		if (highlightI != null)
		{
			g2d.setColor(matrix.getHighlightColor());
			g2d.fillRect(0, (int) (highlightJ * cellSize), getWidth(), (int) Math.round(cellSize));
			g2d.fillRect((int) (getWidth() - (matrix.length() - highlightI) * cellSize), 0, (int) Math.round(cellSize),
					getHeight());
		}
	}

	double getValue(int i, int j)
	{
		double value;
		if (matrix.getData()[i][j] < 0)
		{
			value = -matrix.getDataSqr()[i][j];
		}
		else
		{
			value = matrix.getDataSqr()[i][j];
		}
		return value;
	}

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
		double radiusX = (cell.size - margin * 2 /* radius margin */) * (1.0 - Math.abs(cell.value) * SQUEEZE_COEFFICIENT);
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

		g2d.setColor(interpolate(fillColor, Color.WHITE, interpolation));

		// drawing cell
		if (!cell.compact)
		{
			AffineTransform currentTransform = g2d.getTransform();
			AffineTransform nextTransform = (AffineTransform) currentTransform.clone();
			nextTransform.rotate(rotation, cell.x + cell.size / 2, cell.y + cell.size / 2);
			g2d.setTransform(nextTransform);
			g2d.fillOval((int) (cell.x - radiusX / 2 + cell.size / 2), (int) (cell.y + margin), (int) radiusX, (int) radiusY);
			g2d.setColor(matrix.getEllipseStrokeColor());
			g2d.setStroke(new BasicStroke(matrix.getEllipseStrokeWidth()));
			g2d.drawOval((int) (cell.x - radiusX / 2 + cell.size / 2), (int) (cell.y + margin), (int) radiusX, (int) radiusY);
			g2d.setTransform(currentTransform);

		}
		else
		{
			g2d.fillRect((int) cell.x, (int) cell.y, (int) cell.size, (int) cell.size);
		}
	}

	private Zoom createZoom(Graphics2D g2d, int i, int j)
	{
		Zoom zoom = new Zoom();
		zoom.length = Math.min(ZOOM_LENGTH, matrix.length());

		zoom.startI = Math.min(Math.max(i - zoom.length / 2, 0), matrix.length() - zoom.length);
		zoom.startJ = Math.min(Math.max(j - zoom.length / 2, 0), matrix.length() - zoom.length);

		zoom.zoomSelectionSize = zoom.length * cellSize;
		zoom.cellSize = (getHeight() / 2) / 2 / zoom.length; // todo expose
		zoom.labelsMargin = zoom.cellSize * (1 - LABEL_HEIGHT_PROPORTION) / 2;
		zoom.font = matrix.getFont().deriveFont((float) zoom.cellSize);

		g2d.setFont(zoom.font);

		zoom.horizontalLabels = matrix.getTitles().subList(zoom.startJ, zoom.startJ + zoom.length);
		zoom.horizontalLabelsWidth =
				getLabelsWidth(zoom.horizontalLabels, zoom.font) + zoom.cellSize * (1 - LABEL_HEIGHT_PROPORTION);

		zoom.verticalLabels = matrix.getTitles().subList(zoom.startI, zoom.startI + zoom.length);
		zoom.verticalLabelsWidth =
				getLabelsWidth(zoom.verticalLabels, zoom.font) + zoom.cellSize * (1 - LABEL_HEIGHT_PROPORTION);

		zoom.cellsSize = zoom.cellSize * zoom.length;
		zoom.width = zoom.horizontalLabelsWidth + zoom.cellsSize;
		zoom.height = zoom.verticalLabelsWidth + zoom.cellsSize;
		zoom.x = getWidth() - zoom.width;
		zoom.y = 0;

		return zoom;
	}

	private void paintZoom(Zoom zoom, Graphics2D g2d)
	{
		// drawing zoom selection border
		g2d.setColor(matrix.getZoomSelectionBorderColor());
		g2d.setStroke(new BasicStroke(matrix.getZoomSelectionBorderWidth()));
		g2d.drawRect((int) (getWidth() - (matrix.length() - zoom.startI) * cellSize), (int) (zoom.startJ * cellSize),
				(int) zoom.zoomSelectionSize, (int) zoom.zoomSelectionSize);

		// drawing zoom area
		g2d.setStroke(new BasicStroke(matrix.getZoomBorderWidth()));
		g2d.setColor(matrix.getZoomBorderColor());
		g2d.setFont(zoom.font);
		g2d.clearRect((int) zoom.x, (int) zoom.y, (int) zoom.width, (int) zoom.height);
		g2d.drawRect((int) zoom.x, (int) zoom.y, (int) zoom.width, (int) zoom.height);

		// drawing cells in zoom area
		for (int l = 0; l < zoom.length; l++)
		{
			for (int m = 0; m < zoom.length; m++)
			{
				int i = zoom.startI + l;
				int j = zoom.startJ + m;
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
		Optional<Point> optionalPoint = detectCell(e.getX(), e.getY());
		if (optionalPoint.isPresent())
		{
			Point point = optionalPoint.get();
			zoomI = point.x;
			zoomJ = point.y;
		}

		if (e.getX() < getWidth() - cellSize * matrix.length())
		{
			highlightJ = (int) (e.getY() / cellSize);
			highlightI = highlightJ;
		}
		else
		{
			highlightJ = null;
			highlightI = null;
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		zoomI = null;
		zoomJ = null;
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

	@Override
	public void mouseDragged(MouseEvent e)
	{
		AtomicBoolean doRepaint = new AtomicBoolean();
		if (zoomI != null)
		{
			detectCell(e.getX(), e.getY()).ifPresent(coordinatess -> {
				zoomI = coordinatess.x;
				zoomJ = coordinatess.y;
				doRepaint.set(true);
			});
		}

		if (highlightI != null)
		{
			if (e.getX() < getWidth() - cellSize * matrix.length())
			{
				highlightJ = (int) (e.getY() / cellSize);
				highlightI = highlightJ;
			}
			else
			{
				highlightJ = null;
				highlightI = null;
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

	@Override
	public String getToolTipText(MouseEvent event)
	{
		repaint();
		Optional<Point> optionalCellIndexes = detectCell(event.getX(), event.getY());


		if (optionalCellIndexes.isPresent())
		{
			Point cellIndexes = optionalCellIndexes.get();
			int i = cellIndexes.x;
			int j = cellIndexes.y;
			String text = "<html>";
			DataType dataTypeI = matrix.getDataTypes().get(i);
			DataType dataTypeJ = matrix.getDataTypes().get(j);
			text += matrix.getTitles().get(i) + "<br/>";
			text += matrix.getTitles().get(j) + "<br/>";
			if (dataTypeI == DataType.NUMERIC && (dataTypeJ == DataType.NUMERIC))
			{  // Numeric vs. Numeric

				text += "Pearson's R\u00B2 = " + formatValue(matrix.getDataSqr()[i][j]) + "<br/>";
				text += "Pearson's R = " + formatValue(matrix.getData()[i][j]);
			}
			else if (dataTypeI == DataType.NOMINAL && dataTypeJ == DataType.NOMINAL)
			{ // Nominal vs. Nominal

				text += "Cramer's V = " + formatValue(matrix.getDataSqr()[i][j]);
			}
			else
			{ // others (assume Numeric vs. Nominal)
				text += "ANOVA R\u00B2 = " + formatValue(matrix.getDataSqr()[i][j]);
			}
			text += "</html>";
			return text;
		}
		else if (event.getX() < (getWidth() - cellSize * matrix.length()))
		{
			int i = (int) (event.getY() / cellSize);
			return "<html><p><font size =\"50\" color=\"black\">" + matrix.getTitles().get(i) + "</font></p></html>";
		}
		{
			return super.getToolTipText(event);
		}
	}

	private String formatValue(double value)
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
	 * Creates custom tooltip for this correlation matrix grid, with respect to tooltip presentational properties specified in {@link CorrelationMatrix}.
	 * 
	 * @return created tooltip.
	 */
	@Override
	public JToolTip createToolTip()
	{
		JToolTip tooltip = new CorrelationToolTip();
		tooltip.setFont(matrix.getFont().deriveFont(20f));
		tooltip.setForeground(matrix.getToolTipTextColor());
		tooltip.setBackground(matrix.getToolTipBackgroundColor());
		CompoundBorder border = new CompoundBorder(
				BorderFactory.createLineBorder(matrix.getToolTipBorderColor(), matrix.getToolTipBorderWidth()),
				BorderFactory.createEmptyBorder(20, 20, 20, 20));
		tooltip.setBorder(border);
		tooltip.addComponentListener(new ComponentAdapter()
		{

			@Override
			public void componentHidden(ComponentEvent e)
			{
				CorrelationMatrixGrid.this.repaint();
			}
		});
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

		double freeWidth =
				matrix.getWidth() - matrix.getTemperatureScalePanel().getDefinedWidth() - matrix.getGridMargin() * 2;
		double freeHeight = matrix.getHeight() - matrix.getGridMargin() * 2;
		boolean horizontalFit;
		float fontHeight = (float) (freeHeight / matrix.length());
		double fontWidth = getLabelsWidth(matrix.getTitles(), matrix.getFont().deriveFont(fontHeight));
		double margin = fontHeight * (1 - LABEL_HEIGHT_PROPORTION) / 2;
		double labelCellProportion = (fontWidth + margin * 2) / (fontHeight + margin * 2);

		if (freeHeight > freeWidth)
		{
			// free size is vertical rectangle, but grid is always horizontal one
			horizontalFit = true;
		}
		else
		{
			// Let's that component is exact as available size (may be not in reality).
			// Reason - to check if there would be enough (or more) space for labels if so.
			// If enough - the grid is fitting vertically or exactly, and cell size becomes known.

			double labelsWidth = freeWidth - freeHeight;
			double requiredLabelsWidth = (freeHeight / matrix.length()) * labelCellProportion;
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

			cellSize = freeWidth / (matrix.length() + labelCellProportion);
		}
		else
		{
			cellSize = freeHeight / matrix.length();
		}
		labelsFont = matrix.getFont().deriveFont((float) cellSize * LABEL_HEIGHT_PROPORTION);

		// correcting cell size if label is too long because not proportional font scaling
		double labelWidth = getLabelsWidth(matrix.getTitles(), labelsFont);
		double error = freeWidth - cellSize * matrix.length() - labelWidth - (cellSize * (1 - LABEL_HEIGHT_PROPORTION));
		cellSize = Math.min(cellSize + error / matrix.length(), freeHeight / matrix.length());
		
		if (horizontalFit)
		{
			return new Dimension((int) freeWidth, (int) (matrix.length() * cellSize));
		}
		else
		{
			return new Dimension((int) (freeHeight + labelWidth + cellSize * (1 - LABEL_HEIGHT_PROPORTION)),
					(int) freeHeight);
		}
	}

	/**
	 * Detects if given coordinates correspond to the grid cell with correlation data
	 * and returns cell indexes if so.
	 *
	 * @param x x coordinate ong component
	 * @param y y coordinate on component
	 * @return Cell indexes if there is a cell on given on given coordinates, {@link Optional#empty()} otherwise.
	 */
	private Optional<Point> detectCell(int x, int y)
	{
		double cellsStart = getWidth() - cellSize * matrix.length();
		if (x > cellsStart)
		{
			int i = (int) ((x - cellsStart) / cellSize);
			int j = (int) (y / cellSize);
			if (i < matrix.length() && j < matrix.length())
			{
				return Optional.of(new Point(i, j));
			}
		}
		return Optional.empty();
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
	private static Color interpolate(Color color1, Color color2, double interpolation)
	{
		int interpolatedRed = (int) (color1.getRed() * interpolation + color2.getRed() * (1 - interpolation));
		int interpolatedGreen = (int) (color1.getGreen() * interpolation + color2.getGreen() * (1 - interpolation));
		int interpolatedBlue = (int) (color1.getBlue() * interpolation + color2.getBlue() * (1 - interpolation));
		return new Color(interpolatedRed, interpolatedGreen, interpolatedBlue);
	}
}
