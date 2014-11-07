package thaumicenergistics.tileentities;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.implementations.tiles.ICrankable;

public class TileGearBox
	extends TileEntity
{
	public static final String TILE_ID = "TileGearBox";

	/**
	 * Total amount of power per crank.
	 */
	private static final int BASE_POWER = 6;

	/**
	 * How much accumulated power is required to turn a grinder?
	 */
	private static final int REQUIRED_POWER = 18;

	/**
	 * Tracks the amount of power being sent per side
	 */
	private int[] crankPower = new int[ForgeDirection.VALID_DIRECTIONS.length];

	/**
	 * Tracks if there is a grinder per side
	 */
	private boolean[] hasGrinder = new boolean[ForgeDirection.VALID_DIRECTIONS.length];

	/**
	 * Gets the grinder on the specified side.
	 * 
	 * @param sideIndex
	 * @return
	 */
	private ICrankable getGrinder( final int sideIndex )
	{
		// Get the side
		ForgeDirection side = ForgeDirection.VALID_DIRECTIONS[sideIndex];

		// Get the tile
		TileEntity t = this.worldObj.getTileEntity( side.offsetX + this.xCoord, side.offsetY + this.yCoord, side.offsetZ + this.zCoord );

		if( t instanceof ICrankable )
		{
			return (ICrankable)t;
		}

		return null;
	}

	/**
	 * Locates attached grinders.
	 * 
	 * @return Number of attached grinders found.
	 */
	private int locateGrinders( final boolean isServerThread )
	{
		// Number of attached grinders
		int grinderCount = 0;

		// Check all sides
		for( int sideIndex = 0; sideIndex < ForgeDirection.VALID_DIRECTIONS.length; sideIndex++ )
		{
			// Get the side
			ForgeDirection side = ForgeDirection.VALID_DIRECTIONS[sideIndex];

			// Assume there is not a grinder
			this.hasGrinder[sideIndex] = false;

			// Get the grinder
			ICrankable crank = this.getGrinder( sideIndex );

			// Is there a grinder?
			if( crank == null )
			{
				continue;
			}

			// Can it turn?
			if( isServerThread && ( !crank.canTurn() ) )
			{
				continue;
			}

			// Is it facing the correct direction?
			if( crank.canCrankAttach( side.getOpposite() ) )
			{
				// Increment the grinder count
				grinderCount++ ;

				// Mark there is a grinder.
				this.hasGrinder[sideIndex] = true;
			}

		}

		return grinderCount;
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	/**
	 * Turns the crankshaft.
	 * 
	 * @return
	 */
	public boolean crank()
	{
		boolean isServerSide = EffectiveSide.isServerSide();

		// Get the grinders
		int grinderCount = this.locateGrinders( isServerSide );

		boolean hasGrinder = grinderCount > 0;

		// Don't do work on client side.
		if( !isServerSide )
		{
			return( hasGrinder );
		}

		// Were there any grinders?
		if( hasGrinder )
		{
			// Calculate the amount of power to send to each
			int powerTransfered = TileGearBox.BASE_POWER / grinderCount;

			// Update powers
			for( int sideIndex = 0; sideIndex < this.crankPower.length; sideIndex++ )
			{
				// Is there a grinder on this side?
				if( this.hasGrinder[sideIndex] )
				{
					// Does it have enough power to turn the grinder?
					if( ( this.crankPower[sideIndex] += powerTransfered ) >= TileGearBox.REQUIRED_POWER )
					{
						// Reset the power
						this.crankPower[sideIndex] = 0;

						// Turn the grinder
						this.getGrinder( sideIndex ).applyTurn();
					}
				}
				else
				{
					// No power is going to this side.
					this.crankPower[sideIndex] = 0;
				}
			}
		}

		return( hasGrinder );

	}
}
