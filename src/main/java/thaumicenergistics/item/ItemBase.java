package thaumicenergistics.item;

import net.minecraft.item.Item;

import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public abstract class ItemBase extends Item {

    public ItemBase(String id) {
        super();
        this.setRegistryName(id);
        this.setUnlocalizedName(ModGlobals.MOD_ID + "." + id);
        this.setCreativeTab(ModGlobals.CREATIVE_TAB);
    }
}
