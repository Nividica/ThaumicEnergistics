package thaumicenergistics.registries;

import thaumicenergistics.tileentities.TileEssentiaProvider;
import thaumicenergistics.tileentities.TileInfusionProvider;
import cpw.mods.fml.common.registry.GameRegistry;

public final class TileEntities
{
	public static void registerTiles()
	{
		GameRegistry.registerTileEntity( TileEssentiaProvider.class, TileEssentiaProvider.TILE_ID );
		GameRegistry.registerTileEntity( TileInfusionProvider.class, TileInfusionProvider.TILE_ID );
	}
}
