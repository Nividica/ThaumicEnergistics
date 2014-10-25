package thaumicenergistics.parts;

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
import thaumcraft.common.tiles.TileVisRelay;
import thaumicenergistics.integration.tc.DigiVisSourceData;
import thaumicenergistics.integration.tc.IDigiVisSource;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.texture.BlockTextureManager;
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
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartVisInterface
	extends AbstractAEPartBase
	implements IGridTickable, IDigiVisSource
{
	/**
	 * NBT key for the unique ID
	 */
	private static final String NBT_KEY_UID = "uid";

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
	 * Creates the interface.
	 */
	public AEPartVisInterface()
	{
		super( AEPartsEnum.VisInterface );

		this.UID = System.currentTimeMillis() ^ this.hashCode();
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
			if( ( System.currentTimeMillis() - this.lastColorUpdate ) <= ( AEPartVisInterface.TIME_TO_CLEAR / 2 ) )
			{
				return;
			}

			// Set the update time
			this.lastColorUpdate = System.currentTimeMillis();

		}

		// Set the color
		this.visDrainingColor = color;

		// Update
		this.host.markForUpdate();
	}

	/**
	 * No gui.
	 */
	@Override
	protected boolean canPlayerOpenGui( final int playerID )
	{
		return false;
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
	 * Requests that the interface drain vis from the relay
	 * 
	 * @param digiVisAspect
	 * @param amount
	 * @return
	 */
	@Override
	public int consumeVis( final Aspect digiVisAspect, final int amount )
	{
		// Ensure the interface is active
		if( !this.isActive )
		{
			return 0;
		}

		// Get the relay
		TileVisRelay visRelay = this.getRelay();

		// Ensure there is a relay
		if( visRelay == null )
		{
			return 0;
		}

		// Get the power grid
		IEnergyGrid eGrid = this.gridBlock.getEnergyGrid();

		// Ensure we got the grid
		if( eGrid == null )
		{
			return 0;
		}

		// Simulate a power drain
		double drainedPower = eGrid.extractAEPower( AEPartVisInterface.POWER_PER_REQUESTED_VIS, Actionable.SIMULATE, PowerMultiplier.CONFIG );

		// Ensure we got the power we need
		if( drainedPower < AEPartVisInterface.POWER_PER_REQUESTED_VIS )
		{
			return 0;
		}

		// Ask it for vis
		int amountReceived = visRelay.consumeVis( digiVisAspect, amount );

		// Did we get any vis?
		if( amountReceived > 0 )
		{
			// Set the color
			this.setDrainColor( digiVisAspect.getColor() );

			// Drain the power
			eGrid.extractAEPower( AEPartVisInterface.POWER_PER_REQUESTED_VIS, Actionable.MODULATE, PowerMultiplier.CONFIG );
		}

		// Return the amount we received
		return amountReceived;
	}

	/**
	 * Hit boxes.
	 */
	@Override
	public void getBoxes( final IPartCollsionHelper helper )
	{
		// Face
		helper.addBox( 6.0F, 6.0F, 15.0F, 10.0F, 10.0F, 16.0F );

		// Mid
		helper.addBox( 4.0D, 4.0D, 14.0D, 12.0D, 12.0D, 15.0D );

		// Back
		helper.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );

	}

	/**
	 * Returns the side of the cable the interface is attached to.
	 */
	@Override
	public ForgeDirection getCableSide()
	{
		return this.cableSide;
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
		if( this.gridBlock == null )
		{
			return null;
		}

		// Return the grid
		return this.gridBlock.getGrid();
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
			return tVR;
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

	/**
	 * Player right-clicked the interface.
	 */
	@Override
	public boolean onActivate( final EntityPlayer player, final Vec3 position )
	{
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
		if( data.hasKey( AEPartVisInterface.NBT_KEY_UID ) )
		{
			// Read the UID
			this.UID = data.getLong( AEPartVisInterface.NBT_KEY_UID );
		}
	}

	/**
	 * Reads server-sent data
	 */
	@Override
	public boolean readFromStream( final ByteBuf data ) throws IOException
	{
		// Call super
		super.readFromStream( data );

		// Read the drain color
		this.visDrainingColor = data.readInt();

		return true;

	}

	/**
	 * Draws the interface in the inventory.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		IIcon side = BlockTextureManager.BUS_SIDE.getTexture();
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

		IIcon side = BlockTextureManager.BUS_SIDE.getTexture();
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
			if( ( System.currentTimeMillis() - this.lastColorUpdate ) > AEPartVisInterface.TIME_TO_CLEAR )
			{
				this.setDrainColor( 0 );
			}
		}

		return TickRateModulation.SAME;
	}

	/**
	 * Write the interface data to the tag
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		// Call super
		super.writeToNBT( data );

		// Write the UID
		data.setLong( AEPartVisInterface.NBT_KEY_UID, this.UID );
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
