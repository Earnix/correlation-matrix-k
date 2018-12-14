package com.earnix.eo.gui.correlation;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
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
	private static TemperatureScalePanel temperatureScale;
	private static CorrelationMatrixGrid grid;
	private static JFrame frame;

	@BeforeAll
	static void before() throws InvocationTargetException, InterruptedException
	{
		//        System.setProperty("java.awt.headless", "true");

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

			List<String> rowsTitles = Arrays.asList("Duration", "Method", "Year", "Amount", "Status", "Score");

			matrix = new CorrelationMatrix(rowsTypes, rowsTitles, correlations, correlationsSqr);
			grid = matrix.getGrid();
			temperatureScale = matrix.getTemperatureScalePanel();

			frame = new JFrame();
			frame.setTitle("Correlation Matrix K");
			frame.setSize(800, 800);
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
		});
	}

	@Test
	void cell() throws InvocationTargetException, InterruptedException
	{
		invokeAndWait(() -> {
			Dimension preferredSize = matrix.getGrid().getPreferredSize();
			double expectedCellSize = preferredSize.height / (double) matrix.length();
			Cell cell_0_0 = matrix.getGrid().createCell(0, 0);
			Assertions.assertEquals(cell_0_0.size, preferredSize.height / (double) matrix.length(), 0.1d,
					"Cell size should be equal to height / length");
			Assertions.assertEquals(cell_0_0.y, 0, 0.1, "Top-left cell should have 0 y coordinate");

			// checking cells horizontal lay-outing
			Cell cell_1_0 = matrix.getGrid().createCell(1, 0);
			Assertions.assertEquals(preferredSize.height / (double) matrix.length(), cell_1_0.x - cell_0_0.x, 0.1,
					"Cells should be located in row");

			// checking cells vertical lay-outing;
			Cell cell_1_1 = matrix.getGrid().createCell(1, 1);
			Assertions
					.assertEquals(expectedCellSize, cell_1_1.y - cell_1_0.y, 0.1, "Cells should be located in column");

			// checking cell value
			Assertions.assertEquals(correlationsSqr[1][0], cell_1_0.value, "Value should match input data");
		});


	}

	@Test
	void zoom() throws InvocationTargetException, InterruptedException
	{
		invokeAndWait(() -> {
			//		Dimension preferredSize = grid.getPreferredSize();
			Zoom zoom = grid.createZoom(new CellCoordinates(0, 0));
			Assertions.assertEquals(zoom.length, matrix.getZoomLength(), "Amount of cells in zoom (in square) must match matrix parameter");
			Assertions.assertEquals(zoom.cellsSize, grid.getWidth() / (double) 4, 0.1, "Zoom cells must take 1/4 of grid space");
			Assertions.assertEquals(zoom.startI, 0, "Initial cell indexes should match");
			Assertions.assertEquals(zoom.startJ, 0, "Initial cell indexes should match");
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


}