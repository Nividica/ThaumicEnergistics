package thaumicenergistics.aspect;

import java.util.Comparator;

/**
 * Compares one aspect stack against another.
 * 
 * @author Nividica
 * 
 */
public class AspectStackComparator
	implements Comparator<AspectStack>
{

	private ComparatorMode mode;

	/**
	 * Creates the comparator with sorting mode alphabetic.
	 */
	public AspectStackComparator()
	{
		this( ComparatorMode.MODE_ALPHABETIC );
	}

	/**
	 * Creates the comparator with specified sorting mode.
	 * If the mode is unrecognized, the list will not be sorted.
	 * 
	 * @param mode
	 * Mode to sort by.
	 */
	public AspectStackComparator( ComparatorMode mode )
	{
		this.mode = mode;
	}

	@Override
	public int compare( AspectStack left, AspectStack right )
	{
		switch ( this.mode )
		{
			case MODE_ALPHABETIC:
				// Compare tags
				return this.compareByTag( left, right );

			case MODE_AMOUNT:
				// Compare amounts
				int comp = this.compareByAmount( left, right );
				
				// Are the amounts equal?
				if( comp == 0 )
				{
					// Compare tags
					comp = this.compareByTag( left, right );
				}
				
				return comp;
		}

		return 0;
	}
	
	private int compareByTag( AspectStack left, AspectStack right )
	{
		return left.aspect.getTag().compareTo( right.aspect.getTag() );
	}
	
	private int compareByAmount( AspectStack left, AspectStack right )
	{
		return (int)( right.amount - left.amount );
	}

	public enum ComparatorMode
	{
			/**
			 * Compare based on name
			 */
			MODE_ALPHABETIC,

			/**
			 * Compare based on amount
			 */
			MODE_AMOUNT;
	}

}
