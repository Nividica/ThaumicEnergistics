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
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.tile.storage.TileChest;

/**
 * Inventory container for essentia cells in a ME chest.
 * 
 * @author Nividica
 * 
 */
public class ContainerEssentiaCell
	extends ContainerCellTerminalBase
{
	/**
	 * The aspect the player has selected.
	 */
	private AspectStack selectedAspectStack;

	/**
	 * The ME chest the cell is stored in.
	 */
	private TileChest hostChest;

	/**
	 * Import and export inventory slots.
	 */
	private PrivateInventory privateInventory = new PrivateInventory( ThaumicEnergistics.MOD_ID + ".item.essentia.cell.inventory", 2, 64 )
	{
		@Override
		public boolean isItemValidForSlot( int slotID, ItemStack itemStack )
		{
			return EssentiaItemContainerHelper.isContainer( itemStack );
		}
	};

	/**
	 * Creates the container.
	 * 
	 * @param player
	 * The player that owns this container.
	 * @param world
	 * The world the ME chest is in.
	 * @param x
	 * X position of the ME chest.
	 * @param y
	 * Y position of the ME chest.
	 * @param z
	 * Z position of the ME chest.
	 */
	public ContainerEssentiaCell( EntityPlayer player, World world, int x, int y, int z )
	{
		// Call the super-constructor
		super( player );

		// Is this server side?
		if( !this.player.worldObj.isRemote )
		{
			// Get the tile entity for the chest
			this.hostChest = (TileChest)world.getTileEntity( x, y, z );

			try
			{
				// Get the chest handler
				IMEInventoryHandler<IAEFluidStack> handler = this.hostChest.getHandler( StorageChannel.FLUIDS );

				// Get the monitor
				if( handler != null )
				{
					// Get the cell inventory monitor
					this.monitor = (IMEMonitor<IAEFluidStack>)handler;

					// Attach to the monitor
					this.attachToMonitor();
				}
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

		// Bind our inventory
		this.bindToInventory( this.privateInventory );

	}

	/**
	 * Gets the current list from the AE monitor and sends
	 * it to the client.
	 */
	@Override
	public void forceAspectUpdate()
	{
		if( this.monitor != null )
		{
			new PacketClientEssentiaCell( this.player, EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( this.monitor
							.getStorageList() ) ).sendPacketToPlayer( this.player );
		}
	}

	/**
	 * Drops any items in the import and export inventory.
	 */
	@Override
	public void onContainerClosed( EntityPlayer player )
	{
		super.onContainerClosed( player );

		if( !player.worldObj.isRemote )
		{
			for( int i = 0; i < 2; i++ )
			{
				this.player.dropPlayerItemWithRandomChoice( ( (Slot)this.inventorySlots.get( i ) ).getStack(), false );
			}
		}
	}

	/**
	 * Updates the list of aspects, and sends that list to the client.
	 */
	@Override
	public void postChange( IMEMonitor<IAEFluidStack> monitor, IAEFluidStack change, BaseActionSource source )
	{
		super.postChange( monitor, change, source );

		new PacketClientEssentiaCell( this.player, this.aspectStackList ).sendPacketToPlayer( this.player );
	}

	/**
	 * Updates the selected aspect, aspect stack and gui.
	 */
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
		if( this.player.worldObj.isRemote )
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

	/**
	 * Gets the currently selected aspect.
	 * 
	 * @return
	 */
	public AspectStack getSelectedAspectStack()
	{
		return this.selectedAspectStack;
	}

	/**
	 * Called when the user has clicked on an aspect.
	 * Sends that change to the server for validation.
	 */
	@Override
	public void setSelectedAspect( Aspect selectedAspect )
	{
		new PacketServerEssentiaCell( this.player, selectedAspect ).sendPacketToServer();
	}

	/**
	 * Checks if there is any work to perform.
	 * If there is it does so.
	 */
	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		// Do we have a monitor?
		if( this.monitor != null )
		{
			// Is there work to do?
			if( EssentiaCellTerminalWorker.hasWork( this.inventory ) )
			{

				try
				{
					// Get the energy grid
					IEnergyGrid eGrid = this.hostChest.getProxy().getEnergy();

					// Can we drain energy from the network?
					if( eGrid.extractAEPower( ContainerCellTerminalBase.POWER_PER_TRANSFER, Actionable.SIMULATE, PowerMultiplier.CONFIG ) >= ContainerCellTerminalBase.POWER_PER_TRANSFER )
					{
						// Do the work
						if( EssentiaCellTerminalWorker.doWork( this.inventory, this.monitor, null, this.selectedAspect ) )
						{
							// We did work, drain power
							eGrid.extractAEPower( ContainerCellTerminalBase.POWER_PER_TRANSFER, Actionable.MODULATE, PowerMultiplier.CONFIG );
						}
					}
				}
				catch( GridAccessException e )
				{
				}
			}
		}
	}
}
