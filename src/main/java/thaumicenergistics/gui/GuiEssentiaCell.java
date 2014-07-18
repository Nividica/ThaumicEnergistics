package thaumicenergistics.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import thaumicenergistics.container.ContainerEssentiaCell;
import thaumicenergistics.network.packet.PacketServerEssentiaCell;

public class GuiEssentiaCell extends GuiCellTerminalBase
{
	public GuiEssentiaCell( EntityPlayer player, World world, int x, int y, int z )
	{
		super( player, new ContainerEssentiaCell( player, world, x, y, z ) );
		
		new PacketServerEssentiaCell( this.player ).sendPacketToServer();
	}
	
}
