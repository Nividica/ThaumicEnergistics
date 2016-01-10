package thaumicenergistics.common.features;


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
	public ThEDependencyFeatureBase()
	{
		// Inform the super that we are not enabled by default
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
	protected abstract boolean checkConfigs();

	/**
	 * Gets the features required items.
	 * 
	 * @return
	 */
	protected abstract Object[] getItemReqs( CommonDependantItems cdi );

	/**
	 * Evaluates the dependencies of the feature and enables it if possible.
	 * 
	 * @param cdi
	 */
	public final void evaluateDependencies( final CommonDependantItems cdi )
	{
		// Ask for the features config settings and required items
		this.setAvailable( this.checkConfigs() && this.checkItemReqs( this.getItemReqs( cdi ) ) );
	}

}
