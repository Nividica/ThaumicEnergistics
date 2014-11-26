package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.integration.tc.EssentiaCellTerminalWorker;
import thaumicenergistics.network.packet.client.PacketClientEssentiaTerminal;
import thaumicenergistics.network.packet.server.PacketServerEssentiaTerminal;
import thaumicenergistics.parts.AEPartEssentiaTerminal;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.PlayerSource;

/**
 * Inventory container for the essentia terminal.
 * 
 * @author Nividica
 * 
 */
public class ContainerEssentiaTerminal
	extends ContainerCellTerminalBase
{
	/**
	 * The terminal this is associated with.
	 */
	private AEPartEssentiaTerminal terminal = null;

	/**
	 * Network source representing the player who is interacting with the
	 * container.
	 */
	private PlayerSource playerSource = null;

	/**
	 * The player associated with this open container
	 */
	private EntityPlayer player;

	/**
	 * Creates the container
	 * 
	 * @param terminal
	 * Parent terminal.
	 * @param player
	 * Player that owns this container.
	 */
	public ContainerEssentiaTerminal( final AEPartEssentiaTerminal terminal, final EntityPlayer player )
	{
		// Call the super
		super( player );

		// Set the player
		this.player = player;

		// Set the terminal
		this.terminal = terminal;

		// Get and set the machine source
		this.playerSource = new PlayerSource( player, this.terminal );

		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Get the monitor
			this.monitor = terminal.getGridBlock().getFluidMonitor();

			// Attach to the monitor
			this.attachToMonitor();

			// Add this container to the terminal's list
			terminal.addListener( this );
		}
		else
		{
			// Ask for a list update
			new PacketServerEssentiaTerminal().createFullUpdateRequest( this.player ).sendPacketToServer();
			this.hasRequested = true;
		}

		// Bind our inventory
		this.bindToInventory( terminal.getInventory() );
	}

	/**
	 * Checks if there is any work to perform.
	 * If there is it does so.
	 */
	@Override
	public void detectAndSendChanges()
	{
		// Call super
		super.detectAndSendChanges();

		// Do we have a monitor
		if( this.monitor != null )
		{

			// Can we lock the inventory?
			if( this.terminal.lockInventoryForWork() )
			{
				// Try block ensures lock gets released.
				try
				{
					// Do we have work to do?
					if( EssentiaCellTerminalWorker.instance.hasWork( this.inventory ) )
					{
						// Get the energy grid
						IEnergyGrid eGrid = this.terminal.getGridBlock().getEnergyGrid();

						// Did we get the grid, and can we drain energy?
						if( ( eGrid != null ) &&
										( eGrid.extractAEPower( ContainerCellTerminalBase.POWER_PER_TRANSFER, Actionable.SIMULATE,
											PowerMultiplier.CONFIG ) >= ContainerCellTerminalBase.POWER_PER_TRANSFER ) )
						{
							// Do the work.
							if( EssentiaCellTerminalWorker.instance.doWork( this.inventory, this.monitor, this.playerSource, this.selectedAspect,
								this.player ) )
							{
								// We did work, extract power
								eGrid.extractAEPower( ContainerCellTerminalBase.POWER_PER_TRANSFER, Actionable.MODULATE, PowerMultiplier.CONFIG );
							}
						}
					}
				}
				catch( Exception e )
				{
				}
				finally
				{
					// Release the lock.
					this.terminal.unlockInventory();
				}
			}
		}
	}

	/**
	 * Gets the current list from the AE monitor, as well as the current
	 * sorting mode, and sends it to the client.
	 */
	@Override
	public void onClientRequestFullUpdate()
	{
		// Send the sorting mode
		this.onSortingModeChanged( this.terminal.getSortingMode() );

		// Send the aspect list
		if( this.monitor != null )
		{
			new PacketClientEssentiaTerminal().createUpdateFullList( this.player, this.aspectStackList ).sendPacketToPlayer();
		}
	}

	/**
	 * Removes this container from the terminal.
	 */
	@Override
	public void onContainerClosed( final EntityPlayer player )
	{
		super.onContainerClosed( player );

		if( EffectiveSide.isServerSide() && ( this.terminal != null ) )
		{
			this.terminal.removeListener( this );
		}
	}

	/**
	 * Resent the full list to the client.
	 */
	@Override
	public void onListUpdate()
	{
		//this.onClientRequestFullUpdate();
	}

	/**
	 * Updates the selected aspect and gui.
	 */
	@Override
	public void onReceiveSelectedAspect( final Aspect selectedAspect )
	{
		// Set the selected aspect
		this.selectedAspect = selectedAspect;

		// Is this client side?
		if( EffectiveSide.isClientSide() )
		{
			// Update the gui
			this.guiBase.updateSelectedAspect();
		}
		else
		{
			// Send the change back to the client
			new PacketClientEssentiaTerminal().createSelectedAspectUpdate( this.player, this.selectedAspect ).sendPacketToPlayer();
		}
	}

	/**
	 * Called from the AE part when it's sorting mode has changed
	 */
	public void onSortingModeChanged( final ComparatorMode sortingMode )
	{
		// Inform the client
		new PacketClientEssentiaTerminal().createSortModeUpdate( this.player, sortingMode ).sendPacketToPlayer();
	}

	/**
	 * Forwards the change to the client.
	 */
	@Override
	public void postAspectStackChange( final AspectStack change )
	{
		// Send the change
		new PacketClientEssentiaTerminal().createListChanged( this.player, change ).sendPacketToPlayer();
	}

	/**
	 * Called when the user has clicked on an aspect.
	 * Sends that change to the server for validation.
	 */
	@Override
	public void setSelectedAspect( final Aspect selectedAspect )
	{
		new PacketServerEssentiaTerminal().createUpdateSelectedAspect( this.player, selectedAspect ).sendPacketToServer();
	}

}
