package thaumicenergistics.api.gui;

import java.util.List;
import thaumcraft.api.aspects.Aspect;

public interface IAspectSlotGui
	extends IWidgetHost
{
	public void updateAspects( List<Aspect> aspectList );
}
