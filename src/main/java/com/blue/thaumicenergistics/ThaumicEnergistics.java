package com.blue.thaumicenergistics;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;


@Mod(modid = ThaumicEnergistics.MODID, name = ThaumicEnergistics.NAME, version = ThaumicEnergistics.VERSION, dependencies = ThaumicEnergistics.DEPENDENCIES)
public class ThaumicEnergistics
{
    public static final String MODID = "thaumicenergistics";
    public static final String NAME = "Thaumic Energistics";
    public static final String VERSION = "1.1.1";
    public static final String DEPENDENCIES = "after:thaumcraft";
    public static Logger LOGGER;

    @Mod.Instance
    public static ThaumicEnergistics instance;

    /**
     * Not finished
     */
    @SidedProxy

}
