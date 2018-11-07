package thaumicenergistics;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.config.Config;

import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.init.ModGlobals;

import static net.minecraftforge.common.config.Config.Comment;
import static net.minecraftforge.common.config.Config.Name;

/**
 * @author BrockWS
 */
@SuppressWarnings("ALL")
@Config(modid = ModGlobals.MOD_ID)
public class ThEConfig implements IThEConfig {

    @Name("Essentia Container Capacity")
    @Comment("Specifies how much a item that holds essentia can hold\nFor filling with Essentia Terminal\nBest to set it to how much the item can actually store")
    public static Map<String, Integer> essentiaContainerCapacity = new HashMap<>();

    static {
        essentiaContainerCapacity.put("thaumcraft:phial", 10);
    }

    public ThEConfig() {
    }

    @Override
    public Map<String, Integer> essentiaContainerCapacity() {
        return new HashMap<>(this.essentiaContainerCapacity);
    }
}
