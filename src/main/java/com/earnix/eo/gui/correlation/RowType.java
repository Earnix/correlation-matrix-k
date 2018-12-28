package com.earnix.eo.gui.correlation;

/**
 * Used to specify the type of data in row. Correlation method depends on types of both rows which are correlating.
 * Correlation method is displayed within tooltip.
 * <br>
 * Numeric with numeric - <a href="https://en.wikipedia.org/wiki/Pearson_correlation_coefficient">Pearson correlation coefficient</a>;
 * <br>
 * Nominal with nominal - <a href="https://en.wikipedia.org/wiki/Cram%C3%A9r%27s_V">Cramér's V</a>;
 * <br>
 * Numeric with nominal - <a href="https://researchbasics.education.uconn.edu/anova_regression_and_chi-square/">ANOVA (ANalysis Of VAriance)</a>;
 *
 * @see CorrelationMatrixGrid#getToolTipText(java.awt.event.MouseEvent)
 */
public enum RowType
{/**
 * Indicates numeric row data type.
 */
NUMERIC,
	/**
	 * Indicates nominal row data type.
	 */
	NOMINAL
}
