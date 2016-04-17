package thaumicenergistics.common.network;

import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;

/**
 * Parts who have a set aspect.
 *
 * @author Nividica
 *
 */
public interface IAspectSlotPart
{
	void setAspect( int index, Aspect aspect, EntityPlayer player );
}
