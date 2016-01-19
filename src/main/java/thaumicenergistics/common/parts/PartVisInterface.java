package thaumicenergistics.common.parts;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.lang.ref.WeakReference;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.common.tiles.TileVisRelay;
import thaumicenergistics.api.grid.IDigiVisSource;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.integration.tc.DigiVisSourceData;
import thaumicenergistics.common.integration.tc.VisProviderProxy;
import thaumicenergistics.common.registries.AEPartsEnum;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartVisInterface
	extends ThEPartBase
	implements IGridTickable, IDigiVisSource
{
	/**
	 * NBT key for the unique ID
	 */
	private static final String NBT_KEY_UID = "uid";

	/**
	 * NBT key for if the interface is a source or not.
	 */
	private static final String NBT_KEY_IS_PROVIDER = "isProvider";

	/**
	 * NBT key for the source of this provider.
	 */
	private static final String NBT_KEY_PROVIDER_SOURCE = "linkedSource";

	/**
	 * The amount of time to display the color when not receiving updates
	 */
	private static final int TIME_TO_CLEAR = 500;

	/**
	 * The amount of power to use per each vis of a request.
	 * The amount of vis doesn't matter.
	 */
	private static final int POWER_PER_REQUESTED_VIS = 4;

	/**
	 * Unique ID for this interface
	 */
	private long UID = 0;

	/**
	 * The aspect color we are currently draining
	 */
	private int visDrainingColor = 0;

	/**
	 * The last time the color was refreshed.
	 */
	private long lastColorUpdate = 0;

	/**
	 * Cached reference to the relay we are facing.
	 */
	private WeakReference<TileVisRelay> cachedRelay = new WeakReference<TileVisRelay>( null );

	/**
	 * True if this end of the P2P is a vis provider.
	 */
	private boolean isProvider = false;

	/**
	 * If this end is a provider, this stores the source.
	 */
	private DigiVisSourceData visP2PSourceInfo = new DigiVisSourceData();

	/**
	 * If this end is a provider, this interacts with the vis network.
	 */
	private VisProviderProxy visProviderSubTile = null;

	/**
	 * Creates the interface.
	 */
	public PartVisInterface()
	{
		super( AEPartsEnum.VisInterface );

		this.UID = System.currentTimeMillis() ^ this.hashCode();
	}

	/**
	 * Requests that the interface drain vis from the relay
	 * 
	 * @param digiVisAspect
	 * @param amount
	 * @return
	 */
	private int consumeVisFromVisNetwork( final Aspect digiVisAspect, final int amount )
	{
		// Get the relay
		TileVisRelay visRelay = this.getRelay();

		// Ensure there is a relay
		if( visRelay == null )
		{
			return 0;
		}

		// Get the power grid
		IEnergyGrid eGrid = this.getGridBlock().getEnergyGrid();

		// Ensure we got the grid
		if( eGrid == null )
		{
			return 0;
		}

		// Simulate a power drain
		double drainedPower = eGrid.extractAEPower( PartVisInterface.POWER_PER_REQUESTED_VIS, Actionable.SIMULATE, PowerMultiplier.CONFIG );

		// Ensure we got the power we need
		if( drainedPower < PartVisInterface.POWER_PER_REQUESTED_VIS )
		{
			return 0;
		}

		// Ask it for vis
		int amountReceived = visRelay.consumeVis( digiVisAspect, amount );

		// Did we get any vis?
		if( amountReceived > 0 )
		{
			// Drain the power
			eGrid.extractAEPower( PartVisInterface.POWER_PER_REQUESTED_VIS, Actionable.MODULATE, PowerMultiplier.CONFIG );
		}

		// Return the amount we received
		return amountReceived;
	}

	/**
	 * Verifies that a p2p source is valid
	 * 
	 * @return
	 */
	private boolean isP2PSourceValid()
	{
		// Is there anything even linked to?
		if( !this.visP2PSourceInfo.hasSourceData() )
		{
			return false;
		}

		// Get the source
		IDigiVisSource p2pSource = this.visP2PSourceInfo.tryGetSource( this.getGrid() );

		// There must be source data
		if( p2pSource == null )
		{
			return false;
		}

		// Source must be a vis interface
		if( !( p2pSource instanceof PartVisInterface ) )
		{
			return false;
		}

		// Can't link to self
		if( this.equals( p2pSource ) )
		{
			return false;
		}

		// Source must not be a provider
		if( ( (PartVisInterface)p2pSource ).isVisProvider() )
		{
			return false;
		}

		// Source seems valid
		return true;
	}

	/**
	 * Sets the color we are draining.
	 * 
	 * @param color
	 */
	private void setDrainColor( final int color )
	{

		// Are we setting the color?
		if( color != 0 )
		{
			// Does it match what we already have?
			if( color == this.visDrainingColor )
			{
				// Set the update time
				this.lastColorUpdate = System.currentTimeMillis();

				return;
			}

			// Has the alloted time passed for a change?
			if( ( System.currentTimeMillis() - this.lastColorUpdate ) <= ( PartVisInterface.TIME_TO_CLEAR / 2 ) )
			{
				return;
			}

			// Set the update time
			this.lastColorUpdate = System.currentTimeMillis();

		}

		// Set the color
		this.visDrainingColor = color;

		// Update
		this.markForUpdate();
	}

	private void setIsVisProvider( final boolean isProviding )
	{

		// Is the interface to be a provider?
		if( !isProviding )
		{
			// Clear the source info
			this.visP2PSourceInfo.clearData();

			// Null the subtile
			if( this.visProviderSubTile != null )
			{
				this.visProviderSubTile.invalidate();
				this.visProviderSubTile = null;
			}

		}

		this.isProvider = isProviding;

	}

	/**
	 * How far to extend the cable.
	 */
	@Override
	public int cableConnectionRenderTo()
	{
		return 2;
	}

	/**
	 * Drains vis from either the vis relay network, or from the p2p source.
	 * 
	 * @param digiVisAspect
	 * @param amount
	 * @return
	 */
	@Override
	public int consumeVis( final Aspect digiVisAspect, final int amount )
	{
		// Ensure the interface is active
		if( !this.isActive() )
		{
			return 0;
		}

		int amountReceived = 0;

		// Is the interface a provider?
		if( this.isProvider )
		{
			if( this.isP2PSourceValid() )
			{
				// Get the p2p source
				IDigiVisSource source = this.visP2PSourceInfo.tryGetSource( this.getGrid() );

				// Ask the source for vis
				amountReceived = source.consumeVis( digiVisAspect, amount );
			}
		}
		else
		{
			amountReceived = this.consumeVisFromVisNetwork( digiVisAspect, amount );
		}

		// Was any vis received?
		if( amountReceived > 0 )
		{
			// Set the color
			this.setDrainColor( digiVisAspect.getColor() );
		}

		return amountReceived;
	}

	/**
	 * Hit boxes.
	 */
	@Override
	public void getBoxes( final IPartCollisionHelper helper )
	{
		// Face
		helper.addBox( 6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F );

		// Mid
		helper.addBox( 4.0D, 4.0D, 14.0D, 12.0D, 12.0D, 15.0D );

		// Back
		helper.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );

	}

	@Override
	public IIcon getBreakingTexture()
	{
		return BlockTextureManager.VIS_RELAY_INTERFACE.getTextures()[0];
	}

	/**
	 * Gets the grid the interface is attached to
	 * 
	 * @return
	 */
	@Override
	public IGrid getGrid()
	{
		// Ensure the interface has a gridblock
		if( this.getGridBlock() == null )
		{
			return null;
		}

		// Return the grid
		return this.getGridBlock().getGrid();
	}

	/**
	 * No idle power usage.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return 0;
	}

	/**
	 * Produces a small amount of light.
	 */
	@Override
	public int getLightLevel()
	{
		return 8;
	}

	/**
	 * Gets the relay the interface is facing. If any.
	 * 
	 * @return
	 */
	public TileVisRelay getRelay()
	{
		// Get the cached relay
		TileVisRelay tVR = this.cachedRelay.get();

		// Is there a cached relay?
		if( tVR != null )
		{
			// Ensure it is still there
			if( tVR == this.getHostTile().getWorldObj().getTileEntity( tVR.xCoord, tVR.yCoord, tVR.zCoord ) )
			{
				return tVR;
			}
		}

		// Get the tile we are facing
		TileEntity facingTile = this.getFacingTile();

		// Is it a relay?
		if( facingTile instanceof TileVisRelay )
		{
			// Get the relay
			tVR = (TileVisRelay)facingTile;

			// Is it facing the same direction as we are?
			if( tVR.orientation == this.getSide().ordinal() )
			{
				// Set the cache
				this.cachedRelay = new WeakReference<TileVisRelay>( tVR );

				// Return it
				return tVR;
			}
		}

		return null;
	}

	/**
	 * How often should we tick?
	 */
	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( 30, 30, false, false );
	}

	/**
	 * Gets the unique ID for this interface
	 * 
	 * @return
	 */
	@Override
	public long getUID()
	{
		return this.UID;
	}

	/**
	 * Is the interface on and active?
	 * 
	 * @return
	 */
	@Override
	public boolean isActive()
	{
		return super.isActive();
	}

	public Boolean isVisProvider()
	{
		return this.isProvider;
	}

	/**
	 * Player right-clicked the interface.
	 */
	@Override
	public boolean onActivate( final EntityPlayer player, final Vec3 position )
	{
		// Get what the player is holding
		ItemStack playerHolding = player.inventory.getCurrentItem();

		// Are they holding a memory card?
		if( ( playerHolding != null ) && ( playerHolding.getItem() instanceof IMemoryCard ) )
		{
			// Get the memory card
			IMemoryCard memoryCard = (IMemoryCard)playerHolding.getItem();

			// Get the stored name
			String settingsName = memoryCard.getSettingsName( playerHolding );

			// Does it contain the data about a vis source?
			if( settingsName.equals( DigiVisSourceData.SOURCE_UNLOC_NAME ) )
			{
				// Get the data
				NBTTagCompound data = memoryCard.getData( playerHolding );

				// Load the info
				this.visP2PSourceInfo.readFromNBT( data );

				// Ensure there is valid data
				if( this.visP2PSourceInfo.hasSourceData() )
				{
					// Can the link be established?
					if( !this.isP2PSourceValid() )
					{
						// Unable to link
						memoryCard.notifyUser( player, MemoryCardMessages.INVALID_MACHINE );

						// Clear the source data
						this.visP2PSourceInfo.clearData();
					}
					else
					{
						// Mark that we are now a provider
						this.setIsVisProvider( true );

						// Inform the user
						memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
					}
				}

				// Mark for a save
				this.markForSave();
			}
			// Is the memory card empty?
			else if( settingsName.equals( "gui.appliedenergistics2.Blank" ) && this.isProvider )
			{
				// Mark that we are not a provider
				this.setIsVisProvider( false );

				// Inform the user
				memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_CLEARED );

				// Mark for save
				this.markForSave();
			}

			return true;
		}

		return false;
	}

	/**
	 * Player shift-right-clicked the interface.
	 */
	@Override
	public boolean onShiftActivate( final EntityPlayer player, final Vec3 position )
	{
		// Get what the player is holding
		ItemStack playerHolding = player.inventory.getCurrentItem();

		// Are they holding a memory card?
		if( ( playerHolding != null ) && ( playerHolding.getItem() instanceof IMemoryCard ) )
		{
			// Get the memory card
			IMemoryCard memoryCard = (IMemoryCard)playerHolding.getItem();

			// Create the info data
			DigiVisSourceData data = new DigiVisSourceData( this );

			// Write into the memory card
			memoryCard.setMemoryCardContents( playerHolding, DigiVisSourceData.SOURCE_UNLOC_NAME, data.writeToNBT() );

			// Notify the user
			memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_SAVED );

			// Mark that we are not a provider
			this.setIsVisProvider( false );

			// Mark for save
			this.markForSave();

			return true;
		}

		return false;
	}

	/**
	 * Reads the interface data from the tag
	 */
	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Does it contain the UID?
		if( data.hasKey( PartVisInterface.NBT_KEY_UID ) )
		{
			// Read the UID
			this.UID = data.getLong( PartVisInterface.NBT_KEY_UID );
		}

		// Is there provider data?
		if( data.hasKey( PartVisInterface.NBT_KEY_IS_PROVIDER ) )
		{
			// Set provider status
			this.isProvider = data.getBoolean( PartVisInterface.NBT_KEY_IS_PROVIDER );

			// Read source information
			if( data.hasKey( PartVisInterface.NBT_KEY_PROVIDER_SOURCE ) )
			{
				this.visP2PSourceInfo.readFromNBT( data, PartVisInterface.NBT_KEY_PROVIDER_SOURCE );
			}
		}
	}

	/**
	 * Reads server-sent data
	 */
	@Override
	public boolean readFromStream( final ByteBuf data ) throws IOException
	{
		boolean redraw = false;

		// Call super
		redraw |= super.readFromStream( data );

		// Cache old color
		int oldColor = this.visDrainingColor;

		// Read the drain color
		this.visDrainingColor = data.readInt();

		// Redraw if colors changed
		redraw |= ( this.visDrainingColor != oldColor );

		return redraw;

	}

	/**
	 * Draws the interface in the inventory.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		IIcon side = BlockTextureManager.VIS_RELAY_INTERFACE.getTextures()[2];
		helper.setTexture( side, side, side, BlockTextureManager.VIS_RELAY_INTERFACE.getTexture(), side, side );

		// Face
		helper.setBounds( 6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		// Mid
		helper.setBounds( 4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F );
		helper.renderInventoryBox( renderer );

		// Back
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderInventoryBusLights( helper, renderer );

	}

	/**
	 * Draws the interface in the world.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator tessellator = Tessellator.instance;

		IIcon side = BlockTextureManager.VIS_RELAY_INTERFACE.getTextures()[2];
		helper.setTexture( side, side, side, BlockTextureManager.VIS_RELAY_INTERFACE.getTexture(), side, side );

		// Face
		helper.setBounds( 6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		// Mid
		helper.setBounds( 4.0F, 4.0F, 14.0F, 12.0F, 12.0F, 15.0F );
		helper.renderBlock( x, y, z, renderer );// Mid face

		if( this.visDrainingColor != 0 )
		{
			tessellator.setColorOpaque_I( this.visDrainingColor );
			helper.renderFace( x, y, z, BlockTextureManager.VIS_RELAY_INTERFACE.getTextures()[1], ForgeDirection.SOUTH, renderer );
		}

		// Back (facing bus)
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );
	}

	/**
	 * Called when the interface ticks
	 */
	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int TicksSinceLastCall )
	{
		if( this.visDrainingColor != 0 )
		{
			if( ( System.currentTimeMillis() - this.lastColorUpdate ) > PartVisInterface.TIME_TO_CLEAR )
			{
				this.setDrainColor( 0 );
			}
		}

		// Is the interface a provider?
		if( this.isProvider )
		{
			boolean hasProvider = ( this.visProviderSubTile != null );

			// Validate the P2P Settings
			if( !this.isP2PSourceValid() )
			{
				// Invalid source, remove provider
				if( hasProvider )
				{
					this.visProviderSubTile.invalidate();
					this.visProviderSubTile = null;
				}
			}
			else
			{
				boolean hasRelay = ( this.getRelay() != null );

				// Invalid: Can't have a provider without a relay
				if( !hasRelay && hasProvider )
				{
					// Remove the provider
					this.visProviderSubTile.invalidate();
					this.visProviderSubTile = null;
				}
				// Has relay but no provider
				else if( hasRelay && !hasProvider )
				{
					// Create the provider
					this.visProviderSubTile = new VisProviderProxy( this );

					// Register the provider
					VisNetHandler.addSource( this.getHostTile().getWorldObj(), this.visProviderSubTile );
				}
				else if( hasProvider )
				{
					this.visProviderSubTile.updateEntity();
				}
			}
		}

		return TickRateModulation.SAME;
	}

	/**
	 * Write the interface data to the tag
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data, final PartItemStack saveType )
	{
		// Call super
		super.writeToNBT( data, saveType );

		// Only write NBT if world save
		if( saveType != PartItemStack.World )
		{
			return;
		}

		// Write the UID
		data.setLong( PartVisInterface.NBT_KEY_UID, this.UID );

		if( this.isProvider )
		{
			// Write provider status
			data.setBoolean( PartVisInterface.NBT_KEY_IS_PROVIDER, this.isProvider );

			// Write source data
			this.visP2PSourceInfo.writeToNBT( data, PartVisInterface.NBT_KEY_PROVIDER_SOURCE );
		}

	}

	/**
	 * Sends data to the client
	 */
	@Override
	public void writeToStream( final ByteBuf data ) throws IOException
	{
		// Call super
		super.writeToStream( data );

		// Write the drain color
		data.writeInt( this.visDrainingColor );
	}

}
