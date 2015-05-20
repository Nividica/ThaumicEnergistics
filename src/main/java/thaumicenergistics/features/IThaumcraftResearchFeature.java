package thaumicenergistics.features;

public interface IThaumcraftResearchFeature
{
	/**
	 * Gets the key of the first valid parent that can be found.
	 * 
	 * @return
	 */
	public String getFirstValidParentKey( boolean includeSelf );

	/**
	 * Is the feature available?
	 * 
	 * @return
	 */
	public boolean isAvailable();

	/**
	 * Registers the research with Thaumcraft.
	 */
	public void registerResearch();
}
