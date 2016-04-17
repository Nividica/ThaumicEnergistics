package thaumicenergistics.common.container;

import javax.annotation.Nonnull;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.ViewItems;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.ICraftingIssuerHost;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.common.integration.tc.EssentiaItemContainerHelper.AspectItemType;
import thaumicenergistics.common.inventory.HandlerWirelessEssentiaTerminal;
import thaumicenergistics.common.inventory.TheInternalInventory;
import thaumicenergistics.common.items.ItemCraftingAspect;
import thaumicenergistics.common.items.ItemWirelessEssentiaTerminal;
import thaumicenergistics.common.network.packet.client.Packet_C_EssentiaCellTerminal;
import thaumicenergistics.common.network.packet.server.Packet_S_EssentiaCellTerminal;
import thaumicenergistics.common.storage.AspectStackComparator.AspectStackComparatorMode;
import thaumicenergistics.common.utils.EffectiveSide;

/**
 * {@link ItemWirelessEssentiaTerminal} container.
 *
 * @author Nividica
 *
 */
public class ContainerWirelessEssentiaTerminal
	extends ContainerEssentiaCellTerminalBase
{

	/**
	 * After this many ticks, power will be extracted from the terminal just for
	 * being open.
	 */
	private static final int EXTRACT_POWER_ON_TICK = 10;

	/**
	 * Handler used to interact with the wireless terminal.
	 */
	@Nonnull
	private final HandlerWirelessEssentiaTerminal handler;

	private Aspect tmpSelectedAspect;

	/**
	 * Import and export inventory slots.
	 */
	private TheInternalInventory privateInventory = new TheInternalInventory( ThaumicEnergistics.MOD_ID + ".item.essentia.cell.inventory", 2, 64 )
	{
		@Override
		public boolean isItemValidForSlot( final int slotID, final ItemStack itemStack )
		{
			// Get the type
			AspectItemType iType = EssentiaItemContainerHelper.INSTANCE.getItemType( itemStack );

			// True if jar or jar label
			return ( iType == AspectItemType.EssentiaContainer ) || ( iType == AspectItemType.JarLabel );
		}
	};

	/**
	 * Tracks the number of ticks elapsed.
	 */
	private int powerTickCounter = 1;

	/**
	 * The slot the terminal is in.
	 */
	private int terminalSlotIndex = -1;

	/**
	 *
	 * @param player
	 * @param handler
	 */
	public ContainerWirelessEssentiaTerminal( final EntityPlayer player, final @Nonnull HandlerWirelessEssentiaTerminal handler )
	{
		// Call super
		super( player );

		// Bind our inventory
		this.bindToInventory( this.privateInventory );

		// Set the terminal slot index
		this.terminalSlotIndex = player.inventory.currentItem;

		// Set the handler
		this.handler = handler;

		// Client side?
		if( EffectiveSide.isClientSide() )
		{
			// Request a full update from the server
			Packet_S_EssentiaCellTerminal.sendFullUpdateRequest( player );
		}
	}

	@Override
	protected BaseActionSource getActionSource()
	{
		return this.handler.getActionHost();
	}

	@Override
	protected IGrid getHostGrid()
	{
		try
		{
			return this.handler.getActionableNode().getGrid();
		}
		catch( Exception e )
		{
			return null;
		}
	}

	@Override
	protected Aspect getHostSelectedAspect()
	{
		return this.tmpSelectedAspect;
	}

	@Override
	protected IMEEssentiaMonitor getNewMonitor()
	{
		return this.handler.getEssentiaInventory();
	}

	@Override
	protected void setHostSelectedAspect( final Aspect aspect )
	{
		this.tmpSelectedAspect = aspect;
	}

	@Override
	public boolean canInteractWith( final EntityPlayer p_75145_1_ )
	{
		if( this.handler != null )
		{
			return this.handler.isConnected();
		}
		return false;
	}

	/**
	 * Transfers essentia, checks the network connectivity, and drains power.
	 */
	@Override
	public void doWork( final int elapsedTicks )
	{
		// Validate the handler
		if( this.handler == null )
		{
			// Invalid handler.
			return;
		}

		// Increment the tick counter.
		this.powerTickCounter += elapsedTicks;

		if( this.powerTickCounter > ContainerWirelessEssentiaTerminal.EXTRACT_POWER_ON_TICK )
		{
			// Adjust the power multiplier
			this.handler.updatePowerMultiplier();

			// Take power
			this.handler.extractPower( this.powerTickCounter, Actionable.MODULATE );

			// Update the item
			this.player.inventory.mainInventory[this.terminalSlotIndex] = this.handler.getTerminalItem();

			// Reset the tick counter
			this.powerTickCounter = 0;
		}

		// Transfer essentia if needed
		this.transferEssentiaFromWorkSlots();
	}

	@Override
	public ICraftingIssuerHost getCraftingHost()
	{
		return this.handler;
	}

	@Override
	public void onClientRequestAutoCraft( final EntityPlayer player, final Aspect aspect )
	{
		// Launch the GUI
		ThEGuiHandler.launchGui( ThEGuiHandler.AUTO_CRAFTING_AMOUNT, player, player.worldObj, 0, 0, 0 );

		// Setup the amount container
		if( player.openContainer instanceof ContainerCraftAmount )
		{
			// Get the container
			ContainerCraftAmount cca = (ContainerCraftAmount)this.player.openContainer;

			// Create the open context
			cca.setOpenContext( new ContainerOpenContext( this.handler ) );
			cca.getOpenContext().setWorld( player.worldObj );

			// Create the result item
			IAEItemStack result = AEApi.instance().storage().createItemStack( ItemCraftingAspect.createStackForAspect( aspect, 1 ) );

			// Set the item
			cca.getCraftingItem().putStack( result.getItemStack() );
			cca.setItemToCraft( result );

			// Issue update
			if( player instanceof EntityPlayerMP )
			{
				( (EntityPlayerMP)player ).isChangingQuantityOnly = false;
			}
			cca.detectAndSendChanges();
		}

	}

	@Override
	public void onClientRequestFullUpdate()
	{
		// Send the sorting mode
		Packet_C_EssentiaCellTerminal.sendViewingModes( this.player, this.handler.getSortingMode(), this.handler.getViewMode() );

		// Send the list
		Packet_C_EssentiaCellTerminal.sendFullList( this.player, this.repo.getAll() );
	}

	@Override
	public void onClientRequestSortModeChange( final EntityPlayer player, final boolean backwards )
	{
		// Change the sorting mode
		AspectStackComparatorMode sortingMode;
		if( backwards )
		{
			sortingMode = this.handler.getSortingMode().previousMode();
		}
		else
		{
			sortingMode = this.handler.getSortingMode().nextMode();
		}

		// Set the sorting mode.
		this.handler.setSortingMode( sortingMode );

		// Send confirmation back to client
		Packet_C_EssentiaCellTerminal.sendViewingModes( player, sortingMode, this.handler.getViewMode() );
	}

	@Override
	public void onClientRequestViewModeChange( final EntityPlayer player, final boolean backwards )
	{
		// Change the view mode
		ViewItems viewMode = Platform.rotateEnum( this.handler.getViewMode(), backwards, Settings.VIEW_MODE.getPossibleValues() );

		// Inform the handler of the change
		this.handler.setViewMode( viewMode );

		// Send confirmation back to client
		Packet_C_EssentiaCellTerminal.sendViewingModes( player, this.handler.getSortingMode(), viewMode );
	}

	/**
	 * Drops any items in the import and export inventory.
	 */
	@Override
	public void onContainerClosed( final EntityPlayer player )
	{
		super.onContainerClosed( player );

		if( EffectiveSide.isServerSide() )
		{
			for( int i = 0; i < 2; i++ )
			{
				this.player.dropPlayerItemWithRandomChoice( ( (Slot)this.inventorySlots.get( i ) ).getStack(), false );
			}
		}
	}

	@Override
	public ItemStack slotClick( final int slotNumber, final int buttonPressed, final int flag, final EntityPlayer player )
	{
		try
		{
			Slot clickedSlot = this.getSlotOrNull( slotNumber );
			// Protect the wireless terminal
			if( ( clickedSlot.inventory == this.player.inventory ) && ( clickedSlot.getSlotIndex() == this.terminalSlotIndex ) )
			{
				return null;
			}
		}
		catch( Exception e )
		{
		}

		return super.slotClick( slotNumber, buttonPressed, flag, player );

	}

}
