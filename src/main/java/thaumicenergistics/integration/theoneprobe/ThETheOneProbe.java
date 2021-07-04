package thaumicenergistics.integration.theoneprobe;

import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import thaumicenergistics.integration.IThEIntegration;

import java.util.function.Function;

/**
 * @author Alex811
 */
public class ThETheOneProbe implements IThEIntegration, Function<ITheOneProbe, Void> {
    @Override
    public void preInit() {
        FMLInterModComms.sendFunctionMessage(this.getModID(), "getTheOneProbe", this.getClass().getName());
    }

    @Override
    public String getModID() {
        return "theoneprobe";
    }

    @Override
    public Void apply(ITheOneProbe registrar) {
        registrar.registerProvider(new TileTOPDataProvider());
        registrar.registerBlockDisplayOverride(new PartTOPDisplayOverride());   // Fixes our parts showing up as AE2 parts (lets AE2 add the power-state info, due to inheritance)
        return null;
    }
}
