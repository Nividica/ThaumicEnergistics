package thaumicenergistics.gui;

import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.container.ContainerEssentiaTerminal;
import thaumicenergistics.gui.abstraction.AbstractGuiCellTerminalBase;
import thaumicenergistics.gui.widget.IAspectSelectorGui;
import thaumicenergistics.network.packet.server.PacketServerEssentiaTerminal;
import thaumicenergistics.parts.AEPartEssentiaTerminal;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Gui for the essentia terminal
 * 
 * @author Nividica
 * 
 */
@SideOnly(Side.CLIENT)
public class GuiEssentiaTerminal
	extends AbstractGuiCellTerminalBase
	implements IAspectSelectorGui
{
	protected AEPartEssentiaTerminal terminal;

	protected ContainerEssentiaTerminal containerAspectTerminal;

	public GuiEssentiaTerminal( final AEPartEssentiaTerminal terminal, final EntityPlayer player )
	{
		super( player, new ContainerEssentiaTerminal( terminal, player ) );

		this.terminal = terminal;
	}

	@Override
	protected void sortModeButtonClicked( final ComparatorMode modeRequested )
	{
		// Request update from server
		new PacketServerEssentiaTerminal().createRequestChangeSortMode( this.player, this.terminal, modeRequested ).sendPacketToServer();
	}

}
