package thaumicenergistics.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.ITileDefinition;

import thaumicenergistics.api.IThEBlocks;
import thaumicenergistics.block.BlockBase;
import thaumicenergistics.block.BlockInfusionProvider;
import thaumicenergistics.client.render.IThEModel;
import thaumicenergistics.definitions.ThEBlockDefinition;
import thaumicenergistics.definitions.ThETileDefinition;
import thaumicenergistics.tile.TileInfusionProvider;

import static thaumicenergistics.ThaumicEnergistics.LOGGER;

/**
 * @author BrockWS
 */
@Mod.EventBusSubscriber
public class ThEBlocks implements IThEBlocks {

    public static List<BlockBase> BLOCKS = new ArrayList<>();

    private ITileDefinition infusionProvider;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        LOGGER.info("Registering Blocks");

        event.getRegistry().registerAll(ThEBlocks.BLOCKS.toArray(new BlockBase[0]));

        ThEBlocks.BLOCKS.forEach(BlockBase::registerTileEntity);
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        LOGGER.info("Registering ItemBlocks");

        ThEBlocks.BLOCKS.forEach(block -> event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName())));
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ThEBlocks.BLOCKS.forEach(block -> {
            if (block instanceof IThEModel)
                ((IThEModel) block).initModel();
        });
    }

    private static IBlockDefinition createBlock(BlockBase block) {
        ThEBlocks.BLOCKS.add(block);
        return new ThEBlockDefinition(block, new ItemBlock(block));
    }

    private static ITileDefinition createTile(BlockBase block, Class<? extends TileEntity> tile) {
        ThEBlocks.BLOCKS.add(block);
        return new ThETileDefinition(tile, block, new ItemBlock(block));
    }

    public ThEBlocks() {
        this.infusionProvider = ThEBlocks.createTile(new BlockInfusionProvider("infusion_provider"), TileInfusionProvider.class);
    }

    @Override
    public ITileDefinition infusionProvider() {
        return this.infusionProvider;
    }
}
