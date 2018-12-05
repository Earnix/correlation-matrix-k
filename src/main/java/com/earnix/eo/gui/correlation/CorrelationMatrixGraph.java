package com.earnix.eo.gui.correlation;


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

public class CorrelationMatrixGraph extends JPanel implements MouseListener, MouseMotionListener {

    private static final int ZOOM_LENGTH = 5;
    private final float CIRCLE_WIDTH = 0.8f;
    private static final double DEFAULT_SQUEEZE_COEFFICIENT = 0.8;

    private CorrelationMatrix matrix;

    private double titlesCellWidth;
    private double cellSize;

    private Integer zoomJ;
    private Integer zoomI;
    private Integer highlightI = null;
    private Integer highlightJ = null;

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
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        cellSize = getHeight() / (double) matrix.length();

        g2d.setFont(matrix.getFont().deriveFont((float) cellSize));
        g2d.setColor(Color.GRAY);


        titlesCellWidth = getLabelsWidth(matrix.getTitles(), g2d);


        g2d.drawLine((int) titlesCellWidth, 0, (int) titlesCellWidth, getHeight());


        // drawing highlights
        if (highlightJ != null) {
            paintHighlights(g2d);
        }

        // drawing cells
        g2d.setColor(new Color(0x111111));
        for (int i = 0; i < matrix.length(); i++) {
            for (int j = 0; j < matrix.length(); j++) {
                if (j > i || (isCompact() && j != i)) {
                    Cell cell = createCell(i, j);
                    paintCell(g2d, cell);
                }
            }
        }

        // drawing lines
        g2d.setColor(matrix.getLinesColor());
        for (int i = 0; i <= matrix.length(); i++) {
            int x = (int) (titlesCellWidth + cellSize * i);
            g2d.drawLine(x, 0, x, (int) (cellSize * matrix.length()));

        }
        for (int i = 0; i <= matrix.length(); i++) {
            g2d.drawLine(0, (int) (i * cellSize), (int) (titlesCellWidth + cellSize * matrix.length()),
                    (int) (i * cellSize));
        }

        // drawing titles
        g2d.setColor(Color.BLACK);
        for (int i = 0; i < matrix.length(); i++) {
            g2d.drawString(matrix.getTitles().get(i), 0, (int) (i * cellSize + cellSize));
        }

        // drawing zoom
        if (zoomI != null) {
            Zoom zoom = createZoom(g2d, zoomI, zoomJ);
            paintZoom(zoom, g2d);
        }
        g2d.dispose();
    }

    private void paintHighlights(Graphics2D g2d) {
        Color highlightColor = matrix.getHighlightColor();
        g2d.setColor(highlightColor);
        g2d.fillRect(0, (int) (highlightJ * cellSize), getWidth(), (int) cellSize);
        g2d.fillRect((int) (highlightI * cellSize + titlesCellWidth), 0, (int) cellSize, getHeight());
    }

    double getValue(int i, int j) {
        double value;
        if (matrix.getData()[i][j] < 0) {
            value = -matrix.getDataSqr()[i][j];
        } else {
            value = matrix.getDataSqr()[i][j];
        }
        return value;
    }

    Cell createCell(int i, int j) {
        Cell cell = new Cell();
        cell.x = titlesCellWidth + i * cellSize;
        cell.y = j * cellSize;
        cell.value = getValue(i, j);
        cell.size = cellSize;
        cell.compact = isCompact();
        return cell;
    }


    private void paintCell(Graphics2D g2d, Cell cell) {
        if (Double.isNaN(cell.value)) {
            return;
        }

        // preparing shape properties
        int margin = (int) ((cellSize - cellSize * CIRCLE_WIDTH) / 2);
        double radiusY = cellSize - margin * 2;
        double radiusX = (cellSize - margin * 2 /* radius margin */) * (1.0
                - Math.abs(cell.value) * DEFAULT_SQUEEZE_COEFFICIENT);
        Color fillColor;
        double rotation;
        if (cell.value > 0) {
            fillColor = matrix.getPositiveColor();
            rotation = Math.PI / 4;
        } else {
            fillColor = matrix.getNegativeColor();
            rotation = -Math.PI / 4;
        }

        // interpolating fill color
        int r = 255;
        int g = 255;
        int b = 255;
        double interpolation = Math.abs(cell.value);
        int interpolatedRed = (int) (fillColor.getRed() * interpolation + r * (1 - interpolation));
        int interpolatedGreen = (int) (fillColor.getGreen() * interpolation + g * (1 - interpolation));
        int interpolatedBlue = (int) (fillColor.getBlue() * interpolation + b * (1 - interpolation));
        Color interpolatedColor = new Color(interpolatedRed, interpolatedGreen, interpolatedBlue);
        g2d.setColor(interpolatedColor);

        // drawing cell
        if (!cell.compact) {
            AffineTransform currentTransform = g2d.getTransform();
            AffineTransform nextTransform = (AffineTransform) currentTransform.clone();
            nextTransform.rotate(rotation, cell.x + cellSize / 2, cell.y + radiusY / 2 + margin);
            g2d.setTransform(nextTransform);
            g2d.fillOval((int) (cell.x - radiusX / 2 + cellSize / 2), (int) (cell.y + margin), (int) radiusX,
                    (int) radiusY);
            g2d.setColor(new Color(0x111111));
            g2d.drawOval((int) (cell.x - radiusX / 2 + cellSize / 2), (int) (cell.y + margin), (int) radiusX,
                    (int) radiusY);
            g2d.setTransform(currentTransform);

        } else {
            g2d.fillRect((int) cell.x, (int) cell.y, (int) cellSize, (int) cellSize);
            g2d.setColor(new Color(0x111111));
        }
    }

    private Zoom createZoom(Graphics2D g2d, int i, int j) {
        Zoom zoom = new Zoom();
        zoom.length = Math.min(ZOOM_LENGTH, matrix.length());

        zoom.startI = Math.min(Math.max(i - zoom.length / 2, 0), matrix.length() - zoom.length);
        zoom.startJ = Math.min(Math.max(j - zoom.length / 2, 0), matrix.length() - zoom.length);

        zoom.zoomSelectionSize = 5 * cellSize;
        zoom.cellSize = cellSize; // todo expose
        zoom.font = matrix.getFont().deriveFont((float) zoom.cellSize);
        zoom.horizontalLabels = matrix.getTitles().subList(zoom.startJ, zoom.startJ + zoom.length);
        zoom.horizontalLabelsWidth = getLabelsWidth(zoom.horizontalLabels, g2d);

        zoom.verticalLabels = matrix.getTitles().subList(zoom.startI, zoom.startI + zoom.length);
        zoom.verticalLabelsWidth = getLabelsWidth(zoom.verticalLabels, g2d);

        zoom.cellsSize = zoom.cellSize * zoom.length;
        zoom.width = zoom.horizontalLabelsWidth + zoom.cellsSize;
        zoom.height = zoom.verticalLabelsWidth + zoom.cellsSize;
        zoom.x = titlesCellWidth + (matrix.length() * cellSize) - zoom.width;
        zoom.y = 0;

        return zoom;
    }

    private void paintZoom(Zoom zoom, Graphics2D g2d) {

        // zoom border
        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect((int) (zoom.startI * cellSize + titlesCellWidth), (int) (zoom.startJ * cellSize),
                (int) zoom.zoomSelectionSize, (int) zoom.zoomSelectionSize);
        g2d.setStroke(new BasicStroke(1));
        g2d.setFont(zoom.font);
        g2d.clearRect((int) zoom.x, (int) zoom.y, (int) zoom.width, (int) zoom.height);
        g2d.drawRect((int) zoom.x, (int) zoom.y, (int) zoom.width, (int) zoom.height);

        // drawing zoom cells
        for (int l = 0; l < zoom.length; l++) {
            for (int m = 0; m < zoom.length; m++) {
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

        // drawing zoom table
        for (int k = 0; k < zoom.length; k++) {
            int y = (int) (zoom.y + zoom.height - k * zoom.cellSize);
            // horizontal lines
            g2d.drawLine((int) zoom.x, (int) (zoom.y + y - zoom.cellSize), (int) (zoom.width + zoom.x),
                    (int) (zoom.y + y - zoom.cellSize));
            // vertical lines
            int x = (int) (zoom.x + zoom.width - zoom.cellsSize + zoom.cellSize * k);
            g2d.drawLine(x, (int) zoom.y, x, (int) (zoom.y + zoom.height));
        }


        // drawing labels

        for (int l = 0; l < zoom.length; l++) {
            String label = zoom.horizontalLabels.get(l);
            g2d.drawString(label, (int) zoom.x, (int) (zoom.height - zoom.cellsSize + zoom.cellSize * (l + 1)));
            AffineTransform transform = new AffineTransform();
            int vx = (int) (zoom.x + zoom.horizontalLabelsWidth + (l + 1) * zoom.cellSize);
            int vy = (int) zoom.verticalLabelsWidth;
            transform.rotate(-Math.PI / 2, vx, vy);
            g2d.setTransform(transform);
            g2d.drawString(zoom.verticalLabels.get(l), vx, vy);
            g2d.setTransform(new AffineTransform());
        }
    }

    private double getLabelsWidth(List<String> labels, Graphics2D g2d) {
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

    @Override
    public String getToolTipText(MouseEvent event) {
        repaint();
        Point cell = detectCell(event.getX(), event.getY());


        if (cell != null) {
            int i = cell.x;
            int j = cell.y;
            String text = "<html>";
            CellType dataTypeI = matrix.getDataTypes().get(cell.x);
            CellType dataTypeJ = matrix.getDataTypes().get(cell.y);
            if (dataTypeI == CellType.NUMERIC && (dataTypeJ == CellType.NUMERIC)) {  // Numeric vs. Numeric

                text += "Pearson's R\u00B2 = " + formatValue(matrix.getDataSqr()[i][j]) + "<br/>";
                text += "Pearson's R = " + formatValue(matrix.getData()[i][j]);
            } else if (dataTypeI == CellType.NOMINAL && dataTypeJ == CellType.NOMINAL) { // Nominal vs. Nominal

                text += "Cramer's V = " + formatValue(matrix.getDataSqr()[i][j]);
            } else { // others (assume Numeric vs. Nominal)
                text += "ANOVA R\u00B2 = " + formatValue(matrix.getDataSqr()[i][j]);
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

    private String formatValue(double value) {
        if (Double.isNaN(value)) {
            return "N/A";
        } else {
            return String.format("%.4f", value);
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

    private /* Nullable */ Point detectCell(int x, int y) {
        if (x > titlesCellWidth) {
            int i = (int) ((x - titlesCellWidth) / cellSize);
            int j = (int) (y / cellSize);
            if (i < matrix.length() && j < matrix.length()) {
                return new Point(i, j);
            }
        }
        return null;
    }
}
