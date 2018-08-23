package thaumicenergistics.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import thaumicenergistics.api.internal.model.IThEModel;
import thaumicenergistics.item.ItemBase;
import thaumicenergistics.item.ItemCreativeEssentiaCell;
import thaumicenergistics.item.ItemDummyAspect;
import thaumicenergistics.item.ItemEssentiaCell;
import thaumicenergistics.item.part.ItemEssentiaExportBus;
import thaumicenergistics.item.part.ItemEssentiaImportBus;
import thaumicenergistics.item.part.ItemEssentiaStorageBus;
import thaumicenergistics.item.part.ItemEssentiaTerminal;

import static thaumicenergistics.ThaumicEnergistics.LOGGER;

/**
 * TODO: Move to API based
 *
 * @author BrockWS
 */
@Mod.EventBusSubscriber
public class ModItems {

    public static List<ItemBase> ITEMS = new ArrayList<>();

    public static ItemEssentiaCell itemEssentiaCell1k;
    public static ItemEssentiaCell itemEssentiaCell4k;
    public static ItemEssentiaCell itemEssentiaCell16k;
    public static ItemEssentiaCell itemEssentiaCell64k;
    public static ItemCreativeEssentiaCell itemEssentiaCellCreative;
    public static ItemEssentiaImportBus itemEssentiaImportBus;
    public static ItemEssentiaExportBus itemEssentiaExportBus;
    public static ItemEssentiaStorageBus itemEssentiaStorageBus;
    public static ItemEssentiaTerminal itemEssentiaTerminal;

    public static ItemDummyAspect itemDummyAspect;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        LOGGER.info("Registering Items");
        ModItems.ITEMS.add(itemEssentiaCell1k = new ItemEssentiaCell("1k", 1024, 12));
        ModItems.ITEMS.add(itemEssentiaCell4k = new ItemEssentiaCell("4k", 1024 * 4, 12));
        ModItems.ITEMS.add(itemEssentiaCell16k = new ItemEssentiaCell("16k", 1024 * 16, 12));
        ModItems.ITEMS.add(itemEssentiaCell64k = new ItemEssentiaCell("64k", 1024 * 64, 12));
        ModItems.ITEMS.add(itemEssentiaCellCreative = new ItemCreativeEssentiaCell());
        ModItems.ITEMS.add(itemEssentiaImportBus = new ItemEssentiaImportBus("essentia_import"));
        ModItems.ITEMS.add(itemEssentiaExportBus = new ItemEssentiaExportBus("essentia_export"));
        ModItems.ITEMS.add(itemEssentiaStorageBus = new ItemEssentiaStorageBus("essentia_storage"));
        ModItems.ITEMS.add(itemEssentiaTerminal = new ItemEssentiaTerminal("essentia_terminal"));
        ModItems.ITEMS.add(itemDummyAspect = new ItemDummyAspect());
        event.getRegistry().registerAll(ModItems.ITEMS.toArray(new ItemBase[0]));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        ModItems.ITEMS.forEach(item -> {
            if (item instanceof IThEModel) {
                ((IThEModel) item).initModel();
            }
        });
        ModelLoaderRegistry.registerLoader(ModGlobals.MODEL_LOADER);
    }
}