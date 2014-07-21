package thaumicenergistics.registries;

import thaumicenergistics.tileentities.TileEssentiaProvider;
import cpw.mods.fml.common.registry.GameRegistry;

public final class TileEntities
{
	public static void registerTiles()
	{
		GameRegistry.registerTileEntity( TileEssentiaProvider.class, TileEssentiaProvider.TILE_ID );
	}
}
