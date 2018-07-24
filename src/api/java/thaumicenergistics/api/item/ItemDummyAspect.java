package thaumicenergistics.api.item;

import net.minecraft.item.Item;

import thaumcraft.api.aspects.Aspect;

/**
 * A dummy item that uses the aspect icon for its icon
 * Used by AEEssentiaStack
 *
 * @author BrockWS
 */
public class ItemDummyAspect extends Item {

    private Aspect aspect;

    public ItemDummyAspect(Aspect aspect) {
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName("dummy." + aspect.getName());
    }
}
