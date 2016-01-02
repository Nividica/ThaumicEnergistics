package thaumicenergistics.common.features;

import java.util.EnumSet;
import thaumicenergistics.common.registries.ResearchRegistry.PseudoResearchTypes;

/**
 * Defines what a basic feature of ThE is.
 * 
 * @author Nividica
 * 
 */
public abstract class AbstractBasicFeature
{

	/**
	 * Is the feature available.
	 */
	private boolean available = false;

	/**
	 * Has the feature been called to register itself?
	 */
	private boolean hasRegistered = false;

	/**
	 * Creates the feature.
	 * 
	 * @param initiallyAvailable
	 */
	public AbstractBasicFeature( final boolean initiallyAvailable )
	{
		this.setAvailable( initiallyAvailable );
	}

	/**
	 * Registers the features crafting recipes, if it has any.
	 */
	protected void registerCrafting()
	{
	}

	/**
	 * Registers the research with Thaumcraft, if it has any.
	 */
	protected void registerResearch()
	{
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
	 * Gets the research key of the first valid parent that can be found.
	 * If the feature in question is not enabled, it will pass the call to its
	 * parent.
	 * 
	 * Only valid for Thaumcraft features.
	 * 
	 * @return
	 */
	public String getFirstValidParentKey( final boolean includeSelf )
	{
		return "";
	}

	/**
	 * Gets the features pseudo-parent(s) if it has any.
	 * 
	 * Only valid for Thaumcraft features.
	 * 
	 * @return
	 */
	public EnumSet<PseudoResearchTypes> getPseudoParentTypes()
	{
		return EnumSet.noneOf( PseudoResearchTypes.class );
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

	/**
	 * Asks the feature to register itself.
	 */
	public final void registerFeature()
	{
		// Is the feature enabled and has not yet been registered?
		if( this.isAvailable() && !this.hasRegistered )
		{
			// Register crafting recipe(s)
			this.registerCrafting();

			// Register Thaumcraft research.
			this.registerResearch();

			// Mark that this feature has been registered
			this.hasRegistered = true;
		}
	}

}
