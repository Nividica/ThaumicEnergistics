package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.network.packet.PacketClientEssentiaTerminal;
import thaumicenergistics.network.packet.PacketServerEssentiaTerminal;
import thaumicenergistics.parts.AEPartEssentiaTerminal;
import thaumicenergistics.util.EssentiaCellTerminalWorker;
import thaumicenergistics.util.EssentiaConversionHelper;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;

public class ContainerEssentiaTerminal
	extends ContainerCellTerminalBase
{
	private AEPartEssentiaTerminal terminal = null;
	private MachineSource machineSource = null;

	public ContainerEssentiaTerminal( AEPartEssentiaTerminal terminal, EntityPlayer player )
	{
		super( player );
		
		this.terminal = terminal;
		
		this.machineSource = terminal.getTerminalMachineSource();

		if ( !player.worldObj.isRemote )
		{
			this.monitor = terminal.getGridBlock().getFluidMonitor();

			this.attachToMonitor();

			terminal.addContainer( this );
		}

		this.bindToInventory( terminal.getInventory() );
	}

	@Override
	public void forceAspectUpdate()
	{
		if ( this.monitor != null )
		{
			new PacketClientEssentiaTerminal( this.player, EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( this.monitor
							.getStorageList() ) ).sendPacketToPlayer( this.player );
		}
	}

	@Override
	public void onContainerClosed( EntityPlayer player )
	{
		super.onContainerClosed( player );

		if ( !player.worldObj.isRemote && ( this.terminal != null ) )
		{
			this.terminal.removeContainer( this );
		}
	}

	@Override
	public void postChange( IMEMonitor<IAEFluidStack> monitor, IAEFluidStack change, BaseActionSource source )
	{
		super.postChange( monitor, change, source );

		new PacketClientEssentiaTerminal( this.player, this.aspectStackList ).sendPacketToPlayer( this.player );
	}

	@Override
	public void receiveSelectedAspect( Aspect selectedAspect )
	{	
		// Set the selected aspect
		this.selectedAspect = selectedAspect;
		
		// Is this client side?
		if ( this.player.worldObj.isRemote )
		{
			// Update the gui
			this.guiBase.updateSelectedAspect();
		}
		else
		{
			// Send the change back to the client
			new PacketClientEssentiaTerminal( this.player, this.selectedAspect ).sendPacketToPlayer( this.player );
		}
	}

	@Override
	public void setSelectedAspect( Aspect selectedAspect )
	{
		new PacketServerEssentiaTerminal( this.player, selectedAspect ).sendPacketToServer();
	}
	
	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		
		// TODO: locks on terminal tile to prevent CME's
		
		if( this.monitor != null )
		{	
			if( EssentiaCellTerminalWorker.hasWork( this.inventory ) )
			{
				EssentiaCellTerminalWorker.doWork( this.inventory, this.monitor, this.machineSource, this.selectedAspect );
			}
		}
	}

}
