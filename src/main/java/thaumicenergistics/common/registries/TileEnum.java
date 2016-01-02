package thaumicenergistics.common.registries;

import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.tiles.*;

public enum TileEnum
{
		EssentiaProvider ("TileEssentiaProvider", TileEssentiaProvider.class),
		InfusionProvider ("TileInfusionProvider", TileInfusionProvider.class),
		GearBox ("TileGearBox", TileGearBox.class),
		CellWorkbench ("TileEssentiaCellWorkbench", TileEssentiaCellWorkbench.class),
		ArcaneAssembler ("TileArcaneAssembler", TileArcaneAssembler.class),
		KnowledgeInscriber ("TileKnowledgeInscriber", TileKnowledgeInscriber.class),
		EssentiaVibrationChamber ("TileEssentiaVibrationChamber", TileEssentiaVibrationChamber.class),
		DistillationInscriber ("TileDistillationInscriber", TileDistillationEncoder.class);

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
