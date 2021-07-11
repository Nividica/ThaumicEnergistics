package thaumicenergistics.integration.hwyla;

import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import thaumicenergistics.integration.IThEIntegration;
import thaumicenergistics.tile.TileBase;

/**
 * @author Alex811
 */
public class ThEHwyla implements IThEIntegration {
    @Override
    public void init() {
        FMLInterModComms.sendMessage(this.getModID(), "register", this.getClass().getName() + ".register");
    }

    public static void register(IWailaRegistrar registrar){
        registrar.registerBodyProvider(new TileWailaDataProvider(), TileBase.class);
    }
}
