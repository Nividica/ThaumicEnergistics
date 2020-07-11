package thaumicenergistics.common.container;

import java.util.ArrayList;
import java.util.Iterator;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotInaccessible;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumicenergistics.common.container.slot.SlotRestrictive;
import thaumicenergistics.common.integration.tc.ArcaneCraftingPattern;
import thaumicenergistics.common.integration.tc.ArcaneRecipeHelper;
import thaumicenergistics.common.inventory.HandlerKnowledgeCore;
import thaumicenergistics.common.inventory.TheInternalInventory;
import thaumicenergistics.common.items.ItemKnowledgeCore;
import thaumicenergistics.common.network.packet.client.Packet_C_KnowledgeInscriber;
import thaumicenergistics.common.tiles.TileKnowledgeInscriber;
import thaumicenergistics.common.utils.EffectiveSide;

import javax.annotation.Nonnull;

/**
 * {@link TileKnowledgeInscriber} container.
 *
 * @author Nividica
 *
 */
public class ContainerKnowledgeInscriber
	extends ContainerWithPlayerInventory
{
	/**
	 * What state the save/delete button should be in.
	 *
	 * @author Nividica
	 *
	 */
	public enum CoreSaveState
	{
			Disabled_InvalidRecipe,
			Disabled_CoreFull,
			Disabled_MissingCore,
			Enabled_Save,
			Enabled_Delete
	}

	/**
	 * Maximum number of patterns.
	 */
	private static final int MAXIMUM_PATTERNS = HandlerKnowledgeCore.MAXIMUM_STORED_PATTERNS;

	/**
	 * Y position for the player and hotbar inventory.
	 */
	private static final int PLAYER_INV_POSITION_Y = 162, HOTBAR_INV_POSITION_Y = ContainerKnowledgeInscriber.PLAYER_INV_POSITION_Y + 58;

	/**
	 * Knowledge core slot.
	 */
	public static final int KCORE_SLOT_X = 186, KCORE_SLOT_Y = 8;

	/**
	 * Pattern slots.
	 */
	private static final int PATTERN_SLOT = 0, PATTERN_SLOT_X = 26, PATTERN_SLOT_Y = 18,
					PATTERN_ROWS = 3, PATTERN_COLS = 7, PATTERN_SLOT_SPACING = 18;

	/**
	 * Crafting slots
	 */
	public static final int CRAFTING_MATRIX_SLOT = ContainerKnowledgeInscriber.MAXIMUM_PATTERNS + ContainerKnowledgeInscriber.PATTERN_SLOT,
					CRAFTING_SLOT_X = 26,
					CRAFTING_SLOT_Y = 90, CRAFTING_ROWS = 3, CRAFTING_COLS = 3,
					CRAFTING_GRID_SIZE = ContainerKnowledgeInscriber.CRAFTING_ROWS * ContainerKnowledgeInscriber.CRAFTING_COLS,
					CRAFTING_SLOT_SPACING = 18,
					CRAFTING_RESULT_SLOT = ContainerKnowledgeInscriber.CRAFTING_MATRIX_SLOT + ContainerKnowledgeInscriber.CRAFTING_GRID_SIZE;

	/**
	 * Handles interaction with the knowledge core.
	 */
	private final HandlerKnowledgeCore kCoreHandler;

	/**
	 * Slots
	 */
	private final SlotRestrictive kCoreSlot;
	private final SlotFake resultSlot;
	private final SlotInaccessible[] patternSlots = new SlotInaccessible[ContainerKnowledgeInscriber.MAXIMUM_PATTERNS];
	private final SlotFakeCraftingMatrix[] craftingSlots = new SlotFakeCraftingMatrix[ContainerKnowledgeInscriber.CRAFTING_GRID_SIZE];

	/**
	 * The current recipe, if any.
	 */
	private IArcaneRecipe activeRecipe = null;

	/**
	 * Inscriber tile entity.
	 */
	private TileKnowledgeInscriber inscriber;

	/**
	 * Inventory for the patterns and crafting matrix
	 */
	private final TheInternalInventory internalInventory;

	public ContainerKnowledgeInscriber( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Call super
		super( player );

		// Bind to the players inventory
		this.bindPlayerInventory( player.inventory, ContainerKnowledgeInscriber.PLAYER_INV_POSITION_Y,
			ContainerKnowledgeInscriber.HOTBAR_INV_POSITION_Y );

		// Get the inscriber
		this.inscriber = (TileKnowledgeInscriber)world.getTileEntity( x, y, z );

		// Create the Kcore slot
		this.kCoreSlot = new SlotRestrictive( this.inscriber, TileKnowledgeInscriber.KCORE_SLOT, ContainerKnowledgeInscriber.KCORE_SLOT_X,
						ContainerKnowledgeInscriber.KCORE_SLOT_Y );
		this.addSlotToContainer( this.kCoreSlot );

		// Setup the internal inventory
		this.internalInventory = new TheInternalInventory( "cki", ContainerKnowledgeInscriber.CRAFTING_RESULT_SLOT + 1, 64 );

		// Create pattern slots
		this.initPatternSlots();

		// Create crafting slots
		this.initCraftingSlots();

		// Create the result slot
		this.resultSlot = new SlotFake( this.internalInventory, ContainerKnowledgeInscriber.CRAFTING_RESULT_SLOT, 116, 108 );
		this.addSlotToContainer( this.resultSlot );

		// Create the handler
		this.kCoreHandler = new HandlerKnowledgeCore();
	}

	/**
	 * Determines the current save state.
	 *
	 * @return
	 */
	private CoreSaveState getSaveState()
	{
		CoreSaveState saveState;

		// Is there a core handler?
		if( !this.kCoreHandler.hasCore() )
		{
			saveState = CoreSaveState.Disabled_MissingCore;
		}
		// Is there a valid recipe?
		else if( this.activeRecipe == null )
		{
			saveState = CoreSaveState.Disabled_InvalidRecipe;
		}
		else
		{
			// Get the recipe output
			ItemStack recipeOutput = ArcaneRecipeHelper.INSTANCE.getRecipeOutput( this.internalInventory,
				ContainerKnowledgeInscriber.CRAFTING_MATRIX_SLOT, ContainerKnowledgeInscriber.CRAFTING_GRID_SIZE, this.activeRecipe );

			// Ensure there is an output
			if( recipeOutput == null )
			{
				saveState = CoreSaveState.Disabled_InvalidRecipe;
			}
			else
			{
				// Does the core already have this recipe, or one that produces the same result, stored?
				boolean isNew = !this.kCoreHandler.hasPatternFor( recipeOutput );

				// Would the recipe be a new pattern?
				if( isNew )
				{
					// Is there room for the recipe?
					if( this.kCoreHandler.hasRoomToStorePattern() )
					{
						// Enable saving
						saveState = CoreSaveState.Enabled_Save;
					}
					else
					{
						// Core is full
						saveState = CoreSaveState.Disabled_CoreFull;
					}
				}
				else
				{
					// Enable deleting
					saveState = CoreSaveState.Enabled_Delete;
				}
			}
		}

		return saveState;
	}

	private void initCraftingSlots()
	{
		int slotIndex;
		// Create the crafting slots
		slotIndex = ContainerKnowledgeInscriber.CRAFTING_MATRIX_SLOT;
		for( int row = 0; row < ContainerKnowledgeInscriber.CRAFTING_ROWS; row++ )
		{
			for( int column = 0; column < ContainerKnowledgeInscriber.CRAFTING_COLS; column++ )
			{
				// Calculate the array index
				int index = ( row * ContainerKnowledgeInscriber.CRAFTING_COLS ) + column;

				// Calculate the position
				int posX = ContainerKnowledgeInscriber.CRAFTING_SLOT_X + ( ContainerKnowledgeInscriber.CRAFTING_SLOT_SPACING * column );
				int posY = ContainerKnowledgeInscriber.CRAFTING_SLOT_Y + ( ContainerKnowledgeInscriber.CRAFTING_SLOT_SPACING * row );

				// Add the slot
				this.addSlotToContainer( this.craftingSlots[index] = new SlotFakeCraftingMatrix( this.internalInventory, slotIndex++ , posX, posY ) );

			}
		}
	}

	private void initPatternSlots()
	{
		int slotIndex;
		// Create the pattern slots
		slotIndex = ContainerKnowledgeInscriber.PATTERN_SLOT;
		for( int row = 0; row < ContainerKnowledgeInscriber.PATTERN_ROWS; row++ )
		{
			for( int column = 0; column < ContainerKnowledgeInscriber.PATTERN_COLS; column++ )
			{
				// Calculate the array index
				int index = ( row * ContainerKnowledgeInscriber.PATTERN_COLS ) + column;

				// Calculate the position
				int posX = ContainerKnowledgeInscriber.PATTERN_SLOT_X + ( ContainerKnowledgeInscriber.PATTERN_SLOT_SPACING * column );
				int posY = ContainerKnowledgeInscriber.PATTERN_SLOT_Y + ( ContainerKnowledgeInscriber.PATTERN_SLOT_SPACING * row );

				// Add to the array
				this.patternSlots[index] = new SlotInaccessible( this.internalInventory, slotIndex++ , posX, posY );

				// Add the slot
				this.addSlotToContainer( this.patternSlots[index] );
			}
		}
	}

	private void loadPattern( final ArcaneCraftingPattern pattern )
	{
		if( ( pattern == null ) || ( !pattern.isPatternValid() ) )
		{
			return;
		}
		// Set the slots
		for( int index = 0; index < this.craftingSlots.length; index++ )
		{
			IAEItemStack ingStack = pattern.getInputs()[index];
			if( ingStack != null )
			{
				this.craftingSlots[index].putStack( ingStack.getItemStack() );
			}
			else
			{
				this.craftingSlots[index].putStack( null );
			}
		}
	}

	/**
	 * Prepares input to a pattern from the current recipe
	 *
	 * @param input
	 * @param slotNumber
	 * @return
	 */
	private Object preparePatternInput( final Object input, final int slotNumber )
	{
		if( input instanceof ArrayList )
		{
			// Get the prefered item
			ItemStack preferedItem = this.craftingSlots[slotNumber].getStack();

			// Create the list
			ArrayList<ItemStack> ingList = new ArrayList<ItemStack>();

			// Add the prefered item first
			if( preferedItem != null )
			{
				ingList.add( preferedItem );
			}

			// Add the rest
			ArrayList<ItemStack> inputList = (ArrayList<ItemStack>)input;
			for( ItemStack item : inputList )
			{
				if( ( item == null ) || ( ItemStack.areItemStacksEqual( preferedItem, item ) ) )
				{
					continue;
				}
				ingList.add( item );
			}

			return ingList;
		}
		// Is this a wildcard item?
		else if( ( input instanceof ItemStack ) && ( ( (ItemStack)input ).getItemDamage() == OreDictionary.WILDCARD_VALUE ) )
		{
			// Create a list to hold the users preferred item, and the wildcard item
			ArrayList<ItemStack> ingList = new ArrayList<ItemStack>();
			ingList.add( this.craftingSlots[slotNumber].getStack() );
			ingList.add( (ItemStack)input );
			return ingList;
		}

		return input;
	}

	private boolean prepareShapless( final Object[] inputs )
	{
		// Get the recipe
		ShapelessArcaneRecipe recipe = (ShapelessArcaneRecipe)this.activeRecipe;

		ArrayList ings = recipe.getInput();

		// Check each crafting grid slot
		for( int slotIndex = 0; slotIndex < ContainerKnowledgeInscriber.CRAFTING_GRID_SIZE; ++slotIndex )
		{
			// Get the stack

			ItemStack slotStack = this.craftingSlots[slotIndex].getStack();
			if( slotStack == null )
			{
				continue;
			}

			// Locate it in the ingredients.
			Object matchingIng = null;
			int ingIndex = 0;
			for( ; ingIndex < ings.size(); ++ingIndex )
			{
				Object ing = ings.get( ingIndex );

				if( ArcaneCraftingPattern.canSubstitueFor( ing, slotStack ) )
				{
					matchingIng = ing;
					break;
				}
			}

			// If no match found, do not encode.
			if( matchingIng == null )
			{
				return false;
			}

			// Add to the inputs
			inputs[ingIndex] = this.preparePatternInput( matchingIng, slotIndex );
		}

		// Finished prep
		return true;
	}

	/**
	 * Updates the slots to reflect the stored patterns.
	 */
	private void updatePatternSlots()
	{
		Iterator<ItemStack> iterator = null;

		// Get the list of stored pattern results
		if( this.kCoreHandler.hasCore() )
		{
			ArrayList<ItemStack> storedResults = this.kCoreHandler.getStoredOutputs();
			iterator = storedResults.iterator();
		}

		// Loop over all pattern slots
		for( Slot patternSlot : this.patternSlots )
		{
			// Is there an itemstack to put?
			if( ( iterator != null ) && ( iterator.hasNext() ) )
			{
				// Put the result
				patternSlot.putStack( iterator.next() );
			}
			else
			{
				// Clear the slot
				patternSlot.putStack( null );
			}

			// Update clients with change
			for( int cIndex = 0; cIndex < this.crafters.size(); ++cIndex )
			{
				( (ICrafting)this.crafters.get( cIndex ) ).sendSlotContents( this, patternSlot.slotNumber, patternSlot.getStack() );
			}
		}
	}

	/**
	 * Checks for core insertion or removal.
	 *
	 * @param playerMP
	 * @return
	 */
	@Override
	protected boolean detectAndSendChangesMP(@Nonnull final EntityPlayerMP playerMP )
	{
		// Has a core changed?
		if( !this.kCoreHandler.isHandlingCore( this.kCoreSlot.getStack() ) )
		{
			if( this.kCoreSlot.getHasStack() )
			{
				// Setup the handler
				this.kCoreHandler.open( this.kCoreSlot.getStack() );
			}
			else
			{
				// Close the handler
				this.kCoreHandler.close();
			}

			// Update the slots
			this.updatePatternSlots();

			// Update the save state
			this.sendSaveState( false );

			return true;
		}

		return false;
	}

	/**
	 * Can interact with anyone
	 *
	 * @param player
	 * @return
	 */
	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		if( this.inscriber != null )
		{
			return this.inscriber.isUseableByPlayer( player );
		}
		return false;
	}

	public void onClientRequestClearGrid()
	{
		// Clear the grid
		for( int index = ContainerKnowledgeInscriber.CRAFTING_MATRIX_SLOT; index < ContainerKnowledgeInscriber.CRAFTING_RESULT_SLOT; ++index )
		{
			this.internalInventory.setInventorySlotContents( index, null );
		}

		// Update the matrix
		this.onCraftMatrixChanged( this.internalInventory );
	}

	/**
	 * Attempts to save or delete the active pattern from the kcore.
	 *
	 * @param player
	 */
	public void onClientRequestSaveOrDelete( final EntityPlayer player )
	{
		// Get the current save state
		CoreSaveState saveState = this.getSaveState();

		Object[] inputs = new Object[ContainerKnowledgeInscriber.CRAFTING_GRID_SIZE];

		if( saveState == CoreSaveState.Enabled_Save )
		{
			// Build the ingredient list

			// Is the recipe shaped?
			if( this.activeRecipe instanceof ShapedArcaneRecipe )
			{
				ShapedArcaneRecipe recipe = (ShapedArcaneRecipe)this.activeRecipe;
				for( int slotNumber = 0; slotNumber < recipe.input.length; ++slotNumber )
				{
					inputs[slotNumber] = this.preparePatternInput( recipe.input[slotNumber], slotNumber );
				}
			}
			// Is the recipe shapeless?
			else if( this.activeRecipe instanceof ShapelessArcaneRecipe )
			{
				if( !this.prepareShapless( inputs ) )
				{
					// Could not prep shapeless
					return;
				}
			}
			else
			{
				// Unknown recipe type.
				return;
			}

			// Get the aspect cost
			AspectList recipeAspects = ArcaneRecipeHelper.INSTANCE.getRecipeAspectCost( this.internalInventory,
				ContainerKnowledgeInscriber.CRAFTING_MATRIX_SLOT, ContainerKnowledgeInscriber.CRAFTING_GRID_SIZE, this.activeRecipe );

			// Create the pattern
			ArcaneCraftingPattern pattern = new ArcaneCraftingPattern( this.kCoreSlot.getStack(), recipeAspects, this.resultSlot.getStack(), inputs );

			// Add the pattern
			this.kCoreHandler.addPattern( pattern );

			// Update the slots
			this.updatePatternSlots();
			this.loadPattern( pattern );

			// Update the save state
			this.sendSaveState( true );

			// Mark the inscriber as dirty
			this.inscriber.markDirty();
		}
		else if( saveState == CoreSaveState.Enabled_Delete )
		{
			// Get the pattern for the result item
			ArcaneCraftingPattern pattern = this.kCoreHandler.getPatternForItem( this.resultSlot.getStack() );

			// Ensure there is a pattern for it
			if( pattern != null )
			{
				// Remove it
				this.kCoreHandler.removePattern( pattern );

				// Update the slots
				this.updatePatternSlots();

				// Update the save state
				this.sendSaveState( false );

				// Mark the inscriber as dirty
				this.inscriber.markDirty();
			}
		}
	}

	/**
	 * A client has requested the save state.
	 *
	 * @param player
	 * @param justSaved
	 */
	public void onClientRequestSaveState()
	{
		// Update the client
		this.sendSaveState( false );
	}

	/**
	 * Updates the arcane recipe.
	 *
	 * @param inv
	 */
	@Override
	public void onCraftMatrixChanged( final IInventory inv )
	{
		// Set the active recipe
		this.activeRecipe = ArcaneRecipeHelper.INSTANCE.findMatchingArcaneResult( inv, ContainerKnowledgeInscriber.CRAFTING_MATRIX_SLOT,
			ContainerKnowledgeInscriber.CRAFTING_GRID_SIZE,
			this.player );

		ItemStack craftResult = null;

		// Set the result slot
		if( this.activeRecipe != null )
		{
			craftResult = ArcaneRecipeHelper.INSTANCE.getRecipeOutput( inv, ContainerKnowledgeInscriber.CRAFTING_MATRIX_SLOT,
				ContainerKnowledgeInscriber.CRAFTING_GRID_SIZE, this.activeRecipe );
		}

		this.resultSlot.putStack( craftResult );

		// Update the save state
		if( EffectiveSide.isServerSide() )
		{
			this.sendSaveState( false );
		}

		// Sync
		this.detectAndSendChanges();
	}

	/**
	 * Sends the save-state to the client.
	 */
	public void sendSaveState( final boolean justSaved )
	{
		Packet_C_KnowledgeInscriber.sendSaveState( this.player, this.getSaveState(), justSaved );
	}

	/**
	 * Creates 'ghost' items when a crafting slot is clicked.
	 *
	 * @param slotNumber
	 * @param buttonPressed
	 * @param flag
	 * @param player
	 * @return
	 */
	@Override
	public ItemStack slotClick( final int slotNumber, final int buttonPressed, final int flag, final EntityPlayer player )
	{
		// Get the itemstack the player is holding with the mouse
		ItemStack draggingStack = player.inventory.getItemStack();

		// Was the clicked slot a crafting slot?
		for( Slot slot : this.craftingSlots )
		{
			if( slot.slotNumber == slotNumber )
			{
				// Is the player holding anything?
				if( draggingStack != null )
				{
					ItemStack copiedStack = draggingStack.copy();
					copiedStack.stackSize = 1;

					// Place a copy of the stack into the clicked slot
					slot.putStack( copiedStack );
				}
				else
				{
					// Clear the slot
					slot.putStack( null );
				}

				// Update the matrix
				this.onCraftMatrixChanged( slot.inventory );

				return draggingStack;
			}
		}

		// Was the clicked slot a pattern slot?
		for( Slot slot : this.patternSlots )
		{
			if( slot.slotNumber == slotNumber )
			{
				// Does the slot correspond to a stored pattern?
				if( slot.getHasStack() )
				{
					// Load the pattern
					this.loadPattern( this.kCoreHandler.getPatternForItem( slot.getStack() ) );

					// Update the matrix
					this.onCraftMatrixChanged( slot.inventory );
				}

				return draggingStack;
			}
		}

		// Pass to super
		return super.slotClick( slotNumber, buttonPressed, flag, player );

	}

	@Override
	public ItemStack transferStackInSlot( final EntityPlayer player, final int slotNumber )
	{
		// Is this client side?
		if( EffectiveSide.isClientSide() )
		{
			// Do nothing.
			return null;
		}

		// Get the slot that was shift-clicked
		Slot slot = this.getSlotOrNull( slotNumber );

		// Is there a valid slot with and item?
		if( ( slot != null ) && ( slot.getHasStack() ) )
		{
			boolean didMerge = false;

			// Get the itemstack in the slot
			ItemStack slotStack = slot.getStack();

			// Was the slot clicked in the player or hotbar inventory?
			if( this.slotClickedWasInPlayerInventory( slotNumber ) || this.slotClickedWasInHotbarInventory( slotNumber ) )
			{
				// Attempt to merge with kcore slot
				if( slotStack.getItem() instanceof ItemKnowledgeCore )
				{
					didMerge = this.mergeItemStack( slotStack, this.kCoreSlot.slotNumber, this.kCoreSlot.slotNumber + 1, false );
				}

				// Was the stack merged?
				if( !didMerge )
				{
					// Attempt to merge with player inventory
					didMerge = this.swapSlotInventoryHotbar( slotNumber, slotStack );
				}
			}
			// Was the slot clicked the KCore slot?
			else if( this.kCoreSlot.slotNumber == slotNumber )
			{
				// Attempt to merge with player hotbar
				didMerge = this.mergeSlotWithHotbarInventory( slotStack );

				// Was the stack merged?
				if( !didMerge )
				{
					// Attempt to merge with the player inventory
					didMerge = this.mergeSlotWithPlayerInventory( slotStack );
				}
			}

			// Was the stack merged?
			if( didMerge )
			{

				// Did the merger drain the stack?
				if( ( slotStack == null ) || ( slotStack.stackSize == 0 ) )
				{
					// Set the slot to have no item
					slot.putStack( null );
				}
				else
				{
					// Inform the slot its stack changed;
					slot.onSlotChanged();
				}

				// Send changes
				this.detectAndSendChanges();
			}
		}

		// All done.
		return null;

	}
}
