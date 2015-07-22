package thaumicenergistics;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.grid.GridEssentiaCache;
import thaumicenergistics.grid.IEssentiaGrid;
import thaumicenergistics.gui.ThEGuiHandler;
import thaumicenergistics.integration.IntegrationCore;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.integration.tc.EssentiaTileContainerHelper;
import thaumicenergistics.network.ChannelHandler;
import thaumicenergistics.proxy.CommonProxy;
import thaumicenergistics.registries.AEAspectRegister;
import thaumicenergistics.util.ThELog;
import appeng.api.AEApi;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = ThaumicEnergistics.MOD_ID, name = "Thaumic Energistics", version = ThaumicEnergistics.VERSION, dependencies = "required-after:appliedenergistics2@[rv2-stable-1,);required-after:Thaumcraft@[4.2.3.5,);after:Waila;after:extracells")
public class ThaumicEnergistics
{
	/**
	 * What loading state Thaumic Energistics has completed.
	 */
	private static LoaderState ThEState = LoaderState.NOINIT;

	/**
	 * String ID of the mod.
	 */
	public static final String MOD_ID = "thaumicenergistics";

	/**
	 * Current version of the mod.
	 */
	public static final String VERSION = "0.8.10.8"; // Note: don't forget to change the build.gradle file as well

	/**
	 * Singleton instance
	 */
	@Instance(value = ThaumicEnergistics.MOD_ID)
	public static ThaumicEnergistics INSTANCE;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide = "thaumicenergistics.proxy.ClientProxy", serverSide = "thaumicenergistics.proxy.CommonProxy")
	public static CommonProxy proxy;

	/**
	 * Configuration.
	 */
	public static IThEConfig config;

	/**
	 * Creative tab.
	 */
	public static CreativeTabs ThETab = new CreativeTabs( "ThaumicEnergistics" )
	{

		@Override
		public ItemStack getIconItemStack()
		{
			return ThEApi.instance().blocks().InfusionProvider.getStack();
		}

		@Override
		public Item getTabIconItem()
		{
			return ThEApi.instance().blocks().InfusionProvider.getItem();
		}
	};

	/**
	 * Gets what state/phase of loading that ThE has <strong>completed</strong>.<br>
	 * NOINIT: PreInit has not yet been called<br>
	 * PREINITIALIZATION: PreInit has finished.<br>
	 * INITIALIZATION: Load has finished.<br>
	 * POSTINITIALIZATION: PostInit has finished, ThE is fully loaded.
	 * 
	 * @return
	 */
	public static LoaderState getLoaderState()
	{
		return ThaumicEnergistics.ThEState;
	}

	/**
	 * Called after the preInit event, and before the post init event.
	 * 
	 * @param event
	 */
	@EventHandler
	public void load( final FMLInitializationEvent event )
	{
		// Mark that ThE is in Init
		long startTime = ThELog.beginSection( "Load" );

		// Register block renderers
		ThaumicEnergistics.proxy.registerRenderers();

		// Register tile entities
		ThaumicEnergistics.proxy.registerTileEntities();

		// Register network messages
		ChannelHandler.registerMessages();

		// Register integration
		IntegrationCore.init();

		AEApi.instance().registries().gridCache().registerGridCache( IEssentiaGrid.class, GridEssentiaCache.class );

		// Mark that ThE has finished Init
		ThELog.endSection( "Load", startTime );
		ThaumicEnergistics.ThEState = LoaderState.INITIALIZATION;
	}

	/**
	 * Called after the load event.
	 * 
	 * @param event
	 */
	@EventHandler
	public void postInit( final FMLPostInitializationEvent event )
	{
		// Mark that ThE is in PostInit
		long startTime = ThELog.beginSection( "PostInit" );

		// Register the standard thaumcraft container items and tiles
		EssentiaTileContainerHelper.INSTANCE.registerDefaultContainers();
		EssentiaItemContainerHelper.INSTANCE.registerDefaultContainers();

		// Register features
		ThaumicEnergistics.proxy.registerFeatures();

		// Register my tiles with SpatialIO
		ThaumicEnergistics.proxy.registerSpatialIOMovables();

		// Register fluids
		ThaumicEnergistics.proxy.registerFluids();

		// Give AE items aspects
		try
		{
			AEAspectRegister.INSTANCE.registerAEAspects();
		}
		catch( Exception e )
		{
			ThELog.warning( "Unable to finish aspect registration due to exception:%n%s%n", e.getMessage() );
		}

		// Mark that ThE has finished PostInit
		ThELog.endSection( "PostInit", startTime );
		ThaumicEnergistics.ThEState = LoaderState.POSTINITIALIZATION;
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
		// Mark that ThE is in PreInit
		long startTime = ThELog.beginSection( "PreInit" );

		// Sync with config
		ThaumicEnergistics.config = ConfigurationHandler.loadAndSyncConfigFile( event.getSuggestedConfigurationFile() );

		// Set the instance
		ThaumicEnergistics.INSTANCE = this;

		// Register the gui handler
		NetworkRegistry.INSTANCE.registerGuiHandler( this, new ThEGuiHandler() );

		// Register items
		ThaumicEnergistics.proxy.registerItems();

		// Register blocks
		ThaumicEnergistics.proxy.registerBlocks();

		// Mark that ThE has finished PreInit
		ThELog.endSection( "PreInit", startTime );
		ThaumicEnergistics.ThEState = LoaderState.PREINITIALIZATION;
	}

	// TODO: General: Sync server configs
	// TODO: General: Use area packets over tile sync when possible

}
