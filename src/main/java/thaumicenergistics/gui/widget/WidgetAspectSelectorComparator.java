package thaumicenergistics.gui.widget;

import java.util.Comparator;
import thaumicenergistics.aspect.AspectStackComparator;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;

public class WidgetAspectSelectorComparator
	implements Comparator<WidgetAspectSelector>
{
	public AspectStackComparator internalComparator;

	/**
	 * Creates the comparator with sorting mode alphabetic.
	 */
	public WidgetAspectSelectorComparator()
	{
		this( ComparatorMode.MODE_ALPHABETIC );
	}

	/**
	 * Creates the comparator with specified sorting mode.
	 * If the mode is unrecognized, the list will not be sorted.
	 * 
	 * @param mode
	 * Mode to sort by.
	 */
	public WidgetAspectSelectorComparator( final ComparatorMode mode )
	{
		// Create the internal comparator
		this.internalComparator = new AspectStackComparator( mode );
	}

	@Override
	public int compare( final WidgetAspectSelector left, final WidgetAspectSelector right )
	{
		// Compare
		return this.internalComparator.compare( left.getAspectStackRepresentation(), right.getAspectStackRepresentation() );
	}

}
