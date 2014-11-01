package thaumicenergistics;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import thaumicenergistics.api.IConfig;
import thaumicenergistics.api.TEApi;
import thaumicenergistics.gui.GuiHandler;
import thaumicenergistics.integration.IntegrationCore;
import thaumicenergistics.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.integration.tc.EssentiaTileContainerHelper;
import thaumicenergistics.network.ChannelHandler;
import thaumicenergistics.proxy.CommonProxy;
import thaumicenergistics.registries.AEAspectRegister;
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
	/**
	 * String ID of the mod.
	 */
	public static final String MOD_ID = "thaumicenergistics";

	/**
	 * Current version of the mod.
	 */
	public static final String VERSION = "0.7.4b"; // Note: don't forget to change the build.gradle file as well

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
	public static CreativeTabs ModTab = new CreativeTabs( "ThaumicEnergistics" )
	{

		@Override
		public ItemStack getIconItemStack()
		{
			return TEApi.instance().blocks().InfusionProvider.getStack();
		}

		@Override
		public Item getTabIconItem()
		{
			return TEApi.instance().blocks().InfusionProvider.getItem();
		}
	};

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

		// Give AE items aspects		
		AEAspectRegister.instance.registerAEAspects();

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
		// Sync with config
		ThaumicEnergistics.config = ConfigurationHandler.loadAndSyncConfigFile( event.getSuggestedConfigurationFile() );

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
