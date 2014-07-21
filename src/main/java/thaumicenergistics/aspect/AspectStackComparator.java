package thaumicenergistics.aspect;

import java.util.Comparator;

/**
 * Compares one aspect stack against another.
 * @author Nividica
 *
 */
public class AspectStackComparator implements Comparator<AspectStack>
{
	/**
	 * Compare based on name
	 */
	public static final int MODE_ALPHABETIC = 0;
	
	/**
	 * Compare based on amount
	 */
	public static final int MODE_AMOUNT = 1;
	
	public int mode;
	
	/**
	 * Creates the comparator with sorting mode alphabetic.
	 */
	public AspectStackComparator()
	{
		this.mode = AspectStackComparator.MODE_ALPHABETIC;
	}

	/**
	 * Creates the comparator with specified sorting mode.
	 * If the mode is unrecognized, the list will not be sorted.
	 * 
	 * @param mode Mode to sort by.
	 */
	public AspectStackComparator( int mode )
	{
		this.mode = mode;
	}

	@Override
	public int compare( AspectStack left, AspectStack right )
	{
		switch( this.mode )
		{
			case AspectStackComparator.MODE_ALPHABETIC:
				// Compare tags
				return left.aspect.getTag().compareTo( right.aspect.getTag() );
				
			case AspectStackComparator.MODE_AMOUNT:
				// Compare amounts
				return (int)( left.amount - right.amount );
		}
		
		return 0;
	}

}
