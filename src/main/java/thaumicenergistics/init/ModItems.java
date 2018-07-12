package thaumicenergistics.init;

import thaumicenergistics.item.ItemEssentiaCell;

import net.minecraft.item.Item;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static thaumicenergistics.ThaumicEnergistics.LOG;

/**
 * @author BrockWS
 */
@Mod.EventBusSubscriber
public class ModItems {

    public static ItemEssentiaCell itemEssentiaCell1k;
    public static ItemEssentiaCell itemEssentiaCell4k;
    public static ItemEssentiaCell itemEssentiaCell16k;
    public static ItemEssentiaCell itemEssentiaCell64k;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        LOG.info("Registering Items");
        event.getRegistry().register(itemEssentiaCell1k = new ItemEssentiaCell("1k", 1024, 12));
        event.getRegistry().register(itemEssentiaCell4k = new ItemEssentiaCell("4k", 1024 * 4, 12));
        event.getRegistry().register(itemEssentiaCell16k = new ItemEssentiaCell("16k", 1024 * 16, 12));
        event.getRegistry().register(itemEssentiaCell64k = new ItemEssentiaCell("64k", 1024 * 64, 12));
    }
}