package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.inventory.HandlerWirelessEssentiaTerminal;
import thaumicenergistics.network.packet.client.PacketClientEssentiaCellTerminal;
import thaumicenergistics.network.packet.server.PacketServerEssentiaCellTerminal;
import thaumicenergistics.util.EffectiveSide;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.config.Actionable;

public class ContainerWirelessEssentiaTerminal
	extends AbstractContainerCellTerminalBase
{
	/**
	 * After this many ticks, power will be extracted from the terminal just for
	 * being open.
	 */
	private static final int EXTRACT_POWER_ON_TICK = 10;

	/**
	 * Handler used to interact with the wireless terminal.
	 */
	private final HandlerWirelessEssentiaTerminal handler;

	/**
	 * Import and export inventory slots.
	 */
	private PrivateInventory privateInventory = new PrivateInventory( ThaumicEnergistics.MOD_ID + ".item.essentia.cell.inventory", 2, 64 )
	{
		@Override
		public boolean isItemValidForSlot( final int slotID, final ItemStack itemStack )
		{
			return EssentiaItemContainerHelper.instance.isContainer( itemStack );
		}
	};

	/**
	 * Tracks if the terminal was connected last tick.
	 */
	private boolean wasConnected = true;

	/**
	 * Tracks the number of ticks elapsed.
	 */
	private int powerTickCounter = 1;

	/**
	 * The slot the terminal is in.
	 */
	private int terminalSlotIndex = -1;

	/**
	 * How much to multiply the required power by.
	 */
	private double powerMultipler = 1.0D;

	/**
	 * 
	 * @param player
	 * @param handler
	 * Null on client side.
	 */
	public ContainerWirelessEssentiaTerminal( final EntityPlayer player, final HandlerWirelessEssentiaTerminal handler )
	{
		// Call super
		super( player );

		// Bind our inventory
		this.bindToInventory( this.privateInventory );

		// Set the terminal slot index
		this.terminalSlotIndex = player.inventory.currentItem;

		// Set the handler
		this.handler = handler;

		// Server side?
		if( EffectiveSide.isServerSide() )
		{
			// Set the monitor
			this.monitor = this.handler.getMonitor();

			// Attach to the monitor
			this.attachToMonitor();
		}
		else
		{
			// Request a full update from the server
			new PacketServerEssentiaCellTerminal().createFullUpdateRequest( player ).sendPacketToServer();
			this.hasRequested = true;
		}
	}

	/**
	 * Checks if the terminal is in range of the AP, and updates the network
	 * monitor accordingly.
	 */
	private void updateConnectivity()
	{
		// Is the terminal connected?
		if( this.handler.isConnected() )
		{
			// Terminal is in range and powered

			// Was the terminal disconnected last tick?
			if( !this.wasConnected )
			{
				// Re-acquire the monitor
				this.monitor = this.handler.getMonitor();

				// Re-attach
				this.attachToMonitor();

				// Send the list
				this.onClientRequestFullUpdate();
			}
		}
		else
		{
			// Terminal is out of power, or out of range.

			// Was the terminal connected last tick?
			if( this.wasConnected )
			{
				// Disconnect from the monitor
				this.detachFromMonitor();

				// TODO: Close the gui.

				// Send the empty list
				this.onClientRequestFullUpdate();
			}

			// Set as no longer connected
			this.wasConnected = false;
		}
	}

	@Override
	protected boolean extractPowerForEssentiaTransfer( final int amountOfEssentiaTransfered, final Actionable mode )
	{
		// Calculate the amount of power to drain
		double powerRequired = AbstractContainerCellTerminalBase.POWER_PER_TRANSFER * amountOfEssentiaTransfered * this.powerMultipler;

		// Drain power
		return this.handler.extractPower( powerRequired, mode );
	}

	/**
	 * Transfers essentia, checks the network connectivity, and drains power.
	 */
	@Override
	public void doWork( final int elapsedTicks )
	{
		// Increment the tick counter.
		this.powerTickCounter += elapsedTicks;

		if( this.powerTickCounter > ContainerWirelessEssentiaTerminal.EXTRACT_POWER_ON_TICK )
		{
			// Check the network connectivity
			this.updateConnectivity();

			// Adjust the power multiplier
			this.powerMultipler = this.handler.getWirelessPowerMultiplier();

			// Take power
			this.handler.extractPower( this.powerMultipler * this.powerTickCounter, Actionable.MODULATE );

			// Update the item
			this.player.inventory.mainInventory[this.terminalSlotIndex] = this.handler.getTerminalItem();

			// Reset the tick counter
			this.powerTickCounter = 0;
		}

		// Transfer essentia if needed
		this.transferEssentia( this.handler.getActionHost() );
	}

	@Override
	public void onClientRequestFullUpdate()
	{
		// Send the sorting mode
		new PacketClientEssentiaCellTerminal().createSortModeUpdate( this.player, this.handler.getSortingMode() ).sendPacketToPlayer();

		// Send the list
		new PacketClientEssentiaCellTerminal().createUpdateFullList( this.player, this.aspectStackList ).sendPacketToPlayer();
	}

	@Override
	public void onClientRequestSortModeChange( final ComparatorMode sortingMode, final EntityPlayer player )
	{
		// Set the sorting mode.
		this.handler.setSortingMode( sortingMode );

		// Send confirmation back to client
		new PacketClientEssentiaCellTerminal().createSortModeUpdate( player, sortingMode ).sendPacketToPlayer();
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
	public ItemStack slotClick( final int slotID, final int buttonPressed, final int flag, final EntityPlayer player )
	{
		try
		{
			Slot clickedSlot = this.getSlot( slotID );
			// Protect the wireless terminal
			if( ( clickedSlot.inventory == this.player.inventory ) && ( clickedSlot.getSlotIndex() == this.terminalSlotIndex ) )
			{
				return null;
			}
		}
		catch( Exception e )
		{
		}

		return super.slotClick( slotID, buttonPressed, flag, player );

	}

}
