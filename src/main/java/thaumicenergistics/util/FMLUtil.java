package thaumicenergistics.util;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author BrockWS
 */
public class FMLUtil {

    public static boolean isClient() {
        return FMLUtil.getSide().isClient();
    }

    public static boolean isServer() {
        return FMLUtil.getSide().isServer();
    }

    public static Side getSide() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }
}
