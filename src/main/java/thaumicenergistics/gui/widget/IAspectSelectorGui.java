package thaumicenergistics.gui.widget;

import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.IAspectSelectorContainer;

public interface IAspectSelectorGui
	extends IWidgetHost
{
	public IAspectSelectorContainer getContainer();

	public Aspect getSelectedAspect();

}
