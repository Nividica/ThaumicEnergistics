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

import appeng.api.definitions.IItemDefinition;

import thaumicenergistics.api.IThEItems;
import thaumicenergistics.client.render.IThEModel;
import thaumicenergistics.definitions.ThEItemDefinition;
import thaumicenergistics.item.ItemBase;
import thaumicenergistics.item.ItemCreativeEssentiaCell;
import thaumicenergistics.item.ItemDummyAspect;
import thaumicenergistics.item.ItemEssentiaCell;
import thaumicenergistics.item.part.ItemEssentiaExportBus;
import thaumicenergistics.item.part.ItemEssentiaImportBus;
import thaumicenergistics.item.part.ItemEssentiaStorageBus;
import thaumicenergistics.item.part.ItemEssentiaTerminal;
import thaumicenergistics.util.ThELog;

/**
 * @author BrockWS
 */
@Mod.EventBusSubscriber
public class ThEItems implements IThEItems {

    public static List<ItemBase> ITEMS = new ArrayList<>();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ThELog.info("Registering {} Items", ThEItems.ITEMS.size());
        event.getRegistry().registerAll(ThEItems.ITEMS.toArray(new ItemBase[0]));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        ThEItems.ITEMS.forEach(item -> {
            if (item instanceof IThEModel) {
                ((IThEModel) item).initModel();
            }
        });
        ModelLoaderRegistry.registerLoader(ModGlobals.MODEL_LOADER);
    }

    private static IItemDefinition createItem(ItemBase item) {
        ThEItems.ITEMS.add(item);
        return new ThEItemDefinition(item);
    }

    private final IItemDefinition itemEssentiaCell1k;
    private final IItemDefinition itemEssentiaCell4k;
    private final IItemDefinition itemEssentiaCell16k;
    private final IItemDefinition itemEssentiaCell64k;
    private final IItemDefinition itemEssentiaCellCreative;
    private final IItemDefinition itemEssentiaImportBus;
    private final IItemDefinition itemEssentiaExportBus;
    private final IItemDefinition itemEssentiaStorageBus;
    private final IItemDefinition itemEssentiaTerminal;
    private final IItemDefinition itemDummyAspect;

    public ThEItems() {
        itemEssentiaCell1k = ThEItems.createItem(new ItemEssentiaCell("1k", 1024, 12));
        itemEssentiaCell4k = ThEItems.createItem(new ItemEssentiaCell("4k", 1024 * 4, 12));
        itemEssentiaCell16k = ThEItems.createItem(new ItemEssentiaCell("16k", 1024 * 16, 12));
        itemEssentiaCell64k = ThEItems.createItem(new ItemEssentiaCell("64k", 1024 * 64, 12));
        itemEssentiaCellCreative = ThEItems.createItem(new ItemCreativeEssentiaCell());
        itemEssentiaImportBus = ThEItems.createItem(new ItemEssentiaImportBus("essentia_import"));
        itemEssentiaExportBus = ThEItems.createItem(new ItemEssentiaExportBus("essentia_export"));
        itemEssentiaStorageBus = ThEItems.createItem(new ItemEssentiaStorageBus("essentia_storage"));
        itemEssentiaTerminal = ThEItems.createItem(new ItemEssentiaTerminal("essentia_terminal"));
        itemDummyAspect = ThEItems.createItem(new ItemDummyAspect());
    }

    @Override
    public IItemDefinition essentiaCell1k() {
        return this.itemEssentiaCell1k;
    }

    @Override
    public IItemDefinition essentiaCell4k() {
        return this.itemEssentiaCell4k;
    }

    @Override
    public IItemDefinition essentiaCell16k() {
        return this.itemEssentiaCell16k;
    }

    @Override
    public IItemDefinition essentiaCell64k() {
        return this.itemEssentiaCell64k;
    }

    @Override
    public IItemDefinition essentiaCellCreative() {
        return this.itemEssentiaCellCreative;
    }

    @Override
    public IItemDefinition essentiaImportBus() {
        return this.itemEssentiaImportBus;
    }

    @Override
    public IItemDefinition essentiaExportBus() {
        return this.itemEssentiaExportBus;
    }

    @Override
    public IItemDefinition essentiaStorageBus() {
        return this.itemEssentiaStorageBus;
    }

    @Override
    public IItemDefinition essentiaTerminal() {
        return this.itemEssentiaTerminal;
    }

    @Override
    public IItemDefinition dummyAspect() {
        return this.itemDummyAspect;
    }
}