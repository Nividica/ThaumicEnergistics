package thaumicenergistics.registries;

import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.tileentities.TileArcaneAssembler;
import thaumicenergistics.tileentities.TileEssentiaCellWorkbench;
import thaumicenergistics.tileentities.TileEssentiaProvider;
import thaumicenergistics.tileentities.TileGearBox;
import thaumicenergistics.tileentities.TileInfusionProvider;

public enum TileEnum
{
		EssentiaProvider ("TileEssentiaProvider", TileEssentiaProvider.class),
		InfusionProvider ("TileInfusionProvider", TileInfusionProvider.class),
		GearBox ("TileGearBox", TileGearBox.class),
		CellWorkbench ("TileEssentiaCellWorkbench", TileEssentiaCellWorkbench.class),
		ArcaneAssembler ("TileArcaneAssembler", TileArcaneAssembler.class);

	/**
	 * Unique ID of the tile entity
	 */
	private String ID;

	/**
	 * Tile entity class.
	 */
	private Class clazz;

	private TileEnum( final String ID, final Class clazz )
	{
		this.ID = ID;
		this.clazz = clazz;
	}

	// TODO: Drop legacy support at version 1.0
	@Deprecated
	public String getOldTileID()
	{
		return this.ID;
	}

	/**
	 * Gets the tile entity's class.
	 */
	public Class getTileClass()
	{
		return this.clazz;
	}

	/**
	 * Gets the tile entity's ID.
	 * 
	 * @return
	 */
	public String getTileID()
	{
		return ThaumicEnergistics.MOD_ID + "." + this.ID;
	}
}
