package thaumicenergistics.common.parts;

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
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.grid.AEPartGridBlock;
import thaumicenergistics.common.registries.AEPartsEnum;
import thaumicenergistics.common.utils.EffectiveSide;
import thaumicenergistics.common.utils.ThEUtils;
import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.*;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ThEPartBase
	implements IPart, IGridHost, IActionHost, IPowerChannelState
{
	private final static String NBT_KEY_OWNER = "Owner";

	protected final static int INVENTORY_OVERLAY_COLOR = AEColor.Black.blackVariant;

	/**
	 * Texture brightness when active.
	 */
	protected final static int ACTIVE_FACE_BRIGHTNESS = 0xD000D0;

	/**
	 * Light level of active terminals.
	 */
	protected final static int ACTIVE_TERMINAL_LIGHT_LEVEL = 9;

	/**
	 * Permission levels required to interact with the part.
	 */
	private final SecurityPermissions[] interactionPermissions;

	/**
	 * The PartHost attached to.
	 */
	private IPartHost host;

	/**
	 * The PartHost tile entity.
	 */
	private TileEntity hostTile;

	/**
	 * Which side of the cable is the part attached to.
	 */
	private ForgeDirection cableSide;

	/**
	 * Is the part powered and connected?
	 */
	private boolean isActive;

	/**
	 * Is the network powered?
	 */
	private boolean isPowered;

	/**
	 * The parts grid node.
	 */
	private IGridNode node;

	/**
	 * The IGridBlock for this part.
	 */
	private AEPartGridBlock gridBlock;

	/**
	 * AE2 player ID for the owner of this part.
	 */
	private int ownerID = -1;

	/**
	 * Is the part receiving a redstone signal.
	 */
	private boolean recevingRedstonePower;

	/**
	 * The item that represents this part.
	 */
	public final ItemStack associatedItem;

	/**
	 * Creates the part.
	 * 
	 * @param associatedPart
	 * @param interactionPermissions
	 * Permissions required to interact with the part.
	 */
	public ThEPartBase( final AEPartsEnum associatedPart, final SecurityPermissions ... interactionPermissions )
	{
		// Set the associated item
		this.associatedItem = associatedPart.getStack();

		// Set clearance
		if( ( interactionPermissions != null ) && ( interactionPermissions.length > 0 ) )
		{
			this.interactionPermissions = interactionPermissions;
		}
		else
		{
			this.interactionPermissions = null;
		}
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

				// Mark the host for an update
				this.host.markForUpdate();
			}
		}

		// Fire the neighbor changed event
		this.onNeighborChanged();
	}

	/**
	 * Checks if the specifies player has clearance for the specified
	 * permission.
	 * 
	 * @param player
	 * @param permission
	 * @return
	 */
	protected boolean doesPlayerHavePermission( final EntityPlayer player, final SecurityPermissions permission )
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
		return sGrid.hasPermission( player, permission );
	}

	/**
	 * Checks if the specifies player has clearance for the specified
	 * permission.
	 * 
	 * @param playerID
	 * @param permission
	 * @return
	 */
	protected boolean doesPlayerHavePermission( final int playerID, final SecurityPermissions permission )
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

		// Get the tile entity we are facing
		return world.getTileEntity( x + this.cableSide.offsetX, y + this.cableSide.offsetY, z + this.cableSide.offsetZ );
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

			// Update state
			this.node.updateState();

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

	@Override
	public IGridNode getActionableNode()
	{
		return this.node;
	}

	@Override
	public abstract void getBoxes( IPartCollisionHelper helper );

	@Override
	public abstract IIcon getBreakingTexture();

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

	/**
	 * Gets the parts grid block.
	 * 
	 * @return
	 */
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

	public final IPartHost getHost()
	{
		return this.host;
	}

	/**
	 * Gets the host tile entity of this part.
	 * 
	 * @return
	 */
	public final TileEntity getHostTile()
	{
		return this.hostTile;
	}

	/**
	 * Determines how much power the part takes for being active.
	 */
	public abstract double getIdlePowerUsage();

	@Override
	public ItemStack getItemStack( final PartItemStack type )
	{
		// Get the itemstack
		ItemStack itemStack = this.associatedItem.copy();

		// Save NBT data if the part was wrenched
		if( type == PartItemStack.Wrench )
		{
			// Create the item tag
			NBTTagCompound itemNBT = new NBTTagCompound();

			// Write the data
			this.writeToNBT( itemNBT, PartItemStack.Wrench );

			// Set the tag
			if( !itemNBT.hasNoTags() )
			{
				itemStack.setTagCompound( itemNBT );
			}
		}

		return itemStack;
	}

	/**
	 * Gets the block light level for this part.
	 */
	@Override
	public abstract int getLightLevel();

	/**
	 * Gets the location of this part.
	 * 
	 * @return
	 */
	public final DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this.hostTile.getWorldObj(), this.hostTile.xCoord, this.hostTile.yCoord, this.hostTile.zCoord );
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

	/**
	 * Checks if the part is active and powered.
	 * 
	 * @return
	 */
	@Override
	public boolean isActive()
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
			else
			{
				this.isActive = false;
			}
		}

		return this.isActive;
	}

	@Override
	public boolean isLadder( final EntityLivingBase entity )
	{
		return false;
	}

	@Override
	public boolean isPowered()
	{
		// Server side?
		if( EffectiveSide.isServerSide() )
		{
			// Get the energy grid
			IEnergyGrid eGrid = this.gridBlock.getEnergyGrid();
			if( eGrid != null )
			{
				this.isPowered = eGrid.isNetworkPowered();
			}
			else
			{
				this.isPowered = false;
			}
		}

		return this.isPowered;
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

	/**
	 * Returns true if the part is receiving any level of redstone power.
	 * 
	 * @return
	 */
	public boolean isReceivingRedstonePower()
	{
		return this.recevingRedstonePower;
	}

	@Override
	public boolean isSolid()
	{
		return false;
	}

	/**
	 * Called the player interact with this part?
	 * 
	 * @param player
	 * @return
	 */
	public final boolean isUseableByPlayer( final EntityPlayer player )
	{
		// Null check host
		if( ( this.hostTile == null ) || ( this.host == null ) )
		{
			return false;
		}

		// Does the host still exist in the world and the player in range of it?
		if( !ThEUtils.canPlayerInteractWith( player, this.hostTile ) )
		{
			return false;
		}

		// Is the part still attached?
		if( this.host.getPart( this.cableSide ) != this )
		{
			return false;
		}

		// Are there any permissions to check?
		if( this.interactionPermissions != null )
		{
			// Get the security grid
			ISecurityGrid sGrid = this.gridBlock.getSecurityGrid();
			if( sGrid == null )
			{
				// Security grid was unaccessible.
				return false;
			}

			// Check each permission
			for( SecurityPermissions perm : this.interactionPermissions )
			{
				if( !sGrid.hasPermission( player, perm ) )
				{
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Marks that the host tile needs to be saved to disk.
	 */
	public final void markForSave()
	{
		// Ensure there is a host
		if( this.host != null )
		{
			// Mark
			this.host.markForSave();
		}
	}

	/**
	 * Marks that clients needs to be updated.
	 */
	public final void markForUpdate()
	{
		if( this.host != null )
		{
			this.host.markForUpdate();
		}
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
			ThEGuiHandler.launchGui( this, player, this.hostTile.getWorldObj(), this.hostTile.xCoord, this.hostTile.yCoord, this.hostTile.zCoord );
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
			this.recevingRedstonePower = world.isBlockIndirectlyGettingPowered( x, y, z );

		}
	}

	@Override
	public final void onPlacement( final EntityPlayer player, final ItemStack held, final ForgeDirection side )
	{
		// Set the owner
		this.ownerID = AEApi.instance().registries().players().getID( player.getGameProfile() );
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
		if( data.hasKey( ThEPartBase.NBT_KEY_OWNER ) )
		{
			this.ownerID = data.getInteger( ThEPartBase.NBT_KEY_OWNER );
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean readFromStream( final ByteBuf stream ) throws IOException
	{
		// Cache old values
		boolean oldActive = this.isActive;
		boolean oldPowered = this.isPowered;

		// Read the new values
		this.isActive = stream.readBoolean();
		this.isPowered = stream.readBoolean();

		// Redraw if they don't match.
		return( ( oldActive != this.isActive ) || ( oldPowered != this.isPowered ) );
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

		IIcon sideTexture = BlockTextureManager.BUS_COLOR.getTextures()[2];

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

		IIcon sideTexture = BlockTextureManager.BUS_COLOR.getTextures()[2];

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

	/**
	 * General call to WriteNBT, assumes a world save. DO NOT call this from a
	 * subclass's writeToNBT method.
	 */
	@Override
	public final void writeToNBT( final NBTTagCompound data )
	{
		// Assume world saving.
		this.writeToNBT( data, PartItemStack.World );
	}

	/**
	 * Saves NBT data specific to the save type.
	 * 
	 * @param data
	 * @param saveType
	 */
	public void writeToNBT( final NBTTagCompound data, final PartItemStack saveType )
	{
		if( saveType == PartItemStack.World )
		{
			// Set the owner ID
			data.setInteger( ThEPartBase.NBT_KEY_OWNER, this.ownerID );
		}

	}

	@Override
	public void writeToStream( final ByteBuf stream ) throws IOException
	{
		// Write active
		stream.writeBoolean( this.isActive() );

		// Write powered
		stream.writeBoolean( this.isPowered() );
	}

}
