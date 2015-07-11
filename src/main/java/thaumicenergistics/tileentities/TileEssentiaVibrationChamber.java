package thaumicenergistics.tileentities;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.integration.IWailaSource;
import thaumicenergistics.integration.tc.EssentiaTransportHelper;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.registries.ThEStrings;
import thaumicenergistics.tileentities.abstraction.TileEVCBase;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.me.GridAccessException;

public class TileEssentiaVibrationChamber
	extends TileEVCBase
	implements IGridTickable, IWailaSource
{
	private static String NBTKEY_TIME_REMAINING = "TRemain", NBTKEY_PROCESSING_SPEED = "ProcSpeed", NBTKEY_POWER_PER_TICK = "PwrPerTick",
					NBTKEY_PROCESSING_ASPECT = "ProcAspect";

	/**
	 * How long it takes coal to burn
	 */
	private static int coalBurnTime = 0;

	/**
	 * How much power is produced per tick
	 */
	private static double basePowerProducedPerTick = 5.0;

	/**
	 * Half second.
	 */
	private static int TICKRATE_MIN = 10;

	/**
	 * Two seconds.
	 */
	private static int TICKRATE_MAX = 40;

	/**
	 * Min and Max processing speed
	 */
	private static final int PROCESS_SPEED_MAX = 200, PROCESS_SPEED_MIN = 20;

	/**
	 * How much longer the essentia will be processed.
	 */
	private int timeRemaining = 0;

	/**
	 * How fast processing is occurring.
	 */
	private int processingSpeed = 0;

	/**
	 * How much power is produced per tick.
	 */
	private double powerProducedPerProcessingTick = 0;

	/**
	 * What aspect is currently being processed.
	 */
	public Aspect processingAspect = null;

	/**
	 * Set true when the aspect changed.
	 */
	public boolean processingChanged = false;

	/**
	 * Adjusts processing time and power production based on essentia type.
	 * 
	 * @return
	 */
	private int adjustProcessingValues()
	{
		int pTime = 0;

		// Has the burn time of coal been retrieved?
		if( TileEssentiaVibrationChamber.coalBurnTime == 0 )
		{
			// Get the base processing time of coal
			TileEssentiaVibrationChamber.coalBurnTime = TileEntityFurnace.getItemBurnTime( new ItemStack( Items.coal ) );
		}

		// What kind of essentia is stored?
		if( this.storedEssentia.aspect == Aspect.FIRE )
		{
			pTime = TileEssentiaVibrationChamber.coalBurnTime / 2;
			this.powerProducedPerProcessingTick = TileEssentiaVibrationChamber.basePowerProducedPerTick * 1.0D;
		}
		else if( this.storedEssentia.aspect == Aspect.ENERGY )
		{
			pTime = (int)( TileEssentiaVibrationChamber.coalBurnTime / 1.6F );
			this.powerProducedPerProcessingTick = TileEssentiaVibrationChamber.basePowerProducedPerTick * 1.6D;
		}

		return pTime;
	}

	/**
	 * Converts an essentia to processing time.
	 * 
	 * @return True if essentia was consumed, false otherwise.
	 */
	private boolean consumeEssentia()
	{
		// Is there anything stored?
		if( this.hasStoredEssentia() )
		{
			// Get the processing time
			int pTime = this.adjustProcessingValues();
			if( pTime > 0 )
			{
				// Set the type
				this.processingAspect = this.storedEssentia.aspect;
				this.processingChanged = true;

				// Take one
				--this.storedEssentia.stackSize;

				// Mark dirty
				this.markDirty();
				this.markForUpdate();

				// Add to processing time
				this.timeRemaining += pTime;

				return true;
			}
		}

		return false;
	}

	/**
	 * Adds power to the network.
	 * 
	 * @param ticksSinceLastCall
	 * @return
	 */
	private TickRateModulation doProcessingTick( final int ticksSinceLastCall )
	{
		// Is there any processing time remaining?
		if( this.timeRemaining == 0 )
		{
			return TickRateModulation.IDLE;
		}

		// Bounds check processing speed
		this.processingSpeed = Math.max( TileEssentiaVibrationChamber.PROCESS_SPEED_MIN,
			Math.min( TileEssentiaVibrationChamber.PROCESS_SPEED_MAX, this.processingSpeed ) );

		// Calculate dilation
		double dilation = this.processingSpeed / 100.0;

		// Calculate number of processing ticks
		double processingTicks = ticksSinceLastCall * dilation;

		// Adjust time remaining
		this.timeRemaining -= processingTicks;

		// Finished?
		if( this.timeRemaining <= 0 )
		{
			// Adjust processing time
			processingTicks += this.timeRemaining;

			// Reset time
			this.timeRemaining = 0;

			// Clear the aspect
			this.processingAspect = null;

			// Mark dirty
			this.processingChanged = true;
			this.markForUpdate();
			this.markDirty();
		}

		// Calculate the produced power
		double producedPower = processingTicks * this.powerProducedPerProcessingTick;

		// Assume slower
		TickRateModulation rate = TickRateModulation.SLOWER;

		try
		{
			// Get the energy grid
			IEnergyGrid eGrid = this.gridProxy.getEnergy();

			// Get rejected amount, note that any rejected amount is simply lost.
			double rejectedPower = eGrid.injectPower( producedPower, Actionable.SIMULATE );

			// Was any rejected?
			if( rejectedPower > 0 )
			{
				// Adjust the power
				producedPower = Math.max( 0, producedPower - rejectedPower );

				// Adjust speed
				this.processingSpeed -= ticksSinceLastCall;
			}
			else
			{
				// Increase tickrate
				rate = TickRateModulation.FASTER;

				// Adjust speed
				this.processingSpeed += ticksSinceLastCall;
			}

			// Inject the power
			if( producedPower > 0 )
			{
				eGrid.injectPower( producedPower, Actionable.MODULATE );
			}
		}
		catch( GridAccessException g )
		{
			// Silently ignore.
		}

		return rate;

	}

	/**
	 * Adds essentia to the buffer.
	 * 
	 * @param aspect
	 * @param amount
	 * @param mode
	 * @return
	 */
	@Override
	protected int addEssentia( final Aspect aspect, final int amount, final Actionable mode )
	{
		// Validate essentia type
		if( ( this.hasStoredEssentia() ) && ( this.storedEssentia.aspect != aspect ) )
		{
			// Essentia type does not match
			return 0;
		}

		// Get how much is stored, and the aspect
		int storedAmount = ( this.storedEssentia == null ? 0 : (int)this.storedEssentia.stackSize );
		Aspect storedAspect = ( this.storedEssentia == null ? null : this.storedEssentia.aspect );

		// Calculate how much to be stored
		int addedAmount = Math.min( amount, TileEVCBase.MAX_ESSENTIA_STORED - storedAmount );

		if( ( addedAmount > 0 ) && ( mode == Actionable.MODULATE ) )
		{
			// Create the stack if needed
			if( storedAspect == null )
			{
				this.storedEssentia = new AspectStack( aspect, 0 );
			}
			else
			{
				this.storedEssentia.aspect = aspect;
			}

			// Add to the amount
			this.storedEssentia.stackSize += addedAmount;

			// Mark for update
			this.markForUpdate();

			// Mark for save
			this.markDirty();
		}

		return addedAmount;
	}

	/**
	 * Reads saved data.
	 * 
	 * @param data
	 */
	@Override
	protected void NBTRead( final NBTTagCompound data )
	{
		// Read time remaining
		if( data.hasKey( TileEssentiaVibrationChamber.NBTKEY_TIME_REMAINING ) )
		{
			this.timeRemaining = data.getInteger( TileEssentiaVibrationChamber.NBTKEY_TIME_REMAINING );
		}

		if( this.timeRemaining > 0 )
		{
			// Read processing speed
			if( data.hasKey( TileEssentiaVibrationChamber.NBTKEY_PROCESSING_SPEED ) )
			{
				this.processingSpeed = data.getInteger( TileEssentiaVibrationChamber.NBTKEY_PROCESSING_SPEED );
			}

			// Read power per tick
			if( data.hasKey( TileEssentiaVibrationChamber.NBTKEY_POWER_PER_TICK ) )
			{
				this.powerProducedPerProcessingTick = data.getDouble( TileEssentiaVibrationChamber.NBTKEY_POWER_PER_TICK );
			}

			// Read aspect
			if( data.hasKey( TileEssentiaVibrationChamber.NBTKEY_PROCESSING_ASPECT ) )
			{
				String aspectTag = data.getString( TileEssentiaVibrationChamber.NBTKEY_PROCESSING_ASPECT );
				this.processingAspect = Aspect.aspects.get( aspectTag );
				this.processingChanged = true;
			}

			// Mark to sync with client
			this.markForUpdate();
		}
	}

	/**
	 * Saves the data.
	 * 
	 * @param data
	 */
	@Override
	protected void NBTWrite( final NBTTagCompound data )
	{
		// Write time remaining
		data.setInteger( TileEssentiaVibrationChamber.NBTKEY_TIME_REMAINING, this.timeRemaining );

		if( this.timeRemaining > 0 )
		{
			// Write processing speed
			data.setInteger( TileEssentiaVibrationChamber.NBTKEY_PROCESSING_SPEED, this.processingSpeed );

			// Write power per tick
			data.setDouble( TileEssentiaVibrationChamber.NBTKEY_POWER_PER_TICK, this.powerProducedPerProcessingTick );

			// Is there an aspect?
			if( this.processingAspect != null )
			{
				// Write processing aspect
				data.setString( TileEssentiaVibrationChamber.NBTKEY_PROCESSING_ASPECT, this.processingAspect.getTag() );
			}
		}
	}

	/**
	 * Reads info from the network.
	 * 
	 * @param stream
	 */
	@Override
	protected void networkRead( final ByteBuf stream )
	{
		// Change?
		if( stream.readBoolean() )
		{
			// Read aspect
			this.processingAspect = AbstractPacket.readAspect( stream );
		}

	}

	/**
	 * Writes data to the network.
	 * 
	 * @param stream
	 */
	@Override
	protected void networkWrite( final ByteBuf stream )
	{
		// Write change flag
		stream.writeBoolean( this.processingChanged );

		if( this.processingChanged )
		{
			// Write aspect
			AbstractPacket.writeAspect( this.processingAspect, stream );
		}

		this.processingChanged = false;
	}

	/**
	 * Adds WAILA tooltip strings.
	 * 
	 * @param tooltip
	 */
	@Override
	public void addWailaInformation( final List<String> tooltip )
	{
		// Write stored
		if( this.hasStoredEssentia() )
		{
			tooltip.add( String.format( ThEStrings.GUi_VibrationChamber_Stored.getLocalized(), this.storedEssentia.stackSize,
				this.storedEssentia.aspect.getName() ) );
		}

		// Write processing
		if( this.processingAspect != null )
		{
			tooltip.add( String.format( ThEStrings.GUi_VibrationChamber_Processing.getLocalized(), this.processingAspect.getName() ) );
		}
	}

	/**
	 * How often to tick?
	 * 
	 * @param node
	 * @return
	 */
	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		// Half second, to 2 seconds.
		return new TickingRequest( TileEssentiaVibrationChamber.TICKRATE_MIN, TileEssentiaVibrationChamber.TICKRATE_MAX, false, false );

	}

	/**
	 * Called when the network ticks.
	 * 
	 * @param node
	 * @param ticksSinceLastCall
	 * @return
	 */
	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		boolean replenish = false;

		TickRateModulation rate = TickRateModulation.IDLE;

		// Is there any processing time remaining?
		if( this.timeRemaining > 0 )
		{
			// Process the essentia
			rate = this.doProcessingTick( ticksSinceLastCall );
		}

		// Is there anything stored?
		if( this.hasStoredEssentia() )
		{
			// Is the chamber idle?
			if( this.timeRemaining == 0 )
			{
				// Can essentia be consumed?
				if( this.consumeEssentia() )
				{
					rate = TickRateModulation.URGENT;
				}
			}

			// Is the buffer not full?
			if( this.storedEssentia.stackSize < TileEVCBase.MAX_ESSENTIA_STORED )
			{
				// Replenish if possible
				replenish = true;
			}
		}
		else
		{
			// Nothing is stored, keep moving the rotation timer
			this.suctionRotationTimer += ticksSinceLastCall;

			// Replenish if possible
			replenish = true;
		}

		// Does the essentia need to be replenished?
		if( replenish )
		{
			// Replenish essentia
			EssentiaTransportHelper.INSTANCE.takeEssentiaFromTransportNeighbors( this, this.worldObj, this.xCoord, this.yCoord, this.zCoord );
		}

		return rate;
	}

}
