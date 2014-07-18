package thaumicenergistics.gui.widget;

import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.gui.IAspectSelectorContainer;

public interface IAspectSelectorGui extends IAspectWidgetGui
{
	public IAspectSelectorContainer getContainer();

	public AspectStack getCurrentAspect();

}
