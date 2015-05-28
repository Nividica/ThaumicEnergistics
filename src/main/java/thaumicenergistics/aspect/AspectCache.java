package thaumicenergistics.aspect;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import thaumcraft.api.aspects.Aspect;

/**
 * Cache of Aspects
 * 
 * @author Nividica
 * 
 */
public class AspectCache
{
	/**
	 * The actual cache of aspects.
	 */
	private Hashtable<String, Long> cache = new Hashtable<String, Long>();

	/**
	 * Set to true when amounts in the cache have changed.
	 * Nothing sets this to false.<br>
	 * Can be used for tracking purposes, but you must set to false yourself.
	 */
	public boolean flagAmountsModified = false;

	/**
	 * Set to true when an aspect has been added or removed from the cache.
	 * Nothing sets this to false.<br>
	 * Can be used for tracking purposes, but you must set to false yourself.
	 */
	public boolean flagAspectsModified = false;

	public AspectCache()
	{
	}

	/**
	 * Changes the amount in the cache by the specified difference amount.<br>
	 * If the aspect is null no changes will be made.<br>
	 * If diff is positive and the aspect is not stored, it will be added.
	 * If diff is negative and the aspect is not stored, it will not be added.<br>
	 * If the change results in a negative amount, the amount will be adjusted
	 * to 0.
	 * 
	 * @param aspect
	 * @param diff
	 * @return The new amount stored in the cache. Will be zero if the aspect
	 * was null.
	 */
	public long changeAspect( final Aspect aspect, final long diff )
	{
		// Is the aspect null?
		if( aspect == null )
		{
			// Ignored
			return 0;
		}

		// Get the tag
		String tag = aspect.getTag();

		// Get the current amount
		Long curAmount = this.cache.get( tag );

		if( curAmount == null )
		{
			// If the current amount is null the diff must be positive
			if( diff > 0 )
			{
				// Add the aspect
				this.cache.put( tag, diff );

				// Mark aspects dirty
				this.flagAspectsModified = true;

				return diff;
			}

			// Not added
			return 0;
		}

		// Calculate and set the new amount
		long newAmt = Math.max( 0L, curAmount + diff );
		this.cache.put( tag, newAmt );

		// Mark amount dirty
		this.flagAmountsModified = true;

		// Return the new amount
		return newAmt;

	}

	public void decreaseAspect( final Aspect aspect, final long amount )
	{
		// TODO:
		throw new NotImplementedException();

	}

	public void decreaseAspect( final AspectStack stack )
	{
		// TODO:
		throw new NotImplementedException();

	}

	/**
	 * Returns the how much of the specified aspect is cached.
	 * This value will always be non-negative.
	 * 
	 * @param tag
	 * @return
	 */
	public long getAspectAmount( final Aspect aspect )
	{
		// Ensure the aspect is not null
		if( aspect != null )
		{
			// Return the amount
			return this.getAspectAmount( aspect.getTag() );
		}

		// Invalid aspect
		return 0;
	}

	/**
	 * Returns the how much of the specified aspect is cached.
	 * This value will always be non-negative.
	 * 
	 * @param tag
	 * @return
	 */
	public long getAspectAmount( final String tag )
	{
		// Does the cache have this key?
		if( this.cache.containsKey( tag ) )
		{
			// Return the amount
			return this.cache.get( tag );
		}

		// Not in cache
		return 0;
	}

	/**
	 * Gets the names of the stored essentia in alphabetical order
	 */
	public String[] getOrderedAspectTags()
	{
		// Get the tags
		Set<String> keys = this.cache.keySet();

		// Convert to string array
		String[] tags = keys.toArray( new String[keys.size()] );

		// Sort
		Arrays.sort( tags );

		return tags;
	}

	public void increaseAspect( final Aspect aspect, final int amount )
	{
		// TODO:
		throw new NotImplementedException();
	}

	public void increaseAspect( final AspectStack stack )
	{
		// TODO:
		throw new NotImplementedException();

	}

	public void removeAspect( final Aspect aspect )
	{
		// TODO:
		throw new NotImplementedException();

	}

	/**
	 * Removes aspects with an amount of zero from the cache.
	 */
	public void removeEmpty()
	{
		// Get the iterator
		Iterator<Entry<String, Long>> iterator = this.cache.entrySet().iterator();
		while( iterator.hasNext() )
		{
			// Get the next entry
			Entry<String, Long> entry = iterator.next();

			// Is the amount 0?
			if( entry.getValue() == 0 )
			{
				// Remove
				iterator.remove();

				// Mark aspects dirty
				this.flagAspectsModified = true;
			}
		}
	}

	/**
	 * Sets any stored aspect amount to zero.
	 */
	public void resetAll()
	{
		for( Entry<String, Long> element : this.cache.entrySet() )
		{
			element.setValue( 0L );
		}

	}

	/**
	 * Sets the value in the cache to match the specified amount.
	 * If the aspect is null no changes are made.
	 * If the amount is less than 0, the amount set will be 0.
	 * 
	 * @param aspect
	 * @param amount
	 */
	public void setAspect( final Aspect aspect, long amount )
	{
		// Ensure the aspect is not null
		if( aspect != null )
		{
			// Bounds check the amount
			if( amount < 0 )
			{
				amount = 0;
			}

			// Update the cache
			if( this.cache.put( aspect.getTag(), amount ) != null )
			{
				// Mark amount dirty
				this.flagAmountsModified = true;
			}
			else
			{
				// Mark aspects dirty
				this.flagAspectsModified = true;
			}
		}
	}

	/**
	 * Sets the value in the cache to match the specified AspectStack.
	 * If the stack is null no changes are made.
	 * 
	 * @param aspectStack
	 */
	public void setAspect( final AspectStack aspectStack )
	{
		// Ensure the stack is not null
		if( aspectStack != null )
		{
			this.setAspect( aspectStack.aspect, aspectStack.amount );
		}
	}

	/**
	 * Returns the number of aspects in the cache.
	 * 
	 * @return
	 */
	public int size()
	{
		return this.cache.size();
	}

}
