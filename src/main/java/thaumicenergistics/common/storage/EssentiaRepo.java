package thaumicenergistics.common.storage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.api.storage.IEssentiaRepo;

/**
 * Manages a repository of essentia.
 * 
 * @author Nividica
 * 
 */
public class EssentiaRepo
	implements IEssentiaRepo
{
	/**
	 * The actual cache of aspects.
	 */
	private final Map<Aspect, IAspectStack> cache;

	/**
	 * Creates the repository.
	 */
	public EssentiaRepo()
	{
		// Create the cache
		this.cache = new ConcurrentHashMap<Aspect, IAspectStack>();
	}

	@Override
	public Set<Aspect> aspectSet()
	{
		return this.cache.keySet();
	}

	@Override
	public void clear()
	{
		this.cache.clear();
	}

	@Override
	public boolean containsAspect( final Aspect aspect )
	{
		return this.cache.containsKey( aspect );
	}

	@Override
	public void copyFrom( final Collection<IAspectStack> stacks )
	{
		// Clear
		this.clear();

		// Null check
		if( stacks == null )
		{
			return;
		}

		// Add each
		for( IAspectStack stack : stacks )
		{
			this.cache.put( stack.getAspect(), stack.copy() );
		}
	}

	@Override
	public IAspectStack get( final Aspect aspect )
	{
		return this.cache.getOrDefault( aspect, null );
	}

	@Override
	public Collection<IAspectStack> getAll()
	{
		return this.cache.values();
	}

	@Override
	public IAspectStack getOrDefault( final Aspect aspect, final IAspectStack defaultValue )
	{
		return this.cache.getOrDefault( aspect, defaultValue );
	}

	@Override
	public boolean isEmpty()
	{
		return this.cache.isEmpty();
	}

	@Override
	public IAspectStack postChange( final Aspect aspect, final long change, final Boolean isCraftableNullable )
	{
		// Is the aspect null?
		if( aspect == null )
		{
			// No changes
			return null;
		}

		boolean isCraftable;

		// Get the current stack
		IAspectStack current = this.cache.get( aspect );

		// Is their nothing currently stored?
		if( current == null )
		{
			// Create a new stack
			IAspectStack newStack = null;

			// Set craftability
			isCraftable = ( isCraftableNullable == null ? false : isCraftableNullable );

			// Is the change positive?
			if( change > 0 )
			{
				// Create a new stack
				newStack = new AspectStack( aspect, change, isCraftable );
			}
			// Is it craftable?
			else if( isCraftable )
			{
				// Create a new crafting stack
				newStack = new AspectStack( aspect, 0, isCraftable );
			}

			// Is there a new stack?
			if( newStack != null )
			{
				// Add the stack
				this.cache.put( newStack.getAspect(), newStack );
			}

			// Done
			return null;
		}

		// There is something currently stored
		IAspectStack previous = current.copy();

		// Set craftability
		isCraftable = ( isCraftableNullable == null ? previous.getCraftable() : isCraftableNullable );

		// Calculate the new amount
		long previousAmount = previous.getStackSize();
		long newAmount = Math.max( 0L, previousAmount + change );

		// Stack drained and is not craftable?
		if( ( newAmount == 0 ) && !isCraftable )
		{
			// Remove it
			this.cache.remove( aspect );
		}
		else
		{
			// Update the amount & craftability
			current.setStackSize( newAmount );
			current.setCraftable( isCraftable );
		}

		// Return the previous stack
		return previous;
	}

	@Override
	public IAspectStack postChange( final IAspectStack change )
	{
		// Null check
		if( change == null )
		{
			return null;
		}

		return this.postChange( change.getAspect(), change.getStackSize(), change.getCraftable() );
	}

	@Override
	public IAspectStack remove( final Aspect aspect )
	{
		return this.cache.remove( aspect );
	}

	@Override
	public IAspectStack setAspect( final Aspect aspect, final long amount, final boolean isCraftable )
	{
		if( aspect == null )
		{
			return null;
		}

		// Set the stack, and return the old one.
		return this.cache.put( aspect, new AspectStack( aspect, amount, isCraftable ) );

	}

	@Override
	public IAspectStack setAspect( final IAspectStack stack )
	{
		// Null check
		if( stack == null )
		{
			return null;
		}

		return this.setAspect( stack.getAspect(), stack.getStackSize(), stack.getCraftable() );
	}

	@Override
	public int size()
	{
		return this.cache.size();
	}
}
