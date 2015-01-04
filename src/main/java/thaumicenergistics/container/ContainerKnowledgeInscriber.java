package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumicenergistics.container.slot.SlotRestrictive;
import thaumicenergistics.integration.tc.ArcaneCraftingPattern;
import thaumicenergistics.integration.tc.ArcaneRecipeHelper;
import thaumicenergistics.inventory.HandlerKnowledgeCore;
import thaumicenergistics.network.packet.client.PacketClientKnowledgeInscriber;
import thaumicenergistics.tileentities.TileKnowledgeInscriber;
import thaumicenergistics.util.EffectiveSide;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotInaccessible;

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
	 * Y position for the player and hotbar inventory.
	 */
	private static final int PLAYER_INV_POSITION_Y = 162, HOTBAR_INV_POSITION_Y = PLAYER_INV_POSITION_Y + 58;

	/**
	 * Knowledge core slot.
	 */
	private static final int KCORE_SLOT_X = 186, KCORE_SLOT_Y = 8;

	/**
	 * Pattern slots.
	 */
	private static final int PATTERN_SLOT_X = 26, PATTERN_SLOT_Y = 18, PATTERN_ROWS = 3, PATTERN_COLS = 7, PATTERN_SLOT_SPACING = 18;

	/**
	 * Crafting slots
	 */
	private static final int CRAFTING_SLOT_X = 26, CRAFTING_SLOT_Y = 90, CRAFTING_ROWS = 3, CRAFTING_COLS = 3, CRAFTING_SLOT_SPACING = 18;

	/**
	 * Maximum number of patterns.
	 */
	private static final int MAXIMUM_PATTERNS = HandlerKnowledgeCore.MAXIMUM_STORED_PATTERNS;

	/**
	 * Slots
	 */
	private SlotRestrictive kCoreSlot;
	private SlotInaccessible[] patternSlots = new SlotInaccessible[ContainerKnowledgeInscriber.MAXIMUM_PATTERNS];
	private SlotFakeCraftingMatrix[] craftingSlots = new SlotFakeCraftingMatrix[ContainerKnowledgeInscriber.CRAFTING_ROWS *
					ContainerKnowledgeInscriber.CRAFTING_COLS];
	private SlotFake resultSlot;

	/**
	 * Handles interaction with the knowledge core.
	 */
	private HandlerKnowledgeCore kCoreHandler;

	/**
	 * Player using the inscriber
	 */
	private EntityPlayer player;

	/**
	 * The current recipe, if any.
	 */
	private IArcaneRecipe activeRecipe = null;

	/**
	 * Tracks changes to the core slot.
	 */
	private boolean hadCoreLastCheck = false;

	public ContainerKnowledgeInscriber( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Set the player
		this.player = player;

		// Bind to the players inventory
		this.bindPlayerInventory( player.inventory, ContainerKnowledgeInscriber.PLAYER_INV_POSITION_Y,
			ContainerKnowledgeInscriber.HOTBAR_INV_POSITION_Y );

		// Get the inscriber
		TileKnowledgeInscriber inscriber = (TileKnowledgeInscriber)world.getTileEntity( x, y, z );

		// Get the inscriber's inventory
		IInventory inscriberInventory = inscriber.getInventory();

		int slotIndex = TileKnowledgeInscriber.KCORE_SLOT;

		// Create the Kcore slot
		this.kCoreSlot = new SlotRestrictive( inscriberInventory, slotIndex, ContainerKnowledgeInscriber.KCORE_SLOT_X,
						ContainerKnowledgeInscriber.KCORE_SLOT_Y );
		this.addSlotToContainer( this.kCoreSlot );

		// Create the pattern slots
		slotIndex = TileKnowledgeInscriber.PATTERN_SLOT;
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
				this.patternSlots[index] = new SlotInaccessible( inscriberInventory, slotIndex++ , posX, posY );

				// Add the slot
				this.addSlotToContainer( this.patternSlots[index] );
			}
		}

		// Create the crafting slots
		slotIndex = TileKnowledgeInscriber.CRAFTING_MATRIX_SLOT;
		for( int row = 0; row < ContainerKnowledgeInscriber.CRAFTING_ROWS; row++ )
		{
			for( int column = 0; column < ContainerKnowledgeInscriber.CRAFTING_COLS; column++ )
			{
				// Calculate the array index
				int index = ( row * ContainerKnowledgeInscriber.CRAFTING_COLS ) + column;

				// Calculate the position
				int posX = ContainerKnowledgeInscriber.CRAFTING_SLOT_X + ( ContainerKnowledgeInscriber.CRAFTING_SLOT_SPACING * column );
				int posY = ContainerKnowledgeInscriber.CRAFTING_SLOT_Y + ( ContainerKnowledgeInscriber.CRAFTING_SLOT_SPACING * row );

				// Add to the array
				this.craftingSlots[index] = new SlotFakeCraftingMatrix( inscriberInventory, slotIndex++ , posX, posY );

				// Add the slot
				this.addSlotToContainer( this.craftingSlots[index] );

			}
		}

		// Create the result slot
		this.resultSlot = new SlotFake( inscriberInventory, TileKnowledgeInscriber.CRAFTING_RESULT_SLOT, 116, 108 );
		this.addSlotToContainer( this.resultSlot );

		if( EffectiveSide.isServerSide() )
		{
			// Check for a kcore
			if( this.kCoreSlot.getHasStack() )
			{
				// Mark the presence of the core
				this.hadCoreLastCheck = true;

				// Create the handler
				// TODO: Link with item
				this.kCoreHandler = new HandlerKnowledgeCore();
			}

			// Update the result
			this.onCraftMatrixChanged( inscriberInventory );
		}

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
		if( this.kCoreHandler == null )
		{
			saveState = CoreSaveState.Disabled_MissingCore;
		}
		// Is there a valid recipe?
		else if( ( this.activeRecipe == null ) || ( this.activeRecipe.getRecipeOutput() == null ) )
		{
			saveState = CoreSaveState.Disabled_InvalidRecipe;
		}
		else
		{
			// Does the core already have this recipe, or one that produces the same result, stored?
			boolean isNew = !this.kCoreHandler.hasPatternFor( this.activeRecipe.getRecipeOutput() );

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

		return saveState;
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
		return true;
	}

	/**
	 * Checks for core insertion or removal.
	 */
	@Override
	public void detectAndSendChanges()
	{
		// Call super
		super.detectAndSendChanges();

		// Check for a kcore
		boolean hasCore = this.kCoreSlot.getHasStack();

		// Has a core been placed or removed?
		if( hasCore != this.hadCoreLastCheck )
		{
			if( hasCore )
			{
				// Get the handler
				// TODO: Link with item
				this.kCoreHandler = new HandlerKnowledgeCore();
			}
			else
			{
				// Remove the handler
				this.kCoreHandler = null;
			}

			// Update the client
			this.onClientRequestFullUpdate( this.player );
		}

		// Mark the cores presence
		this.hadCoreLastCheck = hasCore;
	}

	/**
	 * Sends the save-state to the client.
	 */
	public void onClientRequestFullUpdate( final EntityPlayer player )
	{
		// Update the client
		new PacketClientKnowledgeInscriber().createSendSaveState( player, this.getSaveState() ).sendPacketToPlayer();
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

		if( saveState == CoreSaveState.Enabled_Save )
		{
			// Build the ingredient list
			int gridSize = ContainerKnowledgeInscriber.CRAFTING_ROWS * ContainerKnowledgeInscriber.CRAFTING_COLS;
			ItemStack[] inputs = new ItemStack[gridSize];
			for( int index = 0; index < gridSize; index++ )
			{
				inputs[index] = this.craftingSlots[index].getStack();
			}

			// Create the pattern
			ArcaneCraftingPattern pattern = new ArcaneCraftingPattern( this.kCoreSlot.getStack(), this.activeRecipe.getAspects(),
							this.resultSlot.getStack(), inputs );

			// Add the pattern
			this.kCoreHandler.addPattern( pattern );

			// Update the client
			this.onClientRequestFullUpdate( player );
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

				// Update the client
				this.onClientRequestFullUpdate( player );
			}
		}
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
		this.activeRecipe = ArcaneRecipeHelper.instance.findMatchingArcaneResult( inv, TileKnowledgeInscriber.CRAFTING_MATRIX_SLOT, 9, this.player );

		// Set the result slot
		if( ( this.activeRecipe != null ) && ( this.activeRecipe.getRecipeOutput() != null ) )
		{
			this.resultSlot.putStack( this.activeRecipe.getRecipeOutput() );
		}
		else
		{
			this.resultSlot.putStack( null );
		}

		// Update the client
		if( EffectiveSide.isServerSide() )
		{
			this.onClientRequestFullUpdate( this.player );
		}

		// Sync
		this.detectAndSendChanges();
	}

	/**
	 * Creates 'ghost' items when a crafting slot is clicked.
	 * 
	 * @param slotID
	 * @param buttonPressed
	 * @param flag
	 * @param player
	 * @return
	 */
	@Override
	public ItemStack slotClick( final int slotID, final int buttonPressed, final int flag, final EntityPlayer player )
	{
		try
		{
			// Get the clicked slot
			Slot clickedSlot = this.getSlot( slotID );

			// Was the clicked slot a crafting slot?
			if( clickedSlot instanceof SlotFakeCraftingMatrix )
			{
				// Get the itemstack the player is holding with the mouse
				ItemStack draggingStack = player.inventory.getItemStack();

				// Is the player holding anything?
				if( draggingStack != null )
				{
					ItemStack copiedStack = draggingStack.copy();
					copiedStack.stackSize = 1;
					clickedSlot.putStack( copiedStack );
				}
				else
				{
					clickedSlot.putStack( null );
				}

				// Update the matrix
				this.onCraftMatrixChanged( clickedSlot.inventory );

				return draggingStack;
			}
		}
		catch( Exception e )
		{
		}

		return super.slotClick( slotID, buttonPressed, flag, player );

	}

	@Override
	public ItemStack transferStackInSlot( final EntityPlayer player, final int slotNumber )
	{
		return null;
	}
}
