package thaumicenergistics;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.network.ChannelHandler;
import thaumicenergistics.network.GuiHandler;
import thaumicenergistics.proxy.CommonProxy;
import thaumicenergistics.registries.ItemEnum;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = ThaumicEnergistics.MOD_ID, name = "Thaumic Energistics", version = ThaumicEnergistics.VERSION, dependencies = "required-after:appliedenergistics2;required-after:Thaumcraft")
public class ThaumicEnergistics
{
	/**
	 * String ID of the mod.
	 */
	public static final String MOD_ID = "thaumicenergistics";

	/**
	 * Current version of the mod.
	 */
	public static final String VERSION = "0.2.0a"; // Note: dont forget to change the mcmod.info file as well

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
		// ThaumicEnergistics.proxy.registerMovables();
		ThaumicEnergistics.proxy.registerRenderers();
		ThaumicEnergistics.proxy.registerTileEntities();

		ChannelHandler.registerMessages();
	}

	@EventHandler
	public void postInit( FMLPostInitializationEvent event )
	{
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
	}

}

/* TODO: -- Code
 * Bugfix: Machines run without power
 * Bugfix: Shift moves items go to upgrades and network tool slots
 * Find magic numbers such as the openGL and keyboard and replace with meaningful constants
 * Cache major enum.values[]
 * Packet.Write/Read aspect needs compression or a lookup table of some kind. Strings are bad.
 * 
 * ... I think I hate the word 'essentia' now
 */

/*
 * TODO -- Planned Features
 * IAspectSource for infusion and lamps
 * Thaumcraft crafting table with ME network connection
 * Occlude aspect names if not researched
 * Terminal sort modes, by amount, by name. Include ascending and desending order
 * Add aspect tags (thaumometer) to all items
 * Additional UI's redstone mode, priority window
 * Level emitter multi-mode
 * Terminal should be able to do work even when noone is looking at it. (internal timeout perhaps?)
 * 
 * --Possible Features
 * Wireless terminal
 * 
 */

