package thaumicenergistics;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.ImmutablePair;
import thaumicenergistics.gui.GuiHandler;
import thaumicenergistics.integration.IntegrationCore;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.integration.tc.EssentiaTileContainerHelper;
import thaumicenergistics.network.ChannelHandler;
import thaumicenergistics.proxy.CommonProxy;
import thaumicenergistics.registries.ItemEnum;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = ThaumicEnergistics.MOD_ID, name = "Thaumic Energistics", version = ThaumicEnergistics.VERSION, dependencies = "required-after:appliedenergistics2;required-after:Thaumcraft;after:Waila")
public class ThaumicEnergistics
{
	/*
	private static class IncorrectAEVersion
		extends Exception
	{
		private static final long serialVersionUID = 5768931808748164005L;

		public IncorrectAEVersion( final int rv, final String release, final int build )
		{
			super( "Thaumic Energistics is unable to load. Your version of Applied Energistics is incompatible. Please use version rv" + rv + "-" +
							release + "-" + build + " or equivalent." );
		}
	}
	*/

	/**
	 * String ID of the mod.
	 */
	public static final String MOD_ID = "thaumicenergistics";

	/**
	 * Current version of the mod.
	 */
	public static final String VERSION = "0.7.1b"; // Note: don't forget to change the build.gradle file as well

	/**
	 * Singleton instance
	 */
	@Instance(value = ThaumicEnergistics.MOD_ID)
	public static ThaumicEnergistics instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide = "thaumicenergistics.proxy.ClientProxy", serverSide = "thaumicenergistics.proxy.CommonProxy")
	public static CommonProxy proxy;

	/**
	 * Creative tab that displays this mods items
	 */
	public static CreativeTabs ModTab = new CreativeTabs( "ThaumicEnergistics" )
	{

		@Override
		public ItemStack getIconItemStack()
		{
			return new ItemStack( ItemEnum.STORAGE_CASING.getItem() );
		}

		@Override
		public Item getTabIconItem()
		{
			return ItemEnum.STORAGE_CASING.getItem();
		}
	};

	/**
	 * Mod configuration
	 */
	public static Configuration configFile;

	/**
	 * Checks if the installed version of applied energistics is at least the
	 * version this mod was compiled against.
	 * 
	 * @throws IncorrectAEVersion
	 */
	/*
	private static void AE2VersionCheck() throws IncorrectAEVersion
	{
		List<String> releases = new ArrayList<String>( 3 );
		releases.add( "alpha" );
		releases.add( "beta" );
		releases.add( "stable" );
		int revisionCompiledAgainst = 1;
		int releaseCompiledAgainst = 2;
		int buildCompiledAgainst = 1;

		Level vLevel = Level.OFF;

		// Get the mod info
		cpw.mods.fml.common.Mod aeMod = AppEng.class.getAnnotation( cpw.mods.fml.common.Mod.class );

		// Get the version
		String[] version = aeMod.version().split( "-" );

		// Get the revision
		int revision = Integer.parseInt( version[0].substring( 2 ) );

		// Get the release
		int release = releases.indexOf( version[1] );

		// Get the build
		int build = Integer.parseInt( version[2] );

		// Check the revision
		if( revision < revisionCompiledAgainst )
		{
			vLevel = Level.ERROR;
		}
		else if( revision > revisionCompiledAgainst )
		{
			vLevel = Level.WARN;
		}
		else
		{
			// Check the release
			if( release < releaseCompiledAgainst )
			{
				vLevel = Level.ERROR;
			}
			else if( release > releaseCompiledAgainst )
			{
				vLevel = Level.WARN;
			}
			else
			{
				// Check the build
				if( build < buildCompiledAgainst )
				{
					vLevel = Level.ERROR;
				}
				else if( build > buildCompiledAgainst )
				{
					// Only warn if release is not stable
					if( releaseCompiledAgainst < 2 )
					{
						vLevel = Level.WARN;
					}
				}
			}
		}

		if( vLevel == Level.ERROR )
		{
			throw new IncorrectAEVersion( revisionCompiledAgainst, releases.get( releaseCompiledAgainst ), buildCompiledAgainst );
		}

		if( vLevel == Level.WARN )
		{
			FMLLog.bigWarning( "Warning: This verison of Thaumic Energistics was compiled for version rv%d-%s-%d"
							+ " of Applied Energistics 2. There may be compatibiliy issues with this verison.", revisionCompiledAgainst,
				releases.get( releaseCompiledAgainst ), buildCompiledAgainst );
		}

	}
	*/

	/**
	 * Synchronizes the config file and the settings.
	 */
	public static void synchronizeConfigFile()
	{
		// Sync the essentia fluid conversion ratio
		EssentiaConversionHelper.CONVERSION_MULTIPLIER = ThaumicEnergistics.configFile
						.getInt(
							"Essentia Fluid Ratio",
							Configuration.CATEGORY_GENERAL,
							EssentiaConversionHelper.CONVERSION_MULTIPLIER,
							1,
							10000,
							"Controls the conversion ratio of essentia/fluid. 1 essentia is converted to this many milibuckets of fluid. "
											+ "Please be aware that this value effects how much fluid is transferred through the AE system, which also effects transfer speed and power consumption. "
											+ "Very high values may make it impossible to use fluid transfer devices such as the ME IO Port, or anything from EC2." );

		// Has the config file changed?
		if( ThaumicEnergistics.configFile.hasChanged() )
		{
			// Save it
			ThaumicEnergistics.configFile.save();
		}
	}

	/**
	 * Marks the time and the stage name for reporting later.
	 * 
	 * @param stageName
	 * @return
	 */
	private ImmutablePair<Long, String> beginLoadStageTracking( final String stageName )
	{
		// Print begin
		FMLLog.info( "%s: Begining %s()", ThaumicEnergistics.MOD_ID, stageName );

		// Return the current time
		return new ImmutablePair<Long, String>( System.currentTimeMillis(), stageName );
	}

	/**
	 * Reports the time the specified stage took.
	 * 
	 * @param beginInfo
	 */
	private void endLoadStageTracking( final ImmutablePair<Long, String> beginInfo )
	{
		// Calculate time
		long time = System.currentTimeMillis() - beginInfo.left;

		// Print end
		FMLLog.info( "%s: Finished %s(), Took: %dms", ThaumicEnergistics.MOD_ID, beginInfo.right, time );
	}

	/**
	 * Called after the preInit event, and before the post init event.
	 * 
	 * @param event
	 */
	@EventHandler
	public void load( final FMLInitializationEvent event )
	{
		ImmutablePair<Long, String> t = this.beginLoadStageTracking( "load" );

		// Register block renderers
		ThaumicEnergistics.proxy.registerRenderers();

		// Register tile entities
		ThaumicEnergistics.proxy.registerTileEntities();

		// Register network messages
		ChannelHandler.registerMessages();

		// Register integration
		IntegrationCore.init();

		this.endLoadStageTracking( t );
	}

	/**
	 * Called after the load event.
	 * 
	 * @param event
	 */
	@EventHandler
	public void postInit( final FMLPostInitializationEvent event )
	{
		ImmutablePair<Long, String> t = this.beginLoadStageTracking( "postInit" );

		// Register the standard thaumcraft container items and tiles
		EssentiaTileContainerHelper.instance.registerThaumcraftContainers();
		EssentiaItemContainerHelper.instance.registerThaumcraftContainers();

		// Register my tiles with SpatialIO
		ThaumicEnergistics.proxy.registerSpatialIOMovables();

		// Register TC research
		ThaumicEnergistics.proxy.registerResearch();

		// Register fluids
		ThaumicEnergistics.proxy.registerFluids();

		this.endLoadStageTracking( t );
	}

	/**
	 * Called before the load event.
	 * 
	 * @param event
	 * @throws Exception
	 */
	@EventHandler
	public void preInit( final FMLPreInitializationEvent event ) throws Exception
	{
		ImmutablePair<Long, String> t = this.beginLoadStageTracking( "preInit" );

		// Get the configuration file
		ThaumicEnergistics.configFile = new Configuration( event.getSuggestedConfigurationFile() );

		// Sync with config
		ThaumicEnergistics.synchronizeConfigFile();

		// Check the AE2 version
		//ThaumicEnergistics.AE2VersionCheck();

		// Set the instance
		ThaumicEnergistics.instance = this;

		// Register the gui handler
		NetworkRegistry.INSTANCE.registerGuiHandler( this, new GuiHandler() );

		// Register items
		ThaumicEnergistics.proxy.registerItems();

		// Register blocks
		ThaumicEnergistics.proxy.registerBlocks();

		// Register recipes
		ThaumicEnergistics.proxy.registerRecipes();

		this.endLoadStageTracking( t );
	}

}
