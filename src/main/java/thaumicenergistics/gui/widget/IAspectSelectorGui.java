package thaumicenergistics.gui.widget;

import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.container.IAspectSelectorContainer;

public interface IAspectSelectorGui
	extends IWidgetHost
{
	public IAspectSelectorContainer getContainer();

	public AspectStack getSelectedAspect();

}
