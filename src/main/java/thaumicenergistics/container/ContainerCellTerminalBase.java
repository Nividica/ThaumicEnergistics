package thaumicenergistics.container;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.container.slot.SlotRespective;
import thaumicenergistics.gui.GuiCellTerminalBase;
import thaumicenergistics.gui.IAspectSelectorContainer;
import thaumicenergistics.util.EssentiaConversionHelper;
import thaumicenergistics.util.IInventoryUpdateReceiver;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.networking.security.BaseActionSource;
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
	 * X position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_X = 104;

	/**
	 * Y position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_Y = 162;

	/**
	 * The minimum amount of time to wait before playing
	 * sounds again. In ms.
	 */
	private static int MINIMUM_SOUND_WAIT = 900;

	/**
	 * Slot ID for the output
	 */
	public static int OUTPUT_SLOT_ID = 1;

	/**
	 * Slot ID for the input
	 */
	public static int INPUT_SLOT_ID = 0;

	/**
	 * Slot ID offset the player inventory
	 */
	public static int PLAYER_INV_SLOT_OFFSET = 2;

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
	 * Create the container and register the owner
	 * 
	 * @param player
	 */
	public ContainerCellTerminalBase( EntityPlayer player )
	{
		this.player = player;

		this.lastSoundPlaytime = System.currentTimeMillis();
	}

	/**
	 * Binds the container to the specified inventory and the players inventory.
	 * 
	 * @param inventory
	 */
	protected void bindToInventory( PrivateInventory inventory )
	{
		this.inventory = inventory;

		this.inventory.setReceiver( this );

		this.addSlotToContainer( new SlotRespective( inventory, ContainerCellTerminalBase.INPUT_SLOT_ID, ContainerCellTerminalBase.INPUT_POSITION_X,
						ContainerCellTerminalBase.INPUT_POSITION_Y ) );

		this.addSlotToContainer( new SlotFurnace( this.player, inventory, ContainerCellTerminalBase.OUTPUT_SLOT_ID,
						ContainerCellTerminalBase.OUTPUT_POSITION_X, ContainerCellTerminalBase.OUTPUT_POSITION_Y ) );

		this.bindPlayerInventory( this.player.inventory, ContainerCellTerminalBase.PLAYER_INV_SLOT_OFFSET,
			ContainerCellTerminalBase.PLAYER_INV_POSITION_X, ContainerCellTerminalBase.PLAYER_INV_POSITION_Y );

	}

	/**
	 * Attach this container to the AE monitor
	 */
	public void attachToMonitor()
	{
		if ( ( !this.player.worldObj.isRemote ) && ( this.monitor != null ) )
		{
			this.monitor.addListener( this, null );
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
	 * Called when the GUI's aspect list needs refreshing.
	 */
	public abstract void forceAspectUpdate();

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
	 * Unregister this container from the monitor.
	 */
	@Override
	public void onContainerClosed( EntityPlayer player )
	{
		super.onContainerClosed( player );

		if ( !player.worldObj.isRemote )
		{
			if ( this.monitor != null )
			{
				this.monitor.removeListener( this );
			}
		}
	}

	/**
	 * Called when the import or export has changed items.
	 */
	@Override
	public void onInventoryChanged()
	{
		// Is this client side?
		if ( this.player.worldObj.isRemote )
		{
			// Get the itemstack in the output slot
			ItemStack itemStack = this.inventory.getStackInSlot( ContainerCellTerminalBase.OUTPUT_SLOT_ID );

			// Is there anything in the second slot?
			if ( itemStack != null )
			{
				// Has the count changed?
				if ( this.lastInventorySecondSlotCount != itemStack.stackSize )
				{
					// Has enough time passed to play the sound again?
					if ( ( System.currentTimeMillis() - this.lastSoundPlaytime ) > ContainerCellTerminalBase.MINIMUM_SOUND_WAIT )
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
	}

	@Override
	public void onListUpdate()
	{
		// Ignored
	}

	/**
	 * Called by the AE montior when the network changes.
	 */
	@Override
	public void postChange( IMEMonitor<IAEFluidStack> monitor, IAEFluidStack change, BaseActionSource source )
	{
		this.aspectStackList = EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( monitor.getStorageList() );
	}

	/**
	 * Called when the the selected aspect has changed, and that
	 * change has been sent via packet.
	 * 
	 * @param selectedAspect
	 */
	public abstract void receiveSelectedAspect( Aspect selectedAspect );

	/**
	 * Sets the gui associated with this container
	 * 
	 * @param guiBase
	 */
	public void setGui( GuiCellTerminalBase guiBase )
	{
		if ( guiBase != null )
		{
			this.guiBase = guiBase;
		}
	}

	/**
	 * Called when the user clicks an aspect in the GUI
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public abstract void setSelectedAspect( Aspect selectedAspect );

	// TODO: Fix this up, move to superclass
	@Override
	public ItemStack transferStackInSlot( EntityPlayer player, int slotnumber )
	{
		ItemStack itemstack = null;

		Slot slot = (Slot)this.inventorySlots.get( slotnumber );

		if ( ( slot != null ) && ( slot.getHasStack() ) )
		{
			ItemStack itemstack1 = slot.getStack();

			itemstack = itemstack1.copy();

			if ( this.inventory.isItemValidForSlot( 0, itemstack1 ) )
			{
				if ( ( slotnumber == 1 ) || ( slotnumber == 0 ) )
				{
					if ( !this.mergeItemStack( itemstack1, 2, 36, false ) )
					{
						return null;
					}
				}
				else if ( !this.mergeItemStack( itemstack1, 0, 1, false ) )
				{
					return null;
				}
				if ( itemstack1.stackSize == 0 )
				{
					slot.putStack( null );
				}
				else
				{
					slot.onSlotChanged();
				}
			}
			else
			{
				return null;
			}
		}

		return itemstack;
	}

	/**
	 * Called when the aspect list has changed and that
	 * change has been sent via packet
	 * 
	 * @param aspectStackList
	 */
	@SideOnly(Side.CLIENT)
	public void updateAspectList( List<AspectStack> aspectStackList )
	{
		this.aspectStackList = aspectStackList;

		if ( this.guiBase != null )
		{
			this.guiBase.updateAspects();
		}
	}

}
