package thaumicenergistics.parts;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaStorageBus;
import thaumicenergistics.gui.GuiEssentiaStorageBus;
import thaumicenergistics.inventory.HandlerEssentiaStorageBus;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.network.packet.client.PacketClientAspectSlot;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.EffectiveSide;
import thaumicenergistics.util.EssentiaItemContainerHelper;
import thaumicenergistics.util.EssentiaTileContainerHelper;
import thaumicenergistics.util.IInventoryUpdateReceiver;
import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.helpers.IPriorityHost;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartEssentiaStorageBus
	extends AEPartBase
	implements IGridTickable, ICellContainer, IInventoryUpdateReceiver, IAspectSlotPart, IAEAppEngInventory, IPriorityHost
{
	/**
	 * Number of filtered aspects we can have
	 */
	public static final int FILTER_SIZE = 9;

	/**
	 * How much AE power is required to keep the part active.
	 */
	private static final double IDLE_POWER_DRAIN = 1.0D;

	/**
	 * Storage bus priority
	 */
	private int priority = 0;

	/**
	 * The amount in the container last tick
	 */
	private int lastAmountInContainer = 0;

	/**
	 * "Cell' handler for the storage bus
	 */
	private HandlerEssentiaStorageBus handler = new HandlerEssentiaStorageBus( this );

	/**
	 * Filter list
	 */
	private List<Aspect> filteredAspects = new ArrayList<Aspect>( AEPartEssentiaStorageBus.FILTER_SIZE );

	/**
	 * Upgrade inventory
	 */
	private UpgradeInventory upgradeInventory = new UpgradeInventory( this.associatedItem, this, 1 );

	/**
	 * Containers listening for events
	 */
	private List<ContainerPartEssentiaStorageBus> listeners = new ArrayList<ContainerPartEssentiaStorageBus>();

	/**
	 * Creates the bus
	 */
	public AEPartEssentiaStorageBus()
	{
		// Call super
		super( AEPartsEnum.EssentiaStorageBus );

		// Pre-fill the list with nulls
		for( int index = 0; index < AEPartEssentiaStorageBus.FILTER_SIZE; index++ )
		{
			this.filteredAspects.add( null );
		}
	}

	/**
	 * Notifies all listening containers that the filter list changed.
	 */
	private void notifyListenersOfFilteredAspectsChange()
	{
		for( ContainerPartEssentiaStorageBus listner : this.listeners )
		{
			listner.setFilteredAspects( this.filteredAspects );
		}
	}

	/**
	 * Adds a new filter from the specified itemstack.
	 * 
	 * @param player
	 * @param itemStack
	 * @return
	 */
	public boolean addFilteredAspectFromItemstack( EntityPlayer player, ItemStack itemStack )
	{
		// Get the aspect of the item
		Aspect itemAspect = EssentiaItemContainerHelper.getAspectInContainer( itemStack );

		// Is there an aspect?
		if( itemAspect != null )
		{
			// Are we already filtering this aspect?
			if( this.filteredAspects.contains( itemAspect ) )
			{
				return true;
			}

			// Add to the first open slot
			for( int index = 0; index < AEPartEssentiaStorageBus.FILTER_SIZE; index++ )
			{
				// Is this space empty?
				if( this.filteredAspects.get( index ) == null )
				{
					// Set the filter
					this.setAspect( index, itemAspect, player );

					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Adds a listener for events.
	 * 
	 * @param listener
	 */
	public void addListener( ContainerPartEssentiaStorageBus listener )
	{
		if( !this.listeners.contains( listener ) )
		{
			this.listeners.add( listener );
		}
	}

	/**
	 * Ignored
	 */
	@Override
	public void blinkCell( int slot )
	{
	}

	/**
	 * How far out from the cable bus to draw the cable graphic.
	 */
	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
	}

	/**
	 * Hit/Collision boxes.
	 */
	@Override
	public void getBoxes( IPartCollsionHelper helper )
	{
		// Face
		helper.addBox( 1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F );

		// Mid
		helper.addBox( 4.0D, 4.0D, 14.0D, 12.0D, 12.0D, 15.0D );

		// Back
		helper.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );
	}

	/**
	 * Gets the 'cell' handler for the storage bus.
	 */
	@Override
	public List<IMEInventoryHandler> getCellArray( StorageChannel channel )
	{
		// Create a new list
		List<IMEInventoryHandler> list = new ArrayList();

		// Is this the fluid channel?
		if( channel == StorageChannel.FLUIDS )
		{
			// Ensure the handler has been made aware of any containers
			this.handler.onNeighborChange();

			// Add our handler
			list.add( this.handler );
		}

		// Return the list
		return list;

	}

	/**
	 * Returns the client portion of the gui.
	 */
	@Override
	public Object getClientGuiElement( EntityPlayer player )
	{
		return new GuiEssentiaStorageBus( this, player );
	}

	/**
	 * What do we drop when removed from the world.
	 */
	@Override
	public void getDrops( List<ItemStack> drops, boolean wrenched )
	{
		// Were we wrenched?
		if( wrenched )
		{
			// No drops
			return;
		}

		// Get the upgrade card
		ItemStack slotStack = this.upgradeInventory.getStackInSlot( 0 );

		// Is it not null?
		if( ( slotStack != null ) && ( slotStack.stackSize > 0 ) )
		{
			// Add to the drops
			drops.add( slotStack );
		}
	}

	/**
	 * Determines how much power the part takes for just
	 * existing.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return AEPartEssentiaStorageBus.IDLE_POWER_DRAIN;
	}

	/**
	 * Gets the priority for this storage bus.
	 */
	@Override
	public int getPriority()
	{
		return this.priority;
	}

	/**
	 * Gets the server part of the gui.
	 */
	@Override
	public Object getServerGuiElement( EntityPlayer player )
	{
		return new ContainerPartEssentiaStorageBus( this, player );
	}

	/**
	 * Sets how often we would like ticks.
	 */
	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		// We would like a tick ever 20 MC ticks
		return new TickingRequest( 20, 20, false, false );
	}

	/**
	 * Gets the inventory that holds our upgrades.
	 * 
	 * @return
	 */
	public UpgradeInventory getUpgradeInventory()
	{
		return this.upgradeInventory;
	}

	/**
	 * Called when the upgrade inventory changes.
	 */
	@Override
	public void onChangeInventory( IInventory inv, int arg1, InvOperation arg2, ItemStack arg3, ItemStack arg4 )
	{
		this.onInventoryChanged( inv );
	}

	/**
	 * Called when a client is requesting a full update
	 * 
	 * @param player
	 */
	public void onClientRequestFullUpdate( EntityPlayer player )
	{
		// Send the update
		new PacketClientAspectSlot().createFilterListUpdate( this.filteredAspects, player ).sendPacketToPlayer();
	}

	/**
	 * Called to inform the storage bus that the handler has transfered essentia
	 * in/out of the container.
	 * 
	 * @param amountChanged_EU
	 */
	public void onEssentiaTransfered( int amountChanged_EU )
	{
		/* Update the last amount so that on the next tick we don't think
		 * that the amount has changed. 
		 */
		this.lastAmountInContainer += amountChanged_EU;
	}

	/**
	 * Updates the handler on the inverted state.
	 */
	@Override
	public void onInventoryChanged( IInventory sourceInventory )
	{
		this.handler.setInverted( AEApi.instance().materials().materialCardInverter.sameAs( this.upgradeInventory.getStackInSlot( 0 ) ) );
	}

	/**
	 * Updates the grid and handler that a neighbor has changed.
	 */
	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();

		// Ignored client side
		if( EffectiveSide.isClientSide() )
		{
			return;
		}

		this.handler.onNeighborChange();

		if( this.node != null )
		{
			IGrid grid = this.node.getGrid();

			if( grid != null )
			{
				grid.postEvent( new MENetworkCellArrayUpdate() );

				grid.postEvent( new MENetworkStorageEvent( this.gridBlock.getFluidMonitor(), StorageChannel.FLUIDS ) );
			}

			this.host.markForUpdate();
		}
	}

	/**
	 * Reads the part data from NBT
	 */
	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Read the priority
		this.priority = data.getInteger( "priority" );

		// Read the filter list
		for( int index = 0; index < AEPartEssentiaStorageBus.FILTER_SIZE; index++ )
		{
			this.filteredAspects.set( index, Aspect.aspects.get( data.getString( "FilterAspects#" + index ) ) );
		}

		// Read the upgrade inventory
		this.upgradeInventory.readFromNBT( data, "UpgradeInventory" );

		// Update the handler inverted
		this.onInventoryChanged( this.upgradeInventory );

		// Update the handler filter list
		this.handler.setPrioritizedAspects( this.filteredAspects );
	}

	/**
	 * Removes a listener
	 * 
	 * @param listener
	 */
	public void removeListener( ContainerPartEssentiaStorageBus listener )
	{
		this.listeners.remove( listener );
	}

	/**
	 * Renders the storage bus in the player inventory
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( IPartRenderHelper helper, RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.BUS_SIDE.getTexture();
		helper.setTexture( side, side, side, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[0], side, side );

		// Face
		helper.setBounds( 1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		// Mid
		helper.setBounds( 4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F );
		helper.renderInventoryBox( renderer );

		// Color overlay
		helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );
		helper.setInvColor( AEPartBase.INVENTORY_OVERLAY_COLOR );
		ts.setBrightness( 0xF000F0 );
		helper.renderInventoryFace( BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[1], ForgeDirection.SOUTH, renderer );

		// Back
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderInventoryBusLights( helper, renderer );
	}

	/**
	 * Renders the storage bus int he world.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic( int x, int y, int z, IPartRenderHelper helper, RenderBlocks renderer )
	{
		Tessellator tessellator = Tessellator.instance;

		IIcon side = BlockTextureManager.BUS_SIDE.getTexture();
		helper.setTexture( side, side, side, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTexture(), side, side );

		// Front (facing jar)
		helper.setBounds( 1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		tessellator.setColorOpaque_I( this.host.getColor().blackVariant );

		if( this.isActive() )
		{
			tessellator.setBrightness( AEPartBase.ACTIVE_BRIGHTNESS );
		}

		// Mid
		helper.renderFace( x, y, z, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTextures()[1], ForgeDirection.SOUTH, renderer );
		helper.setBounds( 4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F );
		helper.renderBlock( x, y, z, renderer );

		// Back (facing bus)
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );
	}

	/**
	 * Ensures the storage bus gets saved.
	 */
	@Override
	public void saveChanges()
	{
		this.host.markForSave();
	}

	/**
	 * Sets one of the filters.
	 */
	@Override
	public void setAspect( int index, Aspect aspect, EntityPlayer player )
	{
		this.filteredAspects.set( index, aspect );

		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Update the handler
			this.handler.setPrioritizedAspects( this.filteredAspects );

			// Update the clients
			this.notifyListenersOfFilteredAspectsChange();
		}
	}

	@Override
	public void setPriority( int priority )
	{
		this.priority = priority;
	}

	/**
	 * Called periodically by AE2. Checks the Thaumcraft container.
	 */
	@Override
	public TickRateModulation tickingRequest( IGridNode node, int TicksSinceLastCall )
	{
		// Do we have a container?
		if( this.facingContainer != null )
		{
			// Check the amount in the container
			int currentAmount = EssentiaTileContainerHelper.getContainerStoredAmount( this.facingContainer );
			// Has the amount changed?
			if( currentAmount != this.lastAmountInContainer )
			{
				// Update
				this.onNeighborChanged();

				// Set the last amount
				this.lastAmountInContainer = currentAmount;
			}
		}

		// Keep chugging along
		return TickRateModulation.SAME;
	}

	/**
	 * Writes the storage busses state to NBT.
	 */
	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );

		data.setInteger( "priority", this.priority );

		for( int index = 0; index < AEPartEssentiaStorageBus.FILTER_SIZE; index++ )
		{
			Aspect aspect = this.filteredAspects.get( index );

			if( aspect != null )
			{
				data.setString( "FilterAspects#" + index, aspect.getTag() );
			}
			else
			{
				data.setString( "FilterAspects#" + index, "" );
			}
		}

		this.upgradeInventory.writeToNBT( data, "UpgradeInventory" );
	}

}
