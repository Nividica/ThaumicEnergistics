package thaumicenergistics.features;

import thaumicenergistics.registries.FeatureRegistry;

/**
 * Defines a feature that depends on a configuration setting and/or specific
 * items to be available.
 * 
 * @author Nividica
 * 
 */
public abstract class AbstractDependencyFeature
	extends AbstractBasicFeature
{
	public AbstractDependencyFeature( final FeatureRegistry fr )
	{
		// Inform the super that we are not enabled by default
		super( false );

		// Ask for the features config settings and required items
		if( this.checkConfigs() && this.checkItemReqs( this.getItemReqs( fr.getCommonItems() ) ) )
		{
			// This feature is enabled
			this.setAvailable( true );
		}
	}

	/**
	 * Ensures that all the required items are present.
	 * 
	 * @param items
	 * @return
	 */
	private boolean checkItemReqs( final Object[] items )
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
	protected abstract boolean checkConfigs();

	/**
	 * Gets the features required items.
	 * 
	 * @return
	 */
	protected abstract Object[] getItemReqs( CommonDependantItems cdi );

}
