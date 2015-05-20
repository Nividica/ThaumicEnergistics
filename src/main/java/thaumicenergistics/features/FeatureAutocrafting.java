package thaumicenergistics.features;

import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.integration.tc.VisCraftingHelper;
import thaumicenergistics.registries.FeatureRegistry;
import thaumicenergistics.registries.RecipeRegistry;
import thaumicenergistics.registries.ResearchRegistry;
import thaumicenergistics.registries.ResearchRegistry.ResearchTypes;
import thaumicenergistics.tileentities.TileArcaneAssembler;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

public class FeatureAutocrafting
	extends AbstractDependencyFeature
	implements IThaumcraftResearchFeature, ICraftingFeature
{
	public FeatureAutocrafting( final FeatureRegistry fr )
	{
		super( fr );
	}

	/**
	 * Registers the Arcane Assembler research.
	 */
	private void registerArcaneAssembler()
	{
		// Set the research aspects
		AspectList assemblerAspectList = new AspectList();
		assemblerAspectList.add( Aspect.CRAFT, 5 );
		assemblerAspectList.add( Aspect.MECHANISM, 3 );
		assemblerAspectList.add( Aspect.MIND, 3 );
		assemblerAspectList.add( Aspect.EXCHANGE, 3 );
		assemblerAspectList.add( Aspect.AURA, 3 );
		assemblerAspectList.add( Aspect.GREED, 3 );

		// Set the icon
		ItemStack assemblerIcon = ThEApi.instance().blocks().ArcaneAssembler.getStack();

		// Set the pages
		ResearchPage[] assemblerPages = new ResearchPage[] { new ResearchPage( ResearchTypes.ARCANEASSEMBLER.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.BLOCK_ARCANE_ASSEMBLER ),
						new ResearchPage( ResearchTypes.ARCANEASSEMBLER.getPageName( 2 ) ),
						new ResearchPage( ResearchTypes.ARCANEASSEMBLER.getPageName( 3 ) ) };

		// Create the assembler research
		ResearchTypes.ARCANEASSEMBLER.createResearchItem( assemblerAspectList, ResearchRegistry.COMPLEXITY_LARGE, assemblerIcon, assemblerPages );

		// Set the parents
		ResearchTypes.ARCANEASSEMBLER.researchItem.setParents( this.getFirstValidParentKey( false ) );
		ResearchTypes.ARCANEASSEMBLER.researchItem.setParentsHidden( ResearchTypes.ARCANETERMINAL.getKey() );
		ResearchTypes.ARCANEASSEMBLER.researchItem.setConcealed();

		// Trigger when MAC is scanned.
		ResearchTypes.ARCANEASSEMBLER.researchItem.setItemTriggers( FeatureRegistry.instance().getCommonItems().MolecularAssembler );

		// Register the research
		ResearchTypes.ARCANEASSEMBLER.researchItem.registerResearchItem();

	}

	/**
	 * Registers the Knowledge Inscriber research.
	 */
	private void registerKnowledgeInscriber()
	{
		// Set the research aspects
		AspectList kiAspectList = new AspectList();
		kiAspectList.add( Aspect.MIND, 5 );
		kiAspectList.add( Aspect.MECHANISM, 3 );
		kiAspectList.add( Aspect.CRAFT, 3 );
		kiAspectList.add( Aspect.EXCHANGE, 1 );

		// Set the icon
		ItemStack kiIcon = ThEApi.instance().blocks().KnowledgeInscriber.getStack();

		// Set the pages
		ResearchPage[] kiPages = new ResearchPage[] { new ResearchPage( ResearchTypes.KNOWLEDGEINSCRIBER.getPageName( 1 ) ),
						new ResearchPage( RecipeRegistry.ITEM_KNOWLEDGE_CORE ), new ResearchPage( RecipeRegistry.BLOCK_KNOWLEDGE_INSCRIBER ),
						new ResearchPage( ResearchTypes.KNOWLEDGEINSCRIBER.getPageName( 2 ) ) };

		// Create the KI research
		ResearchTypes.KNOWLEDGEINSCRIBER.createResearchItem( kiAspectList, ResearchRegistry.COMPLEXITY_SMALL, kiIcon, kiPages );
		ResearchTypes.KNOWLEDGEINSCRIBER.researchItem.setSecondary();

		// Set the parent
		ResearchTypes.KNOWLEDGEINSCRIBER.researchItem.setParents( ResearchTypes.ARCANEASSEMBLER.getKey() );
		ResearchTypes.KNOWLEDGEINSCRIBER.researchItem.setConcealed();

		// Register the research
		ResearchTypes.KNOWLEDGEINSCRIBER.researchItem.registerResearchItem();
	}

	@Override
	protected boolean checkConfigs()
	{
		// Depends on crafting CPU's
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.CraftingCPU ) )
		{
			return false;
		}

		// Depends on assembler
		if( !AEConfig.instance.isFeatureEnabled( AEFeature.MolecularAssembler ) )
		{
			return false;
		}

		return true;
	}

	@Override
	protected Object[] getItemReqs( final CommonDependantItems cdi )
	{
		return new Object[] { cdi.VibrantGlass, cdi.CalculationProcessor, cdi.LogicProcessor, cdi.MolecularAssembler };
	}

	@Override
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		if( includeSelf && this.isAvailable() )
		{
			return ResearchTypes.ARCANEASSEMBLER.getKey();
		}

		// Pass to parent
		return FeatureRegistry.instance().featureVRI.getFirstValidParentKey( true );
	}

	@Override
	public void registerCrafting()
	{
		// Common items
		CommonDependantItems cdi = FeatureRegistry.instance().getCommonItems();

		// Thaumcraft items
		ItemStack CraftingScepter = VisCraftingHelper.instance.getCraftingScepter();

		// My Items
		ItemStack KnowledgeCore = ThEApi.instance().items().KnowledgeCore.getStack();
		ItemStack KnowledgeInscriber = ThEApi.instance().blocks().KnowledgeInscriber.getStack();
		ItemStack ArcaneAssembler = ThEApi.instance().blocks().ArcaneAssembler.getStack();
		ArcaneAssembler.setTagCompound( TileArcaneAssembler.getCraftTag() );

		// Knowledge Core
		AspectList kCoreAspects = new AspectList();
		kCoreAspects.add( Aspect.WATER, 3 );
		kCoreAspects.add( Aspect.ORDER, 3 );
		kCoreAspects.add( Aspect.EARTH, 1 );

		RecipeRegistry.ITEM_KNOWLEDGE_CORE = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.KNOWLEDGEINSCRIBER.getKey(),
			KnowledgeCore, kCoreAspects, new Object[] { "VLV", "LZL", "VCV", 'V', cdi.VibrantGlass, 'L', cdi.VanillaLapis, 'Z', cdi.ZombieBrain, 'C',
							cdi.CalculationProcessor } );

		// Knowledge Inscriber
		AspectList kiAspects = new AspectList();
		kiAspects.add( Aspect.WATER, 5 );
		kiAspects.add( Aspect.ORDER, 5 );
		kiAspects.add( Aspect.EARTH, 5 );
		kiAspects.add( Aspect.ENTROPY, 5 );
		kiAspects.add( Aspect.FIRE, 5 );
		kiAspects.add( Aspect.AIR, 5 );

		RecipeRegistry.BLOCK_KNOWLEDGE_INSCRIBER = ThaumcraftApi.addArcaneCraftingRecipe( ResearchRegistry.ResearchTypes.KNOWLEDGEINSCRIBER.getKey(),
			KnowledgeInscriber, kiAspects, new Object[] { "IPI", " T ", "ILI", 'I', cdi.IronIngot, 'P', cdi.IlluminatedPanel, 'T', cdi.Thaumonomicon,
							'L', cdi.LogicProcessor } );

		// Arcane Assembler
		AspectList assemblerAspects = new AspectList();
		assemblerAspects.add( Aspect.CRAFT, 64 );
		assemblerAspects.add( Aspect.EXCHANGE, 32 );
		assemblerAspects.add( Aspect.AURA, 16 );
		assemblerAspects.add( Aspect.MAGIC, 16 );
		assemblerAspects.add( Aspect.METAL, 8 );
		assemblerAspects.add( Aspect.CRYSTAL, 8 );

		// Infusion provider recipe items
		ItemStack[] assemblerItems = { CraftingScepter, cdi.AirShard, cdi.EarthShard, cdi.WaterShard, cdi.BallanceShard, cdi.OrderShard,
						cdi.EntropyShard, cdi.FireShard };

		// Create the infusion provider recipe
		RecipeRegistry.BLOCK_ARCANE_ASSEMBLER = ThaumcraftApi.addInfusionCraftingRecipe( ResearchRegistry.ResearchTypes.ARCANEASSEMBLER.getKey(),
			ArcaneAssembler, 7, assemblerAspects, cdi.MolecularAssembler, assemblerItems );
	}

	@Override
	public void registerResearch()
	{
		this.registerArcaneAssembler();
		this.registerKnowledgeInscriber();
	}

}
