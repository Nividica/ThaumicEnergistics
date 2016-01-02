package thaumicenergistics.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerInternalCrafting
	extends Container
{

	public ContainerInternalCrafting()
	{
	}

	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		return false;
	}

}
