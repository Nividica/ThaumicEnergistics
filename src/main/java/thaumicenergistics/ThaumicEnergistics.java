package thaumicenergistics;

import org.apache.commons.lang3.tuple.ImmutablePair;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.gui.GuiHandler;
import thaumicenergistics.integration.IntegrationCore;
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
	/**
	 * String ID of the mod.
	 */
	public static final String MOD_ID = "thaumicenergistics";

	/**
	 * Current version of the mod.
	 */
	public static final String VERSION = "0.6.5b"; // Note: don't forget to change the build.gradle file as well

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

	@EventHandler
	public void load( FMLInitializationEvent event )
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

	@EventHandler
	public void postInit( FMLPostInitializationEvent event )
	{
		ImmutablePair<Long, String> t = this.beginLoadStageTracking( "postInit" );
		
		// Register the standard thaumcraft container items and tiles
		EssentiaTileContainerHelper.instance.registerThaumcraftContainers();
		EssentiaItemContainerHelper.instance.registerThaumcraftContainers();

		// Register my tiles with SpatialIO
		ThaumicEnergistics.proxy.registerSpatialIOMovables();
		
		this.endLoadStageTracking( t );
	}

	@EventHandler
	public void preInit( FMLPreInitializationEvent event )
	{
		ImmutablePair<Long, String> t = this.beginLoadStageTracking( "preInit" );
		
		// Set the instance
		ThaumicEnergistics.instance = this;

		// Register the gui handler
		NetworkRegistry.INSTANCE.registerGuiHandler( this, new GuiHandler() );

		// Register items
		ThaumicEnergistics.proxy.registerItems();
		
		// Register fluids
		ThaumicEnergistics.proxy.registerFluids();
		
		// Register blocks
		ThaumicEnergistics.proxy.registerBlocks();
		
		// Register recipes
		ThaumicEnergistics.proxy.registerRecipes();
		
		// Register TC research
		ThaumicEnergistics.proxy.registerResearch();
		
		this.endLoadStageTracking( t );
	}
	
	private ImmutablePair<Long, String> beginLoadStageTracking( String stageName )
	{
		// Print begin
		FMLLog.info( "%s: Begining %s()", ThaumicEnergistics.MOD_ID, stageName );
		
		// Return the current time
		return new ImmutablePair<Long, String>( System.currentTimeMillis(), stageName );
	}
	
	private void endLoadStageTracking( ImmutablePair<Long, String> beginInfo )
	{
		// Calculate time
		long time = System.currentTimeMillis() - beginInfo.left;
		
		// Print end
		FMLLog.info( "%s: Finished %s(), Took: %dms", ThaumicEnergistics.MOD_ID, beginInfo.right, time );
	}

}

/*
 * NOTE Known issue: ME Chest gui does not update the network when the contents of an essentia cell is changed.
 * Need to ask Algo how to go about fixing this, cause I've tried everything I can think of.
 */

/*
 * NOTE: Known Issue: More than 1 redstone pulse per second will cause IO buses to operate too fast.
 * Update 8/9/2014: Lowest priority issues. Will likely leave as-is.
 */

