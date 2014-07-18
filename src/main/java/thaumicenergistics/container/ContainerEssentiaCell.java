package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.network.packet.PacketClientEssentiaCell;
import thaumicenergistics.network.packet.PacketServerEssentiaCell;
import thaumicenergistics.util.EssentiaCellTerminalWorker;
import thaumicenergistics.util.EssentiaConversionHelper;
import thaumicenergistics.util.EssentiaItemContainerHelper;
import thaumicenergistics.util.PrivateInventory;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.tile.storage.TileChest;

public class ContainerEssentiaCell extends ContainerCellTerminalBase
{
	//private HandlerItemEssentiaCell storageFluid;
	private AspectStack selectedAspectStack;
	private TileChest hostChest;
	
	private PrivateInventory privateInventory = new PrivateInventory( ThaumicEnergistics.MODID + ".item.essentia.cell.inventory", 2, 64 )
	{
		@Override
		public boolean isItemValidForSlot( int slotID, ItemStack itemStack )
		{
			return EssentiaItemContainerHelper.isContainer( itemStack );
		}
	};

	public ContainerEssentiaCell( EntityPlayer player, World world, int x, int y, int z )
	{
		// Call the super-constructor
		super( player );
		
		// Is this server side?
		if ( !this.player.worldObj.isRemote )
		{
			// Get the tile entity for the chest
			this.hostChest = (TileChest)world.getTileEntity( x, y, z );
			
			IMEInventoryHandler<IAEFluidStack> handler = null;
			
			try
			{
				// Get the chest handler
				handler = this.hostChest.getHandler( StorageChannel.FLUIDS );
				
				// Get the monitor
				if( handler != null )
				{
					this.monitor = (IMEMonitor<IAEFluidStack>)handler;
					
					// Attach to the monitor
					this.attachToMonitor();
				}
			}
			catch( Exception e ) { }
		}
		
		// Bind our inventory
		this.bindToInventory( this.privateInventory );
		
	}
	
	@Override
	public void forceAspectUpdate()
	{ 
		if ( this.monitor != null )
		{
			new PacketClientEssentiaCell( this.player, EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( this.monitor
							.getStorageList() ) ).sendPacketToPlayer( this.player );
		}
	}
	
	@Override
	public void onContainerClosed( EntityPlayer player )
	{
		super.onContainerClosed( player );

		if ( !player.worldObj.isRemote )
		{
			for( int i = 0; i < 2; i++ )
			{
				this.player.dropPlayerItemWithRandomChoice( ( (Slot)this.inventorySlots.get( i ) ).getStack(), false );
			}
		}
	}
	
	@Override
	public void postChange( IMEMonitor<IAEFluidStack> monitor, IAEFluidStack change, BaseActionSource source )
	{
		super.postChange( monitor, change, source );
		
		new PacketClientEssentiaCell( this.player, this.aspectStackList ).sendPacketToPlayer( this.player );
	}
	
	@Override
	public void receiveSelectedAspect( Aspect selectedAspect )
	{
		this.selectedAspect = selectedAspect;
		
		if( this.selectedAspect != null )
		{
			for( AspectStack stack : this.aspectStackList )
			{
				if( ( stack != null ) && ( stack.aspect == this.selectedAspect ) )
				{
					this.selectedAspectStack = stack;
					
					break;
				}
			}
		}

		// Is this the client?
		if ( this.player.worldObj.isRemote )
		{
			// Update the gui
			this.guiBase.updateSelectedAspect();
		}
		else
		{
			// Update the client
			new PacketClientEssentiaCell( this.player, this.selectedAspect ).sendPacketToPlayer( this.player );
		}
	}
	
	public AspectStack getSelectedAspectStack()
	{
		return this.selectedAspectStack;
	}
	
	@Override
	public void setSelectedAspect( Aspect selectedAspect )
	{
		new PacketServerEssentiaCell( this.player, selectedAspect ).sendPacketToServer();
	}

	
	
	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		
		if( this.monitor != null )
		{	
			if( EssentiaCellTerminalWorker.hasWork( this.inventory ) )
			{
				EssentiaCellTerminalWorker.doWork( this.inventory, this.monitor, null, this.selectedAspect );
			}
		}
	}
}
