package thaumicenergistics.tileentities;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.integration.tc.EssentiaTransportHelper;
import thaumicenergistics.integration.tc.IEssentiaTransportWithSimulate;
import appeng.api.config.Actionable;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;

public class TileEssentiaProvider
	extends TileProviderBase
	implements IEssentiaTransportWithSimulate
{
	/**
	 * How often should the tile tick.
	 */
	private static final int TICK_RATE_IDLE = 15, TICK_RATE_URGENT = 5;

	/**
	 * Tracks the number of ticks that have occurred.
	 */
	private int tickCount = 0;

	/**
	 * How often should the tile tick.
	 */
	private int tickRate = TileEssentiaProvider.TICK_RATE_IDLE;

	/**
	 * How much power does this require just to be active?
	 */
	@Override
	protected double getIdlePowerusage()
	{
		return 3.0;
	}

	@Override
	protected ItemStack getItemFromTile( final Object obj )
	{
		// Return the itemstack the visually represents this tile
		return ThEApi.instance().blocks().EssentiaProvider.getStack();

	}

	protected Aspect getNeighborWantedAspect( final ForgeDirection side )
	{
		// Get the tile entity next to this side
		TileEntity neighbor = this.worldObj.getTileEntity( this.xCoord + side.offsetX, this.yCoord + side.offsetY, this.zCoord + side.offsetZ );

		// Do we have essentia transport neighbor?
		if( ( neighbor != null ) && ( neighbor instanceof IEssentiaTransport ) )
		{
			// Get the aspect they want
			Aspect wantedAspect = ( (IEssentiaTransport)neighbor ).getSuctionType( side.getOpposite() );

			// Return the aspect they want
			return wantedAspect;
		}

		return null;
	}

	@Override
	public int addEssentia( final Aspect aspect, final int amount, final ForgeDirection side )
	{
		return this.addEssentia( aspect, amount, side, Actionable.MODULATE );
	}

	@Override
	public int addEssentia( final Aspect aspect, final int amount, final ForgeDirection side, final Actionable mode )
	{
		// Assume its all rejected
		long rejectedAmount = amount;

		// Ensure we have a monitor
		if( this.getEssentiaMonitor() )
		{
			// Inject essentia
			rejectedAmount = this.monitor.injectEssentia( aspect, amount, mode, this.getMachineSource() );
		}

		// Calculate the accepted amount
		long acceptedAmount = amount - rejectedAmount;

		if( ( mode == Actionable.MODULATE ) && ( acceptedAmount > 0 ) )
		{
			this.tickRate = TileEssentiaProvider.TICK_RATE_URGENT;
		}

		return (int)acceptedAmount;
	}

	@Override
	public boolean canInputFrom( final ForgeDirection side )
	{
		// Can input from any side
		return true;
	}

	@Override
	public boolean canOutputTo( final ForgeDirection side )
	{
		// Can output to any side
		return true;
	}

	@Override
	public int getEssentiaAmount( final ForgeDirection side )
	{
		// Get the aspect this neighbor wants
		Aspect wantedAspect = this.getNeighborWantedAspect( side );

		// Does the neighbor want anything?
		if( wantedAspect != null )
		{
			return (int)Math.min( this.getAspectAmountInNetwork( wantedAspect ), Integer.MAX_VALUE );
		}

		// No match or no request
		return 0;
	}

	@Override
	public Aspect getEssentiaType( final ForgeDirection side )
	{
		// Get the aspect this neighbor wants
		Aspect wantedAspect = this.getNeighborWantedAspect( side );

		// Does the neighbor want anything?
		if( wantedAspect != null )
		{
			// Does the network have that aspect?
			if( this.getAspectAmountInNetwork( wantedAspect ) > 0 )
			{
				// Return the aspect they want
				return wantedAspect;
			}
		}

		// No match or no request
		return null;
	}

	@Override
	public int getMinimumSuction()
	{
		// Any amount of suction is good enough
		return 1;
	}

	@Override
	public int getSuctionAmount( final ForgeDirection side )
	{
		// Just a wee bit of suction.
		return 8;
	}

	@Override
	public Aspect getSuctionType( final ForgeDirection side )
	{
		// All types
		return null;
	}

	@Override
	public boolean isConnectable( final ForgeDirection side )
	{
		// Can connect on any side
		return true;
	}

	/**
	 * Called during the tileentity update phase.
	 */
	@TileEvent(TileEventType.TICK)
	public void onTick()
	{
		// Ensure this is server side, and that 5 ticks have elapsed
		if( ( !this.worldObj.isRemote ) && ( ++this.tickCount >= this.tickRate ) )
		{
			// Reset the tick count
			this.tickCount = 0;

			// Assume idle
			this.tickRate = TileEssentiaProvider.TICK_RATE_IDLE;

			// Take essentia from the neighbors
			EssentiaTransportHelper.INSTANCE.takeEssentiaFromTransportNeighbors( this, this.worldObj, this.xCoord, this.yCoord, this.zCoord );
		}
	}

	@Override
	public boolean renderExtendedTube()
	{
		// We take up a full block
		return false;
	}

	@Override
	public void setSuction( final Aspect aspect, final int amount )
	{
		// Ignored
	}

	@Override
	public int takeEssentia( final Aspect aspect, final int amount, final ForgeDirection side )
	{
		// Extract essentia from the network, and return the amount extracted
		return this.extractEssentiaFromNetwork( aspect, amount, false );
	}

}
