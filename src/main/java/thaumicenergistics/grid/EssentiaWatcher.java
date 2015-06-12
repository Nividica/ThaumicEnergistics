package thaumicenergistics.grid;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.grid.GridEssentiaCache.WatcherManager;

public class EssentiaWatcher
	implements IEssentiaWatcher
{

	/**
	 * What aspects are being tracked by this watcher.
	 */
	private final HashSet<Aspect> trackedAspects;

	/**
	 * The manager for the cache.
	 */
	private final WatcherManager manager;

	/**
	 * Whom this watcher belongs to.
	 */
	private final WeakReference<IEssentiaWatcherHost> host;

	public EssentiaWatcher( final WatcherManager manager, final IEssentiaWatcherHost host )
	{
		this.manager = manager;
		this.trackedAspects = new HashSet<Aspect>();
		this.host = new WeakReference<IEssentiaWatcherHost>( host );
	}

	@Override
	public boolean add( final Aspect aspect )
	{
		boolean changed = this.trackedAspects.add( aspect );
		if( changed )
		{
			// Update the manager
			this.manager.onWatcherAddAspect( this, aspect );
		}
		return changed;
	}

	@Override
	public boolean addAll( final Collection<? extends Aspect> c )
	{
		boolean changed = false;
		for( Aspect aspect : c )
		{
			changed |= this.add( aspect );
		}
		return changed;
	}

	@Override
	public void clear()
	{
		if( !this.isEmpty() )
		{
			// Update the manager
			this.manager.onWatcherCleared( this, this.trackedAspects );

			// Clear the tracked aspects
			this.trackedAspects.clear();
		}
	}

	@Override
	public boolean contains( final Object o )
	{
		return this.trackedAspects.contains( o );
	}

	@Override
	public boolean containsAll( final Collection<?> c )
	{
		return this.trackedAspects.containsAll( c );
	}

	@Override
	public IEssentiaWatcherHost getHost()
	{
		return this.host.get();
	}

	@Override
	public boolean isEmpty()
	{
		return this.trackedAspects.isEmpty();
	}

	@Override
	public Iterator<Aspect> iterator()
	{
		return this.trackedAspects.iterator();
	}

	@Override
	public boolean remove( final Object o )
	{
		if( o instanceof Aspect )
		{
			boolean changed = this.trackedAspects.remove( o );
			if( changed )
			{
				this.manager.onWatcherRemoveAspect( this, (Aspect)o );
			}
			return changed;
		}
		return false;
	}

	@Override
	public boolean removeAll( final Collection<?> c )
	{
		boolean changed = false;
		for( Object aspect : c )
		{
			changed |= this.remove( aspect );
		}
		return changed;
	}

	/**
	 * This is not supported.
	 */
	@Override
	public boolean retainAll( final Collection<?> c )
	{
		throw new NotImplementedException();
	}

	@Override
	public int size()
	{
		return this.trackedAspects.size();
	}

	@Override
	public Object[] toArray()
	{
		return this.trackedAspects.toArray();
	}

	@Override
	public <T> T[] toArray( final T[] a )
	{
		return this.trackedAspects.toArray( a );
	}

}
