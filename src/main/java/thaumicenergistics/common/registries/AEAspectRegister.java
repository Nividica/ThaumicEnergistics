package thaumicenergistics.common.registries;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumicenergistics.api.IThEItems;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.utils.ThELog;
import appeng.api.AEApi;
import appeng.api.definitions.*;
import appeng.api.features.IGrinderEntry;
import appeng.api.features.IInscriberRecipe;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.core.features.registries.entries.InscriberRecipe;
import appeng.recipes.GroupIngredient;
import appeng.recipes.Ingredient;

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
		private IItemDefinition definition;

		/**
		 * Aspects manually added to the item.
		 */
		public AspectList bonusAspects = new AspectList();

		/**
		 * Itemstack holding the item.
		 */
		public ItemStack itemStack = null;

		/**
		 * The current trial pass.
		 */
		private int pass;

		/**
		 * Creates the info.
		 * 
		 * @param itemDef
		 */
		public AEItemInfo( final IItemDefinition itemDef )
		{
			// Set the definition
			this.definition = itemDef;

			// Is the def available?
			if( itemDef.maybeStack( 1 ).isPresent() )
			{
				this.itemStack = itemDef.maybeStack( 1 ).get();
			}

			if( this.itemStack != null )
			{
				this.displayName = this.itemStack.getDisplayName();
			}
		}

		/**
		 * Adds bonus aspects for certain types.
		 */
		private void addPredefinedBonuses()
		{
			IBlocks aeBlocks = AEApi.instance().definitions().blocks();
			IMaterials aeMats = AEApi.instance().definitions().materials();
			//IItems aeItems = AEApi.instance().definitions().items();

			// Grinder recipe?
			if( this.recipe instanceof IGrinderEntry )
			{
				// Add a bonus aspect for grinding
				this.bonusAspects.add( Aspect.ENTROPY, 1 );

				// Remove any Terra
				this.ingredientAspects.remove( Aspect.EARTH );
			}

			// Molecular Assembler
			else if( this.definition.equals( aeBlocks.molecularAssembler() ) )
			{
				// Add extra craft, machina, potentia and vitrius
				this.bonusAspects.add( Aspect.CRAFT, 4 );
				this.bonusAspects.add( Aspect.ENERGY, 6 );
				this.bonusAspects.add( Aspect.MECHANISM, 2 );
				this.bonusAspects.add( Aspect.CRYSTAL, 1 );
			}

			// Sky stone
			else if( this.definition.equals( aeBlocks.skyStone() ) )
			{
				// Add tene
				this.bonusAspects.add( Aspect.DARKNESS, 3 );
			}

			// Sky chest
			else if( this.definition.equals( aeBlocks.skyChest() ) )
			{
				// Add vac
				this.bonusAspects.add( Aspect.VOID, 4 );

				// Remove terra
				this.ingredientAspects.remove( Aspect.EARTH );
			}

			// Quartz glass
			else if( this.definition.equals( aeBlocks.quartzGlass() ) )
			{
				// Add vit
				this.bonusAspects.add( Aspect.CRYSTAL, 4 );
			}

			// ME Controller
			else if( this.definition.equals( aeBlocks.controller() ) )
			{
				// Add mach, sense, mind
				this.bonusAspects.add( Aspect.MECHANISM, 3 );
				this.bonusAspects.add( Aspect.SENSES, 1 );
				this.bonusAspects.add( Aspect.MIND, 9 );

				// Remove fire, order
				this.ingredientAspects.remove( Aspect.FIRE );
				this.ingredientAspects.remove( Aspect.ORDER );
			}

			// ME Drive
			else if( this.definition.equals( aeBlocks.drive() ) )
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
			else if( this.definition.equals( aeBlocks.chest() ) )
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
			else if( this.definition.equals( aeBlocks.iOPort() ) )
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
			else if( this.definition.equals( aeBlocks.condenser() ) )
			{
				// Add vac
				this.bonusAspects.add( Aspect.VOID, 10 );
			}
			// Redstone card
			else if( this.definition.equals( aeMats.cardRedstone() ) )
			{
				// Add sense
				this.bonusAspects.add( Aspect.SENSES, 2 );
			}
			// Cell workbench
			else if( this.definition.equals( aeBlocks.cellWorkbench() ) )
			{
				// Add cloth
				this.bonusAspects.add( Aspect.CLOTH, 2 );
			}
			// ME Interface
			else if( this.definition.equals( aeBlocks.iface() ) )
			{
				// Add permutatio
				this.bonusAspects.add( Aspect.EXCHANGE, 6 );
			}
			// Logic processor
			else if( this.definition.equals( aeMats.logicProcessor() ) )
			{
				// Remove metal & fire & ordo
				this.ingredientAspects.remove( Aspect.METAL );
				this.ingredientAspects.remove( Aspect.FIRE );
				this.ingredientAspects.remove( Aspect.ORDER );

				// Add mind & metal
				this.bonusAspects.add( Aspect.MIND, 3 );
				this.bonusAspects.add( Aspect.METAL, 1 );
			}
			// Calc processor
			else if( this.definition.equals( aeMats.calcProcessor() ) )
			{
				// Remove metal & fire & ordo
				this.ingredientAspects.remove( Aspect.METAL );
				this.ingredientAspects.remove( Aspect.FIRE );
				this.ingredientAspects.remove( Aspect.ORDER );

				// Add mind & metal
				this.bonusAspects.add( Aspect.MIND, 6 );
				this.bonusAspects.add( Aspect.METAL, 1 );
			}
			// Eng processor
			else if( this.definition.equals( aeMats.engProcessor() ) )
			{
				// Remove metal & fire & ordo
				this.ingredientAspects.remove( Aspect.METAL );
				this.ingredientAspects.remove( Aspect.FIRE );
				this.ingredientAspects.remove( Aspect.ORDER );

				// Add mind
				this.bonusAspects.add( Aspect.MIND, 9 );
			}

		}

		/**
		 * Attempts to build the aspects for the item via recipe => ingredients
		 */
		private void buildAspects()
		{
			// Grinder recipes
			if( AEAspectRegister.this.GRINDER_RECIPES != null )
			{
				if( this.buildAspectsFromGrinder() )
				{
					return;
				}
			}

			// Inscriber recipes
			if( AEAspectRegister.this.INSCRIBER_RECIPES != null )
			{
				if( this.buildAspectsFromInscriber() )
				{
					return;
				}
			}

			// Regular crafting recipes
			if( AEAspectRegister.this.NORMAL_RECIPES != null )
			{
				if( this.buildAspectsFromNormal() )
				{
					return;
				}
			}
		}

		/**
		 * Attempts to build the aspects for the item via grinder recipes.
		 * 
		 * @return
		 */
		private boolean buildAspectsFromGrinder()
		{
			try
			{
				for( IGrinderEntry recipe : AEAspectRegister.this.GRINDER_RECIPES )
				{
					ItemStack recipeOutput = recipe.getOutput();

					// Skip null items
					if( ( recipeOutput == null ) || ( recipeOutput.getItem() == null ) )
					{
						continue;
					}

					if( this.areStacksEqualIgnoreAmount( recipeOutput, this.itemStack ) )
					{
						if( this.isRecipeUsable( recipe ) )
						{
							return true;
						}
					}
				}
			}
			catch( Exception e )
			{
			}

			return false;
		}

		/**
		 * Attempts to build the aspects for the item via inscriber recipes.
		 * 
		 * @return
		 */
		private boolean buildAspectsFromInscriber()
		{
			try
			{
				for( IInscriberRecipe recipe : AEAspectRegister.this.INSCRIBER_RECIPES )
				{
					ItemStack recipeOutput = recipe.getOutput();

					// Skip null items
					if( ( recipeOutput == null ) || ( recipeOutput.getItem() == null ) )
					{
						continue;
					}

					if( this.areStacksEqualIgnoreAmount( recipeOutput, this.itemStack ) )
					{
						if( this.isRecipeUsable( recipe ) )
						{
							return true;
						}
					}
				}
			}
			catch( Exception e )
			{
			}
			return false;
		}

		/**
		 * Attempts to build the aspects for the item via normal recipes.
		 * 
		 * @return
		 */
		private boolean buildAspectsFromNormal()
		{

			try
			{
				for( IRecipe recipe : AEAspectRegister.this.NORMAL_RECIPES )
				{
					// Get the recipe's result
					ItemStack recipeOutput = recipe.getRecipeOutput();

					// Skip null items
					if( ( recipeOutput == null ) || ( recipeOutput.getItem() == null ) )
					{
						continue;
					}

					// Is this what we are looking for?
					if( !this.areStacksEqualIgnoreAmount( recipeOutput, this.itemStack ) )
					{
						continue;
					}

					if( this.isRecipeUsable( recipe ) )
					{
						return true;
					}
				}

			}
			catch( Exception e )
			{
			}

			return false;
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
						// Ensure the item is not in the dependency chain.
						if( !AEAspectRegister.this.DEPENDENCY_CHAIN.contains( ingInfo ) )
						{
							// Register the item
							ingInfo.registerItem( this.pass );

							// Attempt to get the aspects again
							stackAspects = ThaumcraftApiHelper.getObjectAspects( stack );
						}
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

				// Get inputs
				List<ItemStack> inputs = new ArrayList<ItemStack>( iRec.getInputs() );

				// Is there a top?
				if( iRec.getTopOptional().isPresent() )
				{
					// Add top
					inputs.add( iRec.getTopOptional().get() );
				}

				// Is there a bottom?
				if( iRec.getBottomOptional().isPresent() )
				{
					// Add bottom
					inputs.add( iRec.getBottomOptional().get() );
				}

				// Create array
				ingredientObjects = inputs.toArray( new Object[inputs.size()] );

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
							if( ( (Ingredient)ing ).getNameSpace().equalsIgnoreCase( "oreDictionary" ) )
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

					}
					catch( Exception e )
					{
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
		 * Determines if the specified stacks are equal.
		 * 
		 * @param stack1
		 * @param stack2
		 * @return
		 */
		public boolean areStacksEqualIgnoreAmount( final ItemStack stack1, final ItemStack stack2 )
		{
			// Nulls never match
			if( ( stack1 == null ) || ( stack2 == null ) || ( stack1.getItem() == null ) || ( stack2.getItem() == null ) )
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
			else if( obj instanceof IItemDefinition )
			{
				return this.isMatch( ( (IItemDefinition)obj ).maybeStack( 1 ).orNull() );
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
					if( aspect == null )
					{
						continue;
					}

					finalAspects.add( aspect, this.ingredientAspects.getAmount( aspect ) );
				}
			}

			// Add bonus aspects
			if( this.bonusAspects != null )
			{
				for( Aspect aspect : this.bonusAspects.getAspects() )
				{
					if( aspect == null )
					{
						continue;
					}

					finalAspects.add( aspect, this.bonusAspects.getAmount( aspect ) );
				}
			}

			// Get the final aspect count
			int aspectCount = finalAspects.size();

			if( aspectCount == 0 )
			{
				return finalAspects;
			}

			// Can only have 6 aspects
			if( aspectCount > 6 )
			{
				Aspect[] aspects = finalAspects.getAspectsSortedAmount();

				// Remove the lowest aspects
				for( int index = 6; index < aspects.length; index++ )
				{
					finalAspects.remove( aspects[index] );
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
		public void registerItem( final int pass )
		{
			// Set the pass
			this.pass = pass;

			if( this.itemStack != null )
			{
				// Add to the dependency chain
				AEAspectRegister.this.DEPENDENCY_CHAIN.add( this );

				// Get the ingredients
				this.buildAspects();

				// Calculate the preset bonuses
				this.addPredefinedBonuses();

				// Remove from the dependency chain
				AEAspectRegister.this.DEPENDENCY_CHAIN.remove( this );
			}

			// Are there aspects?
			if( ( this.ingredientAspects.size() == 0 ) && ( this.bonusAspects.size() == 0 ) )
			{
				// Was this pass one?
				if( pass == 1 )
				{
					// Add to the unregisterable list for trial again later.
					AEAspectRegister.this.UNREGISTERABLE.add( this );
				}
				else
				{
					// Still could not register on pass 2.
					if( !this.displayName.isEmpty() )
					{
						ThELog.info( "Could not register \"%s\" for TC scanning.", this.displayName );
					}
				}
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
	public static final AEAspectRegister INSTANCE = new AEAspectRegister();

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
	 * Used to prevent stack overflows when two items depend on each other.
	 */
	List<AEItemInfo> DEPENDENCY_CHAIN = new ArrayList<AEItemInfo>();

	/**
	 * Tracks items that could not be registered.
	 */
	List<AEItemInfo> UNREGISTERABLE = new ArrayList<AEItemInfo>();

	/**
	 * Recipe caches
	 */
	List<IRecipe> NORMAL_RECIPES = null;
	List<IGrinderEntry> GRINDER_RECIPES = null;
	Collection<IInscriberRecipe> INSCRIBER_RECIPES = null;

	/**
	 * Private constructor.
	 */
	private AEAspectRegister()
	{

	}

	private boolean getGrinderRecipes()
	{
		// Get the recipes
		this.GRINDER_RECIPES = AEApi.instance().registries().grinder().getRecipes();

		return( ( this.GRINDER_RECIPES != null ) && !this.GRINDER_RECIPES.isEmpty() );
	}

	private boolean getInscriberRecipes()
	{
		// Get the recipes
		this.INSCRIBER_RECIPES = AEApi.instance().registries().inscriber().getRecipes();

		return( ( this.INSCRIBER_RECIPES != null ) && !this.INSCRIBER_RECIPES.isEmpty() );
	}

	/**
	 * Registers the base items from AE. Those without a usable crafting
	 * ancestry, world crafted, or multi-part.
	 */
	private void registerBase()
	{
		IMaterials aeMats = AEApi.instance().definitions().materials();
		IBlocks aeBlocks = AEApi.instance().definitions().blocks();
		IParts aeParts = AEApi.instance().definitions().parts();
		IItems aeItems = AEApi.instance().definitions().items();

		AspectList aspects;

		// Certus Quartz Crystal
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_BASE );
		this.registerItem( aeMats.certusQuartzCrystal(), aspects );

		// Charged Certus Quartz Crystal
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_BASE );
		aspects.add( Aspect.ENERGY, 4 );
		this.registerItem( aeMats.certusQuartzCrystalCharged(), aspects );

		// Pure Certus Quartz Crystal
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_PURE );
		this.registerItem( aeMats.purifiedCertusQuartzCrystal(), aspects );

		// Certus Quartz Ore
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_BASE );
		aspects.add( Aspect.EARTH, 1 );
		this.registerItem( aeBlocks.quartzOre(), aspects );

		// Charged Certus Quartz Ore
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_BASE );
		aspects.add( Aspect.EARTH, 1 );
		aspects.add( Aspect.ENERGY, 4 );
		this.registerItem( aeBlocks.quartzOreCharged(), aspects );

		// Crystal Seed		
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, 1 );
		aspects.add( Aspect.EXCHANGE, 1 );
		this.registerItem( AEApi.instance().definitions().items().crystalSeed(), aspects );

		// Fluix Crystal
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_BASE );
		aspects.add( Aspect.ENERGY, AEAspectRegister.FLUIX_CHARGE );
		this.registerItem( aeMats.fluixCrystal(), aspects );

		// Pure Fluix Crystal		
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_PURE );
		aspects.add( Aspect.ENERGY, AEAspectRegister.FLUIX_CHARGE );
		this.registerItem( aeMats.purifiedFluixCrystal(), aspects );

		// Enderdust		
		aspects = new AspectList();
		aspects.add( Aspect.ELDRITCH, 2 );
		aspects.add( Aspect.MOTION, 2 );
		aspects.add( Aspect.MAGIC, 1 );
		aspects.add( Aspect.ENTROPY, 1 );
		this.registerItem( aeMats.enderDust(), aspects );

		// Pure Nether Quartz
		aspects = new AspectList();
		aspects.add( Aspect.CRYSTAL, AEAspectRegister.CRYSTAL_PURE );
		aspects.add( Aspect.ENERGY, 1 );
		this.registerItem( aeMats.purifiedNetherQuartzCrystal(), aspects );

		// Silicon
		aspects = new AspectList();
		aspects.add( Aspect.FIRE, 1 );
		aspects.add( Aspect.SENSES, 1 );
		aspects.add( Aspect.ORDER, 1 );
		this.registerItem( aeMats.silicon(), aspects );

		// Skystone
		aspects = new AspectList();
		aspects.add( Aspect.EARTH, 1 );
		aspects.add( Aspect.DARKNESS, 1 );
		this.registerItem( aeBlocks.skyStone(), aspects );

		// Matter ball
		aspects = new AspectList();
		aspects.add( Aspect.SLIME, 4 );
		aspects.add( Aspect.EARTH, 4 );
		aspects.add( Aspect.FLIGHT, 4 );
		this.registerItem( aeMats.matterBall(), aspects );

		// Cables -----------------------------------------

		// Setup base aspect list
		AspectList cableAspects = new AspectList();
		cableAspects.add( Aspect.CRYSTAL, 1 );
		cableAspects.add( Aspect.ENERGY, 2 );

		// Pseudo-register it
		this.registerItem( aeBlocks.multiPart(), cableAspects );

		// Glass
		aspects = cableAspects.copy();
		this.registerCableSet( aeParts.cableGlass(), aspects );

		// Covered
		aspects = cableAspects.copy();
		aspects.add( Aspect.CLOTH, 1 );
		this.registerCableSet( aeParts.cableCovered(), aspects );

		// Smart
		aspects = cableAspects.copy();
		aspects.add( Aspect.CLOTH, 1 );
		aspects.add( Aspect.LIGHT, 1 );
		aspects.add( Aspect.ENERGY, 1 );
		this.registerCableSet( aeParts.cableSmart(), aspects );

		// Dense
		aspects = cableAspects.copy();
		aspects.add( Aspect.CLOTH, 1 );
		aspects.add( Aspect.LIGHT, 2 );
		aspects.add( Aspect.ENERGY, 3 );
		this.registerCableSet( aeParts.cableDense(), aspects );

		// Anchor
		aspects = new AspectList();
		aspects.add( Aspect.METAL, 1 );
		this.registerItem( aeParts.cableAnchor(), aspects );

		// P2P tunnels ------------------------------------
		// Setup base list
		AspectList p2pAspects = new AspectList();
		p2pAspects.add( Aspect.METAL, 6 );
		p2pAspects.add( Aspect.ENERGY, 6 );
		p2pAspects.add( Aspect.CRYSTAL, 4 );
		p2pAspects.add( Aspect.GREED, 4 );
		p2pAspects.add( Aspect.ORDER, 4 );
		p2pAspects.add( Aspect.SENSES, 4 );

		// ME & Items
		this.registerItem( aeParts.p2PTunnelME(), p2pAspects );
		this.registerItem( aeParts.p2PTunnelItems(), p2pAspects );
		// Fluid
		aspects = p2pAspects.copy();
		aspects.add( Aspect.WATER, 4 );
		this.registerItem( aeParts.p2PTunnelLiquids(), aspects );
		// Light
		aspects = p2pAspects.copy();
		aspects.add( Aspect.LIGHT, 4 );
		this.registerItem( aeParts.p2PTunnelLight(), aspects );
		// Redstone & Power
		aspects = p2pAspects.copy();
		aspects.add( Aspect.ENERGY, 2 );
		this.registerItem( aeParts.p2PTunnelRedstone(), aspects );
		this.registerItem( aeParts.p2PTunnelRF(), aspects );
		this.registerItem( aeParts.p2PTunnelEU(), aspects );

		// Singularity ------------------------------------
		aspects = new AspectList();
		aspects.add( Aspect.DARKNESS, 10 );
		aspects.add( Aspect.VOID, 10 );
		aspects.add( Aspect.ENTROPY, 10 );
		this.registerItem( aeMats.singularity(), aspects );
		// Entangled
		aspects = new AspectList();
		aspects.add( Aspect.ENERGY, 8 );
		aspects.add( Aspect.EXCHANGE, 8 );
		aspects.add( Aspect.MOTION, 4 );
		aspects.add( Aspect.ELDRITCH, 4 );
		this.registerItem( aeMats.qESingularity(), aspects );

		// Encoded Pattern --------------------------------
		aspects = new AspectList();
		aspects.add( Aspect.CRAFT, 3 );
		aspects.add( Aspect.MIND, 3 );
		this.registerItem( aeItems.encodedPattern(), aspects );

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
	private void registerItem( final IItemDefinition itemDef, final AspectList aspects )
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
	private void registerThEItems()
	{
		IThEItems teItems = ThEApi.instance().items();
		thaumicenergistics.api.IThEBlocks teBlocks = ThEApi.instance().blocks();

		// Get an AE 1K Cell
		ItemStack aeCell = AEApi.instance().definitions().items().cell1k().maybeStack( 1 ).orNull();
		AspectList cellAspects = null;
		if( aeCell != null )
		{
			// Get the aspect list for a 1k cell
			AspectList aeCellAspects = ThaumcraftApiHelper.getObjectAspects( aeCell );
			if( aeCellAspects != null )
			{
				// Copy the aspects
				cellAspects = aeCellAspects.copy();
				int aspectCount = cellAspects.size();

				// Is there too many aspects?
				if( aspectCount > 4 )
				{
					Aspect[] OrderedAspects = cellAspects.getAspectsSortedAmount();

					// Remove lowest
					cellAspects.remove( OrderedAspects[aspectCount - 1] );

					// Still not enough room?
					if( aspectCount > 5 )
					{

						// Remove lowest
						cellAspects.remove( OrderedAspects[aspectCount - 2] );
					}
				}

				// Add magic and aura
				cellAspects.add( Aspect.MAGIC, 3 );
				cellAspects.add( Aspect.AURA, 5 );
				ThaumcraftApi.registerObjectTag( teItems.EssentiaCell_1k.getStack(), cellAspects );
				ThaumcraftApi.registerObjectTag( teItems.EssentiaCell_4k.getStack(), cellAspects );
				ThaumcraftApi.registerObjectTag( teItems.EssentiaCell_16k.getStack(), cellAspects );
				ThaumcraftApi.registerObjectTag( teItems.EssentiaCell_64k.getStack(), cellAspects );
			}
		}

		// Were the cells registered?
		if( ( aeCell == null ) || ( cellAspects == null ) )
		{
			ThELog.info( "Could not register \"%s\" for TC scanning.", "Essentia Cells" );
		}

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
		long sectionStartTime = ThELog.beginSection( "AE Scanables" );

		// Get the normal recipes
		this.NORMAL_RECIPES = CraftingManager.getInstance().getRecipeList();

		// Get the grinder recipes
		if( !this.getGrinderRecipes() )
		{
			ThELog.warning( "Unable to load AE2 Grinder recipes, aspect registration will be incomplete" );
		}

		// Get the inscriber recipes
		if( !this.getInscriberRecipes() )
		{
			ThELog.warning( "Unable to load AE2 Inscriber recipes, aspect registration will be incomplete" );
		}

		// Build the list of items to give aspects to
		this.getItemsFromAERegistryClass( AEApi.instance().definitions().materials() );
		this.getItemsFromAERegistryClass( AEApi.instance().definitions().items() );
		this.getItemsFromAERegistryClass( AEApi.instance().definitions().blocks() );
		this.getItemsFromAERegistryClass( AEApi.instance().definitions().parts() );

		// Give base AE items & materials aspects
		this.registerBase();

		// Register the remaining items
		for( int pass = 1; pass <= 2; pass++ )
		{
			while( this.ITEMS_TO_ADD.size() > 0 )
			{
				// Get the next item
				AEItemInfo itemInfo = this.ITEMS_TO_ADD.get( 0 );

				// Remove
				this.ITEMS_TO_ADD.remove( 0 );

				// Register it
				itemInfo.registerItem( pass );

			}

			// Upon completion of pass 1, move all unregisterable items back into the items to add, and try again. 
			if( pass == 1 )
			{
				this.ITEMS_TO_ADD.addAll( this.UNREGISTERABLE );
				this.UNREGISTERABLE.clear();
			}
		}

		// Finally register my items
		this.registerThEItems();

		// Cleanup
		this.NORMAL_RECIPES = null;
		this.GRINDER_RECIPES = null;
		this.ALL_ITEMS = null;
		this.ITEMS_REGISTERED = null;
		this.ITEMS_TO_ADD = null;
		this.DEPENDENCY_CHAIN = null;
		this.UNREGISTERABLE = null;

		// Log
		ThELog.endSection( "AE Scanables", sectionStartTime );
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
				// Set accessible
				f.setAccessible( true );

				// Get the field
				Object fObj = f.get( AEDefinitionInstance );

				// Ensure it is an item def
				if( fObj instanceof IItemDefinition )
				{
					// Create the info
					itemInfo = new AEItemInfo( (IItemDefinition)fObj );

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
