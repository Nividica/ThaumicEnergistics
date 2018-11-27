package thaumicenergistics.init;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.definitions.IItemDefinition;

import thaumicenergistics.api.IThEItems;
import thaumicenergistics.client.render.IThEModel;
import thaumicenergistics.definitions.ThEItemDefinition;
import thaumicenergistics.item.*;
import thaumicenergistics.item.part.*;
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
    private final IItemDefinition itemArcaneTerminal;
    private final IItemDefinition itemDiffusionCore;
    private final IItemDefinition itemCoalescenceCore;
    private final IItemDefinition itemEssentiaComponent1k;
    private final IItemDefinition itemEssentiaComponent4k;
    private final IItemDefinition itemEssentiaComponent16k;
    private final IItemDefinition itemEssentiaComponent64k;
    private final IItemDefinition itemUpgradeArcane;
    private final IItemDefinition itemDummyAspect;

    public ThEItems() {
        this.itemEssentiaCell1k = ThEItems.createItem(new ItemEssentiaCell("1k", 1024, 12));
        this.itemEssentiaCell4k = ThEItems.createItem(new ItemEssentiaCell("4k", 1024 * 4, 12));
        this.itemEssentiaCell16k = ThEItems.createItem(new ItemEssentiaCell("16k", 1024 * 16, 12));
        this.itemEssentiaCell64k = ThEItems.createItem(new ItemEssentiaCell("64k", 1024 * 64, 12));
        this.itemEssentiaCellCreative = ThEItems.createItem(new ItemCreativeEssentiaCell());
        this.itemEssentiaImportBus = ThEItems.createItem(new ItemEssentiaImportBus("essentia_import"));
        this.itemEssentiaExportBus = ThEItems.createItem(new ItemEssentiaExportBus("essentia_export"));
        this.itemEssentiaStorageBus = ThEItems.createItem(new ItemEssentiaStorageBus("essentia_storage"));
        this.itemEssentiaTerminal = ThEItems.createItem(new ItemEssentiaTerminal("essentia_terminal"));
        this.itemArcaneTerminal = ThEItems.createItem(new ItemArcaneTerminal("arcane_terminal"));
        this.itemDiffusionCore = ThEItems.createItem(new ItemMaterial("diffusion_core"));
        this.itemCoalescenceCore = ThEItems.createItem(new ItemMaterial("coalescence_core"));
        this.itemEssentiaComponent1k = ThEItems.createItem(new ItemMaterial("essentia_component_1k"));
        this.itemEssentiaComponent4k = ThEItems.createItem(new ItemMaterial("essentia_component_4k"));
        this.itemEssentiaComponent16k = ThEItems.createItem(new ItemMaterial("essentia_component_16k"));
        this.itemEssentiaComponent64k = ThEItems.createItem(new ItemMaterial("essentia_component_64k"));
        this.itemUpgradeArcane = ThEItems.createItem(new ItemMaterial("upgrade_arcane"));
        this.itemDummyAspect = ThEItems.createItem(new ItemDummyAspect());
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
    public IItemDefinition arcaneTerminal() {
        return this.itemArcaneTerminal;
    }

    @Override
    public IItemDefinition diffusionCore() {
        return this.itemDiffusionCore;
    }

    @Override
    public IItemDefinition coalescenceCore() {
        return this.itemCoalescenceCore;
    }

    @Override
    public IItemDefinition essentiaComponent1k() {
        return this.itemEssentiaComponent1k;
    }

    @Override
    public IItemDefinition essentiaComponent4k() {
        return this.itemEssentiaComponent4k;
    }

    @Override
    public IItemDefinition essentiaComponent16k() {
        return this.itemEssentiaComponent16k;
    }

    @Override
    public IItemDefinition essentiaComponent64k() {
        return this.itemEssentiaComponent64k;
    }

    @Override
    public IItemDefinition upgradeArcane() {
        return this.itemUpgradeArcane;
    }

    @Override
    public IItemDefinition dummyAspect() {
        return this.itemDummyAspect;
    }
}