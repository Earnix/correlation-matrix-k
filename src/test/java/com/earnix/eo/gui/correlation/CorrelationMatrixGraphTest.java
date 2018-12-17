package com.earnix.eo.gui.correlation;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Double.NaN;
import static javax.swing.SwingUtilities.invokeAndWait;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Taras Maslov
 * 12/5/2018
 */
public class CorrelationMatrixGraphTest
{
	private static CorrelationMatrix matrix;
	private static double[][] correlations;
	private static double[][] correlationsSqr;
	private static TemperatureScale temperatureScale;
	private static CorrelationMatrixGrid grid;
	private static JFrame frame;

	@BeforeAll
	static void before() throws InvocationTargetException, InterruptedException
	{
		invokeAndWait(() -> {
			correlations = new double[][] {
					{ 1.0, NaN, -0.018409163780178296, NaN, -0.04195467750878402, 0.14580489215919248, NaN },
					{ NaN, 1.0, NaN, NaN, NaN, NaN },
					{ -0.018409163780178296, NaN, 1.0, NaN, 0.03593145002583371, 0.022174369861375616 },
					{ NaN, NaN, NaN, 1.0, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN,
							NaN }, { -0.04195467750878402, NaN, 0.03593145002583371, NaN, 1.0, -0.005636468220432189 },
					{ 0.14580489215919248, NaN, 0.022174369861375616, NaN, -0.005636468220432189, 1.0 }, };

			correlationsSqr = new double[][] {
					{ 1.0, 0.020662563380152453, 3.3889731108542847E-4, 0.05163788346883987, 0.0017601949648660676,
							0.02125906657755375, 6.13099166797164E-4 },
					{ 0.020662563380152453, 1.0, NaN, 0.03999903162867131, 1.6373995918379734E-4,
							0.003951308137334179 },
					{ 3.3889731108542847E-4, NaN, 1.0, NaN, 0.001291069100958985, 4.917026787490833E-4 },
					{ 0.05163788346883987, 0.03999903162867131, NaN, 1.0, 3.0316309425459345E-4, 0.002022618888542866 },
					{ 0.0017601949648660676, 1.6373995918379734E-4, 0.001291069100958985, 3.0316309425459345E-4, 1.0,
							3.176977399994201E-5 },
					{ 0.02125906657755375, 0.003951308137334179, 4.917026787490833E-4, 0.002022618888542866,
							3.176977399994201E-5, 1.0 }, };

			List<RowType> rowsTypes = java.util.Arrays
					.asList(RowType.NUMERIC, RowType.NOMINAL, RowType.NUMERIC, RowType.NUMERIC, RowType.NOMINAL,
							RowType.NUMERIC);

			List<String> rowsTitles = Arrays
					.asList("Duration " + String.join("", Collections.nCopies(100, "A")), "Method", "Year", "Amount",
							"Status", "Score");

			matrix = new CorrelationMatrix(rowsTypes, rowsTitles, correlations, correlationsSqr);
			grid = matrix.getGrid();
			temperatureScale = matrix.getTemperatureScalePanel();

			frame = new JFrame();
			frame.setTitle("Correlation Matrix K");
			frame.setSize(1200, 800);
			frame.setResizable(true);
			frame.setLocationByPlatform(true);
			frame.getContentPane().add(matrix);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setVisible(true);
			frame.getContentPane().revalidate();
		});
	}

	@Test
	void length() throws InterruptedException, InvocationTargetException
	{
		invokeAndWait(() -> {
			Assertions.assertEquals(6, matrix.length());
		});
	}

	@Test
	void preferredSize() throws InterruptedException, InvocationTargetException
	{
		invokeAndWait(() -> {
			Assertions.assertTrue(matrix.getPreferredSize().width > matrix.getPreferredSize().getHeight());
			matrix.setSize(500, 500);
			CorrelationMatrixGrid grid = matrix.getGrid();
			Assertions.assertTrue(grid.getPreferredSize().width > grid.getPreferredSize().height);
			matrix.setSize(1200, 800);
		});
	}

	@Test
	void cell() throws InvocationTargetException, InterruptedException
	{
		invokeAndWait(() -> {
			Dimension preferredSize = matrix.getGrid().getPreferredSize();
			double expectedCellSize = preferredSize.height / (double) matrix.length();
			Cell cell_0_0 = matrix.getGrid().createCell(0, 0);
			Assertions.assertEquals(cell_0_0.size, preferredSize.height / (double) matrix.length(), 1d,
					"Cell size should be equal to height / length");
			Assertions.assertEquals(cell_0_0.y, 0, 0.1, "Top-left cell should have 0 y coordinate");

			// checking cells horizontal lay-outing
			Cell cell_1_0 = matrix.getGrid().createCell(1, 0);
			Assertions.assertEquals(preferredSize.height / (double) matrix.length(), cell_1_0.x - cell_0_0.x, 1,
					"Cells should be located in row");

			// checking cells vertical lay-outing;
			Cell cell_1_1 = matrix.getGrid().createCell(1, 1);
			Assertions.assertEquals(expectedCellSize, cell_1_1.y - cell_1_0.y, 1, "Cells should be located in column");

			// checking cell value
			Assertions.assertEquals(correlationsSqr[1][0], cell_1_0.value, "Value should match input data");

			// checking negative value
			Cell cell_0_2 = grid.createCell(0, 2);
			Assertions.assertEquals(-correlationsSqr[0][2], cell_0_2.value);
		});


	}

	@Test
	void zoom() throws InvocationTargetException, InterruptedException
	{
		invokeAndWait(() -> {

			Dimension preferredSize = grid.getPreferredSize();

			Zoom zoom = grid.createZoom(new CellIndex(0, 0));
			Assertions.assertEquals(zoom.length, matrix.getZoomLength(),
					"Amount of cells in zoom (in square) must match matrix parameter");
			Assertions.assertEquals(zoom.cellsSize, grid.getHeight() / (double) 4, 1,
					"Zoom cells must take 1/4 of grid space");
			Assertions.assertEquals(zoom.i, 0, "Initial cell indexes should match");
			Assertions.assertEquals(zoom.j, 0, "Initial cell indexes should match");
			Assertions.assertEquals(zoom.length, 5, "Zoom length is 5 by default");
			Assertions.assertEquals(zoom.cellSize, grid.getHeight() / (double) 4 / 5, 1, "Zoom grid sizing");

			Assertions.assertTrue(zoom.horizontalLabelsWidth + zoom.cellsSize <= preferredSize.width,
					"Zoom labels sizing");

			Assertions.assertEquals(grid.cellsHeight, zoom.height, 1d, "Zoom vertical labels sizing with long label");

			Assertions.assertTrue(zoom.verticalLabels.get(0).endsWith("..."), "Zoom label abbreviation");

			Assertions.assertTrue(zoom.verticalLabels.get(0).length() < 100, "Long label must be abbreviated");

			Assertions.assertEquals(0, zoom.y);
			Assertions.assertEquals(grid.cellsWidth - zoom.width, zoom.x, "Zoom must be located at top right corner");

		});

	}

	@Test
	void interpolateColor() throws InvocationTargetException, InterruptedException
	{
		invokeAndWait(() -> {
			double proportion = 0.7;
			Color result = CorrelationMatrixGrid.interpolateColor(Color.BLACK, Color.WHITE, proportion);
			Assertions.assertEquals(255 - 255 * proportion, result.getRed(), 1, "Red must be interpolated");
			Assertions.assertEquals(255 - 255 * proportion, result.getGreen(), 1, "Green must be interpolated");
			Assertions.assertEquals(255 - 255 * proportion, result.getBlue(), 1, "Blue must be interpolated");
		});

	}

	@Test
	void isCompact() throws InterruptedException, InvocationTargetException
	{
		invokeAndWait(() -> {
			// see matrix.getCompactCellSize();
			Assertions.assertFalse(grid.isCompact(), "Should not be compact with frame size 800x800");
			Assertions.assertFalse(grid.createCell(0, 0).compact);
			frame.setSize(200, 200);
		});

		invokeAndWait(() -> {
			Assertions.assertTrue(grid.isCompact(), "Cell size is less than 16, should be compact");
			Cell cell = grid.createCell(0, 0);
			Assertions.assertTrue(cell.compact);
		});
	}

	@Test
	void ceil()
	{
		Assertions.assertEquals(CorrelationMatrixGrid.ceil(0.1), 1);
	}

	@Test
	void abbreviate()
	{
		String source = String.join("", Collections.nCopies(100, "A"));
		String abbreviated = CorrelationMatrixGrid.abbreviate(source, 64);
		Assertions.assertEquals(64, abbreviated.length());
		Assertions.assertTrue(abbreviated.endsWith("..."));
		Assertions.assertEquals(source.substring(0, 60), abbreviated.substring(0, 60));
	}

	@Test
	void formatCorrelationValue()
	{
		Assertions.assertEquals("1.0000", grid.formatCorrelationValue(1));
		Assertions.assertEquals("N/A", grid.formatCorrelationValue(Double.NaN));
	}

	@Test
	void getValue()
	{
		Assertions.assertEquals(correlationsSqr[0][1], grid.getValue(0, 1));
		Assertions.assertEquals(-correlationsSqr[0][2], grid.getValue(0, 2));
	}
	
	@Test
	void setTemperatureScale(){
		Dimension preferredSize = temperatureScale.getPreferredSize();
		Assertions.assertEquals(matrix.getHeight(), preferredSize.height);
		Assertions.assertTrue(
				matrix.getTemperatureScaleGradientWidth() + TemperatureScale.LABELS_MARGIN < preferredSize.width, 
				"Temperature scale at least includes gradient and margins"
		);
	}
}