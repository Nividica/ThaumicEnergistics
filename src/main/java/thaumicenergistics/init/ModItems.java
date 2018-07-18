package thaumicenergistics.init;

import thaumicenergistics.api.model.IThEModel;
import thaumicenergistics.item.ItemBase;
import thaumicenergistics.item.ItemEssentiaCell;
import thaumicenergistics.item.part.ItemEssentiaExportBus;
import thaumicenergistics.item.part.ItemEssentiaImportBus;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static thaumicenergistics.ThaumicEnergistics.LOGGER;

/**
 * @author BrockWS
 */
@Mod.EventBusSubscriber
public class ModItems {

    public static List<ItemBase> ITEMS = new ArrayList<>();

    public static ItemEssentiaCell itemEssentiaCell1k;
    public static ItemEssentiaCell itemEssentiaCell4k;
    public static ItemEssentiaCell itemEssentiaCell16k;
    public static ItemEssentiaCell itemEssentiaCell64k;
    public static ItemEssentiaImportBus itemEssentiaImportBus;
    public static ItemEssentiaExportBus itemEssentiaExportBus;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        LOGGER.info("Registering Items");
        ModItems.ITEMS.add(itemEssentiaCell1k = new ItemEssentiaCell("1k", 1024, 12));
        ModItems.ITEMS.add(itemEssentiaCell4k = new ItemEssentiaCell("4k", 1024 * 4, 12));
        ModItems.ITEMS.add(itemEssentiaCell16k = new ItemEssentiaCell("16k", 1024 * 16, 12));
        ModItems.ITEMS.add(itemEssentiaCell64k = new ItemEssentiaCell("64k", 1024 * 64, 12));
        ModItems.ITEMS.add(itemEssentiaImportBus = new ItemEssentiaImportBus("essentia_import"));
        ModItems.ITEMS.add(itemEssentiaExportBus = new ItemEssentiaExportBus("essentia_export"));
        event.getRegistry().registerAll(ModItems.ITEMS.toArray(new ItemBase[0]));
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModItems.ITEMS.forEach(item -> {
            if (item instanceof IThEModel)
                ((IThEModel) item).initModel();
        });
    }
}