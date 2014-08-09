package thaumicenergistics.gui;

import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
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
	}

	@Override
	protected void sortModeButtonClicked( ComparatorMode modeRequested )
	{
		// Request update from server
		new PacketServerEssentiaTerminal().createRequestChangeSortMode( this.player, this.terminal, modeRequested ).sendPacketToServer();
	}
	
}
