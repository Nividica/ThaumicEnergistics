package thaumicenergistics.common.integration.tc;

import javax.annotation.Nullable;

public enum GolemUpgradeTypes
{
		Air (0),
		Earth (1),
		Fire (2),
		Water (3),
		Order (4),
		Entropy (5);

	/**
	 * Array of upgrades sorted by ID.
	 */
	public static final GolemUpgradeTypes allUpgrades[];
	static
	{
		GolemUpgradeTypes[] values = GolemUpgradeTypes.values();

		// Setup the array
		allUpgrades = new GolemUpgradeTypes[values.length];
		for( GolemUpgradeTypes upgrade : values )
		{
			allUpgrades[upgrade.upgradeID] = upgrade;
		}
	}

	/**
	 * Thaumcraft ID for the upgrade.
	 */
	public final byte upgradeID;

	private GolemUpgradeTypes( final int ID )
	{
		this.upgradeID = (byte)ID;
	}

	@Nullable
	public static GolemUpgradeTypes getUpgradeByID( final byte ID )
	{
		if( ( ID >= 0 ) && ( ID < allUpgrades.length ) )
		{
			return allUpgrades[ID];
		}
		return null;
	}
}
