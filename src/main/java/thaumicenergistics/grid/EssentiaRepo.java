package thaumicenergistics.grid;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.storage.IEssentiaRepo;
import thaumicenergistics.aspect.AspectStack;

public class EssentiaRepo
	implements IEssentiaRepo
{
	/**
	 * The actual cache of aspects.
	 */
	private final Map<Aspect, AspectStack> cache;

	/**
	 * Creates the repository.
	 */
	public EssentiaRepo()
	{
		// Create the cache
		this.cache = new ConcurrentHashMap<Aspect, AspectStack>();
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
	public void copyFrom( final Collection<AspectStack> stacks )
	{
		// Clear
		this.clear();

		// Null check
		if( stacks == null )
		{
			return;
		}

		// Add each
		for( AspectStack stack : stacks )
		{
			this.cache.put( stack.aspect, stack.copy() );
		}
	}

	@Override
	public AspectStack get( final Aspect aspect )
	{
		return this.cache.getOrDefault( aspect, null );
	}

	@Override
	public Collection<AspectStack> getAll()
	{
		return this.cache.values();
	}

	@Override
	public AspectStack getOrDefault( final Aspect aspect, final AspectStack defaultValue )
	{
		return this.cache.getOrDefault( aspect, defaultValue );
	}

	@Override
	public boolean isEmpty()
	{
		return this.cache.isEmpty();
	}

	@Override
	public AspectStack postChange( final Aspect aspect, final long change, final Boolean isCraftableNullable )
	{
		// Is the aspect null?
		if( aspect == null )
		{
			// No changes
			return null;
		}

		boolean isCraftable;

		// Get the current stack
		AspectStack current = this.cache.get( aspect );

		// Is their nothing currently stored?
		if( current == null )
		{
			// Create a new stack
			AspectStack newStack = null;

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
				this.cache.put( newStack.aspect, newStack );
			}

			// Done
			return null;
		}

		// There is something currently stored
		AspectStack previous = current.copy();

		// Set craftability
		isCraftable = ( isCraftableNullable == null ? previous.isCraftable : isCraftableNullable );

		// Calculate the new amount
		long previousAmount = previous.stackSize;
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
			current.stackSize = newAmount;
			current.isCraftable = isCraftable;
		}

		// Return the previous stack
		return previous;
	}

	@Override
	public AspectStack postChange( final AspectStack change )
	{
		// Null check
		if( change == null )
		{
			return null;
		}

		return this.postChange( change.aspect, change.stackSize, change.isCraftable );
	}

	@Override
	public AspectStack remove( final Aspect aspect )
	{
		return this.cache.remove( aspect );
	}

	@Override
	public AspectStack setAspect( final Aspect aspect, final long amount, final boolean isCraftable )
	{
		if( aspect == null )
		{
			return null;
		}

		// Set the stack, and return the old one.
		return this.cache.put( aspect, new AspectStack( aspect, amount, isCraftable ) );

	}

	@Override
	public AspectStack setAspect( final AspectStack stack )
	{
		// Null check
		if( stack == null )
		{
			return null;
		}

		return this.setAspect( stack.aspect, stack.stackSize, stack.isCraftable );
	}

	@Override
	public int size()
	{
		return this.cache.size();
	}
}
