package com.earnix.eo.gui.correlation;

/**
 * Used to specify the type of data in row. Correlation method depends on types of both rows which are correlating.
 * Method is displayed during tooltip presentation.
 * <br/>
 * Numeric with numeric - <a href="https://en.wikipedia.org/wiki/Pearson_correlation_coefficient">Pearson correlation coefficient</a>;
 * <br/>
 * Nominal with nominal - <a href="http://google.com">Cramér's V</a>;
 * <br/>
 * Numeric with nominal - <a href="https://researchbasics.education.uconn.edu/anova_regression_and_chi-square/">ANOVA (ANalysis Of VAriance)</a>;
 *
 * @see CorrelationMatrixGrid#getToolTipText(java.awt.event.MouseEvent)
 */
public enum RowType
{
	NUMERIC, NOMINAL
}
