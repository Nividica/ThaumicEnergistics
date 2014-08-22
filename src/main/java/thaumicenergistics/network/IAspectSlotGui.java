package thaumicenergistics.network;

import java.util.List;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.gui.widget.IWidgetHost;

public interface IAspectSlotGui
	extends IWidgetHost
{
	public void updateAspects( List<Aspect> aspectList );
}
