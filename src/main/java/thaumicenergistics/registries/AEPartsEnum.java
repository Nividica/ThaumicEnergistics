package thaumicenergistics.registries;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.parts.AEPartArcaneCraftingTerminal;
import thaumicenergistics.parts.AbstractAEPartBase;
import thaumicenergistics.parts.AEPartEssentiaExportBus;
import thaumicenergistics.parts.AEPartEssentiaImportBus;
import thaumicenergistics.parts.AEPartEssentiaLevelEmitter;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;
import thaumicenergistics.parts.AEPartEssentiaTerminal;
import appeng.api.config.Upgrades;

public enum AEPartsEnum
{
		EssentiaImportBus ("essentia.ImportBus", AEPartEssentiaImportBus.class, ThaumicEnergistics.MOD_ID + ".group.essentia.transport", new Pair[] {
						generatePair( Upgrades.CAPACITY, 2 ), generatePair( Upgrades.REDSTONE, 1 ), generatePair( Upgrades.SPEED, 2 ) }),

		EssentiaLevelEmitter ("essentia.levelemitter", AEPartEssentiaLevelEmitter.class),

		EssentiaStorageBus ("essentia.StorageBus", AEPartEssentiaStorageBus.class, null, new Pair[] { generatePair( Upgrades.INVERTER, 1 ) }),

		EssentiaExportBus ("essentia.ExportBus", AEPartEssentiaExportBus.class, ThaumicEnergistics.MOD_ID + ".group.essentia.transport", new Pair[] {
						generatePair( Upgrades.CAPACITY, 2 ), generatePair( Upgrades.REDSTONE, 1 ), generatePair( Upgrades.SPEED, 2 ) }),

		EssentiaTerminal ("essentia.terminal", AEPartEssentiaTerminal.class),

		ArcaneCraftingTerminal ("arcane.crafting.terminal", AEPartArcaneCraftingTerminal.class);

	private String unlocalizedName;

	private Class<? extends AbstractAEPartBase> partClass;

	private String groupName;

	private Map<Upgrades, Integer> upgrades = new HashMap<Upgrades, Integer>();

	private AEPartsEnum( String unlocalizedName, Class<? extends AbstractAEPartBase> partClass )
	{
		this( unlocalizedName, partClass, null );
	}

	private AEPartsEnum( String unlocalizedName, Class<? extends AbstractAEPartBase> partClass, String groupName )
	{
		// Add the mod name and parts sub-folder to the name
		this.unlocalizedName = ThaumicEnergistics.MOD_ID + ".aeparts." + unlocalizedName;

		// Set the class
		this.partClass = partClass;

		// Set the group name
		this.groupName = groupName;
	}

	private AEPartsEnum( String unlocalizedName, Class<? extends AbstractAEPartBase> partClass, String groupName, Pair<Upgrades, Integer> ... upgrades )
	{
		this( unlocalizedName, partClass, groupName );

		for( Pair<Upgrades, Integer> pair : upgrades )
		{
			// Add the upgrade to the map
			this.upgrades.put( pair.getKey(), pair.getValue() );
		}

	}

	private static Pair<Upgrades, Integer> generatePair( Upgrades upgrade, int maximum )
	{
		return new ImmutablePair<Upgrades, Integer>( upgrade, Integer.valueOf( maximum ) );
	}

	/**
	 * Gets an AEPart based on an item stacks damage value.
	 * 
	 * @param damageValue
	 * @return
	 */
	public static AEPartsEnum getPartFromDamageValue( ItemStack itemStack )
	{
		// Get the values
		AEPartsEnum[] values = AEPartsEnum.values();

		// Clamp the damage
		int clamped = MathHelper.clamp_int( itemStack.getItemDamage(), 0, values.length );

		// Get the part
		return values[clamped];
	}

	public static int getPartID( Class<? extends AbstractAEPartBase> partClass )
	{
		int id = -1;

		// Get the values
		AEPartsEnum[] values = AEPartsEnum.values();

		// Check each part
		for( int i = 0; i < values.length; i++ )
		{
			// Is it the same as the specified part?
			if( values[i].getPartClass().equals( partClass ) )
			{
				// Found the id, set and stop searching
				id = i;
				break;
			}
		}

		// Return the id
		return id;

	}

	public AbstractAEPartBase createPartInstance( ItemStack itemStack ) throws InstantiationException, IllegalAccessException
	{
		// Create a new instance of the part
		AbstractAEPartBase part = this.partClass.newInstance();

		// Setup based on the itemStack
		part.setupPartFromItem( itemStack );

		// Return the newly created part
		return part;

	}

	/**
	 * Gets the group associated with this part.
	 * 
	 * @return
	 */
	public String getGroupName()
	{
		return this.groupName;
	}

	/**
	 * Gets the class associated with this part.
	 * 
	 * @return
	 */
	public Class<? extends AbstractAEPartBase> getPartClass()
	{
		return this.partClass;
	}

	public ItemStack getStack()
	{
		return ItemEnum.PART_ITEM.getItemStackWithDamage( this.ordinal() );
	}

	public String getStatName()
	{
		return StatCollector.translateToLocal( this.unlocalizedName + ".name" );
	}

	/**
	 * Gets the unlocalized name for this part.
	 * 
	 * @return
	 */
	public String getUnlocalizedName()
	{
		return this.unlocalizedName;
	}

	public Map<Upgrades, Integer> getUpgrades()
	{
		return this.upgrades;
	}
}
