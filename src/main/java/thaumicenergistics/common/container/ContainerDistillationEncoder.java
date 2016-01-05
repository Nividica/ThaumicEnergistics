package thaumicenergistics.common.container;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.research.ScanManager;
import thaumicenergistics.api.storage.IInventoryUpdateReceiver;
import thaumicenergistics.common.container.slot.SlotRestrictive;
import thaumicenergistics.common.items.ItemCraftingAspect;
import thaumicenergistics.common.registries.ItemEnum;
import thaumicenergistics.common.tiles.TileDistillationEncoder;
import thaumicenergistics.common.utils.DistillationPatternHelper;
import thaumicenergistics.common.utils.EffectiveSide;
import appeng.api.AEApi;
import appeng.container.slot.SlotFake;

public class ContainerDistillationEncoder
	extends ContainerWithPlayerInventory
{
	/**
	 * Y position for the player and hotbar inventory.
	 */
	private static final int PLAYER_INV_POSITION_Y = 152, HOTBAR_INV_POSITION_Y = PLAYER_INV_POSITION_Y + 58;

	/**
	 * Position of the source item slot
	 */
	public static final int SLOT_SOURCE_ITEM_POS_X = 15, SLOT_SOURCE_ITEM_POS_Y = 69;

	/**
	 * Starting position of the source aspect slots
	 */
	private static final int SLOT_SOURCE_ASPECTS_POS_X = 65, SLOT_SOURCE_ASPECTS_POS_Y = 24;

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
	 * Player who opened the GUI.
	 */
	protected EntityPlayer player;

	/**
	 * Host encoder.
	 */
	protected TileDistillationEncoder hostTile;

	/**
	 * Slot holding the source item.
	 */
	public final SlotFake slotSourceItem;

	/**
	 * Slots holding the source item's aspects.
	 */
	public final SlotFake[] slotSourceAspects = new SlotFake[TileDistillationEncoder.SLOT_SOURCE_ASPECTS_COUNT];

	/**
	 * Slot holding the selected aspect.
	 */
	protected SlotFake slotSelectedAspect;

	/**
	 * Blank patterns slot.
	 */
	protected SlotRestrictive slotPatternsBlank;

	/**
	 * Encoded pattern slot.
	 */
	protected SlotRestrictive slotPatternEncoded;

	/**
	 * Pattern helper
	 */
	protected DistillationPatternHelper patternHelper;

	/**
	 * Who/what to send updates to when slots change.
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
	public ContainerDistillationEncoder( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Set the player
		this.player = player;

		// Get the encoder
		this.hostTile = (TileDistillationEncoder)world.getTileEntity( x, y, z );

		// Get the encoder's inventory
		IInventory hostInv = this.hostTile.getInventory();

		// Add the source item slot
		this.slotSourceItem = new SlotFake( hostInv, TileDistillationEncoder.SLOT_SOURCE_ITEM, SLOT_SOURCE_ITEM_POS_X, SLOT_SOURCE_ITEM_POS_Y );
		this.addSlotToContainer( this.slotSourceItem );

		// Add the source aspect slots
		for( int index = 0; index < this.slotSourceAspects.length; ++index )
		{
			// Calculate Y
			int posY = SLOT_SOURCE_ASPECTS_POS_Y + ( index * 18 );

			// Create the slot
			this.slotSourceAspects[index] = new SlotFake( hostInv, TileDistillationEncoder.SLOT_SOURCE_ASPECTS + index, SLOT_SOURCE_ASPECTS_POS_X,
							posY );

			// Add it
			this.addSlotToContainer( this.slotSourceAspects[index] );
		}

		// Add the selected aspect slot
		this.slotSelectedAspect = new SlotFake( hostInv, TileDistillationEncoder.SLOT_SELECTED_ASPECT, SLOT_SELECTED_ASPECT_POS_X,
						SLOT_SELECTED_ASPECT_POS_Y );
		this.addSlotToContainer( this.slotSelectedAspect );

		// Add blank pattern slot
		this.slotPatternsBlank = new SlotRestrictive( hostInv, TileDistillationEncoder.SLOT_BLANK_PATTERNS,
						SLOT_PATTERNS_BLANK_POS_X, SLOT_PATTERNS_BLANK_POS_Y );
		this.addSlotToContainer( this.slotPatternsBlank );

		// Add encoded pattern slot
		this.slotPatternEncoded = new SlotRestrictive( hostInv, TileDistillationEncoder.SLOT_ENCODED_PATTERN,
						SLOT_PATTERN_ENCODED_POS_X, SLOT_PATTERN_ENCODED_POS_Y );
		this.addSlotToContainer( this.slotPatternEncoded );

		// Bind to the players inventory
		this.bindPlayerInventory( player.inventory, PLAYER_INV_POSITION_Y, HOTBAR_INV_POSITION_Y );

		// Create the helper
		this.patternHelper = new DistillationPatternHelper();

	}

	/**
	 * Called when a pattern is loaded from the encoded pattern slot.
	 */
	protected void onLoadedPattern()
	{
		// Set the source item
		this.setSourceItem( this.patternHelper.getInput() );

		// Set the selected aspect
		this.setSelectedAspect( this.patternHelper.getOutput() );
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
			return;
		}

		// Set the aspect
		this.slotSelectedAspect.putStack( aspectStack.copy() );
	}

	/**
	 * Sets the source item and updates aspects.
	 */
	protected void setSourceItem( final ItemStack sourceStack )
	{
		// Ignored on client side
		if( EffectiveSide.isClientSide() )
		{
			return;
		}

		// Clear slots
		this.slotSourceItem.clearStack();
		this.slotSelectedAspect.clearStack();
		for( int index = 0; index < this.slotSourceAspects.length; ++index )
		{
			this.slotSourceAspects[index].clearStack();
		}

		// Null?
		if( sourceStack == null )
		{
			// Done
			return;
		}

		// Copy the itemstack
		ItemStack sourceItem = sourceStack.copy();

		// Set size to 1
		sourceItem.stackSize = 1;

		// Set the source item
		this.slotSourceItem.putStack( sourceItem );

		// Get the aspects
		AspectList itemAspects = ThaumcraftApiHelper.getObjectAspects( sourceItem );
		Aspect[] sortedAspects = null;

		// Does the item have any aspects?
		if( ( itemAspects == null ) || ( itemAspects.size() == 0 ) )
		{
			// Done
			return;
		}

		// Generate hash
		int hash = ScanManager.generateItemHash( sourceItem.getItem(), sourceItem.getItemDamage() );

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
			if( playerScanned && ( !this.slotSelectedAspect.getHasStack() ) )
			{
				// Attempt to place into selected
				this.setSelectedAspect( aspectItem );
			}
		}

	}

	/**
	 * Can interact with any real player.
	 */
	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		return !( player instanceof FakePlayer );
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

		// Update
		this.detectAndSendChanges();
	}

	@Override
	public void putStackInSlot( final int slotNumber, final ItemStack stack )
	{
		// Call super
		super.putStackInSlot( slotNumber, stack );

		// Call receiver
		if( EffectiveSide.isClientSide() && ( this.slotUpdateReceiver != null ) )
		{
			this.slotUpdateReceiver.onInventoryChanged( this.getSlot( slotNumber ).inventory );
		}
	}

	@Override
	public ItemStack slotClick( final int slotNumber, final int buttonPressed, final int flag, final EntityPlayer player )
	{
		// Source item slot?
		if( this.slotSourceItem.slotNumber == slotNumber )
		{
			// Get the item the player is dragging
			ItemStack heldItem = player.inventory.getItemStack();

			// Set the source slot
			this.setSourceItem( heldItem );

			// Update
			this.detectAndSendChanges();

			// Done
			return null;
		}

		// One of the source aspect slots?
		for( int index = 0; index < this.slotSourceAspects.length; ++index )
		{
			if( this.slotSourceAspects[index].slotNumber == slotNumber )
			{
				if( this.slotSourceAspects[index].getHasStack() )
				{
					// Place it into the selected
					this.setSelectedAspect( this.slotSourceAspects[index].getStack() );

					// Update
					this.detectAndSendChanges();
				}

				// Done
				return null;
			}
		}

		// Encoded pattern slot?
		if( this.slotPatternEncoded.slotNumber == slotNumber )
		{
			// Is the slot empty?
			if( !this.slotPatternEncoded.getHasStack() )
			{
				// Get the item the player is dragging
				ItemStack heldItem = player.inventory.getItemStack();

				// Attempt to load it
				this.patternHelper.readPattern( heldItem );

				// Valid pattern?
				if( this.patternHelper.isValid() )
				{
					// Load the pattern
					this.onLoadedPattern();

					// Update
					this.detectAndSendChanges();
				}
				else
				{
					// Don't allow this pattern
					return null;
				}
			}
		}

		// Selected aspect?
		if( this.slotSelectedAspect.slotNumber == slotNumber )
		{
			// No interaction
			return null;
		}

		return super.slotClick( slotNumber, buttonPressed, flag, player );
	}

	@Override
	public ItemStack transferStackInSlot( final EntityPlayer player, final int slotNumber )
	{
		boolean handled = false;

		// Get the slot
		Slot clickedSlot = this.getSlot( slotNumber );

		// Slot empty?
		if( !clickedSlot.getHasStack() )
		{
			// Done
			return null;
		}

		// Get the stack
		ItemStack slotStack = clickedSlot.getStack();

		// Is the slot in the player inventory?
		if( clickedSlot.inventory == this.player.inventory )
		{
			// Will the blank pattern slot take this?
			if( this.slotPatternsBlank.isItemValid( slotStack ) )
			{
				handled = this.mergeItemStack( slotStack, this.slotPatternsBlank.slotNumber, this.slotPatternsBlank.slotNumber + 1, false );
			}

			// Will the encoded pattern take this?
			if( !handled && this.slotPatternEncoded.isItemValid( slotStack ) )
			{
				// Attempt to load the pattern
				this.patternHelper.readPattern( slotStack );
				if( this.patternHelper.isValid() )
				{
					handled = this.mergeItemStack( slotStack, this.slotPatternEncoded.slotNumber, this.slotPatternEncoded.slotNumber + 1, false );
					if( handled )
					{
						this.onLoadedPattern();
					}
				}
			}

			if( !handled )
			{
				// Set the source slot
				this.setSourceItem( slotStack );

				handled = true;
			}

		}

		// Pattern slot?
		if( ( !handled ) && ( ( this.slotPatternsBlank.slotNumber == slotNumber ) || ( this.slotPatternEncoded.slotNumber == slotNumber ) ) )
		{
			handled = this.mergeSlotWithHotbarInventory( slotStack );

			if( !handled )
			{
				handled = this.mergeSlotWithPlayerInventory( slotStack );
			}
		}

		// Was this handled?
		if( handled )
		{
			if( slotStack.stackSize <= 0 )
			{
				clickedSlot.putStack( null );
			}

			// Update
			this.detectAndSendChanges();
		}
		return null;
	}
}
