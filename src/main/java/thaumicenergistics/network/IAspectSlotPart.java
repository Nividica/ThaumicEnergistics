package thaumicenergistics.network;

import net.minecraft.entity.player.EntityPlayer;
import thaumcraft.api.aspects.Aspect;

public interface IAspectSlotPart
{
	public void setAspect( int index, Aspect aspect, EntityPlayer player );
}
