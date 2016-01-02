package thaumicenergistics.api.grid;

import java.util.Collection;
import thaumcraft.api.aspects.Aspect;

public interface IEssentiaWatcher
	extends Collection<Aspect>
{
	public IEssentiaWatcherHost getHost();
}
