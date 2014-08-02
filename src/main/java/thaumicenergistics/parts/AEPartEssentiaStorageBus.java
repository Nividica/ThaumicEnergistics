package thaumicenergistics.parts;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaStorageBus;
import thaumicenergistics.gui.GuiEssentiaStorageBus;
import thaumicenergistics.inventory.HandlerEssentiaStorageBus;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.network.packet.PacketAspectSlot;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.EssentiaItemContainerHelper;
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
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// TODO: Priority Window
public class AEPartEssentiaStorageBus
	extends AEPartBase
	implements ICellContainer, IInventoryUpdateReceiver, IAspectSlotPart, IAEAppEngInventory, IGridTickable
{
	private static final int FILTER_SIZE = 54;
	
	/**
	 * How much AE power is required to keep the part active.
	 */
	private static final double IDLE_POWER_DRAIN = 1.0D;

	private int priority = 0;

	private boolean containerWasEmptyLastTick = false;

	private HandlerEssentiaStorageBus handler = new HandlerEssentiaStorageBus( this );

	private List<Aspect> filteredAspects = new ArrayList<Aspect>( AEPartEssentiaStorageBus.FILTER_SIZE );

	private UpgradeInventory upgradeInventory = new UpgradeInventory( this.associatedItem, this, 1 );

	public AEPartEssentiaStorageBus()
	{
		super( AEPartsEnum.EssentiaStorageBus );

		// Prefill the list
		for( int index = 0; index < AEPartEssentiaStorageBus.FILTER_SIZE; index++ )
		{
			this.filteredAspects.add( null );
		}
	}

	public boolean addFilteredAspectFromItemstack( EntityPlayer player, ItemStack itemStack )
	{
		Aspect itemAspect = EssentiaItemContainerHelper.getAspectInContainer( itemStack );

		if ( itemAspect != null )
		{
			// Are we already filtering this aspect?
			if ( this.filteredAspects.contains( itemAspect ) )
			{
				return true;
			}

			// Add to the first open slot
			for( int index = 0; index < AEPartEssentiaStorageBus.FILTER_SIZE; index++ )
			{
				// Is this space empty?
				if ( this.filteredAspects.get( index ) == null )
				{
					// Set the filter
					this.filteredAspects.set( index, itemAspect );

					// Is this server side?
					if ( !player.worldObj.isRemote )
					{
						// Update the client
						this.sendInformation( player );
					}

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void blinkCell( int slot )
	{
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
	}

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

	@Override
	public List<IMEInventoryHandler> getCellArray( StorageChannel channel )
	{
		List<IMEInventoryHandler> list = new ArrayList();

		if ( channel == StorageChannel.FLUIDS )
		{
			list.add( this.handler );
		}

		return list;

	}

	@Override
	public Object getClientGuiElement( EntityPlayer player )
	{
		return new GuiEssentiaStorageBus( this, player );
	}

	public TileEntity getHostTile()
	{
		return this.hostTile;
	}

	@Override
	public int getPriority()
	{
		return this.priority;
	}

	@Override
	public Object getServerGuiElement( EntityPlayer player )
	{
		return new ContainerPartEssentiaStorageBus( this, player );
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		// We would like a tick ever 20 MC ticks
		return new TickingRequest( 20, 20, false, false );
	}

	public UpgradeInventory getUpgradeInventory()
	{
		return this.upgradeInventory;
	}

	@Override
	public void onChangeInventory( IInventory inv, int arg1, InvOperation arg2, ItemStack arg3, ItemStack arg4 )
	{
		this.onInventoryChanged( inv );
	}

	@Override
	public void onInventoryChanged( IInventory sourceInventory )
	{
		this.handler.setInverted( AEApi.instance().materials().materialCardInverter.sameAs( this.upgradeInventory.getStackInSlot( 0 ) ) );
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();

		this.handler.onNeighborChange();

		if ( this.node != null )
		{
			IGrid grid = this.node.getGrid();

			if ( grid != null )
			{
				grid.postEvent( new MENetworkCellArrayUpdate() );

				grid.postEvent( new MENetworkStorageEvent( this.gridBlock.getFluidMonitor(), StorageChannel.FLUIDS ) );
			}

			this.host.markForUpdate();
		}
	}

	@Override
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );

		this.priority = data.getInteger( "priority" );

		for( int index = 0; index < AEPartEssentiaStorageBus.FILTER_SIZE; index++ )
		{
			this.filteredAspects.set( index, Aspect.aspects.get( data.getString( "FilterAspects#" + index ) ) );
		}

		this.upgradeInventory.readFromNBT( data, "UpgradeInventory" );

		this.onInventoryChanged( this.upgradeInventory );
	}

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
		
		if ( this.isActive() )
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

	@Override
	public void saveChanges()
	{
		this.host.markForSave();
	}

	public void sendInformation( EntityPlayer player )
	{
		new PacketAspectSlot( this.filteredAspects ).sendPacketToPlayer( player );
	}

	@Override
	public void setAspect( int index, Aspect aspect, EntityPlayer player )
	{
		this.filteredAspects.set( index, aspect );

		this.handler.setPrioritizedAspects( this.filteredAspects );

		this.sendInformation( player );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int TicksSinceLastCall )
	{
		// Do we have a container?
		if ( this.facingContainer != null )
		{
			// Check the amount in the container
			int currentAmount = this.facingContainer.getAspects().size();

			// Was the container empty last tick, and it is not now?
			if ( this.containerWasEmptyLastTick && ( currentAmount > 0 ) )
			{
				// Mark that it is not empty
				this.containerWasEmptyLastTick = false;

				// Neighbor has changed
				this.onNeighborChanged();
			}
			// Was the container not empty last tick, and now it is?
			else if ( !this.containerWasEmptyLastTick && ( currentAmount == 0 ) )
			{
				// Mark as empty
				this.containerWasEmptyLastTick = true;
			}
		}

		// Keep chugging along
		return TickRateModulation.SAME;
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );

		data.setInteger( "priority", this.priority );

		for( int index = 0; index < AEPartEssentiaStorageBus.FILTER_SIZE; index++ )
		{
			Aspect aspect = this.filteredAspects.get( index );

			if ( aspect != null )
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

}
