package thaumicenergistics.registries;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.Items;
import thaumicenergistics.api.TEApi;
import appeng.api.AEApi;
import appeng.api.definitions.Blocks;
import appeng.api.definitions.Materials;
import appeng.api.definitions.Parts;
import appeng.api.features.IGrinderEntry;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.api.util.AEItemDefinition;
import appeng.recipes.GroupIngredient;
import appeng.recipes.Ingredient;
import appeng.recipes.handlers.Inscribe;
import appeng.recipes.handlers.Inscribe.InscriberRecipe;
import cpw.mods.fml.common.FMLLog;

/**
 * Gives items from AE2 aspects when scanned.
 * 
 * @author Nividica
 * 
 */
public class AEAspectRegister
{
	/**
	 * Helper class to derive aspects from an item definition.
	 * 
	 * @author Nividica
	 * 
	 */
	private class AEItemInfo
	{
		/**
		 * Human readable name of the item.
		 */
		private String displayName = "";

		/**
		 * Selected ingredients used to craft this item.
		 */
		private ItemStack[] ingredients = null;

		/**
		 * Aspects derived from the ingredients.
		 */
		private AspectList ingredientAspects = new AspectList();

		/**
		 * Recipe used to craft this item.
		 */
		private Object recipe = null;

		/**
		 * Definition of the item.
		 */
		private AEItemDefinition definition;

		/**
		 * Aspects manually added to the item.
		 */
		public AspectList bonusAspects = new AspectList();

		/**
		 * Itemstack holding the item.
		 */
		public ItemStack itemStack;

		/**
		 * Creates the info.
		 * 
		 * @param itemDef
		 */
		public AEItemInfo( final AEItemDefinition itemDef )
		{
			this.itemStack = itemDef.stack( 1 );
			this.displayName = this.itemStack.getDisplayName();
			this.definition = itemDef;
		}

		/**
		 * Adds bonus aspects for certain types.
		 */
		private void addPredefinedBonuses()
		{
			Blocks aeBlocks = AEApi.instance().blocks();
			Materials aeMats = AEApi.instance().materials();

			// Grinder recipe?
			if( this.recipe instanceof IGrinderEntry )
			{
				// Add a bonus aspect for grinding
				this.bonusAspects.add( Aspect.ENTROPY, 1 );

				// Remove any Terra
				this.ingredientAspects.remove( Aspect.EARTH );
			}

			// Molecular Assembler
			else if( this.definition.equals( aeBlocks.blockMolecularAssembler ) )
			{
				// Add extra craft, machina, potentia and vitrius
				this.bonusAspects.add( Aspect.CRAFT, 4 );
				this.bonusAspects.add( Aspect.ENERGY, 6 );
				this.bonusAspects.add( Aspect.MECHANISM, 2 );
				this.bonusAspects.add( Aspect.CRYSTAL, 1 );
			}

			// Sky stone
			else if( this.definition.equals( aeBlocks.blockSkyStone ) )
			{
				// Add tene
				this.bonusAspects.add( Aspect.DARKNESS, 3 );
			}

			// Sky chest
			else if( this.definition.equals( aeBlocks.blockSkyChest ) )
			{
				// Add vac
				this.bonusAspects.add( Aspect.VOID, 4 );

				// Remove terra
				this.ingredientAspects.remove( Aspect.EARTH );
			}

			// Quartz glass
			else if( this.definition.equals( aeBlocks.blockQuartzGlass ) )
			{
				// Add vit
				this.bonusAspects.add( Aspect.CRYSTAL, 4 );
			}

			// ME Controller
			else if( this.definition.equals( aeBlocks.blockController ) )
			{
				// Add mach, sense, mind
				this.bonusAspects.add( Aspect.MECHANISM, 3 );
				this.bonusAspects.add( Aspect.SENSES, 1 );
				this.bonusAspects.add( Aspect.MIND, 5 );

				// Remove fire, order
				this.ingredientAspects.remove( Aspect.FIRE );
				this.ingredientAspects.remove( Aspect.ORDER );
			}

			// ME Drive
			else if( this.definition.equals( aeBlocks.blockDrive ) )
			{
				// Add vac
				this.bonusAspects.add( Aspect.VOID, 4 );

				// Remove fire, order, sense, mach
				this.ingredientAspects.remove( Aspect.FIRE );
				this.ingredientAspects.remove( Aspect.ORDER );
				this.ingredientAspects.remove( Aspect.SENSES );
				this.ingredientAspects.remove( Aspect.MECHANISM );
			}

			// ME Chest
			else if( this.definition.equals( aeBlocks.blockChest ) )
			{
				// Add vac
				this.bonusAspects.add( Aspect.VOID, 4 );

				// Remove fire, order, sense, mach
				this.ingredientAspects.remove( Aspect.FIRE );
				this.ingredientAspects.remove( Aspect.ORDER );
				this.ingredientAspects.remove( Aspect.SENSES );
				this.ingredientAspects.remove( Aspect.MECHANISM );
			}

			// ME IO Port
			else if( this.definition.equals( aeBlocks.blockIOPort ) )
			{
				// Add perm
				this.bonusAspects.add( Aspect.EXCHANGE, 4 );

				// Remove fire, order, sense, mach
				this.ingredientAspects.remove( Aspect.FIRE );
				this.ingredientAspects.remove( Aspect.ORDER );
				this.ingredientAspects.remove( Aspect.SENSES );
				this.ingredientAspects.remove( Aspect.MECHANISM );
			}

			// ME IO Port
			else if( this.definition.equals( aeBlocks.blockCondenser ) )
			{
				// Add vac
				this.bonusAspects.add( Aspect.VOID, 10 );
			}

			else if( this.definition.equals( aeMats.materialCardRedstone ) )
			{
				// Add sense
				this.bonusAspects.add( Aspect.SENSES, 2 );
			}

		}

		/**
		 * Builds the aspects for the item via recipe => ingredients
		 */
		private void buildAspects()
		{
			// Grinder recipes
			for( IGrinderEntry recipe : AEAspectRegister.this.GRINDER_RECIPES )
			{
				ItemStack recipeOutput = recipe.getOutput();

				if( this.areStacksEqualIgnoreAmount( recipeOutput, this.itemStack ) )
				{
					if( this.isRecipeUsable( recipe ) )
					{
						return;
					}
				}
			}

			// Inscriber recipes
			for( InscriberRecipe recipe : Inscribe.recipes )
			{
				ItemStack recipeOutput = recipe.output;

				if( this.areStacksEqualIgnoreAmount( recipeOutput, this.itemStack ) )
				{
					if( this.isRecipeUsable( recipe ) )
					{
						return;
					}
				}
			}

			// Regular crafting recipes
			for( IRecipe recipe : AEAspectRegister.this.NORMAL_RECIPES )
			{
				// Get the recipe's result
				ItemStack recipeStack = recipe.getRecipeOutput();

				if( recipeStack == null )
				{
					continue;
				}

				// Is this what we are looking for?
				if( !this.areStacksEqualIgnoreAmount( recipeStack, this.itemStack ) )
				{
					continue;
				}

				if( this.isRecipeUsable( recipe ) )
				{
					return;
				}

			}
		}

		/**
		 * Builds the aspect list from the items ingredients.
		 * 
		 * @param itemInfo
		 */
		private void findAspectsForIngredients()
		{
			for( ItemStack stack : this.ingredients )
			{
				// Get the aspects for the stack
				AspectList stackAspects = ThaumcraftApiHelper.getObjectAspects( stack );

				// Are there no aspects for this item?
				if( ( stackAspects == null ) || ( stackAspects.size() == 0 ) )
				{
					// Is the item an AE item?
					AEItemInfo ingInfo = AEAspectRegister.this.getInfoForDefinitionOrStack( stack );

					if( ingInfo != null )
					{
						// Register the item
						ingInfo.registerItem();

						// Attempt to get the aspects again
						stackAspects = ThaumcraftApiHelper.getObjectAspects( stack );
					}
				}

				// Are there still no aspects to add?
				if( ( stackAspects == null ) || ( stackAspects.size() == 0 ) )
				{
					continue;
				}

				// Assign each aspect
				for( Aspect currentStackAspect : stackAspects.getAspects() )
				{
					// Get the current amount
					int existingAmount = this.ingredientAspects.getAmount( currentStackAspect );

					// Get the ingredient amount 
					int ingredientAspectAmount = stackAspects.getAmount( currentStackAspect );

					// Merge
					if( existingAmount < ingredientAspectAmount )
					{
						this.ingredientAspects.add( currentStackAspect, ( ingredientAspectAmount - existingAmount ) );
					}
				}
			}
		}

		/**
		 * Builds a list of ingredients for this item.
		 */
		private void getIngredientsForRecipe()
		{
			Object[] ingredientObjects = null;

			// What kind of recipe is this?
			if( this.recipe instanceof appeng.recipes.game.ShapedRecipe )
			{
				// Get ingredients
				ingredientObjects = ( (appeng.recipes.game.ShapedRecipe)this.recipe ).getInput();
			}

			else if( this.recipe instanceof appeng.recipes.game.ShapelessRecipe )
			{
				// Get ingredients
				ingredientObjects = ( (appeng.recipes.game.ShapelessRecipe)this.recipe ).getInput().toArray();
			}

			else if( this.recipe instanceof IGrinderEntry )
			{
				// Get ingredient
				ingredientObjects = new Object[] { ( (IGrinderEntry)this.recipe ).getInput() };

				// Add a bonus aspect for grinding
				this.bonusAspects.add( Aspect.ENTROPY, 1 );
			}

			else if( this.recipe instanceof InscriberRecipe )
			{
				// Cast
				InscriberRecipe iRec = (InscriberRecipe)this.recipe;

				// Get imprintable count
				int imprintCount = iRec.imprintable.length;

				// Create array
				ingredientObjects = new Object[2 + imprintCount];

				// Add imprintables
				for( int i = 0; i < imprintCount; i++ )
				{
					ingredientObjects[i] = iRec.imprintable[i];
				}

				// Add plates
				ingredientObjects[imprintCount] = iRec.plateA;
				ingredientObjects[imprintCount + 1] = iRec.plateB;

			}

			else if( this.recipe instanceof net.minecraft.item.crafting.ShapedRecipes )
			{
				// Get ingredients
				ingredientObjects = ( (net.minecraft.item.crafting.ShapedRecipes)this.recipe ).recipeItems;
			}

			else if( this.recipe instanceof net.minecraft.item.crafting.ShapelessRecipes )
			{
				// Get ingredients
				ingredientObjects = ( (net.minecraft.item.crafting.ShapelessRecipes)this.recipe ).recipeItems.toArray();
			}

			// Did we get the object list?
			if( ingredientObjects != null )
			{
				ItemStack currentIngredientStack;
				List<ItemStack> ingredientStackList = new ArrayList<ItemStack>();

				// Get the itemstack of each one
				for( int i = 0; i < ingredientObjects.length; i++ )
				{
					// Get the ingredient
					Object ing = ingredientObjects[i];

					// What is the ingredient?
					if( ing == null )
					{
						continue;
					}

					// Clear the stack
					currentIngredientStack = null;
					try
					{
						if( ing instanceof Ingredient )
						{
							if( ( (Ingredient)ing ).nameSpace.equalsIgnoreCase( "oreDictionary" ) )
							{
								currentIngredientStack = ( (Ingredient)ing ).getItemStackSet()[0];
							}
							else
							{
								currentIngredientStack = ( (Ingredient)ing ).getItemStack();
							}
						}
						else if( ing instanceof GroupIngredient )
						{
							currentIngredientStack = ( (GroupIngredient)ing ).getItemStackSet()[0];
						}
						else if( ing instanceof ItemStack )
						{
							currentIngredientStack = (ItemStack)ing;
						}
						else
						{
							//System.out.println( "Missing ingredient clause for" );
							//System.out.println( ing.getClass() );
							//System.out.println();
						}

					}
					catch( Exception e )
					{
						//System.out.println( e );
					}

					// Skip null stacks
					if( currentIngredientStack == null )
					{
						continue;
					}

					// Add to the list
					ingredientStackList.add( currentIngredientStack );
				}

				// Was anything added?
				if( ingredientStackList.size() > 0 )
				{
					// Convert to array
					this.ingredients = new ItemStack[ingredientStackList.size()];
					this.ingredients = ingredientStackList.toArray( this.ingredients );
				}
			}
			else
			{
				//System.out.println( this.displayName + ": getIngredients() failed" );
			}
		}

		/**
		 * Determines if the recipe is valid, and aspects
		 * can be derived from it.
		 * 
		 * @param recipe
		 * @return
		 */
		private boolean isRecipeUsable( final Object recipe )
		{
			// Set the recipe
			this.recipe = recipe;

			// Get the ingredients
			this.getIngredientsForRecipe();

			// Did we get the ingredients?
			if( this.ingredients != null )
			{
				// Get the aspects
				this.findAspectsForIngredients();

				// Did we get aspects?
				if( this.ingredientAspects.size() != 0 )
				{
					return true;
				}
			}

			this.recipe = null;
			return false;
		}

		/**
		 * Trims low values when high values are present.
		 */
		private void reballanceIngredientAspects()
		{
			int aspectCount = this.ingredientAspects.size();

			if( aspectCount == 0 )
			{
				return;
			}

			// Find maximum & minimum
			int max = 0;
			int min = Integer.MAX_VALUE;
			for( Aspect aspect : this.ingredientAspects.getAspects() )
			{
				int amount = this.ingredientAspects.getAmount( aspect );

				max = Math.max( max, amount );
				min = Math.min( min, amount );
			}

			// Calculate trim point
			int trimPoint = max / 5;

			if( trimPoint < min )
			{
				// Nothing to trim
				return;
			}

			// Get the iterator
			Iterator<Entry<Aspect, Integer>> iterator = this.ingredientAspects.aspects.entrySet().iterator();

			while( iterator.hasNext() )
			{
				// Get next
				Entry<Aspect, Integer> entry = iterator.next();

				// Trim?
				if( entry.getValue() < trimPoint )
				{
					// Trim.
					iterator.remove();
				}
			}
		}

		/**
		 * Determines if the specified stacks are equal.
		 * 
		 * @param stack1
		 * @param stack2
		 * @return
		 */
		public boolean areStacksEqualIgnoreAmount( final ItemStack stack1, final ItemStack stack2 )
		{
			// Nulls never match
			if( ( stack1 == null ) || ( stack2 == null ) )
			{
				return false;
			}

			if( stack1.getItem().equals( stack2.getItem() ) )
			{
				if( stack1.getHasSubtypes() )
				{
					if( stack1.getItemDamage() == stack2.getItemDamage() )
					{
						return true;
					}
				}
				else
				{
					return true;
				}

			}

			return false;
		}

		/**
		 * Checks if this item matches the specified object.
		 */
		@Override
		public boolean equals( final Object obj )
		{
			if( obj instanceof AEItemInfo )
			{
				return this.isMatch( ( (AEItemInfo)obj ).itemStack );
			}
			else if( obj instanceof ItemStack )
			{
				return this.isMatch( (ItemStack)obj );
			}
			else if( obj instanceof AEItemDefinition )
			{
				return this.isMatch( ( (AEItemDefinition)obj ).stack( 1 ) );
			}

			return false;
		}

		/**
		 * Builds the final aspect list used for this item.
		 * 
		 * @return
		 */
		public AspectList getFinalAspects()
		{
			AspectList finalAspects = new AspectList();

			// Add ingredient aspects
			if( this.ingredientAspects != null )
			{
				for( Aspect aspect : this.ingredientAspects.getAspects() )
				{
					finalAspects.add( aspect, this.ingredientAspects.getAmount( aspect ) );
				}
			}

			// Add bonus aspects
			if( this.bonusAspects != null )
			{
				for( Aspect aspect : this.bonusAspects.getAspects() )
				{
					finalAspects.add( aspect, this.bonusAspects.getAmount( aspect ) );
				}
			}

			return finalAspects;
		}

		/**
		 * Hashcode
		 */
		@Override
		public int hashCode()
		{
			return this.itemStack.getItem().hashCode() + this.itemStack.getItemDamage();
		}

		/**
		 * Compares the specified itemstack with this itemstack.
		 * 
		 * @param stack
		 * @return
		 */
		public boolean isMatch( final ItemStack stack )
		{
			return this.areStacksEqualIgnoreAmount( stack, this.itemStack );
		}

		/**
		 * Builds this items aspect list, then registers it with Thaumcraft.
		 */
		public void registerItem()
		{
			// Get the ingredients
			this.buildAspects();

			// Calculate the preset bonuses
			this.addPredefinedBonuses();

			// Clean up the ingredient aspects
			this.reballanceIngredientAspects();

			// Are there aspects?
			if( ( this.ingredientAspects.size() == 0 ) && ( this.bonusAspects.size() == 0 ) )
			{
				// Nothing to register
				FMLLog.info( "%s: '%s' was not registered.", ThaumicEnergistics.MOD_ID, this.displayName );
				return;
			}

			// Register the item
			AEAspectRegister.this.registerItem( this );
		}

		/**
		 * Friendly name.
		 */
		@Override
		public String toString()
		{
			return this.displayName;
		}
	}

	/**
	 * Instance of the registry.
	 */
	public static final AEAspectRegister instance = new AEAspectRegister();

	/**
	 * Common aspect amounts
	 */
	private static int CRYSTAL_BASE = 2;
	private static int CRYSTAL_PURE = 5;
	private static int FLUIX_CHARGE = 6;

	/**
	 * All discovered items from AE.
	 */
	private List<AEItemInfo> ALL_ITEMS = new ArrayList<AEItemInfo>();

	/**
	 * Items that have not yet been added.
	 */
	private List<AEItemInfo> ITEMS_TO_ADD = new ArrayList<AEItemInfo>();

	/**
	 * Items that have been added.
	 */
	private List<AEItemInfo> ITEMS_REGISTERED = new ArrayList<AEItemInfo>();

	/**
	 * Recipe caches
	 */
	List<IRecipe> NORMAL_RECIPES;
	List<IGrinderEntry> GRINDER_RECIPES;

	/**
	 * Private constructor.
	 */
	private AEAspectRegister()
	{

	}

	/**
	 * Registers the base items from AE. Those without a usable crafting
	 * ancestry, world crafted, or multi-part.
	 */
	private void registerBase()
	{
		Materials aeMats = AEApi.instance().materials();
		Blocks aeBlocks = AEApi.instance().blocks();
		Parts aeParts = AEApi.instance().parts();
		//Items aeItems = AEApi.instance().items();

		AspectList aspects;

		// Certus Quartz Crystal
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_BASE );
		this.registerItem( aeMats.materialCertusQuartzCrystal, aspects );

		// Charged Certus Quartz Crystal
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_BASE );
		aspects.add( Aspect.ENERGY, 4 );
		this.registerItem( aeMats.materialCertusQuartzCrystalCharged, aspects );

		// Pure Certus Quartz Crystal
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_PURE );
		this.registerItem( aeMats.materialPureifiedCertusQuartzCrystal, aspects );

		// Certus Quartz Ore
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_BASE );
		aspects.add( Aspect.EARTH, 1 );
		this.registerItem( aeBlocks.blockQuartzOre, aspects );

		// Charged Certus Quartz Ore
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_BASE );
		aspects.add( Aspect.EARTH, 1 );
		aspects.add( Aspect.ENERGY, 4 );
		this.registerItem( aeBlocks.blockQuartzOreCharged, aspects );

		// Crystal Seed		
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, 1 );
		aspects.add( Aspect.EXCHANGE, 1 );
		this.registerItem( AEApi.instance().items().itemCrystalSeed, aspects );

		// Fluix Crystal
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_BASE );
		aspects.add( Aspect.ENERGY, AEAspectRegister.FLUIX_CHARGE );
		this.registerItem( aeMats.materialFluixCrystal, aspects );

		// Pure Fluix Crystal		
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_PURE );
		aspects.add( Aspect.ENERGY, AEAspectRegister.FLUIX_CHARGE );
		this.registerItem( aeMats.materialPureifiedFluixCrystal, aspects );

		// Enderdust		
		aspects = new AspectList();
		aspects.add( Aspect.ELDRITCH, 2 );
		aspects.add( Aspect.MOTION, 2 );
		aspects.add( Aspect.MAGIC, 1 );
		aspects.add( Aspect.ENTROPY, 1 );
		this.registerItem( aeMats.materialEnderDust, aspects );

		// Pure Nether Quartz
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_PURE );
		aspects.add( Aspect.ENERGY, 1 );
		this.registerItem( aeMats.materialPureifiedNetherQuartzCrystal, aspects );

		// Silicon
		aspects = new AspectList();
		aspects.add( Aspect.FIRE, 1 );
		aspects.add( Aspect.SENSES, 1 );
		aspects.add( Aspect.ORDER, 1 );
		this.registerItem( aeMats.materialSilicon, aspects );

		// Skystone
		aspects = new AspectList();
		aspects.add( Aspect.EARTH, 1 );
		aspects.add( Aspect.DARKNESS, 1 );
		this.registerItem( aeBlocks.blockSkyStone, aspects );

		// Matter ball
		aspects = new AspectList();
		aspects.add( Aspect.SLIME, 4 );
		aspects.add( Aspect.EARTH, 4 );
		aspects.add( Aspect.FLIGHT, 4 );
		this.registerItem( aeMats.materialMatterBall, aspects );

		// Cables -----------------------------------------

		// Setup base aspect list
		AspectList cableAspects = new AspectList();
		cableAspects.add( Aspect.CRYSTAL, 1 );
		cableAspects.add( Aspect.ENERGY, 2 );

		// Pseudo-register it
		this.registerItem( aeBlocks.blockMultiPart, cableAspects );

		// Glass
		aspects = cableAspects.copy();
		this.registerCableSet( AEApi.instance().parts().partCableGlass, aspects );

		// Covered
		aspects = cableAspects.copy();
		aspects.add( Aspect.CLOTH, 1 );
		this.registerCableSet( AEApi.instance().parts().partCableCovered, aspects );

		// Smart
		aspects = cableAspects.copy();
		aspects.add( Aspect.CLOTH, 1 );
		aspects.add( Aspect.LIGHT, 1 );
		aspects.add( Aspect.ENERGY, 1 );
		this.registerCableSet( AEApi.instance().parts().partCableSmart, aspects );

		// Dense
		aspects = cableAspects.copy();
		aspects.add( Aspect.CLOTH, 1 );
		aspects.add( Aspect.LIGHT, 2 );
		aspects.add( Aspect.ENERGY, 3 );
		this.registerCableSet( AEApi.instance().parts().partCableDense, aspects );

		// Anchor
		aspects = new AspectList();
		aspects.add( Aspect.METAL, 1 );
		this.registerItem( AEApi.instance().parts().partCableAnchor, aspects );

		//P2P tunnels ------------------------------------
		// Setup base list
		AspectList p2pAspects = new AspectList();
		p2pAspects.add( Aspect.METAL, 6 );
		p2pAspects.add( Aspect.ENERGY, 6 );
		p2pAspects.add( Aspect.CRYSTAL, 4 );
		p2pAspects.add( Aspect.GREED, 4 );
		p2pAspects.add( Aspect.ORDER, 4 );
		p2pAspects.add( Aspect.SENSES, 4 );

		// ME & Items
		this.registerItem( aeParts.partP2PTunnelME, p2pAspects );
		this.registerItem( aeParts.partP2PTunnelItems, p2pAspects );
		// Fluid
		aspects = p2pAspects.copy();
		aspects.add( Aspect.WATER, 4 );
		this.registerItem( aeParts.partP2PTunnelLiquids, aspects );
		// Light
		aspects = p2pAspects.copy();
		aspects.add( Aspect.LIGHT, 4 );
		this.registerItem( aeParts.partP2PTunnelLight, aspects );
		// Redstone
		aspects = p2pAspects.copy();
		aspects.add( Aspect.ENERGY, 2 );
		this.registerItem( aeParts.partP2PTunnelRedstone, aspects );

	}

	/**
	 * Registers all cable colors of this set with the specified aspects.
	 * 
	 * @param cableSet
	 * @param aspects
	 */
	private void registerCableSet( final AEColoredItemDefinition cableSet, final AspectList aspects )
	{
		// Get the colors
		AEColor[] colors = AEColor.values();

		// Register each color
		for( AEColor color : colors )
		{
			ThaumcraftApi.registerObjectTag( cableSet.stack( color, 1 ), aspects );
		}
	}

	/**
	 * Registers the item def with thaumcraft.
	 * 
	 * @param itemDef
	 * @param aspects
	 */
	private void registerItem( final AEItemDefinition itemDef, final AspectList aspects )
	{
		// Get the info for the def
		AEItemInfo itemInfo = this.getInfoForDefinitionOrStack( itemDef );

		if( itemInfo == null )
		{
			// Should not happen
			return;
		}

		// Set the aspects
		itemInfo.bonusAspects = aspects;

		// Register it
		this.registerItem( itemInfo );

	}

	/**
	 * Updates the aspects for the thaumic energistics items.
	 */
	private void registerTEItems()
	{
		// Get the aspect list for a 1k cell
		AspectList cellAspects = ThaumcraftApiHelper.getObjectAspects( AEApi.instance().items().itemCell1k.stack( 1 ) ).copy();

		cellAspects.add( Aspect.MAGIC, 3 );
		cellAspects.add( Aspect.AURA, 5 );

		Items teItems = TEApi.instance().items();
		thaumicenergistics.api.Blocks teBlocks = TEApi.instance().blocks();
		ThaumcraftApi.registerObjectTag( teItems.EssentiaCell_1k.getStack(), cellAspects );
		ThaumcraftApi.registerObjectTag( teItems.EssentiaCell_4k.getStack(), cellAspects );
		ThaumcraftApi.registerObjectTag( teItems.EssentiaCell_16k.getStack(), cellAspects );
		ThaumcraftApi.registerObjectTag( teItems.EssentiaCell_64k.getStack(), cellAspects );

		// Set the aspects for the iron gearbox
		AspectList ironGearboxAspects = new AspectList();
		ironGearboxAspects.add( Aspect.METAL, 7 );
		ironGearboxAspects.add( Aspect.MECHANISM, 5 );
		ironGearboxAspects.add( Aspect.EARTH, 2 );
		ironGearboxAspects.add( Aspect.ENTROPY, 2 );
		ThaumcraftApi.registerObjectTag( teBlocks.IronGearBox.getStack(), ironGearboxAspects );

		// Set the aspects for the thaumium gearbox
		AspectList thaumGearbox = ironGearboxAspects.copy();
		thaumGearbox.add( Aspect.MAGIC, 10 );
		thaumGearbox.add( Aspect.MECHANISM, 5 );
		thaumGearbox.add( Aspect.METAL, 3 );
		ThaumcraftApi.registerObjectTag( teBlocks.ThaumiumGearBox.getStack(), thaumGearbox );

	}

	/**
	 * Gives AppliedEnergistics blocks and items Thaumcraft aspects.
	 */
	public void registerAEAspects()
	{
		// Log
		FMLLog.info( "%s: Registering AppliedEnergistics scanables with Thaumcraft.", ThaumicEnergistics.MOD_ID );

		// Get the current recipes
		this.NORMAL_RECIPES = CraftingManager.getInstance().getRecipeList();
		this.GRINDER_RECIPES = AEApi.instance().registries().grinder().getRecipes();

		// Build the list of items to give aspects to
		this.getItemsFromAERegistryClass( AEApi.instance().materials() );
		this.getItemsFromAERegistryClass( AEApi.instance().items() );
		this.getItemsFromAERegistryClass( AEApi.instance().blocks() );
		this.getItemsFromAERegistryClass( AEApi.instance().parts() );

		// Give base AE items & materials aspects
		this.registerBase();

		// Register the remaining items
		while( this.ITEMS_TO_ADD.size() > 0 )
		{
			// Get the next item
			AEItemInfo itemInfo = this.ITEMS_TO_ADD.get( 0 );

			// Remove
			this.ITEMS_TO_ADD.remove( 0 );

			// Register it
			itemInfo.registerItem();

		}

		// Finally register my cells
		this.registerTEItems();

		// Cleanup
		this.NORMAL_RECIPES = null;
		this.GRINDER_RECIPES = null;
		this.ALL_ITEMS = null;
		this.ITEMS_REGISTERED = null;
		this.ITEMS_TO_ADD = null;

		// Log
		FMLLog.info( "%s: AE scanables registered.", ThaumicEnergistics.MOD_ID );
	}

	/**
	 * Gets the item info for the item def or itemstack.
	 * 
	 * @param itemDef
	 * @return
	 */
	AEItemInfo getInfoForDefinitionOrStack( final Object itemDefinitionOrStack )
	{
		AEItemInfo itemInfo = null;

		// Get the index from the list of items to add
		int index = -1;
		for( int i = 0; i < this.ALL_ITEMS.size(); i++ )
		{
			// Match?
			if( this.ALL_ITEMS.get( i ).equals( itemDefinitionOrStack ) )
			{
				index = i;
				break;
			}
		}

		// Found match?
		if( index >= 0 )
		{
			itemInfo = this.ALL_ITEMS.get( index );
		}

		return itemInfo;
	}

	/**
	 * Gets the declared items from an AE registry.
	 * 
	 * @param AEDefinitionInstance
	 */
	void getItemsFromAERegistryClass( final Object AEDefinitionInstance )
	{
		AEItemInfo itemInfo;

		// Get the item fields
		Field[] fields = AEDefinitionInstance.getClass().getDeclaredFields();

		// Loop over all fields
		for( Field f : fields )
		{
			try
			{
				// Get the field
				Object fObj = f.get( AEDefinitionInstance );

				// Ensure it is an item def
				if( fObj instanceof AEItemDefinition )
				{
					// Create the info
					itemInfo = new AEItemInfo( (AEItemDefinition)fObj );

					// Has the stack already been registered?
					if( this.ITEMS_REGISTERED.contains( itemInfo ) )
					{
						// Skip it
						continue;
					}

					// Add to the list
					this.ITEMS_TO_ADD.add( itemInfo );
					this.ALL_ITEMS.add( itemInfo );

				}
			}
			catch( Exception e )
			{
				continue;
			}
		}
	}

	/**
	 * Registers the item info with thaumcraft.
	 * 
	 * @param itemInfo
	 * @param aspects
	 */
	void registerItem( final AEItemInfo itemInfo )
	{
		// Remove from the add list
		this.ITEMS_TO_ADD.remove( itemInfo );

		// Ensure it has not already been registered
		if( !this.ITEMS_REGISTERED.contains( itemInfo ) )
		{
			// Add to the registered list
			this.ITEMS_REGISTERED.add( itemInfo );

			// Register it
			ThaumcraftApi.registerObjectTag( itemInfo.itemStack, itemInfo.getFinalAspects() );
		}
	}
}
