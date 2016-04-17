package thaumicenergistics.common.grid;

import java.util.Collection;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import thaumicenergistics.api.grid.IMEEssentiaMonitorReceiver;
import thaumicenergistics.api.storage.IAspectStack;

/**
 * Decouples the essentia grid from it's power grid.
 *
 * @author Nividica
 *
 */
public class EssentiaPassThroughMonitor
	extends EssentiaMonitor
{
	private final IMEEssentiaMonitor internalMonitor;

	public EssentiaPassThroughMonitor( final IMEEssentiaMonitor source, final IEnergyGrid energyGrid )
	{
		// Call super
		super();

		// Set the source
		this.internalMonitor = source;

		// Set the energy grid
		this.energyGrid = energyGrid;
	}

	@Override
	public void addListener( final IMEEssentiaMonitorReceiver listener, final Object verificationToken )
	{
		this.internalMonitor.addListener( listener, verificationToken );
	}

	@Override
	public long extractEssentia( final Aspect aspect, final long amount, final Actionable mode, final BaseActionSource source, final boolean powered )
	{
		if( powered )
		{
			// Simulate power extraction
			double powerRequest = EssentiaMonitor.AE_PER_ESSENTIA * amount;
			double powerReceived = this.energyGrid.extractAEPower( powerRequest, Actionable.SIMULATE, PowerMultiplier.CONFIG );

			// Was enough power extracted?
			if( powerReceived < powerRequest )
			{
				// Not enough power
				return 0;
			}
		}

		// Extract
		long extractedAmount = this.internalMonitor.extractEssentia( aspect, amount, mode, source, false );

		// Extract power if modulating
		if( ( extractedAmount > 0 ) && ( powered ) && ( mode == Actionable.MODULATE ) )
		{
			this.energyGrid.extractAEPower( EssentiaMonitor.AE_PER_ESSENTIA * extractedAmount, Actionable.MODULATE, PowerMultiplier.CONFIG );
		}

		return extractedAmount;
	}

	@Override
	public long getEssentiaAmount( final Aspect aspect )
	{
		return this.internalMonitor.getEssentiaAmount( aspect );
	}

	@Override
	public Collection<IAspectStack> getEssentiaList()
	{
		return this.internalMonitor.getEssentiaList();
	}

	@Override
	public long injectEssentia( final Aspect aspect, final long amount, final Actionable mode, final BaseActionSource source, final boolean powered )
	{
		if( powered )
		{
			// Simulate power extraction
			double powerRequest = EssentiaMonitor.AE_PER_ESSENTIA * amount;
			double powerReceived = this.energyGrid.extractAEPower( powerRequest, Actionable.SIMULATE, PowerMultiplier.CONFIG );

			// Was enough power extracted?
			if( powerReceived < powerRequest )
			{
				// Not enough power
				return 0;
			}
		}

		// Inject
		long rejectedAmount = this.internalMonitor.injectEssentia( aspect, amount, mode, source, false );
		long injectedAmount = amount - rejectedAmount;

		// Extract power if modulating
		if( ( injectedAmount > 0 ) && ( powered ) && ( mode == Actionable.MODULATE ) )
		{
			this.energyGrid.extractAEPower( EssentiaMonitor.AE_PER_ESSENTIA * injectedAmount, Actionable.MODULATE, PowerMultiplier.CONFIG );
		}

		return rejectedAmount;
	}

	@Override
	public void removeListener( final IMEEssentiaMonitorReceiver listener )
	{
		this.internalMonitor.removeListener( listener );
	}

}
