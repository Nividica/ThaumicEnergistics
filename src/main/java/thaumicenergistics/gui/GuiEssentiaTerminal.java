package thaumicenergistics.gui;

import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.container.ContainerEssentiaTerminal;
import thaumicenergistics.gui.widget.IAspectSelectorGui;
import thaumicenergistics.network.packet.server.PacketServerEssentiaTerminal;
import thaumicenergistics.parts.AEPartEssentiaTerminal;

public class GuiEssentiaTerminal
	extends GuiCellTerminalBase
	implements IAspectSelectorGui
{
	protected AEPartEssentiaTerminal terminal;
	
	protected ContainerEssentiaTerminal containerAspectTerminal;

	public GuiEssentiaTerminal(AEPartEssentiaTerminal terminal, EntityPlayer player)
	{
		super( player, new ContainerEssentiaTerminal( terminal, player ) );
		
		this.terminal = terminal;
		
		// Ask for a list update
		new PacketServerEssentiaTerminal().createFullUpdateRequest( this.player ).sendPacketToServer();
	}
	
}
