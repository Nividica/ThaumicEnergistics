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
    int[] getAvailableAspectSlots();

    @Nullable
    Aspect getAspect(int index);

    void setAspect(int index, Aspect aspect, EntityPlayer player);
}
