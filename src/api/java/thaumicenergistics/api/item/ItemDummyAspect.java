package thaumicenergistics.api.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import thaumcraft.api.aspects.Aspect;

/**
 * A dummy item that uses the aspect icon for its icon
 * Used by AEEssentiaStack
 * <p>
 * TODO: Render item based on Aspect Image + Colour
 *
 * @author BrockWS
 */
public class ItemDummyAspect extends Item {

    private Aspect aspect;

    public ItemDummyAspect() {
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setRegistryName("dummy_aspect");
        // TODO: Probably set name to aspect name
        this.setUnlocalizedName("dummy_aspect");
    }

    public ItemDummyAspect setAspect(Aspect aspect) {
        this.aspect = aspect;
        return this;
    }

    public Aspect getAspect() {
        return this.aspect;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        // Hide from create tabs
    }
}
