package thaumicenergistics.parts;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.container.ContainerPartEssentiaLevelEmitter;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.gui.GuiEssentiaLevelEmitter;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.network.IAspectSlotPart;
import thaumicenergistics.network.packet.client.PacketClientAspectSlot;
import thaumicenergistics.network.packet.client.PacketClientEssentiaEmitter;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.registries.EnumCache;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartEssentiaLevelEmitter
	extends AbstractAEPartBase
	implements IAspectSlotPart, IMEMonitorHandlerReceiver<IAEFluidStack>
{
	/**
	 * How much AE power is required to keep the part active.
	 */
	private static final double IDLE_POWER_DRAIN = 0.3D;

	/**
	 * NBT key for the aspect filter.
	 */
	private static final String ASPECT_FILTER_NBT_KEY = "aspect";

	/**
	 * NBT key for the redstone mode.
	 */
	private static final String REDSTONE_MODE_NBT_KEY = "mode";

	/**
	 * NBT key for the wanted amount.
	 */
	private static final String WANTED_AMOUNT_NBT_KEY = "wantedAmount";

	/**
	 * NBT key for if we are emitting.
	 */
	private static final String IS_EMITTING_NBT_KEY = "emitting";

	/**
	 * Aspect we are watching.
	 */
	private Aspect filterAspect;

	/**
	 * Mode the emitter is in
	 */
	private RedstoneMode redstoneMode = RedstoneMode.HIGH_SIGNAL;

	/**
	 * Threshold value
	 */
	private long wantedAmount;

	/**
	 * Current value
	 */
	private long currentAmount;

	/**
	 * True if the emitter is emitting a redstone signal.
	 */
	private boolean isEmitting = false;

	/**
	 * Creates the part
	 */
	public AEPartEssentiaLevelEmitter()
	{
		super( AEPartsEnum.EssentiaLevelEmitter );
	}

	/**
	 * Checks if the emitter is emitting a redstone signal.
	 * 
	 * @return
	 */
	private void checkEmitting()
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
				// Is the current amount more than the wanted amount?
				emitting = ( this.currentAmount >= this.wantedAmount );
				break;

			case LOW_SIGNAL:
				// Is the current amount less than the wanted amount?
				emitting = ( this.currentAmount <= this.wantedAmount );
				break;

			case IGNORE:
			case SIGNAL_PULSE:
				break;

		}

		if( emitting != this.isEmitting )
		{
			// Set the new state
			this.isEmitting = emitting;

			// Update the neighbors
			this.markAndNotify();
		}
	}

	/**
	 * Ensures we are, or are not, registered with the
	 * network monitor.
	 */
	private void checkRegistration()
	{
		// Get the storage grid
		IStorageGrid storageGrid = this.gridBlock.getStorageGrid();

		// Ensure we got the grid
		if( storageGrid == null )
		{
			return;
		}

		// Is the filter aspect null?
		if( this.filterAspect == null )
		{
			// Unregister
			storageGrid.getFluidInventory().removeListener( this );
		}
		else
		{
			// Register
			storageGrid.getFluidInventory().addListener( this, this.gridBlock.getGrid() );
		}
	}

	/**
	 * Marks that we are dirty, and that we need to
	 * send an update to the client. Then updates all
	 * neighbor blocks.
	 */
	private void markAndNotify()
	{
		// Mark that we need to be saved and updated
		this.host.markForSave();
		this.host.markForUpdate();

		// Update the neighbors
		this.tile.getWorldObj().notifyBlocksOfNeighborChange( this.tile.xCoord, this.tile.yCoord, this.tile.zCoord, Blocks.air );
		this.tile.getWorldObj().notifyBlocksOfNeighborChange( this.tile.xCoord + this.cableSide.offsetX, this.tile.yCoord + this.cableSide.offsetX,
			this.tile.zCoord + this.cableSide.offsetX, Blocks.air );
	}

	private void onMonitorUpdate( final IMEMonitor<IAEFluidStack> monitor )
	{
		// Do we have a filter?
		if( this.filterAspect == null )
		{
			// Set the current amount to 0
			this.setCurrentAmount( 0 );
		}

		// Get the gas for the filter aspect
		GaseousEssentia aspectGas = GaseousEssentia.getGasFromAspect( this.filterAspect );

		// Is there a fluid form of the aspect?
		if( aspectGas == null )
		{
			// Set the current amount to 0
			this.setCurrentAmount( 0 );
		}

		// Convert to AE fluid stack
		IAEFluidStack asGasStack = EssentiaConversionHelper.instance.createAEFluidStackInFluidUnits( aspectGas, 1 );

		// Get how much is in the system
		IAEFluidStack fluidStack = monitor.getStorageList().findPrecise( asGasStack );

		// Was there any in the system?
		if( fluidStack == null )
		{
			// Set current amount to zero
			this.setCurrentAmount( 0 );
		}
		else
		{
			// Set the current amount
			this.setCurrentAmount( EssentiaConversionHelper.instance.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() ) );
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

			// Mark that we need to save
			this.host.markForSave();

			// Check if we should be emitting
			this.checkEmitting();
		}
	}

	/**
	 * Checks if the specified player can open the gui.
	 */
	@Override
	protected boolean canPlayerOpenGui( final int playerID )
	{
		// Does the player have build permissions
		return this.doesPlayerHaveSecurityClearance( playerID, SecurityPermissions.BUILD );
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
	 * Called when we change channels/grids
	 * 
	 * @param channelEvent
	 */
	@MENetworkEventSubscribe
	public void channelChanged( final MENetworkChannelsChanged channelEvent )
	{
		this.onListUpdate();
		this.checkRegistration();
		this.checkEmitting();
	}

	/**
	 * Collision boxes
	 */
	@Override
	public void getBoxes( final IPartCollisionHelper helper )
	{
		helper.addBox( 7.0D, 7.0D, 11.0D, 9.0D, 9.0D, 16.0D );
	}

	/**
	 * Gets the emitter gui
	 */
	@Override
	public Object getClientGuiElement( final EntityPlayer player )
	{
		return new GuiEssentiaLevelEmitter( this, player );
	}

	/**
	 * Determines how much power the part takes for just
	 * existing.
	 */
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
	 * Called to see if we should continue monitoring on
	 * a specific grid.
	 */
	@Override
	public boolean isValid( final Object token )
	{
		// Get the grid
		IGrid grid = this.gridBlock.getGrid();

		if( grid != null )
		{
			return grid == token;
		}

		return false;
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
		this.host.markForSave();

		// Send validated amount back to the client
		new PacketClientEssentiaEmitter().createWantedAmountUpdate( this.wantedAmount, player ).sendPacketToPlayer();

		// Check if we should be emitting
		this.checkEmitting();
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
		this.checkEmitting();

		// Send the new mode to the client
		new PacketClientEssentiaEmitter().createRedstoneModeUpdate( this.redstoneMode, player ).sendPacketToPlayer();

	}

	/**
	 * Called when the a client has requested a full update.
	 * 
	 * @param player
	 */
	public void onClientUpdateRequest( final EntityPlayer player )
	{
		// Send the full update to the client
		new PacketClientEssentiaEmitter().createFullUpdate( this.redstoneMode, this.wantedAmount, player ).sendPacketToPlayer();

		// Send the filter to the client
		List<Aspect> filter = new ArrayList<Aspect>();
		filter.add( this.filterAspect );
		new PacketClientAspectSlot().createFilterListUpdate( filter, player ).sendPacketToPlayer();
	}

	/**
	 * Called when a network event changes the state of
	 * the storage grid.
	 */
	@Override
	public void onListUpdate()
	{
		// Ensure we have a filter
		if( this.filterAspect == null )
		{
			return;
		}

		// Get the storage grid
		IStorageGrid sGrid = this.gridBlock.getStorageGrid();

		// Did we get the grid?
		if( sGrid != null )
		{
			this.onMonitorUpdate( sGrid.getFluidInventory() );
		}

	}

	/**
	 * Called when a network event changes the amount
	 * of something in the storage grid.
	 */
	@Override
	public void postChange( final IBaseMonitor<IAEFluidStack> monitor, final Iterable<IAEFluidStack> changes, final BaseActionSource source )
	{
		// Ensure we have a filter
		if( this.filterAspect == null )
		{
			return;
		}

		// Ensure there was a change
		if( changes == null )
		{
			return;
		}

		// Ensure one of the changes an essentia gas
		for( IAEFluidStack change : changes )
		{
			if( ( change.getFluid() instanceof GaseousEssentia ) )
			{
				// Propagate the update
				this.onMonitorUpdate( (IMEMonitor<IAEFluidStack>)monitor );

				// Stop searching
				break;
			}
		}
	}

	/**
	 * Called when a AE power event occurs
	 * 
	 * @param powerEvent
	 */
	@MENetworkEventSubscribe
	public void powerChanged( final MENetworkPowerStatusChange powerEvent )
	{
		this.onListUpdate();
		this.checkRegistration();
		this.checkEmitting();
	}

	/**
	 * Spawns redstone particles when emitting
	 */
	@Override
	public void randomDisplayTick( final World world, final int x, final int y, final int z, final Random r )
	{
		// Are we emitting?
		if( this.isEmitting )
		{
			// Calculate a new random coordinate
			double particleX = ( this.cableSide.offsetX * 0.45F ) + ( ( r.nextFloat() - 0.5F ) * 0.2D );
			double particleY = ( this.cableSide.offsetY * 0.45F ) + ( ( r.nextFloat() - 0.5F ) * 0.2D );
			double particleZ = ( this.cableSide.offsetZ * 0.45F ) + ( ( r.nextFloat() - 0.5F ) * 0.2D );

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
		this.filterAspect = Aspect.aspects.get( data.getString( AEPartEssentiaLevelEmitter.ASPECT_FILTER_NBT_KEY ) );

		// Read the redstone mode
		this.redstoneMode = EnumCache.AE_REDSTONE_MODES[data.getInteger( AEPartEssentiaLevelEmitter.REDSTONE_MODE_NBT_KEY )];

		// Read the wanted amount
		this.wantedAmount = data.getLong( AEPartEssentiaLevelEmitter.WANTED_AMOUNT_NBT_KEY );

		// Read if we are emitting
		this.isEmitting = data.getBoolean( AEPartEssentiaLevelEmitter.IS_EMITTING_NBT_KEY );
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
		// Call super
		super.readFromStream( stream );

		// Read the activity state
		this.isEmitting = stream.readBoolean();

		return true;
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
		helper.setBounds( 9.0F, 4.0F, 14.0F, 11.0F, 11.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		// Set the active texture
		helper.setTexture( BlockTextureManager.ESSENTIA_LEVEL_EMITTER.getTextures()[1] );
		helper.setBounds( 9.0F, 11.0F, 14.0F, 11.0F, 13.0F, 16.0F );
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
			Tessellator.instance.setBrightness( AbstractAEPartBase.ACTIVE_BRIGHTNESS );

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
		// Set the filtered aspect
		this.filterAspect = aspect;

		// Are we client side?
		if( EffectiveSide.isClientSide() )
		{
			return;
		}

		this.checkRegistration();

		// Check network amount
		this.onListUpdate();

		// Send the aspect to the client
		List<Aspect> filter = new ArrayList<Aspect>();
		filter.add( aspect );
		new PacketClientAspectSlot().createFilterListUpdate( filter, player ).sendPacketToPlayer();
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
		Aspect itemAspect = EssentiaItemContainerHelper.instance.getAspectInContainer( itemStack );

		// Ensure we got an aspect
		if( itemAspect == null )
		{
			return false;
		}

		// Set the aspect
		this.setAspect( 0, itemAspect, player );

		return true;
	}

	/**
	 * Writes the state of the emitter to the tag
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		// Call super
		super.writeToNBT( data );

		// Do we have a filter?
		if( this.filterAspect != null )
		{
			// Write the name of the aspect
			data.setString( AEPartEssentiaLevelEmitter.ASPECT_FILTER_NBT_KEY, this.filterAspect.getTag() );
		}
		else
		{
			// Write an empty string
			data.setString( AEPartEssentiaLevelEmitter.ASPECT_FILTER_NBT_KEY, "" );
		}

		// Write the redstone mode ordinal
		data.setInteger( AEPartEssentiaLevelEmitter.REDSTONE_MODE_NBT_KEY, this.redstoneMode.ordinal() );

		// Write the threshold amount
		data.setLong( AEPartEssentiaLevelEmitter.WANTED_AMOUNT_NBT_KEY, this.wantedAmount );

		// Write if we are emitting
		data.setBoolean( AEPartEssentiaLevelEmitter.IS_EMITTING_NBT_KEY, this.isEmitting );
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
