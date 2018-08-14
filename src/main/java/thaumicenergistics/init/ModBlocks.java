package thaumicenergistics.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import thaumicenergistics.api.internal.model.IThEModel;
import thaumicenergistics.block.BlockBase;
import thaumicenergistics.block.BlockInfusionProvider;

import static thaumicenergistics.ThaumicEnergistics.LOGGER;

/**
 * TODO: Allow api access
 *
 * @author BrockWS
 */
@Mod.EventBusSubscriber
public class ModBlocks {

    public static List<BlockBase> BLOCKS = new ArrayList<>();

    public static BlockInfusionProvider blockInfusionProvider;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        LOGGER.info("Registering Blocks");
        ModBlocks.BLOCKS.add(blockInfusionProvider = new BlockInfusionProvider("infusion_provider"));

        event.getRegistry().registerAll(ModBlocks.BLOCKS.toArray(new BlockBase[0]));

        ModBlocks.BLOCKS.forEach(BlockBase::registerTileEntity);
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        LOGGER.info("Registering ItemBlocks");

        ModBlocks.BLOCKS.forEach(block -> event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName())));
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModBlocks.BLOCKS.forEach(block -> {
            if (block instanceof IThEModel)
                ((IThEModel) block).initModel();
        });
    }
}
