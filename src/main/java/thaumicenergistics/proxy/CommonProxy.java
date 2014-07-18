package thaumicenergistics.proxy;

import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.registries.ItemEnum;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy
{
	public void registerBlocks()
	{
	}

	public void registerFluids()
	{
		GaseousEssentia.registerGases();
	}

	public void registerItems()
	{

		for( ItemEnum item : ItemEnum.values() )
		{
			GameRegistry.registerItem( item.getItem(), item.getInternalName() );
		}

	}

	public void registerMovables()
	{
	}

	public void registerRenderers()
	{
	}

	public void registerTileEntities()
	{
	}

}
