package com.earnix.eo.gui.correlation;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;

import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * @author Taras Maslov
 * 11/22/2018
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CorrelationMatrixGraph extends JPanel implements MouseListener, MouseMotionListener {
    private final float CIRCLE_WIDTH = 0.8f;

    CorrelationMatrix matrix;

    double titlesCellWidth;
    double cellSize;

    Integer zoomJ;
    Integer zoomI;
    Integer highlightI = null;
    Integer highlightJ = null;

    CorrelationMatrixGraph(CorrelationMatrix matrix) {
        this.matrix = matrix;
        addMouseListener(this);
        addMouseMotionListener(this);
        setBackground(Color.WHITE);
        setOpaque(true);
        ToolTipManager.sharedInstance().registerComponent(this);
        setMinimumSize(new Dimension(300, 300));
    }

    private boolean isCompact() {
        return cellSize < matrix.getCompactCellSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        val g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        cellSize = getHeight() / (double) matrix.length();

        g2d.setFont(matrix.getFont().deriveFont((float) cellSize));
        g2d.setColor(Color.GRAY);


        titlesCellWidth = getLabelsWidth(matrix.getTitles(), g2d);


        g2d.drawLine((int) titlesCellWidth, 0, (int) titlesCellWidth, getHeight());


        // highlights

        if (highlightJ != null) {
            paintHighlights(g2d);
        }

        g2d.setColor(Color.GRAY);


        // cells

        g2d.setColor(new Color(0x111111));
        for (int i = 0; i < matrix.length(); i++) {
            for (int j = 0; j < matrix.length(); j++) {
                if (j > i || (isCompact() && j != i)) {
                    val cell = createCell(i, j);
                    paintCell(g2d, cell);
                }
            }
        }

        // lines 

        g2d.setColor(new Color(0x7F000000, true));
        for (int i = 0; i <= matrix.length(); i++) {
            int x = (int) (titlesCellWidth + cellSize * i);
            g2d.drawLine(x, 0, x, (int) (cellSize * matrix.length()));

        }
        for (int i = 0; i <= matrix.length(); i++) {
            g2d.drawLine(0, (int) (i * cellSize), (int) (titlesCellWidth + cellSize * matrix.length()),
                    (int) (i * cellSize));
        }

        // titles

        g2d.setColor(Color.BLACK);
        for (int i = 0; i < matrix.length(); i++) {
            g2d.drawString(matrix.getTitles().get(i), 0, (int) (i * cellSize + cellSize));
        }

        // zoom

        if (zoomI != null) {
            Zoom zoom = prepareZoom(g2d, zoomI, zoomJ);
            paintZoom(zoom, g2d);
        }
        g2d.dispose();
    }

    private void paintHighlights(Graphics2D g2d) {
        val highlightColor = new Color(0xB2e3d7b4, true);
        g2d.setColor(highlightColor);
        g2d.fillRect(0, (int) (highlightJ * cellSize), getWidth(), (int) cellSize);
        g2d.fillRect((int) (highlightI * cellSize + titlesCellWidth), 0, (int) cellSize, getHeight());
    }

    private Cell createCell(int i, int j) {
        val cell = new Cell();
        cell.x = titlesCellWidth + i * cellSize;
        cell.y = j * cellSize;
        if (matrix.getData()[i][j] < 0) {
            cell.value = -matrix.getDataSqr()[i][j];
        } else {
            cell.value = -matrix.getDataSqr()[i][j];
        }
        cell.size = cellSize;
        return cell;
    }


    private void paintCell(Graphics2D g2d, Cell cell) {
        if (Double.isNaN(cell.value)) {
            return;
        }

        int margin = (int) ((cellSize - cellSize * CIRCLE_WIDTH) / 2);

        double radiusY = cellSize - margin * 2;
        double radiusX;

        radiusX = (cellSize - margin * 2 /* radius margin */) * (1.0 - Math.abs(cell.value) /* squeezeCoefficient */);
        //		- radiusX / 2;

        Color fillColor;
        double rotation;
        if (cell.value > 0) {
            fillColor = matrix.getColor1();
            rotation = Math.PI / 4;
        } else {
            fillColor = matrix.getColor2();
            rotation = -Math.PI / 4;
        }

        int r = 255;
        int g = 255;
        int b = 255;

        double interpolation = Math.abs(cell.value);

        int interpolatedRed = (int) (fillColor.getRed() * interpolation + r * (1 - interpolation));
        int interpolatedGreen = (int) (fillColor.getGreen() * interpolation + g * (1 - interpolation));
        int interpolatedBlue = (int) (fillColor.getBlue() * interpolation + b * (1 - interpolation));

        Color interpolated = new Color(interpolatedRed, interpolatedGreen, interpolatedBlue);
        g2d.setColor(interpolated);

        if (!cell.compact) {
            AffineTransform transform = new AffineTransform();
            transform.rotate(rotation, cell.x + cellSize / 2, cell.y + radiusY / 2 + margin);
            g2d.setTransform(transform);

            g2d.fillOval((int) (cell.x - radiusX / 2 + cellSize / 2), (int) (cell.y + margin), (int) radiusX,
                    (int) radiusY);
            g2d.setColor(new Color(0x111111));
            g2d.drawOval((int) (cell.x - radiusX / 2 + cellSize / 2), (int) (cell.y + margin), (int) radiusX,
                    (int) radiusY);
            g2d.setTransform(new AffineTransform());
        } else {
            g2d.fillRect((int) cell.x, (int) cell.y, (int) cellSize, (int) cellSize);
            g2d.setColor(new Color(0x111111));
        }


        // interpolating color
    }

    private Zoom prepareZoom(Graphics2D g2d, int i, int j) {
        Zoom zoom = new Zoom();
        zoom.zoomLength = Math.min(5, matrix.length());

        zoom.zoomStartI = Math.min(Math.max(i - zoom.zoomLength / 2, 0), matrix.length() - zoom.zoomLength);
        zoom.zoomStartJ = Math.min(Math.max(j - zoom.zoomLength / 2, 0), matrix.length() - zoom.zoomLength);

        zoom.zoomSelectionSize = 5 * cellSize;


        zoom.zoomCellSize = cellSize; // todo expose
        zoom.font = matrix.getFont().deriveFont((float) zoom.zoomCellSize);
        //        g2d.setFont(zoom.font);
        zoom.horizontalLabels = matrix.getTitles().subList(zoom.zoomStartJ, zoom.zoomStartJ + zoom.zoomLength);
        zoom.horizontalLabelsWidth = getLabelsWidth(zoom.horizontalLabels, g2d);

        zoom.verticalLabels = matrix.getTitles().subList(zoom.zoomStartI, zoom.zoomStartI + zoom.zoomLength);
        zoom.verticalLabelsWidth = getLabelsWidth(zoom.verticalLabels, g2d);

        zoom.zoomCellsSize = zoom.zoomCellSize * zoom.zoomLength;
        zoom.zoomWidth = zoom.horizontalLabelsWidth + zoom.zoomCellsSize;
        zoom.zoomHeight = zoom.verticalLabelsWidth + zoom.zoomCellsSize;
        zoom.zoomX = titlesCellWidth + (matrix.length() * cellSize) - zoom.zoomWidth;
        zoom.zoomY = 0;

        return zoom;
    }

    private void paintZoom(Zoom zoom, Graphics2D g2d) {

        g2d.setColor(Color.black);
        // zoom border
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect((int) (zoom.zoomStartI * cellSize + titlesCellWidth), (int) (zoom.zoomStartJ * cellSize),
                (int) zoom.zoomSelectionSize, (int) zoom.zoomSelectionSize);
        g2d.setStroke(new BasicStroke(1));
        g2d.setFont(zoom.font);
        g2d.clearRect((int) zoom.zoomX, (int) zoom.zoomY, (int) zoom.zoomWidth, (int) zoom.zoomHeight);
        g2d.drawRect((int) zoom.zoomX, (int) zoom.zoomY, (int) zoom.zoomWidth, (int) zoom.zoomHeight);

        // drawing zoom cells
        for (int l = 0; l < zoom.zoomLength; l++) {
            for (int m = 0; m < zoom.zoomLength; m++) {
                if (zoom.zoomStartI + l < zoom.zoomStartJ + m) {
                    int x = (int) (zoom.zoomX + zoom.zoomWidth - zoom.zoomCellsSize + l * zoom.zoomCellSize);
                    int y = (int) (zoom.zoomY + zoom.zoomHeight - zoom.zoomCellsSize + m * zoom.zoomCellSize);
                    double value = matrix.getDataSqr()[zoom.zoomStartI + l][zoom.zoomStartJ + m];
                    Cell cell = new Cell();
                    cell.x = x;
                    cell.y = y;
                    cell.compact = isCompact();
                    cell.size = zoom.zoomCellSize;
                    cell.value = value;
                    paintCell(g2d, cell);
                }
            }
        }

        // drawing zoom table
        for (int k = 0; k < zoom.zoomLength; k++) {
            int y = (int) (zoom.zoomY + zoom.zoomHeight - k * zoom.zoomCellSize);
            // horizontal lines
            g2d.drawLine((int) zoom.zoomX, (int) (zoom.zoomY + y - zoom.zoomCellSize),
                    (int) (zoom.zoomWidth + zoom.zoomX), (int) (zoom.zoomY + y - zoom.zoomCellSize));
            // vertical lines
            int x = (int) (zoom.zoomX + zoom.zoomWidth - zoom.zoomCellsSize + zoom.zoomCellSize * k);
            g2d.drawLine(x, (int) zoom.zoomY, x, (int) (zoom.zoomY + zoom.zoomHeight));
        }


        // drawing labels

        for (int l = 0; l < zoom.zoomLength; l++) {
            String label = zoom.horizontalLabels.get(l);
            g2d.drawString(label, (int) zoom.zoomX,
                    (int) (zoom.zoomHeight - zoom.zoomCellsSize + zoom.zoomCellSize * (l + 1)));
            AffineTransform transform = new AffineTransform();
            int vx = (int) (zoom.zoomX + zoom.horizontalLabelsWidth + (l + 1) * zoom.zoomCellSize);
            int vy = (int) zoom.verticalLabelsWidth;
            transform.rotate(-Math.PI / 2, vx, vy);
            g2d.setTransform(transform);
            g2d.drawString(zoom.verticalLabels.get(l), vx, vy);
            g2d.setTransform(new AffineTransform());
        }
    }

    double getLabelsWidth(List<String> labels, Graphics2D g2d) {
        return labels.stream().mapToDouble(title -> g2d.getFontMetrics().stringWidth(title)).max()
                .orElseThrow(IllegalStateException::new);
    }

    ;

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point cell = detectCell(e.getX(), e.getY());
        if (cell != null) {
            zoomI = cell.x;
            zoomJ = cell.y;
        }

        if (e.getX() < titlesCellWidth) {
            highlightJ = (int) (e.getY() / cellSize);
            highlightI = highlightJ;
        } else {
            highlightJ = null;
            highlightI = null;
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        zoomI = null;
        zoomJ = null;
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        boolean repaint = false;
        if (zoomI != null) {
            Point cell = detectCell(e.getX(), e.getY());
            if (cell != null) {
                zoomI = cell.x;
                zoomJ = cell.y;
                repaint = true;
            }
        }

        if (highlightI != null) {
            if (e.getX() < titlesCellWidth) {
                highlightJ = (int) (e.getY() / cellSize);
                highlightI = highlightJ;
            } else {
                highlightJ = null;
                highlightI = null;
            }
            repaint = true;
        }
        if (repaint) {
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    //	@Override
    //	public Point getToolTipLocation(MouseEvent event)
    //	{
    //		int x = event.getX();
    //		int y = event.getY();
    //	}

    @Override
    public String getToolTipText(MouseEvent event) {
        repaint();
        Point cell = detectCell(event.getX(), event.getY());


        if (cell != null) {
            val i = cell.x;
            val j = cell.y;
            String text = "<html>";
            val dataTypeI = matrix.getDataTypes().get(cell.x);
            val dataTypeJ = matrix.getDataTypes().get(cell.y);
            if (dataTypeI == CellType.NUMERIC && (dataTypeJ == CellType.NUMERIC)) {  // Numeric vs. Numeric

                text += "Pearson's R\u00B2 = " + String.format("%.4f", matrix.getDataSqr()[i][j]) + "<br/>";
                text += "Pearson's R = " + String.format("%.4f", matrix.getData()[i][j]);
            } else if (dataTypeI == CellType.NOMINAL && dataTypeJ == CellType.NOMINAL) { // Nominal vs. Nominal

                text += "Cramer's V = " + matrix.getDataSqr()[i][j];
            } else { // others (assume Numeric vs. Nominal)
                text += "ANOVA R\u00B2 = " + matrix.getDataSqr()[i][j];
            }

            text += "</html>";
            // todo check how NaN should act

//			String text = String.valueOf(detectCell(event.getX(), event.getY()).x);
//			String column1 = matrix.getTitles().get(cell.x);
//			String column2 = matrix.getTitles().get(cell.y);
//			double correlation = matrix.getData()[cell.x][cell.y];
//			double corrSqr = matrix.getDataSqr()[cell.x][cell.y];
//
//
//			String html =
//					"<html><p><font color=\"black\" " + "size=\"20\" face=\"Tahoma\">" + column1 + "<br/>" + column2
//							+ "<br>" + (Double.isNaN(correlation) ?
//							("Pearsons R: " + correlation + "<br/>" + "Pearsons R2: " + corrSqr) :
//							("AVONA R2: " + corrSqr)) + "</br>" + "</font></p></html>";

            return text;
        } else if (event.getX() < titlesCellWidth) {
            int i = (int) (event.getX() / cellSize);
            return "<html><p><font size =\"50\" color=\"black\">" + matrix.getTitles().get(i) + "</font></p></html>";
        }
        {
            return super.getToolTipText(event);
        }
    }

    @Override
    public JToolTip createToolTip() {
        JToolTip tooltip = super.createToolTip();
        tooltip.setFont(matrix.getFont().deriveFont(20f));
        tooltip.setBackground(Color.WHITE);
        tooltip.setBorder(new EmptyBorder(20, 20, 20, 20));
        tooltip.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentHidden(ComponentEvent e) {
                CorrelationMatrixGraph.this.repaint();
            }
        });
        return tooltip;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        return new Dimension(preferredSize.height, preferredSize.height);
    }

    private /* Nullable */ Point detectCell(int x, int y) {
        if (titlesCellWidth > x) {
            return null;
        }

        int i = (int) ((x - titlesCellWidth) / cellSize);
        int j = (int) (y / cellSize);

        return new Point(i, j);
    }
}
