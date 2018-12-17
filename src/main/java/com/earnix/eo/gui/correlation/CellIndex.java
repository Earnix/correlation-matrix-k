package com.earnix.eo.gui.correlation;

/**
 * Represents 2-dimensional index of correlation value.
 */
class CellIndex
{
	/**
	 * Row index.
	 */
	final int i;

	/**
	 * Column index.
	 */
	final int j;

	/**
	 * Creates new cell index for given row and column.
	 *
	 * @param i row
	 * @param j column
	 */
	CellIndex(int i, int j)
	{
		this.i = i;
		this.j = j;
	}
}
