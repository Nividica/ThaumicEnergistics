package thaumicenergistics.integration.tc;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PseudoResearchItem
	extends ResearchItem
{
	private ResearchItem realResearch;

	/**
	 * Icon as itemstack
	 * 
	 * @param key
	 * @param category
	 * @param column
	 * @param row
	 * @param icon
	 */
	private PseudoResearchItem( final String key, final String category, final int column, final int row, final ItemStack icon )
	{
		// Call super and create the research item
		super( key, category, new AspectList(), column, row, 1, icon );

		// Set as a stub
		this.setStub().setHidden();
	}

	/**
	 * Icon as resource location
	 * 
	 * @param key
	 * @param category
	 * @param column
	 * @param row
	 * @param icon
	 */
	private PseudoResearchItem( final String key, final String category, final int column, final int row, final ResourceLocation icon )
	{
		// Call super and create the research item
		super( key, category, new AspectList(), column, row, 1, icon );

		// Set as a stub
		this.setStub().setHidden();
	}

	/**
	 * Creates a new pseudo research item.
	 * 
	 * @param key
	 * @param category
	 * @param realKey
	 * @param realCategory
	 * @param column
	 * @param row
	 * @return
	 */
	public static PseudoResearchItem newPseudo( final String key, final String category, final String realKey, final String realCategory,
												final int column, final int row )
	{
		PseudoResearchItem pseudo;

		// Get the actual research item
		ResearchItem realResearch = ResearchCategories.researchCategories.get( realCategory ).research.get( realKey );

		// Create the pseudo research
		if( realResearch.icon_item != null )
		{
			pseudo = new PseudoResearchItem( key, category, column, row, realResearch.icon_item );
		}
		else
		{
			pseudo = new PseudoResearchItem( key, category, column, row, realResearch.icon_resource );
		}

		// Set the real research item
		pseudo.realResearch = realResearch;

		// Link to the research
		pseudo.linkToRealResearch();

		return pseudo;
	}

	/**
	 * Adds this research as a sibling.
	 * This will ensure this unlocks when the real research does.
	 */
	private void linkToRealResearch()
	{

		// Add to the siblings
		// Does the original research have any siblings?
		if( this.realResearch.siblings == null )
		{
			// Add this to the siblings
			this.realResearch.setSiblings( this.key );
		}
		else
		{
			// Get the siblings
			String[] prevSibs = this.realResearch.siblings;
			String[] newSibs = new String[prevSibs.length + 1];

			// Copy prev into new
			System.arraycopy( prevSibs, 0, newSibs, 0, prevSibs.length );

			// Add this to the siblings
			newSibs[prevSibs.length] = this.key;

			// Re-set the siblings
			this.realResearch.setSiblings( newSibs );
		}
	}

	/**
	 * Redirect function to get the name of the research from
	 * the real research.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public String getName()
	{
		return this.realResearch.getName();
	}

	/**
	 * Redirect function to get the research pages from the
	 * real research.
	 */
	@Override
	public ResearchPage[] getPages()
	{
		return this.realResearch.getPages();
	}

	/**
	 * Redirect function to get the research text from the
	 * real research.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public String getText()
	{
		return this.realResearch.getText();
	}

}
