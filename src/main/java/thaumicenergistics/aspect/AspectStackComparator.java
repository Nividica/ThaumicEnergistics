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

		/**
		 * Cache of the enum values
		 */
		public static final ComparatorMode[] VALUES = ComparatorMode.values();
	}

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
	public AspectStackComparator( final ComparatorMode mode )
	{
		this.mode = mode;
	}

	private int compareByAmount( final AspectStack left, final AspectStack right )
	{
		return (int)( right.amount - left.amount );
	}

	private int compareByTag( final AspectStack left, final AspectStack right )
	{
		return left.aspect.getTag().compareTo( right.aspect.getTag() );
	}

	@Override
	public int compare( final AspectStack left, final AspectStack right )
	{
		switch ( this.mode )
		{
			case MODE_ALPHABETIC:
				// Compare tags
				return this.compareByTag( left, right );

			case MODE_AMOUNT:
				// Compare amounts
				int comparedAmounts = this.compareByAmount( left, right );

				// Are the amounts equal?
				if( comparedAmounts == 0 )
				{
					// Compare tags
					comparedAmounts = this.compareByTag( left, right );
				}

				return comparedAmounts;
		}

		return 0;
	}

}
