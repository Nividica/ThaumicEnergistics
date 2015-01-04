package thaumicenergistics.inventory;

import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import thaumicenergistics.integration.tc.ArcaneCraftingPattern;

public class HandlerKnowledgeCore
{
	public static final int MAXIMUM_STORED_PATTERNS = 21;

	private ArrayList<ArcaneCraftingPattern> patterns = new ArrayList<ArcaneCraftingPattern>( HandlerKnowledgeCore.MAXIMUM_STORED_PATTERNS );

	public HandlerKnowledgeCore()
	{
	}

	/**
	 * Adds a pattern to the core.
	 */
	public void addPattern( final ArcaneCraftingPattern pattern )
	{
		// Ensure there is room to store the pattern
		if( !this.hasRoomToStorePattern() )
		{
			return;
		}

		// TODO: Check for duplicate patterns
		this.patterns.add( pattern );
	}

	/**
	 * Gets the pattern that produces the result.
	 * 
	 * @param resultStack
	 * @return
	 */
	public ArcaneCraftingPattern getPatternForItem( final ItemStack resultStack )
	{
		for( ArcaneCraftingPattern p : this.patterns )
		{
			if( ( p != null ) && ( p.result != null ) )
			{
				if( ItemStack.areItemStacksEqual( p.result.getItemStack(), resultStack ) )
				{
					return p;
				}
			}
		}

		return null;
	}

	/**
	 * Gets the results of all stored patterns.
	 * 
	 * @return
	 */
	public ArrayList<ItemStack> getStoredOutputs()
	{
		ArrayList<ItemStack> results = new ArrayList<ItemStack>();

		for( ArcaneCraftingPattern p : this.patterns )
		{
			if( ( p != null ) && ( p.result != null ) )
			{
				results.add( p.result.getItemStack() );
			}
		}

		return results;
	}

	/**
	 * Returns true if there is a pattern stored that produces the specified
	 * result.
	 * 
	 * @param resultStack
	 * @return
	 */
	public boolean hasPatternFor( final ItemStack resultStack )
	{
		return( this.getPatternForItem( resultStack ) != null );
	}

	/**
	 * Returns true if there is room to store a new pattern.
	 * 
	 * @return
	 */
	public boolean hasRoomToStorePattern()
	{
		return( this.patterns.size() < HandlerKnowledgeCore.MAXIMUM_STORED_PATTERNS );
	}

	/**
	 * Removes the specified pattern from the core.
	 * 
	 * @param pattern
	 */
	public void removePattern( final ArcaneCraftingPattern pattern )
	{
		this.patterns.remove( pattern );
	}

}
