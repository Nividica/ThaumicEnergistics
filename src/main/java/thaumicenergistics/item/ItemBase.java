package thaumicenergistics.item;

import net.minecraft.item.Item;

import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public abstract class ItemBase extends Item {

    public ItemBase(String id) {
        this(id, true);
    }

    public ItemBase(String id, boolean setCreativeTab) {
        this.setRegistryName(id);
        this.setUnlocalizedName(ModGlobals.MOD_ID + "." + id);
        if (setCreativeTab)
            this.setCreativeTab(ModGlobals.CREATIVE_TAB);
    }
}
