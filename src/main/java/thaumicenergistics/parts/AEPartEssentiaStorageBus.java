package thaumicenergistics.parts;

import java.util.ArrayList;
import java.util.Arrays;
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
import thaumicenergistics.render.BlockTextureManager;
import thaumicenergistics.util.IInventoryUpdateReceiver;
import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkStorageEvent;
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

//TODO: Priority Window
public class AEPartEssentiaStorageBus extends AEPartBase implements ICellContainer, IInventoryUpdateReceiver, IAspectSlotPart, IAEAppEngInventory
{
	private int priority = 0;

	private HandlerEssentiaStorageBus handler = new HandlerEssentiaStorageBus( this );
	private Aspect[] filteredAspects = new Aspect[54];
	private UpgradeInventory upgradeInventory = new UpgradeInventory( this.associatedItem, this, 1 );

	public AEPartEssentiaStorageBus()
	{
		super( AEPartsEnum.EssentiaStorageBus );
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
	public void getBoxes( IPartCollsionHelper bch )
	{
		// Face
		bch.addBox( 1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F );
		
		// Mid
		bch.addBox( 4.0D, 4.0D, 14.0D, 12.0D, 12.0D, 15.0D );
		
		// Back
		bch.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );
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

	public UpgradeInventory getUpgradeInventory()
	{
		return this.upgradeInventory;
	}

	@Override
	public void onChangeInventory( IInventory arg0, int arg1, InvOperation arg2, ItemStack arg3, ItemStack arg4 )
	{
		this.onInventoryChanged();
	}

	@Override
	public void onInventoryChanged()
	{
		this.handler.setInverted( AEApi.instance().materials().materialCardInverter.sameAs( this.upgradeInventory.getStackInSlot( 0 ) ) );
	}

	@Override
	public void onNeighborChanged()
	{
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

		for( int i = 0; i < this.filteredAspects.length; i++ )
		{
			this.filteredAspects[i] = Aspect.aspects.get( data.getString( "FilterAspects#" + i ) );
		}

		this.upgradeInventory.readFromNBT( data, "UpgradeInventory" );

		this.onInventoryChanged();

		this.onNeighborChanged();
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
		helper.setInvColor( AEPartBase.inventoryOverlayColor );
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
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.BUS_SIDE.getTexture();
		helper.setTexture( side, side, side, BlockTextureManager.ESSENTIA_STORAGE_BUS.getTexture(), side, side );
		
		// Front (facing jar)
		helper.setBounds( 1.0F, 1.0F, 15.0F, 15.0F, 15.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		ts.setColorOpaque_I( this.host.getColor().blackVariant );
		if ( this.isActive() )
		{
			ts.setBrightness( 15728880 );
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
		new PacketAspectSlot( Arrays.asList( this.filteredAspects ) ).sendPacketToPlayer( player );
	}

	@Override
	public void setAspect( int index, Aspect aspect, EntityPlayer player )
	{
		this.filteredAspects[index] = aspect;

		this.handler.setPrioritizedAspects( this.filteredAspects );

		this.sendInformation( player );
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );

		data.setInteger( "priority", this.priority );

		for( int i = 0; i < this.filteredAspects.length; i++ )
		{
			Aspect aspect = this.filteredAspects[i];

			if ( aspect != null )
			{
				data.setString( "FilterAspects#" + i, aspect.getTag() );
			}
			else
			{
				data.setString( "FilterAspects#" + i, "" );
			}
		}

		this.upgradeInventory.writeToNBT( data, "UpgradeInventory" );
	}

}
