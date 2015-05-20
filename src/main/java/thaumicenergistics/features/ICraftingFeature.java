package thaumicenergistics.features;

public interface ICraftingFeature
{
	/**
	 * Is the feature available?
	 * 
	 * @return
	 */
	public boolean isAvailable();

	/**
	 * Registers the features crafting recipes.
	 */
	public void registerCrafting();
}
