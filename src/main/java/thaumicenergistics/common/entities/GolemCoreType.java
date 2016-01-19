package thaumicenergistics.common.entities;

import javax.annotation.Nullable;

public enum GolemCoreType
{
		Fill (0),
		Empty (1),
		Gather (2),
		Harvest (3),
		Guard (4),
		Liquid (5),
		Essentia (6),
		Lumber (7),
		Use (8),
		Butcher (9),
		Sorting (10),
		Fish (11);

	/**
	 * Array of cores sorted by ID.
	 */
	public static final GolemCoreType allCores[];
	static
	{
		GolemCoreType[] values = GolemCoreType.values();

		// Setup the array
		allCores = new GolemCoreType[values.length];
		for( GolemCoreType core : values )
		{
			allCores[core.coreID] = core;
		}
	}

	/**
	 * Thaumcraft core ID
	 */
	public final byte coreID;

	private GolemCoreType( final int ID )
	{
		this.coreID = (byte)ID;
	}

	@Nullable
	public static GolemCoreType getCoreByID( final byte ID )
	{
		if( ID >= 0 && ID < allCores.length )
		{
			return allCores[ID];
		}
		return null;
	}
}
