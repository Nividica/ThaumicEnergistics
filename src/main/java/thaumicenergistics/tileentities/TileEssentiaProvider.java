package thaumicenergistics.tileentities;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumicenergistics.api.TEApi;
import thaumicenergistics.aspect.AspectStack;
import appeng.api.config.Actionable;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;

public class TileEssentiaProvider
	extends TileProviderBase
	implements IEssentiaTransport
{
	/**
	 * How often should the tile tick.
	 */
	private static final int TICK_RATE_IDLE = 15, TICK_RATE_URGENT = 5;

	private final AETileEventHandler tickHandler = new AETileEventHandler( TileEventType.TICK )
	{
		@Override
		public void Tick()
		{
			TileEssentiaProvider.this.onTick();
		}
	};

	/**
	 * Tracks the number of ticks that have occurred.
	 */
	private int tickCount = 0;

	/**
	 * How often should the tile tick.
	 */
	private int tickRate = TileEssentiaProvider.TICK_RATE_IDLE;

	public TileEssentiaProvider()
	{
		// Add the tick handler
		this.addNewHandler( this.tickHandler );
	}

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
		return TEApi.instance().blocks().EssentiaProvider.getStack();

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
	protected void onChannelUpdate()
	{
		// Ignored
	}

	@Override
	public int addEssentia( final Aspect aspect, final int amount, final ForgeDirection side )
	{
		return this.injectEssentiaIntoNetwork( aspect, amount, Actionable.MODULATE );
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
			// Get the stack from the network
			AspectStack matchingStack = this.getAspectStackFromNetwork( wantedAspect );

			// Does the network have that aspect?
			if( matchingStack != null )
			{
				// Return the amount we have
				return (int)matchingStack.amount;
			}
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
			if( this.getAspectStackFromNetwork( wantedAspect ) != null )
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

	/**
	 * Called during the tileentity update phase.
	 */
	void onTick()
	{
		// Ensure this is server side, and that 5 ticks have elapsed
		if( ( !this.worldObj.isRemote ) && ( ++this.tickCount >= this.tickRate ) )
		{
			// Reset the tick count
			this.tickCount = 0;

			// Assume idle
			this.tickRate = TileEssentiaProvider.TICK_RATE_IDLE;

			// Search every side
			for( ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
			{
				// Get the tile
				TileEntity tile = this.worldObj.getTileEntity( side.offsetX + this.xCoord, side.offsetY + this.yCoord, side.offsetZ + this.zCoord );

				// Ensure it is a transport
				if( !( tile instanceof IEssentiaTransport ) )
				{
					// Not transport, skip it.
					continue;
				}

				// Cast
				IEssentiaTransport transport = (IEssentiaTransport)tile;
				ForgeDirection opSide = side.getOpposite();

				// Does the transport have essentia to give?
				if( transport.getEssentiaAmount( opSide ) == 0 )
				{
					// No essentia.
					continue;
				}

				// Set tick rate to urgent.
				this.tickRate = TileEssentiaProvider.TICK_RATE_URGENT;

				// Is there enough suction to pull it out?
				if( ( this.getSuctionAmount( side ) < transport.getMinimumSuction() ) ||
								( this.getSuctionAmount( side ) < transport.getSuctionAmount( opSide ) ) )
				{
					// Not enough suction.
					continue;
				}

				// Get the aspect of the essentia.
				Aspect aspect = transport.getEssentiaType( side );

				// Ensure there is an aspect
				if( aspect == null )
				{
					// Null aspect
					continue;
				}

				// Can the essentia be imported?
				if( this.injectEssentiaIntoNetwork( aspect, 1, Actionable.SIMULATE ) == 1 )
				{
					// Take from tile and import
					this.injectEssentiaIntoNetwork( aspect, transport.takeEssentia( aspect, 1, opSide ), Actionable.MODULATE );

				}
			}
		}
	}

}
