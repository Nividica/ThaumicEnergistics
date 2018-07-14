package thaumicenergistics.item;

import thaumicenergistics.api.model.IThEModel;
import thaumicenergistics.init.ModGlobals;

import net.minecraft.item.Item;

/**
 * @author BrockWS
 */
public abstract class ItemBase extends Item implements IThEModel {

    public ItemBase(String id) {
        super();
        this.setRegistryName(id);
        this.setUnlocalizedName(id);
        this.setCreativeTab(ModGlobals.CREATIVE_TAB);
    }
}
