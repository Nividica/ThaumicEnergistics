package thaumicenergistics.common.container;

import java.util.List;
import javax.annotation.Nonnull;
import appeng.api.AEApi;
import appeng.container.slot.SlotFake;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.research.ScanManager;
import thaumicenergistics.api.storage.IInventoryUpdateReceiver;
import thaumicenergistics.common.container.slot.SlotRestrictive;
import thaumicenergistics.common.inventory.HandlerDistillationPattern;
import thaumicenergistics.common.inventory.TheInternalInventory;
import thaumicenergistics.common.items.ItemCraftingAspect;
import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.tiles.TileDistillationPatternEncoder;
import thaumicenergistics.common.utils.EffectiveSide;

/**
 * {@link TileDistillationPatternEncoder} container.
 *
 * @author Nividica
 *
 */
public class ContainerDistillationPatternEncoder
	extends ContainerWithPlayerInventory
{
	/**
	 * Y position for the player and hotbar inventory.
	 */
	private static final int PLAYER_INV_POSITION_Y = 152, HOTBAR_INV_POSITION_Y = ContainerDistillationPatternEncoder.PLAYER_INV_POSITION_Y + 58;

	/**
	 * Position of the source item slot
	 */
	public static final int SLOT_SOURCE_ITEM_POS_X = 15, SLOT_SOURCE_ITEM_POS_Y = 69;

	/**
	 * Starting position of the source aspect slots
	 */
	private static final int SLOT_SOURCE_ASPECTS_POS_X = 65, SLOT_SOURCE_ASPECTS_POS_Y = 24, SLOT_SOURCE_ASPECTS_COUNT = 6;

	/**
	 * Position of the save aspect.
	 */
	private static final int SLOT_SELECTED_ASPECT_POS_X = 116, SLOT_SELECTED_ASPECT_POS_Y = 69;

	/**
	 * Position of the blank patterns.
	 */
	private static final int SLOT_PATTERNS_BLANK_POS_X = 146, SLOT_PATTERNS_BLANK_POS_Y = 75;

	/**
	 * Position of the encoded pattern.
	 */
	private static final int SLOT_PATTERN_ENCODED_POS_X = 146, SLOT_PATTERN_ENCODED_POS_Y = 113;

	/**
	 * Host encoder.
	 */
	private final TileDistillationPatternEncoder encoder;

	/**
	 * Slot holding the selected aspect.
	 */
	private final SlotFake slotSelectedAspect;

	/**
	 * Blank patterns slot.
	 */
	private final SlotRestrictive slotPatternsBlank;

	/**
	 * Encoded pattern slot.
	 */
	private final SlotRestrictive slotPatternEncoded;

	/**
	 * Pattern helper
	 */
	private final HandlerDistillationPattern patternHelper;

	/**
	 * Stores the aspects for the source item.
	 */
	private final TheInternalInventory internalInventory;

	/**
	 * Cached versions of the source and pattern.
	 */
	private ItemStack cachedSource, cachedPattern;

	/**
	 * Slot holding the source item.
	 */
	public final SlotFake slotSourceItem;

	/**
	 * Slots holding the source item's aspects.
	 */
	public final SlotFake[] slotSourceAspects = new SlotFake[ContainerDistillationPatternEncoder.SLOT_SOURCE_ASPECTS_COUNT];

	/**
	 * Gui to send updates to when slots change.
	 */
	public IInventoryUpdateReceiver slotUpdateReceiver;

	/**
	 * Constructor.
	 *
	 * @param player
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	public ContainerDistillationPatternEncoder( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Call super
		super( player );

		// Get the encoder
		this.encoder = (TileDistillationPatternEncoder)world.getTileEntity( x, y, z );

		// Create the internal inventory
		this.internalInventory = new TheInternalInventory( "dpeAspects", 7, 64 )
		{
			@Override
			public boolean isItemValidForSlot( final int slotIndex, final ItemStack itemStack )
			{
				return( ( itemStack == null ) || ( itemStack.getItem() instanceof ItemCraftingAspect ) );
			}
		};

		// Add the source aspect slots
		for( int index = 0; index < ContainerDistillationPatternEncoder.SLOT_SOURCE_ASPECTS_COUNT; ++index )
		{
			// Calculate Y
			int posY = ContainerDistillationPatternEncoder.SLOT_SOURCE_ASPECTS_POS_Y + ( index * 18 );

			// Create the slot
			this.slotSourceAspects[index] = new SlotFake( this.internalInventory, index,
							ContainerDistillationPatternEncoder.SLOT_SOURCE_ASPECTS_POS_X, posY );
			this.addSlotToContainer( this.slotSourceAspects[index] );
		}

		// Add the selected aspect slot
		this.slotSelectedAspect = new SlotFake( this.internalInventory, ContainerDistillationPatternEncoder.SLOT_SOURCE_ASPECTS_COUNT,
						ContainerDistillationPatternEncoder.SLOT_SELECTED_ASPECT_POS_X,
						ContainerDistillationPatternEncoder.SLOT_SELECTED_ASPECT_POS_Y );
		this.addSlotToContainer( this.slotSelectedAspect );

		// Add the source item slot
		this.slotSourceItem = new SlotFake( this.encoder, TileDistillationPatternEncoder.SLOT_SOURCE_ITEM,
						ContainerDistillationPatternEncoder.SLOT_SOURCE_ITEM_POS_X,
						ContainerDistillationPatternEncoder.SLOT_SOURCE_ITEM_POS_Y );
		this.addSlotToContainer( this.slotSourceItem );

		// Add blank pattern slot
		this.slotPatternsBlank = new SlotRestrictive( this.encoder, TileDistillationPatternEncoder.SLOT_BLANK_PATTERNS,
						ContainerDistillationPatternEncoder.SLOT_PATTERNS_BLANK_POS_X,
						ContainerDistillationPatternEncoder.SLOT_PATTERNS_BLANK_POS_Y );
		this.addSlotToContainer( this.slotPatternsBlank );

		// Add encoded pattern slot
		this.slotPatternEncoded = new SlotRestrictive( this.encoder, TileDistillationPatternEncoder.SLOT_ENCODED_PATTERN,
						ContainerDistillationPatternEncoder.SLOT_PATTERN_ENCODED_POS_X,
						ContainerDistillationPatternEncoder.SLOT_PATTERN_ENCODED_POS_Y );
		this.addSlotToContainer( this.slotPatternEncoded );

		// Bind to the players inventory
		this.bindPlayerInventory( player.inventory, ContainerDistillationPatternEncoder.PLAYER_INV_POSITION_Y,
			ContainerDistillationPatternEncoder.HOTBAR_INV_POSITION_Y );

		// Create the helper
		this.patternHelper = new HandlerDistillationPattern();

	}

	/**
	 * Clears all aspect slots.
	 */
	private void clearAspectSlots()
	{
		this.slotSelectedAspect.clearStack();
		for( int index = 0; index < this.slotSourceAspects.length; ++index )
		{
			this.slotSourceAspects[index].clearStack();
		}
	}

	/**
	 * Returns true if the transfer was handled.
	 * Assumes that clickedslot has an itemstack.
	 *
	 * @param clickedSlot
	 * @return
	 */
	private boolean handleSlotTransfer( @Nonnull final Slot clickedSlot )
	{
		// Get the stack
		ItemStack slotStack = clickedSlot.getStack();

		// Is the slot in the player inventory?
		if( clickedSlot.inventory == this.player.inventory )
		{
			// Will the blank pattern slot take this?
			if( this.slotPatternsBlank.isItemValid( slotStack ) )
			{
				return this.mergeItemStack( slotStack, this.slotPatternsBlank.slotNumber, this.slotPatternsBlank.slotNumber + 1, false );
			}

			// Will the encoded pattern take this?
			if( this.slotPatternEncoded.isItemValid( slotStack ) )
			{
				return this.mergeItemStack( slotStack, this.slotPatternEncoded.slotNumber, this.slotPatternEncoded.slotNumber + 1, false );
			}
			// Set the source slot
			ItemStack copy = slotStack.copy();
			copy.stackSize = 1;
			this.slotSourceItem.putStack( copy );
			return true;

		}

		// Pattern slot?
		if( ( ( this.slotPatternsBlank == clickedSlot ) || ( this.slotPatternEncoded == clickedSlot ) ) )
		{
			// Merge with the hotbar?
			if( !this.mergeSlotWithHotbarInventory( slotStack ) )
			{
				// Merge with the player inventory
				return this.mergeSlotWithPlayerInventory( slotStack );
			}
			// Merged with hotbar
			return true;
		}

		return false;
	}

	/**
	 * Attempts to load a pattern in the encoded slot.
	 */
	private void loadPattern()
	{
		// Read the pattern
		this.cachedPattern = this.slotPatternEncoded.getStack();
		this.patternHelper.readPattern( this.cachedPattern );

		// Is the pattern valid?
		if( this.patternHelper.isValid() )
		{
			// Set the source item
			this.slotSourceItem.putStack( this.patternHelper.getInput() );
			this.scanSourceItem( false );

			// Set the selected aspect
			this.setSelectedAspect( this.patternHelper.getOutput() );
		}
	}

	/**
	 * Called when the source item has changed.
	 *
	 * @param setSelectedAspect
	 */
	private void scanSourceItem( final boolean setSelectedAspect )
	{

		this.cachedSource = this.slotSourceItem.getStack();

		// Clear aspect slots
		this.clearAspectSlots();

		// Null?
		if( this.cachedSource == null )
		{
			// Done
			return;
		}

		// Get the aspects
		AspectList itemAspects = ThaumcraftApiHelper.getObjectAspects( this.cachedSource );
		itemAspects = ThaumcraftApiHelper.getBonusObjectTags( this.cachedSource, itemAspects );
		Aspect[] sortedAspects = null;

		// Does the item have any aspects?
		if( ( itemAspects == null ) || ( itemAspects.size() == 0 ) )
		{
			// Done
			return;
		}

		// Generate hash
		int hash = ScanManager.generateItemHash( this.cachedSource.getItem(), this.cachedSource.getItemDamage() );

		// Get the list of scanned objects
		List<String> list = Thaumcraft.proxy.getScannedObjects().get( this.player.getCommandSenderName() );

		// Assume all slot will have an aspect
		int numOfAspects = this.slotSourceAspects.length;

		// Has the player scanned the item?
		boolean playerScanned = ( ( list != null ) && ( ( list.contains( "@" + hash ) ) || ( list.contains( "#" + hash ) ) ) );
		if( playerScanned )
		{
			// Get sorted
			sortedAspects = itemAspects.getAspectsSortedAmount();

			// Set number to display
			numOfAspects = Math.min( numOfAspects, sortedAspects.length );
		}

		// Add each aspect
		Aspect aspect;
		for( int i = 0; i < numOfAspects; ++i )
		{
			// Create an itemstack
			ItemStack aspectItem = ItemEnum.CRAFTING_ASPECT.getStack();

			if( sortedAspects != null )
			{
				// Get the aspect
				aspect = sortedAspects[i];

				// Set the aspect
				ItemCraftingAspect.setAspect( aspectItem, aspect );

				// Set the size
				aspectItem.stackSize = itemAspects.getAmount( aspect );
			}

			// Put into slot
			this.slotSourceAspects[i].putStack( aspectItem );

			// Selected aspect empty?
			if( setSelectedAspect && playerScanned && !this.slotSelectedAspect.getHasStack() )
			{
				// Set selected
				this.setSelectedAspect( aspectItem );
			}
		}
	}

	@Override
	protected boolean detectAndSendChangesMP( final EntityPlayerMP playerMP )
	{
		// Does the pattern slot need to be sync'd?
		if( this.slotPatternEncoded.getStack() != this.cachedPattern )
		{
			// Load the pattern
			this.loadPattern();
			return true;
		}
		// Does the source item need to be sync'd?
		else if( this.slotSourceItem.getStack() != this.cachedSource )
		{
			// Scan the source item
			this.scanSourceItem( true );
			return true;
		}

		return false;
	}

	/**
	 * Set's the selected aspect stack.
	 *
	 * @param aspectStack
	 */
	protected void setSelectedAspect( final ItemStack aspectStack )
	{
		// Clear the stack
		this.slotSelectedAspect.clearStack();

		// Is there anything to put?
		if( aspectStack == null )
		{
			return;
		}

		// Does the stack have an aspect?
		Aspect aspect = ItemCraftingAspect.getAspect( aspectStack );
		if( aspect == null )
		{
			return;
		}

		// Has the player discovered this aspect?
		if( !ItemCraftingAspect.canPlayerSeeAspect( this.player, aspect ) )
		{
			// Set the selected aspect to unknown
			this.slotSelectedAspect.putStack( ItemEnum.CRAFTING_ASPECT.getStack() );
			return;
		}

		// Set the aspect
		this.slotSelectedAspect.putStack( aspectStack.copy() );
	}

	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		if( this.encoder != null )
		{
			return this.encoder.isUseableByPlayer( player );
		}
		return false;
	}

	/**
	 * Returns the player for this container.
	 *
	 * @return
	 */
	public EntityPlayer getPlayer()
	{
		return this.player;
	}

	/**
	 * Called when a pattern is to be encoded.
	 */
	public void onEncodePattern()
	{
		ItemStack pattern = null;
		boolean takeBlank = false;

		// Is there anything in the encoded pattern slot?
		if( this.slotPatternEncoded.getHasStack() )
		{
			// Set the pattern to it
			pattern = this.slotPatternEncoded.getStack();
		}
		// Is there a blank pattern to draw from?
		else if( this.slotPatternsBlank.getHasStack() )
		{
			// Create a new encoded pattern
			pattern = AEApi.instance().definitions().items().encodedPattern().maybeStack( 1 ).orNull();
			if( pattern == null )
			{
				// Patterns are disabled?
				// How did you even get here?!
				return;
			}
			takeBlank = true;
		}
		else
		{
			// Nothing to save to
			return;
		}

		// Set the pattern items
		if( !this.patternHelper.setPatternItems( this.slotSourceItem.getDisplayStack(), this.slotSelectedAspect.getDisplayStack() ) )
		{
			// Nothing to save
			return;
		}

		// Encode!
		this.patternHelper.encodePattern( pattern );

		// Set the pattern slot
		this.slotPatternEncoded.putStack( pattern );

		if( takeBlank )
		{
			// Decrement the blank patterns
			this.slotPatternsBlank.decrStackSize( 1 );
		}
	}

	@Override
	public void putStackInSlot( final int slotNumber, final ItemStack stack )
	{
		// Call super
		super.putStackInSlot( slotNumber, stack );

		// Source item changed?
		if( ( this.slotUpdateReceiver != null ) && ( this.slotSourceItem.slotNumber == slotNumber ) )
		{
			this.slotUpdateReceiver.onInventoryChanged( this.slotSourceItem.inventory );
		}
	}

	@Override
	public ItemStack slotClick( final int slotNumber, final int buttonPressed, final int flag, final EntityPlayer player )
	{
		// Selected aspect?
		if( this.slotSelectedAspect.slotNumber == slotNumber )
		{
			// No interaction
			return null;
		}

		// If true detect will be called, and null returned.
		boolean handled = false;

		// Source item slot?
		if( this.slotSourceItem.slotNumber == slotNumber )
		{
			// Get the item the player is dragging
			ItemStack heldItem = player.inventory.getItemStack();

			// Set the source slot
			if( heldItem != null )
			{
				ItemStack copy = heldItem.copy();
				copy.stackSize = 1;
				this.slotSourceItem.putStack( copy );
			}
			else
			{
				this.slotSourceItem.putStack( null );
			}

			handled = true;
		}

		// One of the source aspect slots?
		if( !handled )
		{
			for( int index = 0; index < this.slotSourceAspects.length; ++index )
			{
				if( this.slotSourceAspects[index].slotNumber == slotNumber )
				{
					if( this.slotSourceAspects[index].getHasStack() )
					{
						// Place it into the selected
						this.setSelectedAspect( this.slotSourceAspects[index].getStack() );
					}

					// Done
					handled = true;
					break;
				}
			}
		}

		// Was the click handled?
		if( handled )
		{
			// Detect any changes
			this.detectAndSendChanges();

			// Done
			return null;
		}

		// Call super
		return super.slotClick( slotNumber, buttonPressed, flag, player );
	}

	@Override
	public ItemStack transferStackInSlot( final EntityPlayer player, final int slotNumber )
	{
		if( EffectiveSide.isClientSide() )
		{
			// Ignored client side
			return null;
		}

		// Get the slot
		Slot clickedSlot = this.getSlotOrNull( slotNumber );

		// Slot empty?
		if( ( clickedSlot == null ) || !clickedSlot.getHasStack() )
		{
			// Done
			return null;
		}

		// Was this handled?
		if( this.handleSlotTransfer( clickedSlot ) )
		{
			// Was the itemstack in the slot drained?
			if( clickedSlot.getHasStack() && ( clickedSlot.getStack().stackSize <= 0 ) )
			{
				// Set to null
				clickedSlot.putStack( null );
			}
			else
			{
				// Let the slot know it has changed.
				clickedSlot.onSlotChanged();
			}

			// Update
			this.detectAndSendChanges();
		}

		return null;
	}
}
