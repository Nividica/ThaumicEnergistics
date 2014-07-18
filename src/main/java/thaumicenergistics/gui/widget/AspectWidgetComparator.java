package thaumicenergistics.gui.widget;

import java.util.Comparator;

// TODO: Add compare modes and merge with aspect comparator
public class AspectWidgetComparator implements Comparator<AbstractAspectWidget>
{

	@Override
	public int compare( AbstractAspectWidget left, AbstractAspectWidget right )
	{
		// Compare string tags
		return left.getAspect().getTag().compareTo( right.getAspect().getTag() );
	}

}
