package thaumicenergistics.parts;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.grid.AEPartGridBlock;
import thaumicenergistics.gui.GuiHandler;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.registries.ItemEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.core.WorldSettings;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class AbstractAEPartBase
	implements IPart, IGridHost, IActionHost
{
	private final static String NBT_KEY_OWNER = "Owner";

	protected final static int INVENTORY_OVERLAY_COLOR = AEColor.Black.blackVariant;

	protected final static int ACTIVE_BRIGHTNESS = 0xD000D0;

	protected ForgeDirection cableSide;

	protected IGridNode node;

	protected IPartHost host;

	protected boolean isActive;

	protected AEPartGridBlock gridBlock;

	protected TileEntity tile;

	protected TileEntity hostTile;

	protected boolean redstonePowered;

	/**
	 * AE2 player ID for the owner of this part.
	 */
	protected int ownerID = -1;

	public final ItemStack associatedItem;

	public AbstractAEPartBase( final AEPartsEnum associatedPart )
	{
		// Set the associated item 
		this.associatedItem = ItemEnum.PART_ITEM.getItemStackWithDamage( associatedPart.ordinal() );
	}

	private void updateStatus()
	{
		// Ignored client side
		if( EffectiveSide.isClientSide() )
		{
			return;
		}

		// Do we have a node?
		if( this.node != null )
		{
			// Get the active state
			boolean currentlyActive = this.node.isActive();

			// Has that state changed?
			if( currentlyActive != this.isActive )
			{
				// Set our active state
				this.isActive = currentlyActive;

				// Fire the neighbor changed event
				this.onNeighborChanged();

				// Mark the host for an update
				this.host.markForUpdate();
			}
		}
	}

	/**
	 * Checks with the subclass if a player can open this parts gui.
	 * 
	 * @return
	 */
	protected abstract boolean canPlayerOpenGui( int playerID );

	/**
	 * Checks if the specifies player has clearance for the specified
	 * permission.
	 * 
	 * @param player
	 * @param permission
	 * @return
	 */
	protected boolean doesPlayerHaveSecurityClearance( final int playerID, final SecurityPermissions permission )
	{
		// Get the security grid
		ISecurityGrid sGrid = this.gridBlock.getSecurityGrid();

		// Did we get the grid?
		if( sGrid == null )
		{
			// No security grid to check against.
			return false;
		}

		// Return the permission
		return sGrid.hasPermission( playerID, permission );
	}

	/**
	 * Gets the tile entity the part is facing, if any.
	 * 
	 * @return
	 */
	protected TileEntity getFacingTile()
	{
		// Get the world
		World world = this.hostTile.getWorldObj();

		// Get our location
		int x = this.hostTile.xCoord;
		int y = this.hostTile.yCoord;
		int z = this.hostTile.zCoord;

		// Get the tile entity we are connected to
		return world.getTileEntity( x + this.cableSide.offsetX, y + this.cableSide.offsetY, z + this.cableSide.offsetZ );
	}

	/**
	 * Checks if the part is active and powered.
	 * 
	 * @return
	 */
	protected boolean isActive()
	{
		// Are we server side?
		if( EffectiveSide.isServerSide() )
		{
			// Do we have a node?
			if( this.node != null )
			{
				// Get it's activity
				this.isActive = this.node.isActive();
			}
		}

		return this.isActive;
	}

	/**
	 * Called when the part is added to the world.
	 */
	@Override
	public void addToWorld()
	{
		// Ignored on client side
		if( FMLCommonHandler.instance().getEffectiveSide().isServer() )
		{
			// Create the grid block
			this.gridBlock = new AEPartGridBlock( this );

			// Create the node
			this.node = AEApi.instance().createGridNode( this.gridBlock );

			// Set the player id
			this.node.setPlayerID( this.ownerID );

			this.setPower( null );
		}
	}

	@Override
	public abstract int cableConnectionRenderTo();

	@Override
	public boolean canBePlacedOn( final BusSupport type )
	{
		// Can not be placed on dense cable
		return type != BusSupport.DENSE_CABLE;
	}

	@Override
	public boolean canConnectRedstone()
	{
		return false;
	}

	/**
	 * Called before a gui is shown to ensure a player has permission to do so.
	 * 
	 * @param player
	 * @return
	 */
	public final boolean doesPlayerHavePermissionToOpenGui( final EntityPlayer player )
	{
		// Get the player ID
		int pID = WorldSettings.getInstance().getPlayerID( player.getGameProfile() );

		// Is this the owner?
		if( pID == this.ownerID )
		{
			// Owner can always interact
			return true;
		}

		// Ensure they have build(make configuration changes) permission.
		return this.canPlayerOpenGui( pID );
	}

	@Override
	public IGridNode getActionableNode()
	{
		return this.node;
	}

	@Override
	public abstract void getBoxes( IPartCollisionHelper helper );

	@Override
	public IIcon getBreakingTexture()
	{
		return BlockTextureManager.BUS_SIDE.getTexture();
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.SMART;
	}

	public Object getClientGuiElement( final EntityPlayer player )
	{
		return null;
	}

	@Override
	public void getDrops( final List<ItemStack> drops, final boolean wrenched )
	{
	}

	@Override
	public final IGridNode getExternalFacingNode()
	{
		return null;
	}

	public AEPartGridBlock getGridBlock()
	{
		return this.gridBlock;
	}

	@Override
	public IGridNode getGridNode()
	{
		return this.node;
	}

	@Override
	public IGridNode getGridNode( final ForgeDirection direction )
	{
		return this.node;
	}

	public IPartHost getHost()
	{
		return this.host;
	}

	/**
	 * Get the host tile of this part.
	 * 
	 * @return
	 */
	public final TileEntity getHostTile()
	{
		return this.hostTile;
	}

	/**
	 * Determines how much power the part takes for just
	 * existing.
	 */
	public abstract double getIdlePowerUsage();

	/**
	 * Gets an itemstack that represents the specified part.
	 */
	@Override
	public ItemStack getItemStack( final PartItemStack partItemStack )
	{
		// Get the itemstack
		ItemStack itemStack = new ItemStack( ItemEnum.PART_ITEM.getItem(), 1, AEPartsEnum.getPartID( this.getClass() ) );

		if( partItemStack != PartItemStack.Break )
		{
			NBTTagCompound itemNBT = new NBTTagCompound();
			this.writeToNBT( itemNBT );
			itemStack.setTagCompound( itemNBT );
		}

		return itemStack;
	}

	/**
	 * Gets the block light level for this part.
	 */
	@Override
	public int getLightLevel()
	{
		return( this.isActive() ? 15 : 0 );
	}

	/**
	 * Gets the location of this part.
	 * 
	 * @return
	 */
	public final DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this.tile.getWorldObj(), this.tile.xCoord, this.tile.yCoord, this.tile.zCoord );
	}

	public Object getServerGuiElement( final EntityPlayer player )
	{
		return null;
	}

	/**
	 * Gets the side of the host that this part is attached to.
	 * 
	 * @return
	 */
	public ForgeDirection getSide()
	{
		return this.cableSide;
	}

	/**
	 * Gets the unlocalized name of this part.
	 * 
	 * @return
	 */
	public String getUnlocalizedName()
	{
		return this.associatedItem.getUnlocalizedName() + ".name";
	}

	@Override
	public boolean isLadder( final EntityLivingBase entity )
	{
		return false;
	}

	@Override
	public int isProvidingStrongPower()
	{
		return 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return 0;
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	@Override
	public boolean onActivate( final EntityPlayer player, final Vec3 position )
	{
		// Is the player sneaking?
		if( player.isSneaking() )
		{
			return false;
		}

		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Launch the gui
			GuiHandler.launchGui( this, player, this.hostTile.getWorldObj(), this.hostTile.xCoord, this.hostTile.yCoord, this.hostTile.zCoord );
		}

		return true;
	}

	@Override
	public void onEntityCollision( final Entity entity )
	{
	}

	@Override
	public void onNeighborChanged()
	{
		if( this.hostTile != null )
		{
			// Get the world
			World world = this.hostTile.getWorldObj();

			// Get our location
			int x = this.hostTile.xCoord;
			int y = this.hostTile.yCoord;
			int z = this.hostTile.zCoord;

			// Check redstone state
			this.redstonePowered = world.isBlockIndirectlyGettingPowered( x, y, z );

		}
	}

	@Override
	public final void onPlacement( final EntityPlayer player, final ItemStack held, final ForgeDirection side )
	{
		// Set the owner
		this.ownerID = WorldSettings.getInstance().getPlayerID( player.getGameProfile() );
	}

	@Override
	public boolean onShiftActivate( final EntityPlayer player, final Vec3 position )
	{
		return false;
	}

	@Override
	public void randomDisplayTick( final World world, final int x, final int y, final int z, final Random r )
	{
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Read the owner
		if( data.hasKey( AbstractAEPartBase.NBT_KEY_OWNER ) )
		{
			this.ownerID = data.getInteger( AbstractAEPartBase.NBT_KEY_OWNER );
		}
	}

	@Override
	public boolean readFromStream( final ByteBuf data ) throws IOException
	{
		this.isActive = data.readBoolean();
		return true;
	}

	@Override
	public void removeFromWorld()
	{
		if( this.node != null )
		{
			this.node.destroy();
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderDynamic( final double x, final double y, final double z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		// Ignored
	}

	@SideOnly(Side.CLIENT)
	@Override
	public abstract void renderInventory( IPartRenderHelper helper, RenderBlocks renderer );

	@SideOnly(Side.CLIENT)
	public void renderInventoryBusLights( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		// Set color to white
		helper.setInvColor( 0xFFFFFF );

		IIcon busColorTexture = BlockTextureManager.BUS_COLOR.getTextures()[0];

		IIcon sideTexture = BlockTextureManager.BUS_SIDE.getTexture();

		helper.setTexture( busColorTexture, busColorTexture, sideTexture, sideTexture, busColorTexture, busColorTexture );

		// Rend the box
		helper.renderInventoryBox( renderer );

		// Set the brightness
		Tessellator.instance.setBrightness( 0xD000D0 );

		helper.setInvColor( AEColor.Transparent.blackVariant );

		IIcon lightTexture = BlockTextureManager.BUS_COLOR.getTextures()[1];

		// Render the lights
		helper.renderInventoryFace( lightTexture, ForgeDirection.UP, renderer );
		helper.renderInventoryFace( lightTexture, ForgeDirection.DOWN, renderer );
		helper.renderInventoryFace( lightTexture, ForgeDirection.NORTH, renderer );
		helper.renderInventoryFace( lightTexture, ForgeDirection.EAST, renderer );
		helper.renderInventoryFace( lightTexture, ForgeDirection.SOUTH, renderer );
		helper.renderInventoryFace( lightTexture, ForgeDirection.WEST, renderer );
	}

	@SideOnly(Side.CLIENT)
	@Override
	public abstract void renderStatic( int x, int y, int z, IPartRenderHelper helper, RenderBlocks renderer );

	@SideOnly(Side.CLIENT)
	public void renderStaticBusLights( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		IIcon busColorTexture = BlockTextureManager.BUS_COLOR.getTextures()[0];

		IIcon sideTexture = BlockTextureManager.BUS_SIDE.getTexture();

		helper.setTexture( busColorTexture, busColorTexture, sideTexture, sideTexture, busColorTexture, busColorTexture );

		// Render the box
		helper.renderBlock( x, y, z, renderer );

		// Are we active?
		if( this.isActive() )
		{
			// Set the brightness
			Tessellator.instance.setBrightness( 0xD000D0 );

			// Set the color to match the cable
			Tessellator.instance.setColorOpaque_I( this.host.getColor().blackVariant );
		}
		else
		{
			// Set the color to black
			Tessellator.instance.setColorOpaque_I( 0 );
		}

		IIcon lightTexture = BlockTextureManager.BUS_COLOR.getTextures()[1];

		// Render the lights
		helper.renderFace( x, y, z, lightTexture, ForgeDirection.UP, renderer );
		helper.renderFace( x, y, z, lightTexture, ForgeDirection.DOWN, renderer );
		helper.renderFace( x, y, z, lightTexture, ForgeDirection.NORTH, renderer );
		helper.renderFace( x, y, z, lightTexture, ForgeDirection.EAST, renderer );
		helper.renderFace( x, y, z, lightTexture, ForgeDirection.SOUTH, renderer );
		helper.renderFace( x, y, z, lightTexture, ForgeDirection.WEST, renderer );
	}

	@Override
	public boolean requireDynamicRender()
	{
		return false;
	}

	@Override
	public void securityBreak()
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();

		// Get this item
		drops.add( this.getItemStack( null ) );

		// Get the drops for this part
		this.getDrops( drops, false );

		// Drop it
		appeng.util.Platform.spawnDrops( this.hostTile.getWorldObj(), this.hostTile.xCoord, this.hostTile.yCoord, this.hostTile.zCoord, drops );

		// Remove the part
		this.host.removePart( this.cableSide, false );

	}

	@Override
	public final void setPartHostInfo( final ForgeDirection side, final IPartHost host, final TileEntity tile )
	{
		this.cableSide = side;

		this.host = host;

		this.tile = tile;

		this.hostTile = tile;

	}

	@MENetworkEventSubscribe
	public final void setPower( final MENetworkPowerStatusChange event )
	{
		this.updateStatus();

		this.host.markForUpdate();
	}

	/**
	 * Setup the part based on the passed item.
	 * 
	 * @param itemPart
	 */
	public void setupPartFromItem( final ItemStack itemPart )
	{
		if( itemPart.hasTagCompound() )
		{
			this.readFromNBT( itemPart.getTagCompound() );
		}
	}

	@MENetworkEventSubscribe
	public void updateChannels( final MENetworkChannelsChanged event )
	{
		this.updateStatus();
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		// Set the owner ID
		data.setInteger( AbstractAEPartBase.NBT_KEY_OWNER, this.ownerID );
	}

	@Override
	public void writeToStream( final ByteBuf data ) throws IOException
	{
		data.writeBoolean( ( this.node != null ) && ( this.node.isActive() ) );
	}

}
