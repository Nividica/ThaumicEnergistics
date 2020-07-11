package thaumicenergistics.common.container;

import appeng.tile.misc.TileVibrationChamber;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.network.packet.client.Packet_C_EssentiaVibrationChamber;
import thaumicenergistics.common.tiles.TileEssentiaVibrationChamber;
import thaumicenergistics.common.tiles.abstraction.TileEVCBase;
import thaumicenergistics.common.utils.EffectiveSide;
import thaumicenergistics.common.utils.ThEUtils;

import javax.annotation.Nonnull;

/**
 * {@link TileVibrationChamber} container.
 *
 * @author Nividica
 *
 */
public class ContainerEssentiaVibrationChamber
	extends TheContainerBase
{
	/**
	 * The number of items to average.
	 */
	private static final int TICK_AVG_COUNT = 4;

	/**
	 * The essentia vibration chamber.
	 */
	private final TileEssentiaVibrationChamber chamber;

	private float powerPerTick = 0, powerPercent = 1.0F, ticksPercent = 1.0F, ticksRemaningMovingAverage = 0.0F;
	private int averageTicksRemaning;

	public ContainerEssentiaVibrationChamber( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Call super
		super( player );

		// Get the tile entity
		TileEntity te = world.getTileEntity( x, y, z );

		// Ensure it is an E.V.C
		if( te instanceof TileEssentiaVibrationChamber )
		{
			// Set the chamber
			this.chamber = (TileEssentiaVibrationChamber)te;
		}
		else
		{
			// Invalid tile entity
			this.chamber = null;
			return;
		}

		// Client side?
		if( EffectiveSide.isClientSide() )
		{
			// Nothing to do client side.
			return;
		}

		// Register for updates
		this.chamber.registerListener( this );

	}

	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		if( this.chamber != null )
		{
			return ThEUtils.canPlayerInteractWith( player, this.chamber );
		}
		return false;
	}

	/**
	 * Gets the power level as a percentage.
	 *
	 * @return
	 */
	public float getPowerPercent()
	{
		return this.powerPercent;
	}

	/**
	 * Gets the power produced per tick.
	 *
	 * @return
	 */
	public float getPowerPerTick()
	{
		return this.powerPerTick;
	}

	/**
	 * Gets the aspect being processed by the EVC, can be null.
	 *
	 * @return
	 */
	public Aspect getProcessingAspect()
	{
		if( this.chamber != null )
		{
			return this.chamber.getProcessingAspect();
		}

		return null;
	}

	/**
	 * Gets the amount of essentia stored in the EVC.
	 *
	 * @return
	 */
	public int getStoredEssentiaAmount()
	{
		if( this.chamber != null )
		{
			return this.chamber.getEssentiaAmount( null );
		}

		return 0;
	}

	/**
	 * Gets the aspect stored in the EVC, can be null.
	 *
	 * @return
	 */
	public Aspect getStoredEssentiaAspect()
	{
		if( this.chamber != null )
		{
			return this.chamber.getEssentiaType( null );
		}

		return null;
	}

	/**
	 * Gets the amount of essentia stored in the EVC as a percentage.
	 *
	 * @return
	 */
	public float getStoredEssentiaPercent()
	{
		return 1.0F - ( this.getStoredEssentiaAmount() / (float)TileEVCBase.MAX_ESSENTIA_STORED );
	}

	/**
	 * Gets the number of ticks left for processing.
	 *
	 * @return
	 */
	public int getTicksRemaining()
	{
		return this.averageTicksRemaning;
	}

	/**
	 * Gets the time remaining as a percentage.
	 *
	 * @return
	 */
	public float getTicksRemainingPercent()
	{
		return this.ticksPercent;
	}

	/**
	 * Called server side when the EVC updates; client side when an update packet arrives.
	 *
	 * @param powerPerTick
	 * @param ticksRemaining
	 * @param totalTicks
	 */
	public void onChamberUpdate( final float powerPerTick, final float maxPowerPerTick, final int ticksRemaining, final int totalTicks )
	{
		// Server side?
		if( EffectiveSide.isServerSide() )
		{
			// Send update packet
			Packet_C_EssentiaVibrationChamber.sendUpdate(
				this.player, powerPerTick,
				maxPowerPerTick, ticksRemaining, totalTicks );
		}
		else
		{
			// Set power per tick
			this.powerPerTick = powerPerTick;

			// Calculate power percent
			this.powerPercent = 1.0F - ( maxPowerPerTick > 0 ? ( powerPerTick / maxPowerPerTick ) : 0 );

			// First tick?
			if( this.ticksRemaningMovingAverage == 0 )
			{
				this.ticksRemaningMovingAverage = ticksRemaining;
			}
			// Update tick average
			else
			{
				float invertedWeight = 1;
				if( ticksRemaining > this.ticksRemaningMovingAverage )
				{
					invertedWeight = 0.3F;
				}

				this.ticksRemaningMovingAverage -= this.ticksRemaningMovingAverage /
								( ContainerEssentiaVibrationChamber.TICK_AVG_COUNT * invertedWeight );
				this.ticksRemaningMovingAverage += ticksRemaining / ( ContainerEssentiaVibrationChamber.TICK_AVG_COUNT * invertedWeight );
			}

			// Cast to int
			this.averageTicksRemaning = (int)this.ticksRemaningMovingAverage;

			// Calculate time percent
			this.ticksPercent = 1.0F - ( totalTicks > 0 ? ( ticksRemaining / (float)totalTicks ) : 0 );
		}
	}

	@Override
	public void onContainerClosed(@Nonnull final EntityPlayer player )
	{
		// Call super
		super.onContainerClosed( player );

		// Server side?
		if( EffectiveSide.isServerSide() && ( this.chamber != null ) )
		{
			this.chamber.removeListener( this );
		}
	}

}
