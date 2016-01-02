package thaumicenergistics.parts;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.networking.IEssentiaWatcher;
import thaumicenergistics.api.networking.IEssentiaWatcherHost;
import thaumicenergistics.container.ContainerPartEssentiaLevelEmitter;
import thaumicenergistics.grid.IMEEssentiaMonitor;
import thaumicenergistics.gui.GuiEssentiaLevelEmitter;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.network.packet.client.Packet_C_AspectSlot;
import thaumicenergistics.network.packet.client.Packet_C_EssentiaEmitter;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.registries.EnumCache;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartEssentiaLevelEmitter
	extends AbstractAEPartBase
	implements IAspectSlotPart, IEssentiaWatcherHost
{
	/**
	 * How much AE power is required to keep the part active.
	 */
	private static final double IDLE_POWER_DRAIN = 0.3D;

	/**
	 * NBT keys.
	 */
	private static final String NBT_KEY_ASPECT_FILTER = "aspect", NBT_KEY_REDSTONE_MODE = "mode", NBT_KEY_WANTED_AMOUNT = "wantedAmount",
					NBT_KEY_IS_EMITTING = "emitting";

	/**
	 * Default redstone mode the part starts with.
	 */
	private static final RedstoneMode DEFAULT_REDSTONE_MODE = RedstoneMode.HIGH_SIGNAL;

	/**
	 * Aspect we are watching.
	 */
	private Aspect trackedEssentia;

	/**
	 * Mode the emitter is in
	 */
	private RedstoneMode redstoneMode = AEPartEssentiaLevelEmitter.DEFAULT_REDSTONE_MODE;

	/**
	 * Threshold value
	 */
	private long wantedAmount = 0;

	/**
	 * Current value
	 */
	private long currentAmount;

	/**
	 * True if the emitter is emitting a redstone signal.
	 */
	private boolean isEmitting = false;

	/**
	 * Watches the for changes in the essentia grid.
	 */
	private IEssentiaWatcher essentiaWatcher;

	/**
	 * Creates the part
	 */
	public AEPartEssentiaLevelEmitter()
	{
		super( AEPartsEnum.EssentiaLevelEmitter );
	}

	/**
	 * Updates the watcher to the tracked essentia.
	 */
	private void configureWatcher()
	{
		boolean didSet = false;

		//this.debugTrackingStage = 0;
		//this.debugMonID = -1;

		if( this.essentiaWatcher != null )
		{
			//this.debugTrackingStage = 1;

			// Clear any existing watched value
			this.essentiaWatcher.clear();

			// Is there an essentia being tracked?
			if( this.trackedEssentia != null )
			{
				//this.debugTrackingStage = 2;
				// Configure the watcher
				this.essentiaWatcher.add( this.trackedEssentia );

				// Get the essentia monitor
				IMEEssentiaMonitor essMon = this.getGridBlock().getEssentiaMonitor();

				// Ensure there is a grid.
				if( essMon != null )
				{
					// Update the amount.
					this.setCurrentAmount( essMon.getEssentiaAmount( this.trackedEssentia ) );
					didSet = true;
					//this.debugTrackingStage = 3;
					//this.debugMonID = essMon.hashCode();

				}
			}
		}

		// Was the current amount set?
		if( !didSet )
		{
			// Reset
			this.setCurrentAmount( 0 );
		}
	}

	/**
	 * Sets the current amount in the network, of the aspect
	 * we are watching/filtering.
	 * 
	 * @param amount
	 */
	private void setCurrentAmount( final long amount )
	{
		// Has the amount changed?
		if( amount != this.currentAmount )
		{
			// Set the current amount
			this.currentAmount = amount;

			// Check if we should be emitting
			this.updateEmittingState();
		}
	}

	/**
	 * Checks if the emitter should be emitting a redstone signal.
	 * 
	 * @return
	 */
	private void updateEmittingState()
	{
		boolean emitting = false;

		// Are we active?
		if( !this.isActive() )
		{
			// In the event that we have lost activity, do not change state
			return;
		}

		switch ( this.redstoneMode )
		{
		case HIGH_SIGNAL:
			// Is the current amount more than or equal to the wanted amount?
			emitting = ( this.currentAmount >= this.wantedAmount );
			break;

		case LOW_SIGNAL:
			// Is the current amount less than the wanted amount?
			emitting = ( this.currentAmount < this.wantedAmount );
			break;

		case IGNORE:
		case SIGNAL_PULSE:
			break;

		}

		// Did the emitting state change?
		if( emitting != this.isEmitting )
		{
			// Set the new state
			this.isEmitting = emitting;

			// Mark that we need to be saved and updated
			this.markForSave();
			this.markForUpdate();

			// Get the host tile entity & side
			TileEntity hte = this.getHostTile();
			ForgeDirection side = this.getSide();

			// Update the neighbors
			Platform.notifyBlocksOfNeighbors( hte.getWorldObj(), hte.xCoord, hte.yCoord, hte.zCoord );
			Platform.notifyBlocksOfNeighbors( hte.getWorldObj(), hte.xCoord + side.offsetX, hte.yCoord + side.offsetX, hte.zCoord + side.offsetX );
		}
	}

	/**
	 * How far the network cable should extend to meet us.
	 */
	@Override
	public int cableConnectionRenderTo()
	{
		return 8;
	}

	/**
	 * Checks if the specified player can open the gui.
	 */
	@Override
	public boolean doesPlayerHavePermissionToOpenGui( final EntityPlayer player )
	{
		// Does the player have build permissions
		return this.doesPlayerHavePermission( player, SecurityPermissions.BUILD );
	}

	@Override
	public void getBoxes( final IPartCollisionHelper helper )
	{
		helper.addBox( 7.0D, 7.0D, 11.0D, 9.0D, 9.0D, 16.0D );
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[1];
	}

	/**
	 * Gets the emitter gui
	 */
	@Override
	public Object getClientGuiElement( final EntityPlayer player )
	{
		return new GuiEssentiaLevelEmitter( this, player );
	}

	@Override
	public double getIdlePowerUsage()
	{
		return AEPartEssentiaLevelEmitter.IDLE_POWER_DRAIN;
	}

	/**
	 * Light level based on if emitter is emitting.
	 */
	@Override
	public int getLightLevel()
	{
		return( this.isEmitting ? 7 : 0 );
	}

	/**
	 * Gets the emitter container
	 */
	@Override
	public Object getServerGuiElement( final EntityPlayer player )
	{
		return new ContainerPartEssentiaLevelEmitter( this, player );
	}

	/**
	 * Called to see if this is emitting strong redstone power
	 */
	@Override
	public int isProvidingStrongPower()
	{
		return this.isEmitting ? 15 : 0;
	}

	/**
	 * Called to see if this is emitting weak redstone power
	 */
	@Override
	public int isProvidingWeakPower()
	{
		return this.isProvidingStrongPower();
	}

	/**
	 * Called when a player has adjusted the amount wanted via
	 * gui buttons.
	 * 
	 * @param adjustmentAmount
	 * @param player
	 */
	public void onClientAdjustWantedAmount( final int adjustmentAmount, final EntityPlayer player )
	{
		this.onClientSetWantedAmount( this.wantedAmount + adjustmentAmount, player );
	}

	/**
	 * Called when a player has changed the wanted amount
	 * 
	 * @param wantedAmount
	 * @param player
	 */
	public void onClientSetWantedAmount( final long wantedAmount, final EntityPlayer player )
	{
		// Set the wanted amount
		this.wantedAmount = wantedAmount;

		// Bounds check it
		if( this.wantedAmount < 0L )
		{
			this.wantedAmount = 0L;
		}
		else if( this.wantedAmount > 9999999999L )
		{
			this.wantedAmount = 9999999999L;
		}

		// Mark that we need saving
		this.markForSave();

		// Send validated amount back to the client
		Packet_C_EssentiaEmitter.setWantedAmount( this.wantedAmount, player );

		// Check if we should be emitting
		this.updateEmittingState();
	}

	/**
	 * Called when a player has pressed the redstone toggle button in the gui.
	 * 
	 * @param player
	 */
	public void onClientToggleRedstoneMode( final EntityPlayer player )
	{
		switch ( this.redstoneMode )
		{
		case HIGH_SIGNAL:
			this.redstoneMode = RedstoneMode.LOW_SIGNAL;
			break;

		case LOW_SIGNAL:
			this.redstoneMode = RedstoneMode.HIGH_SIGNAL;
			break;

		case IGNORE:
		case SIGNAL_PULSE:
			break;
		}

		// Check if we should be emitting
		this.updateEmittingState();

		// Send the new mode to the client
		Packet_C_EssentiaEmitter.sendRedstoneMode( this.redstoneMode, player );

	}

	/**
	 * Called when the a client has requested a full update.
	 * 
	 * @param player
	 */
	public void onClientUpdateRequest( final EntityPlayer player )
	{
		// Send the full update to the client
		Packet_C_EssentiaEmitter.sendEmitterState( this.redstoneMode, this.wantedAmount, player );

		// Send the filter to the client
		List<Aspect> filter = new ArrayList<Aspect>();
		filter.add( this.trackedEssentia );
		Packet_C_AspectSlot.setFilterList( filter, player );
	}

	/**
	 * Called when essentia levels change.
	 */
	@Override
	public void onEssentiaChange( final Aspect aspect, final long storedAmount, final long changeAmount )
	{
		this.setCurrentAmount( storedAmount );
	}

	/**
	 * Spawns redstone particles when emitting
	 */
	@Override
	public void randomDisplayTick( final World world, final int x, final int y, final int z, final Random r )
	{
		// Is the emitter, emitting?
		if( this.isEmitting )
		{
			// Get the side
			ForgeDirection side = this.getSide();

			// Calculate a new random coordinate
			double particleX = ( side.offsetX * 0.45F ) + ( ( r.nextFloat() - 0.5F ) * 0.2D );
			double particleY = ( side.offsetY * 0.45F ) + ( ( r.nextFloat() - 0.5F ) * 0.2D );
			double particleZ = ( side.offsetZ * 0.45F ) + ( ( r.nextFloat() - 0.5F ) * 0.2D );

			world.spawnParticle( "reddust", 0.5D + x + particleX, 0.5D + y + particleY, 0.5D + z + particleZ, 0.0D, 0.0D, 0.0D );
		}
	}

	/**
	 * Reads the state of the emitter from an NBT tag
	 */
	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Read the filter
		if( data.hasKey( AEPartEssentiaLevelEmitter.NBT_KEY_ASPECT_FILTER ) )
		{
			this.trackedEssentia = Aspect.aspects.get( data.getString( AEPartEssentiaLevelEmitter.NBT_KEY_ASPECT_FILTER ) );
		}

		// Read the redstone mode
		if( data.hasKey( AEPartEssentiaLevelEmitter.NBT_KEY_REDSTONE_MODE ) )
		{
			this.redstoneMode = EnumCache.AE_REDSTONE_MODES[data.getInteger( AEPartEssentiaLevelEmitter.NBT_KEY_REDSTONE_MODE )];
		}

		// Read the wanted amount
		if( data.hasKey( AEPartEssentiaLevelEmitter.NBT_KEY_WANTED_AMOUNT ) )
		{
			this.wantedAmount = data.getLong( AEPartEssentiaLevelEmitter.NBT_KEY_WANTED_AMOUNT );
		}

		// Read if emitting
		if( data.hasKey( AEPartEssentiaLevelEmitter.NBT_KEY_IS_EMITTING ) )
		{
			this.isEmitting = data.getBoolean( AEPartEssentiaLevelEmitter.NBT_KEY_IS_EMITTING );
		}
	}

	/**
	 * Called client side when a sync packet has been received.
	 * 
	 * @throws IOException
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public boolean readFromStream( final ByteBuf stream ) throws IOException
	{
		boolean redraw = false;

		// Cache the old emitting
		boolean oldEmit = this.isEmitting;

		// Call super
		redraw |= super.readFromStream( stream );

		// Read the activity state
		this.isEmitting = stream.readBoolean();

		// Redraw if changed
		redraw |= ( this.isEmitting != oldEmit );

		return redraw;
	}

	/**
	 * Renders the emitter in the inventory
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		// Set the base texture
		helper.setTexture( BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[0] );
		helper.setBounds( 7.0F, 1.0F, 14.0F, 9.0F, 7.0F, 16.0F );
		//helper.setBounds( 7.0F, 7.0F, 11.0F, 9.0F, 9.0F, 14.0F );
		helper.renderInventoryBox( renderer );

		// Set the active texture
		helper.setTexture( BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[1] );
		helper.setBounds( 7.0F, 7.0F, 14.0F, 9.0F, 9.0F, 16.0F );
		helper.renderInventoryBox( renderer );

	}

	/**
	 * Renders the emitter in the world
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		// Set the base texture
		helper.setTexture( BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[0] );
		helper.setBounds( 7.0F, 7.0F, 11.0F, 9.0F, 9.0F, 14.0F );
		helper.renderBlock( x, y, z, renderer );

		// Is the part emitting?
		if( this.isEmitting )
		{
			// Set the active texture
			helper.setTexture( BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[1] );

			// Set the brightness
			Tessellator.instance.setBrightness( AbstractAEPartBase.ACTIVE_FACE_BRIGHTNESS );

		}
		else
		{
			// Set the inactive texture
			helper.setTexture( BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[2] );
		}

		// Set shaft bounds
		helper.setBounds( 7.0F, 7.0F, 14.0F, 9.0F, 9.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );
	}

	/**
	 * Set's the aspect we are filtering
	 */
	@Override
	public void setAspect( final int index, final Aspect aspect, final EntityPlayer player )
	{
		if( this.trackedEssentia != aspect )
		{
			// Set the filtered aspect
			this.trackedEssentia = aspect;

			// Are we client side?
			if( EffectiveSide.isClientSide() )
			{
				return;
			}

			// Mark that we need to be saved and updated
			this.markForSave();

			// Update the watcher
			this.configureWatcher();

			// Send the aspect to the client
			List<Aspect> filter = new ArrayList<Aspect>();
			filter.add( aspect );
			Packet_C_AspectSlot.setFilterList( filter, player );
		}
	}

	/**
	 * Called from the container to set the filter based on an itemstack.
	 * 
	 * @param player
	 * @param itemStack
	 * @return
	 */
	public boolean setFilteredAspectFromItemstack( final EntityPlayer player, final ItemStack itemStack )
	{
		// Get the aspect
		Aspect itemAspect = EssentiaItemContainerHelper.INSTANCE.getFilterAspectFromItem( itemStack );

		// Ensure we got an aspect
		if( itemAspect == null )
		{
			return false;
		}

		// Set the aspect
		this.setAspect( 0, itemAspect, player );

		return true;
	}

	@Override
	public void updateWatcher( final IEssentiaWatcher newWatcher )
	{
		// Set the watcher
		this.essentiaWatcher = newWatcher;

		// And configure it
		this.configureWatcher();
	}

	/**
	 * Writes the state of the emitter to the tag
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data, final PartItemStack saveType )
	{
		// Call super
		super.writeToNBT( data, saveType );

		// Do we have a filter?
		if( this.trackedEssentia != null )
		{
			// Write the name of the aspect
			data.setString( AEPartEssentiaLevelEmitter.NBT_KEY_ASPECT_FILTER, this.trackedEssentia.getTag() );
		}

		// Write the redstone mode ordinal
		if( this.redstoneMode != AEPartEssentiaLevelEmitter.DEFAULT_REDSTONE_MODE )
		{
			data.setInteger( AEPartEssentiaLevelEmitter.NBT_KEY_REDSTONE_MODE, this.redstoneMode.ordinal() );
		}

		// Write the threshold amount
		if( this.wantedAmount > 0 )
		{
			data.setLong( AEPartEssentiaLevelEmitter.NBT_KEY_WANTED_AMOUNT, this.wantedAmount );
		}

		// Write if emitting
		if( ( saveType != PartItemStack.Wrench ) && this.isEmitting )
		{
			data.setBoolean( AEPartEssentiaLevelEmitter.NBT_KEY_IS_EMITTING, true );
		}
	}

	/**
	 * Called when a packet to sync client and server is being created.
	 * 
	 * @throws IOException
	 */
	@Override
	public void writeToStream( final ByteBuf stream ) throws IOException
	{
		// Call super
		super.writeToStream( stream );

		// Write the activity state
		stream.writeBoolean( this.isEmitting );
	}
}
