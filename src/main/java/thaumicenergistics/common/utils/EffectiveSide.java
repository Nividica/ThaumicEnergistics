package thaumicenergistics.common.utils;

import cpw.mods.fml.common.FMLCommonHandler;

public final class EffectiveSide
{
	/**
	 * Cache the handler
	 */
	private static FMLCommonHandler FCH = FMLCommonHandler.instance();

	/**
	 * True if the thread executing this code is client side.
	 * 
	 * @return
	 */
	public static final boolean isClientSide()
	{
		return FCH.getEffectiveSide().isClient();
	}

	/**
	 * True if the thread executing this code is server side.
	 * 
	 * @return
	 */
	public static final boolean isServerSide()
	{
		return FCH.getEffectiveSide().isServer();
	}
}
