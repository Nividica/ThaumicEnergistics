package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

/**
 * @author BrockWS
 */
public class DummyContainer extends Container {

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return false;
    }
}
