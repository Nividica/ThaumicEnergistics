package thaumicenergistics.common.features;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines a feature that has Thaumcraft research.
 * 
 * @author Nividica
 * 
 */
public abstract class ThEThaumcraftResearchFeature
	extends ThEDependencyFeatureBase
{
	/**
	 * The research key for this research.
	 */
	public final String researchKey;

	/**
	 * Creates the feature with the specified research key.
	 * 
	 * @param parent
	 */
	public ThEThaumcraftResearchFeature( @Nonnull final String researchKey )
	{
		this.researchKey = researchKey;
	}

	/**
	 * Gets the parent for this feature, if there is one.
	 * 
	 * @return
	 */
	@Nullable
	protected abstract ThEThaumcraftResearchFeature getParentFeature();

	@Override
	protected void registerAdditional()
	{
		// Call super
		super.registerAdditional();

		// Register Thaumcraft research.
		this.registerResearch();

		// Register pseudo parents
		this.registerPseudoParents();
	}

	/**
	 * Registers the research with Thaumcraft, if it has any.
	 */
	protected abstract void registerResearch();

	/**
	 * Gets the research key of the first valid parent that can be found.
	 * If the feature in question is not enabled, it will pass the call to its
	 * parent.
	 * 
	 * @param includeSelf
	 * @return
	 */
	public final String getFirstValidParentKey( final boolean includeSelf )
	{
		if( this.isAvailable() && includeSelf )
		{
			return this.researchKey;
		}
		else if( this.getParentFeature() != null )
		{
			// Pass to parent
			return this.getParentFeature().getFirstValidParentKey( true );
		}
		else
		{
			// No parent
			return "";
		}
	}

	/**
	 * If the research item has any pseudo parents, register them here.
	 */
	public abstract void registerPseudoParents();
}
