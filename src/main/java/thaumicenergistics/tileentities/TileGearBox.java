package thaumicenergistics.tileentities;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.implementations.tiles.ICrankable;

public class TileGearBox
	extends TileEntity
{
	/**
	 * Amount of power generated each time the gearbox is cranked.
	 * This power is divided among the connected shafts.
	 */
	private static final int BASE_POWER = 6;

	/**
	 * How much shaft power is required to apply a turn?
	 */
	private static final int REQUIRED_POWER = 18;

	/**
	 * Number of valid sides.
	 */
	private static final int SIDE_COUNT = ForgeDirection.VALID_DIRECTIONS.length;

	/**
	 * Tracks the amount of power being sent per side
	 */
	private int[] shafts = new int[SIDE_COUNT];

	/**
	 * Stores the located crankables.
	 */
	private ICrankable[] crankables = new ICrankable[SIDE_COUNT];

	/**
	 * Tracks if the crankable can be turned.
	 */
	private boolean[] canTurn = new boolean[TileGearBox.SIDE_COUNT];

	/**
	 * The number of crankable tiles attached to the gearbox.
	 */
	private int crankableCount = -1;

	/**
	 * Calculates the amount of power to send to each crank.
	 * 
	 * @return
	 */
	private int calculateTransferPower()
	{
		// Number of crankables that can turn.
		int powerDivisor = 0;

		// Calculate how many can accept a turn
		for( int sideIndex = 0; sideIndex < TileGearBox.SIDE_COUNT; sideIndex++ )
		{
			ICrankable c = this.crankables[sideIndex];
			if( ( c != null ) && ( c.canTurn() ) )
			{
				this.canTurn[sideIndex] = true;
				powerDivisor++ ;
			}
		}

		// Can any turn?
		if( powerDivisor == 0 )
		{
			// None can turn
			return 0;
		}

		// Calculate the amount of power to send to each
		return TileGearBox.BASE_POWER / powerDivisor;
	}

	/**
	 * Applies the specified power to each shaft.
	 * Once a shaft has enough stored power, it applies a turn to
	 * its crankable.
	 * 
	 * @param powerTransfered
	 */
	private void updateShafts( final int powerTransfered )
	{
		for( int sideIndex = 0; sideIndex < TileGearBox.SIDE_COUNT; sideIndex++ )
		{
			// Can this side turn?
			if( this.canTurn[sideIndex] )
			{
				// Does it have enough power to turn the grinder?
				if( ( this.shafts[sideIndex] += powerTransfered ) >= TileGearBox.REQUIRED_POWER )
				{
					// Reset the power
					this.shafts[sideIndex] = 0;

					// Turn it
					this.crankables[sideIndex].applyTurn();
				}
			}
			else
			{
				// No power is going to this side.
				this.shafts[sideIndex] = 0;
			}
		}
	}

	/**
	 * Cranks the gearbox.
	 * 
	 * @return
	 */
	public boolean crank()
	{
		// Update if needed
		if( this.crankableCount == -1 )
		{
			this.updateCrankables();
		}

		// Are there any crankables?
		if( this.crankableCount == 0 )
		{
			// Nothing to crank.
			return false;
		}

		// Don't do work on client side
		if( EffectiveSide.isClientSide() )
		{
			return( true );
		}

		// Get the power transfer amount
		int powerTransfered = this.calculateTransferPower();

		// Ensure there is some power to transfer
		if( powerTransfered == 0 )
		{
			// Nothing to crank
			return false;
		}

		// Update the shafts
		this.updateShafts( powerTransfered );

		// Did work
		return true;

	}

	/**
	 * Locates attached crankables.
	 * 
	 * @return Number of attached crankables found.
	 */
	public void updateCrankables()
	{
		// Reset attached to zero
		this.crankableCount = 0;

		// Check all sides
		for( int sideIndex = 0; sideIndex < TileGearBox.SIDE_COUNT; sideIndex++ )
		{
			// Get the side
			ForgeDirection side = ForgeDirection.VALID_DIRECTIONS[sideIndex];

			// Assume there is not a crankable
			this.crankables[sideIndex] = null;

			// Get the tile
			TileEntity tile = this.worldObj.getTileEntity( side.offsetX + this.xCoord, side.offsetY + this.yCoord, side.offsetZ + this.zCoord );

			// Is there a crankable?
			if( !( tile instanceof ICrankable ) )
			{
				continue;
			}

			// Get the crankable
			ICrankable crank = (ICrankable)tile;

			// Is it facing the correct direction?
			if( crank.canCrankAttach( side.getOpposite() ) )
			{
				// Increment the crankable count
				this.crankableCount++ ;

				// Mark there is a crankable.
				this.crankables[sideIndex] = crank;
			}

		}
	}
}
