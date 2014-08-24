package thaumicenergistics;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.gui.GuiHandler;
import thaumicenergistics.integration.IntegrationCore;
import thaumicenergistics.network.ChannelHandler;
import thaumicenergistics.proxy.CommonProxy;
import thaumicenergistics.registries.ItemEnum;
import thaumicenergistics.util.EssentiaItemContainerHelper;
import thaumicenergistics.util.EssentiaTileContainerHelper;
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
	public static final String VERSION = "0.6.2a"; // Note: don't forget to change the build.gradle file as well

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
		// Register block renderers
		ThaumicEnergistics.proxy.registerRenderers();
		
		// Register tile entities
		ThaumicEnergistics.proxy.registerTileEntities();

		// Register network messages
		ChannelHandler.registerMessages();
		
		// Register integration
		IntegrationCore.init();
	}

	@EventHandler
	public void postInit( FMLPostInitializationEvent event )
	{
		// Register the standard thaumcraft container items and tiles
		EssentiaTileContainerHelper.registerThaumcraftContainers();
		EssentiaItemContainerHelper.registerThaumcraftContainers();

		// Register my tiles with SpatialIO
		ThaumicEnergistics.proxy.registerSpatialIOMovables();
	}

	@EventHandler
	public void preInit( FMLPreInitializationEvent event )
	{
		// Set the instance
		ThaumicEnergistics.instance = this;

		NetworkRegistry.INSTANCE.registerGuiHandler( this, new GuiHandler() );

		ThaumicEnergistics.proxy.registerItems();
		ThaumicEnergistics.proxy.registerFluids();
		ThaumicEnergistics.proxy.registerBlocks();
		ThaumicEnergistics.proxy.registerRecipes();
		ThaumicEnergistics.proxy.registerResearch();
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

/*
 * TODO -- Planned Features
 * Terminal sort mode ascending and descending order
 * Additional UI's redstone mode
 * 
 * --Possible Features
 * Wireless terminal
 * Level emitter multi-mode
 * 
 */

