package thaumicenergistics.util;

import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * @author BrockWS
 */
public class ForgeUtil {

    public static boolean isClient() {
        return ForgeUtil.getSide().isClient();
    }

    public static boolean isServer() {
        return ForgeUtil.getSide().isServer();
    }

    public static Side getSide() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }

    public static <K extends IForgeRegistryEntry<K>> IForgeRegistry<K> getRegistry(Class<K> reg) {
        return GameRegistry.findRegistry(reg);
    }

    public static <K extends IForgeRegistryEntry<K>> IForgeRegistryEntry getRegistryEntry(Class<K> reg, ResourceLocation resourceLocation) {
        return ForgeUtil.getRegistry(reg).getValue(resourceLocation);
    }
}
