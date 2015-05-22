package thaumicenergistics.tileentities;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.integration.tc.EssentiaTransportHelper;
import thaumicenergistics.integration.tc.IEssentiaTransportWithSimulate;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEssentiaVibrationChamber
	extends AENetworkTile
	implements IGridTickable, IEssentiaTransportWithSimulate, IAspectSource
{
	private static final byte STATE_OFF = 0, STATE_IGNIS = 1, STATE_POTENTIA = 2;

	/**
	 * Ticking rates.
	 */
	private static final int TICKRATE_IDLE = 40, TICKRATE_URGENT = 10;

	/**
	 * The maximum amount of stored essentia.
	 */
	private static final int MAX_ESSENTIA_STORED = 64;

	/**
	 * How much essentia is stored.
	 */
	private int storedEssentiaAmount = 0;

	/**
	 * The type of essentia that is stored.
	 */
	private Aspect storedEssentiaAspect = null;

	/**
	 * The rate at which ticks should arrive.
	 */
	private TickRateModulation tickRate = TickRateModulation.SAME;

	/**
	 * 1-100: Ignis, 100-200: Potentia
	 */
	private int suctionRotationTimer = 1;

	@Override
	protected ItemStack getItemFromTile( final Object obj )
	{
		// Return the itemstack the visually represents this tile
		return ThEApi.instance().blocks().EssentiaVibrationChamber.getStack();

	}

	@Override
	public int addEssentia( final Aspect aspect, final int amount, final ForgeDirection side )
	{
		return this.addEssentia( aspect, amount, side, Actionable.MODULATE );
	}

	@Override
	public int addEssentia( final Aspect aspect, final int amount, final ForgeDirection side, final Actionable mode )
	{
		// Validate essentia type
		if( ( this.storedEssentiaAspect != null ) && ( this.storedEssentiaAspect != aspect ) )
		{
			// Essentia type does not match
			return 0;
		}

		// Calculate how much will be stored
		int addedAmount = Math.min( amount, TileEssentiaVibrationChamber.MAX_ESSENTIA_STORED - this.storedEssentiaAmount );

		if( ( addedAmount > 0 ) && ( mode == Actionable.MODULATE ) )
		{
			// Set aspect if needed
			if( this.storedEssentiaAspect == null )
			{
				this.storedEssentiaAspect = aspect;
			}

			// Add to the amount
			this.storedEssentiaAmount += addedAmount;

			// Adjust tick rate
			if( this.storedEssentiaAmount < 10 )
			{
				this.tickRate = TickRateModulation.URGENT;
			}
			else
			{
				this.tickRate = TickRateModulation.FASTER;
			}

			// Mark for update
			this.markForUpdate();

			// Mark for save
			this.markDirty();
		}

		return addedAmount;

	}

	@Override
	public int addToContainer( final Aspect aspect, final int amount )
	{
		return this.addEssentia( aspect, amount, null, Actionable.MODULATE );
	}

	@Override
	public boolean canInputFrom( final ForgeDirection side )
	{
		return( side != this.getForward() );
	}

	/**
	 * Can not output.
	 */
	@Override
	public boolean canOutputTo( final ForgeDirection side )
	{
		return false;
	}

	@Override
	public int containerContains( final Aspect aspect )
	{
		return( aspect == this.storedEssentiaAspect ? this.storedEssentiaAmount : 0 );
	}

	@Override
	public boolean doesContainerAccept( final Aspect aspect )
	{
		// Is there stored essentia?
		if( this.storedEssentiaAspect != null )
		{
			// Match to stored essentia
			return aspect == this.storedEssentiaAspect;
		}

		// Nothing is stored, accepts ignis or potentia
		return( ( aspect == Aspect.FIRE ) || ( aspect == Aspect.ENERGY ) );
	}

	@Deprecated
	@Override
	public boolean doesContainerContain( final AspectList aspectList )
	{
		// Is there stored essentia?
		if( this.storedEssentiaAspect == null )
		{
			return false;
		}

		return aspectList.aspects.containsKey( this.storedEssentiaAspect );
	}

	@Override
	public boolean doesContainerContainAmount( final Aspect aspect, final int amount )
	{
		// Does the stored essentia match the aspect?
		if( ( this.storedEssentiaAspect == null ) || ( this.storedEssentiaAspect != aspect ) )
		{
			// Does not match
			return false;
		}

		return( this.storedEssentiaAmount >= amount );
	}

	@Override
	public AspectList getAspects()
	{
		// Create a new list
		AspectList aspectList = new AspectList();

		// Is there stored essentia?
		if( this.storedEssentiaAspect != null )
		{
			// Add the essentia aspect and amount
			aspectList.add( this.storedEssentiaAspect, this.storedEssentiaAmount );
		}

		return aspectList;
	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.COVERED;
	}

	/**
	 * Can not output.
	 */
	@Override
	public int getEssentiaAmount( final ForgeDirection side )
	{
		return this.storedEssentiaAmount;
	}

	@Override
	public Aspect getEssentiaType( final ForgeDirection side )
	{
		return this.storedEssentiaAspect;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	/**
	 * Can not output.
	 */
	@Override
	public int getMinimumSuction()
	{
		return 0;
	}

	@Override
	public int getSuctionAmount( final ForgeDirection side )
	{
		return( this.storedEssentiaAmount < TileEssentiaVibrationChamber.MAX_ESSENTIA_STORED ? 128 : 0 );
	}

	@Override
	public Aspect getSuctionType( final ForgeDirection side )
	{
		// Is there anything stored?
		if( this.storedEssentiaAspect != null )
		{
			// Suction type must match what is stored
			return this.storedEssentiaAspect;
		}

		// Is the timer over 100?
		if( this.suctionRotationTimer > 100 )
		{
			// Does the timer need to be reset?
			if( this.suctionRotationTimer > 200 )
			{
				this.suctionRotationTimer = 0;
			}

			// Potentia
			return Aspect.ENERGY;
		}

		// Ignis
		return Aspect.FIRE;
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		// TODO: Sleep?
		return new TickingRequest( TileEssentiaVibrationChamber.TICKRATE_URGENT, TileEssentiaVibrationChamber.TICKRATE_IDLE, false, false );

	}

	@Override
	public boolean isConnectable( final ForgeDirection side )
	{
		return( side != this.getForward() );
	}

	@TileEvent(TileEventType.NETWORK_READ)
	@SideOnly(Side.CLIENT)
	public boolean onReceiveNetworkData( final ByteBuf stream )
	{
		// Read essentia type
		byte state = stream.readByte();

		// Assign essentia type
		if( state == TileEssentiaVibrationChamber.STATE_POTENTIA )
		{
			this.storedEssentiaAspect = Aspect.ENERGY;
		}
		else if( state == TileEssentiaVibrationChamber.STATE_IGNIS )
		{
			this.storedEssentiaAspect = Aspect.FIRE;
		}
		else
		{
			this.storedEssentiaAspect = null;
		}

		// Read essentia amount
		this.storedEssentiaAmount = stream.readByte();

		return true;
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void onSendNetworkData( final ByteBuf stream ) throws IOException
	{
		// Write essentia type
		if( this.storedEssentiaAspect == Aspect.ENERGY )
		{
			// On: Potentia
			stream.writeByte( TileEssentiaVibrationChamber.STATE_POTENTIA );
		}
		else if( this.storedEssentiaAspect == Aspect.FIRE )
		{
			// On: Ignis
			stream.writeByte( TileEssentiaVibrationChamber.STATE_IGNIS );
		}
		else
		{
			// Off
			stream.writeByte( TileEssentiaVibrationChamber.STATE_OFF );
		}

		// Write essentia amount
		stream.writeByte( (byte)this.storedEssentiaAmount );
	}

	/**
	 * Full block, not extension needed.
	 */
	@Override
	public boolean renderExtendedTube()
	{
		return false;
	}

	@Override
	public void setAspects( final AspectList aspectList )
	{
		// Ignored
	}

	/**
	 * Sets the owner of this tile.
	 * 
	 * @param player
	 */
	public void setOwner( final EntityPlayer player )
	{
		this.gridProxy.setOwner( player );
	}

	@Override
	public void setSuction( final Aspect aspect, final int amount )
	{
		// Ignored
	}

	/**
	 * Sets up the chamber
	 * 
	 * @return
	 */
	public TileEssentiaVibrationChamber setupChamberTile()
	{
		// Ignored on client side
		if( FMLCommonHandler.instance().getEffectiveSide().isServer() )
		{
			// Set idle power usage to zero
			this.gridProxy.setIdlePowerUsage( 0.0D );
		}

		return this;
	}

	/**
	 * Can not output.
	 */
	@Override
	public int takeEssentia( final Aspect aspect, final int amount, final ForgeDirection side )
	{
		return 0;
	}

	/**
	 * Can not output.
	 */
	@Override
	public boolean takeFromContainer( final Aspect aspect, final int amount )
	{
		return false;
	}

	/**
	 * Can not output.
	 */
	@Deprecated
	@Override
	public boolean takeFromContainer( final AspectList arg0 )
	{
		return false;
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		// Assume slower
		this.tickRate = TickRateModulation.SLOWER;

		// Does the essentia need to be replenished?
		if( this.storedEssentiaAmount < TileEssentiaVibrationChamber.MAX_ESSENTIA_STORED )
		{
			// Replenish essentia
			EssentiaTransportHelper.instance.takeEssentiaFromTransportNeighbors( this, this.worldObj, this.xCoord, this.yCoord, this.zCoord );
		}

		// Is there anything stored?
		if( this.storedEssentiaAspect == null )
		{
			// Nothing is stored, keep moving the rotation timer
			this.suctionRotationTimer += ticksSinceLastCall;

			// TODO: Resume work on vibration chamber here
			//System.out.println( this.suctionRotationTimer );
		}

		return this.tickRate;
	}
}
