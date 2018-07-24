package thaumicenergistics.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import thaumicenergistics.api.model.IThEModel;

import static thaumicenergistics.ThaumicEnergistics.LOGGER;

/**
 * @author BrockWS
 */
@Mod.EventBusSubscriber
public class ModBlocks {

    public static List BLOCKS = new ArrayList<>();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        LOGGER.info("Registering Blocks");
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModBlocks.BLOCKS.forEach(block -> {
            if (block instanceof IThEModel)
                ((IThEModel) block).initModel();
        });
    }
}
