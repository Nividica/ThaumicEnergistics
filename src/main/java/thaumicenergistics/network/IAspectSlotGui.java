package thaumicenergistics.network;

import java.util.List;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.gui.IWidgetHost;

public interface IAspectSlotGui
	extends IWidgetHost
{
	public void updateAspects( List<Aspect> aspectList );
}
