package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.network.packet.client.PacketClientEssentiaCellTerminal;
import thaumicenergistics.network.packet.server.PacketServerEssentiaCellTerminal;
import thaumicenergistics.parts.AEPartEssentiaTerminal;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.config.Actionable;
import appeng.api.networking.security.PlayerSource;

/**
 * Inventory container for the essentia terminal.
 * 
 * @author Nividica
 * 
 */
public class ContainerEssentiaTerminal
	extends AbstractContainerCellTerminalBase
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
			new PacketServerEssentiaCellTerminal().createFullUpdateRequest( this.player ).sendPacketToServer();
			this.hasRequested = true;
		}

		// Bind our inventory
		this.bindToInventory( terminal.getInventory() );
	}

	@Override
	protected boolean extractPowerForEssentiaTransfer( final int amountOfEssentiaTransfered, final Actionable mode )
	{
		return this.terminal.extractPowerForEssentiaTransfer( amountOfEssentiaTransfered, mode );
	}

	/**
	 * Transfers essentia.
	 */
	@Override
	public void doWork( final int elapsedTicks )
	{
		// Transfer essentia if needed.
		this.transferEssentia( this.playerSource );
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
			new PacketClientEssentiaCellTerminal().createUpdateFullList( this.player, this.aspectStackList ).sendPacketToPlayer();
		}
	}

	@Override
	public void onClientRequestSortModeChange( final ComparatorMode sortingMode, final EntityPlayer player )
	{
		this.terminal.onClientRequestSortingModeChange( sortingMode );
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
	 * Called from the terminal when it's sorting mode has changed
	 */
	public void onSortingModeChanged( final ComparatorMode sortingMode )
	{
		// Inform the client
		new PacketClientEssentiaCellTerminal().createSortModeUpdate( this.player, sortingMode ).sendPacketToPlayer();
	}

}
