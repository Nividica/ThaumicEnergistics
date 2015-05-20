package thaumicenergistics.features;

public abstract class AbstractBasicFeature
{

	/**
	 * Is the feature available.
	 */
	private boolean available = false;

	public AbstractBasicFeature( final boolean initiallyAvailable )
	{
		this.setAvailable( initiallyAvailable );
	}

	/**
	 * Set if the feature is available.
	 * 
	 * @param available
	 */
	protected void setAvailable( final boolean available )
	{
		this.available = available;
	}

	/**
	 * Is the feature available?
	 * 
	 * @return
	 */
	public boolean isAvailable()
	{
		return this.available;
	}

}
