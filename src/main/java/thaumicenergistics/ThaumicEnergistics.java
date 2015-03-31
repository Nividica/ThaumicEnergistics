package thaumicenergistics;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.api.IConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.gui.ThEGuiHandler;
import thaumicenergistics.integration.IntegrationCore;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.integration.tc.EssentiaTileContainerHelper;
import thaumicenergistics.network.ChannelHandler;
import thaumicenergistics.proxy.CommonProxy;
import thaumicenergistics.registries.AEAspectRegister;
import thaumicenergistics.util.ThELog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = ThaumicEnergistics.MOD_ID, name = "Thaumic Energistics", version = ThaumicEnergistics.VERSION, dependencies = "required-after:appliedenergistics2@[rv2-beta-7,);required-after:Thaumcraft@[4.2.3.0,);after:Waila;after:extracells")
public class ThaumicEnergistics
{
	/**
	 * String ID of the mod.
	 */
	public static final String MOD_ID = "thaumicenergistics";

	/**
	 * Current version of the mod.
	 */
	public static final String VERSION = "0.8.9.1b-rv2"; // Note: don't forget to change the build.gradle file as well

	/**
	 * Singleton instance
	 */
	@Instance(value = ThaumicEnergistics.MOD_ID)
	public static ThaumicEnergistics instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide = "thaumicenergistics.proxy.ClientProxy", serverSide = "thaumicenergistics.proxy.CommonProxy")
	public static CommonProxy proxy;

	/**
	 * Mod configuration
	 */
	public static IConfig config;

	/**
	 * Creative tab that displays this mods items
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
	 * Called after the preInit event, and before the post init event.
	 * 
	 * @param event
	 */
	@EventHandler
	public void load( final FMLInitializationEvent event )
	{
		long startTime = ThELog.beginSection( "Load" );

		// Register block renderers
		ThaumicEnergistics.proxy.registerRenderers();

		// Register tile entities
		ThaumicEnergistics.proxy.registerTileEntities();

		// Register network messages
		ChannelHandler.registerMessages();

		// Register integration
		IntegrationCore.init();

		ThELog.endSection( "Load", startTime );
	}

	/**
	 * Called after the load event.
	 * 
	 * @param event
	 */
	@EventHandler
	public void postInit( final FMLPostInitializationEvent event )
	{
		long startTime = ThELog.beginSection( "PostInit" );

		// Register the standard thaumcraft container items and tiles
		EssentiaTileContainerHelper.instance.registerThaumcraftContainers();
		EssentiaItemContainerHelper.instance.registerThaumcraftContainers();

		// Register recipes
		ThaumicEnergistics.proxy.registerRecipes();

		// Register my tiles with SpatialIO
		ThaumicEnergistics.proxy.registerSpatialIOMovables();

		// Register TC research
		ThaumicEnergistics.proxy.registerResearch();

		// Register fluids
		ThaumicEnergistics.proxy.registerFluids();

		// Give AE items aspects
		try
		{
			AEAspectRegister.instance.registerAEAspects();
		}
		catch( Exception e )
		{
			ThELog.warning( "Unable to finish aspect registration due to exception:%n%s%n", e.getMessage() );
		}

		ThELog.endSection( "PostInit", startTime );
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
		long startTime = ThELog.beginSection( "PreInit" );

		// Sync with config
		ThaumicEnergistics.config = ConfigurationHandler.loadAndSyncConfigFile( event.getSuggestedConfigurationFile() );

		// Set the instance
		ThaumicEnergistics.instance = this;

		// Register the gui handler
		NetworkRegistry.INSTANCE.registerGuiHandler( this, new ThEGuiHandler() );

		// Register items
		ThaumicEnergistics.proxy.registerItems();

		// Register blocks
		ThaumicEnergistics.proxy.registerBlocks();

		ThELog.endSection( "PreInit", startTime );
	}

	// TODO: Sync server configs
	// TODO: Knowledge Inscriber GUI, add particles like focal manipulator
	// TODO: Use area packets over tile sync when possible

}
