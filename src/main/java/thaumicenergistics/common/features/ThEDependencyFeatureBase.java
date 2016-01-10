package thaumicenergistics.common.features;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.FeatureRegistry;

/**
 * Defines a feature that depends on a configuration setting and/or specific
 * items to be available.
 * 
 * @author Nividica
 * 
 */
public abstract class ThEDependencyFeatureBase
	extends ThEFeatureBase
{
	/**
	 * True if the requirements have been checked.
	 */
	private boolean hasCheckedReqs = false;

	public ThEDependencyFeatureBase()
	{
		// Disabled by default
		super( false );
	}

	/**
	 * Ensures that all the required items are present.
	 * 
	 * @param items
	 * @return
	 */
	private final boolean checkItemReqs( final Object[] items )
	{
		// Are there no item reqs?
		if( items == null )
		{
			return true;
		}

		// Check all requirements
		for( Object item : items )
		{
			if( item == null )
			{
				// Something is null, feature can not be enabled.
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if this feature can be enabled based on ThE, TC and/or AE configs.
	 */
	protected abstract boolean checkConfigs( @Nonnull IThEConfig theConfig );

	/**
	 * Gets the features required items.
	 * 
	 * @return
	 */
	@Nullable
	protected abstract Object[] getItemReqs( CommonDependantItems cdi );

	@Override
	protected void registerAdditional()
	{
	}

	/**
	 * Evaluates the dependencies of the feature and enables it if possible.
	 * 
	 * @param cdi
	 */
	@Override
	public boolean isAvailable()
	{
		if( !this.hasCheckedReqs )
		{
			// Ask for the features config settings and required items
			this.available = this.checkConfigs( ThEApi.instance().config() )
							&& this.checkItemReqs( this.getItemReqs( FeatureRegistry.instance().cdi ) );
			this.hasCheckedReqs = true;
		}

		return super.isAvailable();
	}

}
