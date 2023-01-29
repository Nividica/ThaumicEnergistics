package thaumicenergistics.common.network;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import thaumcraft.api.aspects.Aspect;

/**
 * Parts who have a set aspect.
 *
 * @author Nividica
 *
 */
public interface IAspectSlotPart {

    default int[] getAvailableAspectSlots() {
        return new int[0];
    }

    @Nullable
    default Aspect getAspect(int index) {
        return null;
    }

    void setAspect(int index, Aspect aspect, EntityPlayer player);
}
