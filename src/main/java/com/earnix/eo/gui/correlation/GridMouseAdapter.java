package com.earnix.eo.gui.correlation;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * @author Taras Maslov
 * 12/28/2018
 */
class GridMouseAdapter extends MouseAdapter
{
	private final CorrelationMatrixGrid grid;

	GridMouseAdapter(CorrelationMatrixGrid grid)
	{
		this.grid = grid;
	}

	/**
	 * Detects if mouse was pressed on data cell, title cell or elsewhere.
	 * In case of title cell - enables highlights (store title's index).
	 * In case of data cell - creates zoom model.
	 * Removes current highlight otherwise. Requests component's repaint.
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(MouseEvent e)
	{
		Optional<CellIndex> optionalZoomIndex = grid.detectCell(e.getX(), e.getY());
		if (optionalZoomIndex.isPresent())
		{
			grid.zoom = grid.createZoom(optionalZoomIndex.get());
		}
		else
		{
			Optional<CellIndex> optionalHighlightIndex = grid.detectTitleCell(e.getX(), e.getY());
			grid.highlightIndex = optionalHighlightIndex.orElse(null);
		}
		grid.repaint();
	}

	/**
	 * I zoom is currently active - removes it's model and triggers component's repaint.
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (grid.zoom != null)
		{
			grid.zoom = null;
			grid.repaint();
		}
	}

	/**
	 * Detects if mouse was dragged with active zoom or highlight feature.
	 * If so - check whether feature's data coordinates were changed.
	 * If so - updates its model and triggers repaint.
	 * If mouse was dragged outside feature's cells - disables it.
	 * {@inheritDoc}
	 */
	@Override
	public void mouseDragged(MouseEvent e)
	{
		boolean repaint = false;
		if (grid.zoom != null)
		{
			// updating or removing current zoom if needed

			Optional<CellIndex> optionalIndex = grid.detectCell(e.getX(), e.getY());
			if (optionalIndex.isPresent())
			{
				CellIndex nextZoomIndex = optionalIndex.get();
				if (nextZoomIndex.i != grid.zoom.i || nextZoomIndex.j != grid.zoom.j)
				{
					grid.zoom = grid.createZoom(optionalIndex.get());
					repaint = true;
				}
			}
			else
			{
				// mouse was dragged away the data cells area
				grid.zoom = null;
				repaint = true;
			}
		}

		if (grid.highlightIndex != null)
		{
			// updating or removing current highlight if needed
			Optional<CellIndex> optionalIndex = grid.detectTitleCell(e.getX(), e.getY());
			if (optionalIndex.isPresent())
			{
				CellIndex index = optionalIndex.get();
				if (index.j != grid.highlightIndex.i && index.j != grid.highlightIndex.j)
				{
					grid.highlightIndex = index;
					repaint = true;
				}
			}
			else
			{
				// mouse was dragged away the titles cells are
				grid.highlightIndex = null;
				repaint = true;
			}
		}

		if (repaint)
		{
			grid.repaint();
		}
	}
}

