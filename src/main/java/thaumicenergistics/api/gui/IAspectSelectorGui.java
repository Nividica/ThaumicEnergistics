package thaumicenergistics.api.gui;

import thaumcraft.api.aspects.Aspect;

public interface IAspectSelectorGui
	extends IWidgetHost
{
	public IAspectSelectorContainer getContainer();

	public Aspect getSelectedAspect();

}
