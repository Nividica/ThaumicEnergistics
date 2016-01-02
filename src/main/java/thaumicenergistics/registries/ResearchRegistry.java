package thaumicenergistics.registries;

import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.integration.tc.PseudoResearchItem;

public class ResearchRegistry
{
	// Fake research, serves as placeholder for existing research
	public static enum PseudoResearchTypes
	{
			DISTILESSENTIA ("DISTILESSENTIA", "ALCHEMY", -2, 0),
			TUBEFILTER ("TUBEFILTER", "ALCHEMY", -3, 0),
			MIRROR ("MIRROR", "ARTIFICE", -4, 0),
			JAR ("JARLABEL", "ALCHEMY", -4, 0),
			INFUSION ("INFUSION", "ARTIFICE", -6, 0),
			COREUSE ("COREUSE", "GOLEMANCY", 4, 3),
			DUPE ("ALCHEMICALDUPLICATION", "ALCHEMY", -5, -6),
			WARDED ("WARDEDARCANA", "ARTIFICE", 1, 2),
			FOCUSFIRE ("FOCUSFIRE", "THAUMATURGY", -4, -7),
			VISPOWER ("VISPOWER", "THAUMATURGY", 4, 0),
			SCEPTRE ("SCEPTRE", "THAUMATURGY", 5, 0);

		private String realResearchKey;
		private String realResearchCategory;
		private int column;
		private int row;
		private PseudoResearchItem researchItem;
		private boolean hasRegistered = false;

		private PseudoResearchTypes( final String key, final String cat, final int column, final int row )
		{
			this.realResearchCategory = cat;
			this.realResearchKey = key;
			this.column = column;
			this.row = row;
		}

		public String getKey()
		{
			return ThaumicEnergistics.MOD_ID + ".Pseudo." + this.realResearchKey;
		}

		public void registerPsudeoResearch()
		{
			if( !this.hasRegistered )
			{
				this.researchItem = PseudoResearchItem.newPseudo( this.getKey(), TERESEARCH_TAB, this.realResearchKey, this.realResearchCategory,
					this.column, this.row );
				this.researchItem.registerResearchItem();
				this.hasRegistered = true;
			}
		}
	}

	// Research types
	public static enum ResearchTypes
	{
			BASIC ("RESEARCH", 0, 0),
			CORES ("CORES", 0, -2),
			STORAGE ("STORAGE", 0, 2),
			IO ("IO", -2, -2),
			ARCANETERMINAL ("ARCANETERM", 2, -1),
			ESSENTIATERMINAL ("ESSTERM", -1, -4),
			ESSENTIAPROVIDER ("ESSPROV", -2, -4),
			INFUSIONPROVIDER ("INFPROV", -5, -2),
			IRONGEARBOX ("IRONGEARBOX", 3, 2),
			THAUMIUMGEARBOX ("THAUMGBOX", 3, 3),
			CERTUSDUPE ("CERTUSDUPE", -5, -5),
			VISINTERFACE ("VISINT", 4, -1),
			ARCANEASSEMBLER ("ARCANEASSEMBLER", 6, -1),
			KNOWLEDGEINSCRIBER ("KNOWLEDGEINSCRIBER", 6, 0),
			FOCUSWRENCH ("FOCUSWRENCH", -3, -7),
			ESSENTIAVIBRATIONCHAMBER ("ESSVIBCMBR", 1, -4);

		private String internalName;

		private int column;

		private int row;

		public ResearchItem researchItem;

		private ResearchTypes( final String internalName, final int column, final int row )
		{
			this.internalName = "TE" + internalName;
			this.row = row;
			this.column = column;
		}

		/**
		 * Convenience function to aid in research item creation.
		 * 
		 * @param aspectList
		 * @param column
		 * @param row
		 * @param complexity
		 * @param icon
		 * @param pages
		 */
		public void createResearchItem( final AspectList aspectList, final int complexity, final ItemStack icon, final ResearchPage[] pages )
		{
			this.researchItem = new ResearchItem( this.getKey(), TERESEARCH_TAB, aspectList, this.column, this.row, complexity, icon );
			this.researchItem.setPages( pages );
		}

		public String getKey()
		{
			return ThaumicEnergistics.MOD_ID + "." + this.internalName;
		}

		public String getPageName( final int index )
		{
			return ThaumicEnergistics.MOD_ID + ".research_page." + this.internalName + "." + index;
		}

		public ResearchItem getResearchItem()
		{
			return this.researchItem;
		}
	}

	// Complexity of research
	public static final int COMPLEXITY_SMALL = 1;
	public static final int COMPLEXITY_MEDIUM = 2;
	public static final int COMPLEXITY_LARGE = 3;

	// Research Tab name
	public static final String TERESEARCH_TAB = ThaumicEnergistics.MOD_ID;
}
