package thaumicenergistics.container;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.container.slot.SlotRestrictive;
import thaumicenergistics.gui.GuiCellTerminalBase;
import thaumicenergistics.util.EffectiveSide;
import thaumicenergistics.util.EssentiaConversionHelper;
import thaumicenergistics.util.IInventoryUpdateReceiver;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Base class for cell and terminal inventory containers
 * 
 * @author Nividica
 * 
 */
public abstract class ContainerCellTerminalBase
	extends ContainerWithPlayerInventory
	implements IMEMonitorHandlerReceiver<IAEFluidStack>, IAspectSelectorContainer, IInventoryUpdateReceiver
{
	/**
	 * X position for the output slot
	 */
	private static int OUTPUT_POSITION_X = 26;

	/**
	 * Y position for the output slot
	 */
	private static int OUTPUT_POSITION_Y = 74;

	/**
	 * X position for the input slot
	 */
	private static int INPUT_POSITION_X = 8;

	/**
	 * Y position for the input slot
	 */
	private static int INPUT_POSITION_Y = 74;

	/**
	 * Y position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_Y = 104;

	/**
	 * Y position for the hotbar inventory
	 */
	private static int HOTBAR_INV_POSITION_Y = 162;

	/**
	 * The minimum amount of time to wait before playing
	 * sounds again. In ms.
	 */
	private static int MINIMUM_SOUND_WAIT = 900;

	/**
	 * Amount of AE power required per transfer
	 */
	protected static double POWER_PER_TRANSFER = 0.3;

	/**
	 * Slot ID for the output
	 */
	public static int OUTPUT_SLOT_ID = 1;

	/**
	 * Slot ID for the input
	 */
	public static int INPUT_SLOT_ID = 0;

	/**
	 * AE network monitor
	 */
	protected IMEMonitor<IAEFluidStack> monitor;

	/**
	 * List of aspects on the network
	 */
	protected List<AspectStack> aspectStackList = new ArrayList<AspectStack>();

	/**
	 * The aspect the user has selected.
	 */
	protected Aspect selectedAspect;

	/**
	 * The player that owns this container.
	 */
	protected EntityPlayer player;

	/**
	 * The gui associated with this container
	 */
	protected GuiCellTerminalBase guiBase;

	/**
	 * Import and export inventory
	 */
	protected PrivateInventory inventory;

	/**
	 * The last known stack size stored in the export slot
	 */
	private int lastInventorySecondSlotCount = 0;

	/**
	 * The last time, in ms, the splashy sound played
	 */
	private long lastSoundPlaytime = 0;

	/**
	 * Slot number of the input slot
	 */
	private int inputSlotNumber = -1;

	/**
	 * Slot number of the output slot
	 */
	private int outputSlotNumber = -1;

	/**
	 * Holds a list of changes sent to the gui before the
	 * full list is sent.
	 */
	private List<AspectStack> pendingChanges = new ArrayList<AspectStack>();

	/**
	 * Set to true once a full list request is sent to the server.
	 */
	protected boolean hasRequested = false;

	/**
	 * Create the container and register the owner
	 * 
	 * @param player
	 */
	public ContainerCellTerminalBase( EntityPlayer player )
	{
		this.player = player;

		if( EffectiveSide.isClientSide() )
		{
			this.lastSoundPlaytime = System.currentTimeMillis();
		}
		else
		{
			this.hasRequested = true;
		}
	}

	/**
	 * Determines if the specified aspect stack is a different from an existing
	 * stack in the specified list.
	 * 
	 * @param potentialChange
	 * @return
	 * If a match is found: Pair <ExistingIndex, ChangedStack>
	 * If the item is new: Pair <-1, potentialChange>
	 * 
	 */
	private ImmutablePair<Integer, AspectStack> isChange( AspectStack potentialChange, List<AspectStack> comparedAgainst )
	{
		AspectStack matchingStack = null;

		for( int index = 0; index < comparedAgainst.size(); index++ )
		{
			// Tenativly set the matching stack
			matchingStack = comparedAgainst.get( index );

			// Check if it is a match
			if( potentialChange.aspect == matchingStack.aspect )
			{
				// Found a match, determine how much it has changed
				long changeAmount = potentialChange.amount - matchingStack.amount;

				// Create the changed stack
				AspectStack changedStack = new AspectStack( matchingStack.aspect, changeAmount );

				return new ImmutablePair<Integer, AspectStack>( index, changedStack );
			}
		}

		// No match change is new item
		return new ImmutablePair<Integer, AspectStack>( -1, potentialChange );
	}

	/**
	 * Merges a change with the cached aspect list
	 * 
	 * @param changeDetails
	 * @return
	 */
	private boolean mergeChange( ImmutablePair<Integer, AspectStack> changeDetails )
	{
		// Get the index that changed
		int changedIndex = changeDetails.getLeft();

		// Get the stack that changed
		AspectStack changedStack = changeDetails.getRight();

		// Did anything change?
		if( changedStack.amount == 0 )
		{
			// Nothing changed
			return false;
		}

		// Was there a match?
		if( changedIndex != -1 )
		{
			// Get the new amount
			long newAmount = this.aspectStackList.get( changedIndex ).amount + changedStack.amount;

			// Was the stack drained?
			if( newAmount <= 0 )
			{
				// Remove from list
				this.aspectStackList.remove( changedIndex );
			}
			else
			{
				// Update the list
				this.aspectStackList.get( changedIndex ).amount = newAmount;
			}
		}
		// New addition
		else
		{
			this.aspectStackList.add( changedStack );
		}

		// List updated.
		return true;
	}

	@SideOnly(Side.CLIENT)
	private void playTransferAudio()
	{
		// Get the itemstack in the output slot
		ItemStack itemStack = this.inventory.getStackInSlot( ContainerCellTerminalBase.OUTPUT_SLOT_ID );

		// Is there anything in the second slot?
		if( itemStack != null )
		{
			// Has the count changed?
			if( this.lastInventorySecondSlotCount != itemStack.stackSize )
			{
				// Has enough time passed to play the sound again?
				if( ( System.currentTimeMillis() - this.lastSoundPlaytime ) > ContainerCellTerminalBase.MINIMUM_SOUND_WAIT )
				{
					// Play swimy sound
					Minecraft.getMinecraft().getSoundHandler()
									.playSound( PositionedSoundRecord.func_147674_a( new ResourceLocation( "game.neutral.swim" ), 1.0F ) );

					// Set the playtime
					this.lastSoundPlaytime = System.currentTimeMillis();
				}

				// Set the count
				this.lastInventorySecondSlotCount = itemStack.stackSize;
			}
		}
		else
		{
			// Reset the count
			this.lastInventorySecondSlotCount = 0;
		}
	}

	/**
	 * Binds the container to the specified inventory and the players inventory.
	 * 
	 * @param inventory
	 */
	protected void bindToInventory( PrivateInventory inventory )
	{
		// Set the inventory
		this.inventory = inventory;

		// Register the container as an update receiver
		this.inventory.setReceiver( this );

		// Create the input slot
		Slot workSlot = new SlotRestrictive( inventory, ContainerCellTerminalBase.INPUT_SLOT_ID, ContainerCellTerminalBase.INPUT_POSITION_X,
						ContainerCellTerminalBase.INPUT_POSITION_Y );

		// Add the input slot
		this.addSlotToContainer( workSlot );

		// Set the input slot number
		this.inputSlotNumber = workSlot.slotNumber;

		// Create the output slot
		workSlot = new SlotFurnace( this.player, inventory, ContainerCellTerminalBase.OUTPUT_SLOT_ID, ContainerCellTerminalBase.OUTPUT_POSITION_X,
						ContainerCellTerminalBase.OUTPUT_POSITION_Y );

		// Add the output slot
		this.addSlotToContainer( workSlot );

		// Set the output slot number
		this.outputSlotNumber = workSlot.slotNumber;

		// Bind to the player's inventory
		this.bindPlayerInventory( this.player.inventory, ContainerCellTerminalBase.PLAYER_INV_POSITION_Y,
			ContainerCellTerminalBase.HOTBAR_INV_POSITION_Y );

	}

	/**
	 * Attach this container to the AE monitor
	 */
	public void attachToMonitor()
	{
		if( ( EffectiveSide.isServerSide() ) && ( this.monitor != null ) )
		{
			this.monitor.addListener( this, null );

			// Update our cached list of aspects
			this.aspectStackList = EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( this.monitor.getStorageList() );
		}
	}

	/**
	 * Who can interact with the container?
	 */
	@Override
	public boolean canInteractWith( EntityPlayer player )
	{
		return true;
	}

	/**
	 * Gets the list of aspect stacks in the container.
	 * 
	 * @return
	 */
	public List<AspectStack> getAspectStackList()
	{
		return this.aspectStackList;
	}

	/**
	 * Get the player that owns this container
	 * 
	 * @return
	 */
	public EntityPlayer getPlayer()
	{
		return this.player;
	}

	/**
	 * Gets the aspect that the player has selected.
	 * 
	 * @return
	 */
	public Aspect getSelectedAspect()
	{
		return this.selectedAspect;
	}

	/**
	 * Is this container still valid for receiving updates
	 * from the AE monitor?
	 */
	@Override
	public boolean isValid( Object verificationToken )
	{
		return true;
	}

	/**
	 * Merges a change with the cached aspect list
	 * 
	 * @param change
	 * @return
	 */
	public boolean mergeChange( AspectStack change )
	{
		// Get the index of the change
		int index = this.isChange( change, this.aspectStackList ).getLeft();

		// Create the change
		ImmutablePair<Integer, AspectStack> changeDetails = new ImmutablePair<Integer, AspectStack>( index, change );

		// Attempt the merger
		return this.mergeChange( changeDetails );

	}

	/**
	 * Called when a client requests the state of the container.
	 * Updates our cached list of aspects
	 */
	public void onClientRequestFullUpdate()
	{
		// Update our cached list of aspects
		//this.aspectStackList = EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( this.monitor.getStorageList() );
	}

	/**
	 * Unregister this container from the monitor.
	 */
	@Override
	public void onContainerClosed( EntityPlayer player )
	{
		super.onContainerClosed( player );

		if( EffectiveSide.isServerSide() )
		{
			if( this.monitor != null )
			{
				this.monitor.removeListener( this );
			}
		}
	}

	/**
	 * Called when the list of fluids on the ME network changes.
	 */
	@Override
	public void onInventoryChanged( IInventory sourceInventory )
	{
		// Is this client side?
		if( EffectiveSide.isClientSide() )
		{
			this.playTransferAudio();
		}
	}

	@Override
	public void onListUpdate()
	{
		/* TODO: Re-visit this or figure out how to make storage bus send updates
		// Ignored client side
		if( EffectiveSide.isClientSide() )
		{
			return;
		}

		// Get the monitor list
		List<AspectStack> monitorList = EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( this.monitor.getStorageList() );

		// Get the iterator
		Iterator<AspectStack> cachedIterator = this.aspectStackList.iterator();

		// Compare to the current list
		while( cachedIterator.hasNext() )
		{
			// Get the next item
			AspectStack cachedStack = cachedIterator.next();

			// Get the details about this potential change
			ImmutablePair<Integer, AspectStack> changeDetails = this.isChange( cachedStack, monitorList );

			// Get the montiorlist index
			int monitorListIndex = changeDetails.getLeft();

			// Was there a match?
			if( monitorListIndex != -1 )
			{
				// Remove from the list
				monitorList.remove( monitorListIndex );
			}

			// Get the changed amount
			long changedAmount = -changeDetails.getRight().amount;

			// Did anything change?
			if( changedAmount == 0 )
			{
				continue;
			}

			// Was the item removed?
			if( monitorListIndex == -1 )
			{
				// Remove from the cache
				cachedIterator.remove();
			}

			// Update the current stack
			cachedStack.amount += changedAmount;

			// Inform the subclass
			this.postAspectStackChange( new AspectStack( cachedStack.aspect, changedAmount ) );
		}

		// Any remaining items in the monitor list must be new
		for( AspectStack newStack : monitorList )
		{
			// Add the stack to our cache
			this.aspectStackList.add( newStack );

			// Inform the subclass
			this.postAspectStackChange( newStack );
		}
		*/
	}

	/**
	 * Called when the server sends a full list of network aspects.
	 * 
	 * @param aspectStackList
	 */
	public void onReceiveAspectList( List<AspectStack> aspectStackList )
	{
		// Set the aspect list
		this.aspectStackList = aspectStackList;

		// Check pending changes
		if( ( this.aspectStackList != null ) && ( !this.pendingChanges.isEmpty() ) )
		{
			// Update list with pending changes
			for( int index = 0; index < this.pendingChanges.size(); index++ )
			{
				this.onReceiveAspectListChange( this.pendingChanges.get( index ) );
			}

			// Clear pending
			this.pendingChanges.clear();
		}

		// Update the gui
		if( this.guiBase != null )
		{
			this.guiBase.updateAspects();
		}
	}

	/**
	 * Called when an aspect in the list changes amount.
	 * 
	 * @param change
	 */
	public void onReceiveAspectListChange( AspectStack change )
	{
		// Ignored server side
		if( EffectiveSide.isServerSide() )
		{
			return;
		}

		// Ensure the change is not null
		if( change == null )
		{
			return;
		}

		// Have we requested the full list yet?
		if( !this.hasRequested )
		{
			return;
		}

		// Do we have a list?
		if( this.aspectStackList == null )
		{
			// Not yet received full list, add to pending
			this.pendingChanges.add( change );
			return;
		}

		// Can we merge this change with the list?
		if( this.mergeChange( change ) )
		{
			// Update the gui
			if( ( this.guiBase != null ) && this.pendingChanges.isEmpty() )
			{
				this.guiBase.updateAspects();
			}
		}
	}

	/**
	 * Called when the the selected aspect has changed, and that
	 * change has been sent via packet.
	 * 
	 * @param selectedAspect
	 */
	public abstract void onReceiveSelectedAspect( Aspect selectedAspect );

	/**
	 * Informs the subclass of a change in the ME network.
	 * 
	 * @param change
	 */
	public abstract void postAspectStackChange( AspectStack change );

	/**
	 * Called by the AE monitor when the network changes.
	 */

	@Override
	public final void postChange( IBaseMonitor<IAEFluidStack> monitor, IAEFluidStack change, BaseActionSource source )
	{
		// Update the client
		this.postAspectStackChange( EssentiaConversionHelper.convertAEFluidStackToAspectStack( change ) );
	}

	/**
	 * Sets the gui associated with this container
	 * 
	 * @param guiBase
	 */
	public void setGui( GuiCellTerminalBase guiBase )
	{
		if( guiBase != null )
		{
			this.guiBase = guiBase;
		}
	}

	/**
	 * Called when the user clicks an aspect in the GUI
	 */
	@Override
	public abstract void setSelectedAspect( Aspect selectedAspect );

	@Override
	public ItemStack transferStackInSlot( EntityPlayer player, int slotNumber )
	{
		// Get the slot that was shift-clicked
		Slot slot = (Slot)this.inventorySlots.get( slotNumber );

		// Is there a valid slot with and item?
		if( ( slot != null ) && ( slot.getHasStack() ) )
		{
			boolean didMerge = false;

			// Get the itemstack in the slot
			ItemStack slotStack = slot.getStack();

			// Was the slot clicked the input slot or output slot?
			if( ( slotNumber == this.inputSlotNumber ) || ( slotNumber == this.outputSlotNumber ) )
			{
				// Attempt to merge with the player inventory
				didMerge = this.mergeSlotWithPlayerInventory( slotStack );
			}
			// Was the slot clicked in the player or hotbar inventory?
			else if( this.slotClickedWasInPlayerInventory( slotNumber ) || this.slotClickedWasInHotbarInventory( slotNumber ) )
			{
				// Is the item valid for the input slot?
				if( ( (Slot)this.inventorySlots.get( this.inputSlotNumber ) ).isItemValid( slotStack ) )
				{
					// Attempt to merge with the input slot
					didMerge = this.mergeItemStack( slotStack, this.inputSlotNumber, this.inputSlotNumber + 1, false );
				}

				// Did we merge?
				if( !didMerge )
				{
					didMerge = this.swapSlotInventoryHotbar( slotNumber, slotStack );
				}

			}

			// Did the merger drain the stack?
			if( slotStack.stackSize == 0 )
			{
				// Set the slot to have no item
				slot.putStack( null );
			}
			else
			{
				// Inform the slot its stack changed;
				slot.onSlotChanged();
			}

		}

		return null;
	}
}
