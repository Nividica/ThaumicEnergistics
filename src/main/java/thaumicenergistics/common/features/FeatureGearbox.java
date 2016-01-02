package thaumicenergistics.common.features;

import java.util.EnumSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;

public class FeatureGearbox
	extends AbstractBasicFeature
{

	public FeatureGearbox()
	{
		super( true );
	}

	@Override
	protected void registerCrafting()
	{
		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// Ore dictionary items
		Object WoodGear = "gearWood";
		String IronGear = "gearIron";

		// Are there any wooden gears?
		if( OreDictionary.getOres( "gearWood" ).size() == 0 )
		{
			// Fall back on a stick :-/
			WoodGear = new ItemStack( (Item)Item.itemRegistry.getObject( "stick" ) );
		}

		// My Items
		ItemStack IronGearBox = ThEApi.instance().blocks().IronGearBox.getStack();
		ItemStack ThaumiumGearBox = ThEApi.instance().blocks().ThaumiumGearBox.getStack();
		ItemStack ThEIronGear = ThEApi.instance().items().IronGear.getStack();

		// Set Iron Gear aspects
		AspectList ironGearAspects = new AspectList();
		ironGearAspects.add( Aspect.EARTH, 1 );
		ironGearAspects.add( Aspect.FIRE, 1 );

		// Iron Gear recipe
		Object[] recipeIronGear = new Object[] { " I ", " W ", "I I", 'I', cdi.IronIngot, 'W', WoodGear };

		// Register Iron Gear
		RecipeRegistry.MATERIAL_IRON_GEAR = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.IRONGEARBOX.getKey(), ThEIronGear,
			ironGearAspects, recipeIronGear );

		// Set Iron Gear Box aspects
		AspectList igbAspects = new AspectList();
		igbAspects.add( Aspect.AIR, 2 );
		igbAspects.add( Aspect.ORDER, 2 );

		// Iron Gear Box recipe
		Object[] recipeIronGearBox = new Object[] { "SGS", "GGG", "SGS", 'S', cdi.Cobblestone, 'G', IronGear };

		RecipeRegistry.BLOCK_IRONGEARBOX = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.IRONGEARBOX.getKey(), IronGearBox,
			igbAspects, recipeIronGearBox );

		// Set Thaumium Gear Box aspects
		AspectList tgbAspects = new AspectList();
		tgbAspects.add( Aspect.METAL, 16 );
		tgbAspects.add( Aspect.MAGIC, 16 );

		// Register Thaumium Gear Box
		RecipeRegistry.BLOCK_THAUMIUMGEARBOX = ThaumcraftApi.addCrucibleRecipe( ResearchRegistry.ResearchTypes.THAUMIUMGEARBOX.getKey(),
			ThaumiumGearBox, IronGearBox, tgbAspects );
	}

	@Override
	protected void registerResearch()
	{
		// Set the research aspects for the Iron Gear Box
		AspectList igbAspects = new AspectList();
		igbAspects.add( Aspect.MECHANISM, 6 );
		igbAspects.add( Aspect.METAL, 4 );
		igbAspects.add( Aspect.EXCHANGE, 4 );

		// Set the icon for the Iron Gear Box
		ItemStack igbIcon = ThEApi.instance().items().IronGear.getStack();

		// Set the pages for the Iron Gear Box
		ResearchPage[] igbPages = new ResearchPage[] { new ResearchPage( ResearchTypes.IRONGEARBOX.getPageName( 1 ) ),
						new ResearchPage( ResearchTypes.IRONGEARBOX.getPageName( 2 ) ), new ResearchPage( RecipeRegistry.MATERIAL_IRON_GEAR ),
						new ResearchPage( RecipeRegistry.BLOCK_IRONGEARBOX ) };

		// Create the research for the Iron Gear Box
		ResearchTypes.IRONGEARBOX.createResearchItem( igbAspects, ResearchRegistry.COMPLEXITY_SMALL, igbIcon, igbPages );

		// Set as secondary and register
		ResearchTypes.IRONGEARBOX.researchItem.setSecondary().registerResearchItem();

		// Set the research aspects for the Thaumium Gear Box
		AspectList tgbAspects = new AspectList();
		tgbAspects.add( Aspect.MECHANISM, 10 );
		tgbAspects.add( Aspect.MAGIC, 8 );
		tgbAspects.add( Aspect.METAL, 5 );

		// Set the icon for the Thaumium Gear Box
		ItemStack tgbIcon = ThEApi.instance().blocks().ThaumiumGearBox.getStack();

		// Set the pages for the Thaumium Gear Box
		ResearchPage[] tgbPages = new ResearchPage[] { new ResearchPage( ResearchTypes.THAUMIUMGEARBOX.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.BLOCK_THAUMIUMGEARBOX ) };

		// Create the item for the Thaumium Gear Box
		ResearchTypes.THAUMIUMGEARBOX.createResearchItem( tgbAspects, ResearchRegistry.COMPLEXITY_SMALL, tgbIcon, tgbPages );

		// Set parents for the Thaumium Gear Box
		ResearchTypes.THAUMIUMGEARBOX.researchItem.setParents( this.getFirstValidParentKey( true ), PseudoResearchTypes.COREUSE.getKey() );
		ResearchTypes.THAUMIUMGEARBOX.researchItem.setParentsHidden( "COREUSE" );

		// Set as secondary and register
		ResearchTypes.THAUMIUMGEARBOX.researchItem.setSecondary().registerResearchItem();
	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.IRONGEARBOX.getKey();
		}

		// No parent to pass to
		return "";
	}

	@Override
	public EnumSet<PseudoResearchTypes> getPseudoParentTypes()
	{
		return EnumSet.of( PseudoResearchTypes.COREUSE );
	}

}
