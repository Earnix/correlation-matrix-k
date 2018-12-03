package com.earnix.eo.gui.correlation;

import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import java.util.Objects;

/**
 * @author Taras Maslov
 * 11/22/2018
 */
public class CorrelationMatrixGraph extends JPanel implements MouseListener, MouseMotionListener {

    private final int length;
    private final Object[] dataTypes;
    private final java.util.List<String> titles;
    private final double[][] data;
    private final double[][] dataSqr;

    private Integer highlightX = null;
    private Integer highlightY = null;


    private final float CIRCLE_WIDTH = 0.8f;
    private double titlesCellWidth;
    private double cellSize;
    private Integer zoomJ;
    private Integer zoomI;


    // presenatation properties

    private Font font;


    public CorrelationMatrixGraph(Object[] dataTypes, java.util.List<String> titles, double[][] data,
                                  double[][] dataSqr) {

        //		Objects.requireNonNull(dataTypes);
        Objects.requireNonNull(titles);
        Objects.requireNonNull(data);
        Objects.requireNonNull(dataSqr);

        //		if (dataTypes.length != titles.length || titles.length != data.length || data.length != dataSqr.length)
        //		{
        //			throw new IllegalArgumentException();
        //		}

        this.length = titles.size();

        this.dataTypes = dataTypes;
        this.titles = titles;
        this.data = data;
        this.dataSqr = dataSqr;

        addMouseListener(this);
        addMouseMotionListener(this);
        setBackground(Color.WHITE);
        setOpaque(true);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    private boolean isCompact() {
        return getWidth() < 500 || getHeight() < 500;
    }

    @Override
    protected void paintComponent(Graphics g) {
        System.err.println("rep");
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        cellSize = getHeight() / (double) length;

        g2d.setFont(new Font("Tahoma", Font.PLAIN, (int) cellSize));
        g2d.setColor(Color.GRAY);


        titlesCellWidth = getLabelsWidth(titles, g2d);


        g2d.drawLine((int) titlesCellWidth, 0, (int) titlesCellWidth, getHeight());


        // highlights

        if (highlightY != null) {
            paintHighlights(g2d);
        }

        g2d.setColor(Color.GRAY);


        // cells

        g2d.setColor(new Color(0x111111));
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data.length; j++) {
                if (j > i || (isCompact() && j != i)) {
                    Cell cell = prepareCell(g2d, i, j);
                    paintCell((Graphics2D) g2d, cell);
                }

            }
        }

        // lines 

        g2d.setColor(new Color(0x7F000000, true));
        for (int i = 0; i <= data.length; i++) {
            int x = (int) (titlesCellWidth + cellSize * i);
            g2d.drawLine(x, 0, x, (int) (cellSize * length));

        }
        for (int i = 0; i <= length; i++) {
            g2d.drawLine(0, (int) (i * cellSize), (int) (titlesCellWidth + cellSize * length), (int) (i * cellSize));
        }

        // titles

        g2d.setColor(Color.BLACK);
        for (int i = 0; i < length; i++) {
            g2d.drawString(titles.get(i), 0, (int) (i * cellSize + cellSize));
        }

        // zoom

        if (zoomI != null) {
            Zoom zoom = prepareZoom(g2d, zoomI, zoomJ);
            paintZoom(zoom, g2d);
        }
        g2d.dispose();
    }

    private void paintHighlights(Graphics2D g2d) {
        Color highlightColor = new Color(0xB2e3d7b4, true);
        g2d.setColor(highlightColor);
        g2d.fillRect(0, (int) (highlightY * cellSize), getWidth(), (int) cellSize);
        g2d.fillRect((int) (highlightX * cellSize + titlesCellWidth), 0, (int) cellSize, getHeight());
    }

    private Cell prepareCell(Graphics2D g2d, int i, int j) {
        System.err.println("rep cell");

        Cell cell = new Cell();

        cell.x = titlesCellWidth + i * cellSize;
        cell.y = j * cellSize;
        cell.value = dataSqr[i][j];
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
            fillColor = new Color(0xfa7b64);
            rotation = Math.PI / 4;
        } else {
            fillColor = new Color(0x274184);
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

            g2d.fillOval((int) (cell.x - radiusX / 2 + cellSize / 2), (int) (cell.y + margin), (int) radiusX, (int) radiusY);
            g2d.setColor(new Color(0x111111));
            g2d.drawOval((int) (cell.x - radiusX / 2 + cellSize / 2), (int) (cell.y + margin), (int) radiusX, (int) radiusY);
            g2d.setTransform(new AffineTransform());
        } else {
            g2d.fillRect((int) cell.x, (int) cell.y, (int) cellSize, (int) cellSize);
            g2d.setColor(new Color(0x111111));
        }


        // interpolating color


    }

    private Zoom prepareZoom(Graphics2D g2d, int i, int j) {
        Zoom zoom = new Zoom();
        zoom.zoomLength = Math.min(5, length);

        zoom.zoomStartI = Math.min(Math.max(i - zoom.zoomLength / 2, 0), length - zoom.zoomLength);
        zoom.zoomStartJ = Math.min(Math.max(j - zoom.zoomLength / 2, 0), length - zoom.zoomLength);

        zoom.zoomSelectionSize = 5 * cellSize;


        zoom.zoomCellSize = cellSize; // todo expose
        zoom.font = new Font("Tahoma", Font.PLAIN, (int) zoom.zoomCellSize);
//        g2d.setFont(zoom.font);
        zoom.horizontalLabels = this.titles.subList(zoom.zoomStartJ, zoom.zoomStartJ + zoom.zoomLength);
        zoom.horizontalLabelsWidth = getLabelsWidth(zoom.horizontalLabels, g2d);

        zoom.verticalLabels = this.titles.subList(zoom.zoomStartI, zoom.zoomStartI + zoom.zoomLength);
        zoom.verticalLabelsWidth = getLabelsWidth(zoom.verticalLabels, g2d);

        zoom.zoomCellsSize = zoom.zoomCellSize * zoom.zoomLength;
        zoom.zoomWidth = zoom.horizontalLabelsWidth + zoom.zoomCellsSize;
        zoom.zoomHeight = zoom.verticalLabelsWidth + zoom.zoomCellsSize;
        zoom.zoomX = titlesCellWidth + (length * cellSize) - zoom.zoomWidth;
        zoom.zoomY = 0;

        return zoom;
    }

    ;

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
                    double value = dataSqr[zoom.zoomStartI + l][zoom.zoomStartJ + m];
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
            g2d.drawLine((int) zoom.zoomX, (int) (zoom.zoomY + y - zoom.zoomCellSize), (int) (zoom.zoomWidth + zoom.zoomX),
                    (int) (zoom.zoomY + y - zoom.zoomCellSize));
            // vertical lines
            int x = (int) (zoom.zoomX + zoom.zoomWidth - zoom.zoomCellsSize + zoom.zoomCellSize * k);
            g2d.drawLine(x, (int) zoom.zoomY, x, (int) (zoom.zoomY + zoom.zoomHeight));
        }


        // drawing labels

        for (int l = 0; l < zoom.zoomLength; l++) {
            String label = zoom.horizontalLabels.get(l);
            g2d.drawString(label, (int) zoom.zoomX, (int) (zoom.zoomHeight - zoom.zoomCellsSize + zoom.zoomCellSize * (l + 1)));
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
            highlightY = (int) (e.getY() / cellSize);
            highlightX = highlightY;
        } else {
            highlightY = null;
            highlightX = null;
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

        if (highlightX != null) {
            if (e.getX() < titlesCellWidth) {
                highlightY = (int) (e.getY() / cellSize);
                highlightX = highlightY;
            } else {
                highlightY = null;
                highlightX = null;
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
            String text = String.valueOf(detectCell(event.getX(), event.getY()).x);
            String column1 = titles.get(cell.x);
            String column2 = titles.get(cell.y);
            double correlation = data[cell.x][cell.y];
            double corrSqr = dataSqr[cell.x][cell.y];


            String html =
                    "<html><p><font color=\"black\" " + "size=\"20\" face=\"Tahoma\">" + column1 + "<br/>" + column2
                            + "<br>" + (Double.isNaN(correlation) ?
                            ("Pearsons R: " + correlation + "<br/>" + "Pearsons R2: " + corrSqr) :
                            ("AVONA R2: " + corrSqr)) + "</br>" + "</font></p></html>";

            return html;
        } else if (event.getX() < titlesCellWidth) {
            int i = (int) (event.getX() / cellSize);
            return "<html><p><font size =\"50\" color=\"black\">" + titles.get(i) + "</font></p></html>";
        }
        {
            return super.getToolTipText(event);
        }
    }

    @Override
    public JToolTip createToolTip() {
        JToolTip tooltip = super.createToolTip();
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
