package thaumicenergistics.common.network.packet.server;

import java.util.concurrent.Future;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AELog;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.network.NetworkHandler;

/**
 * Server-bound confirm crafting packet.
 *
 * @author Nividica
 *
 */
public class Packet_S_ConfirmCraftingJob
	extends ThEServerPacket
{
	private static final byte MODE_REQUEST_CONFIRM = 1;

	private long amount;
	private boolean heldShift;

	public static void sendConfirmAutoCraft( final EntityPlayer player, final long amount, final boolean isShiftHeld )
	{
		Packet_S_ConfirmCraftingJob packet = new Packet_S_ConfirmCraftingJob();

		// Set the player
		packet.player = player;

		// Set the mode
		packet.mode = Packet_S_ConfirmCraftingJob.MODE_REQUEST_CONFIRM;

		// Set amount
		packet.amount = amount;

		// Set shift
		packet.heldShift = isShiftHeld;

		// Send it
		NetworkHandler.sendPacketToServer( packet );
	}

	@Override
	protected void readData( final ByteBuf stream )
	{
		// Read amount
		this.amount = stream.readLong();

		// Read shift
		this.heldShift = stream.readBoolean();
	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		// Write amount
		stream.writeLong( this.amount );

		// Write shift
		stream.writeBoolean( this.heldShift );
	}

	@Override
	public void execute()
	{
		// Sanity check.
		if( this.mode != Packet_S_ConfirmCraftingJob.MODE_REQUEST_CONFIRM )
		{
			return;
		}

		// NOTE: The code from here down was copied from "PacketCraftRequest" from AE2
		// The only change is which GUI is launched.

		if( this.player.openContainer instanceof ContainerCraftAmount )
		{
			final ContainerCraftAmount cca = (ContainerCraftAmount)this.player.openContainer;
			final Object target = cca.getTarget();
			if( target instanceof IGridHost )
			{
				final IGridHost gh = (IGridHost)target;
				final IGridNode gn = gh.getGridNode( ForgeDirection.UNKNOWN );
				if( gn == null )
				{
					return;
				}

				final IGrid g = gn.getGrid();
				if( ( g == null ) || ( cca.whatToMake == null ) )
				{
					return;
				}

				cca.whatToMake.setStackSize( this.amount );

				Future<ICraftingJob> futureJob = null;
				try
				{
					final ICraftingGrid cg = g.getCache( ICraftingGrid.class );
					futureJob = cg.beginCraftingJob( cca.getWorld(), cca.getGrid(), cca.getActionSrc(), cca.whatToMake, null );

					final ContainerOpenContext context = cca.openContext;
					if( context != null )
					{
						// Begin Thaumic Energistics Changes ===============================

						ThEGuiHandler.launchGui( ThEGuiHandler.AUTO_CRAFTING_CONFIRM, this.player, this.player.worldObj, 0, 0, 0 );

						// End Thaumic Energistics Changes ===============================

						if( this.player.openContainer instanceof ContainerCraftConfirm )
						{
							final ContainerCraftConfirm ccc = (ContainerCraftConfirm)this.player.openContainer;
							ccc.autoStart = this.heldShift;
							ccc.job = futureJob;
							cca.detectAndSendChanges();
						}
					}
				}
				catch( final Throwable e )
				{
					if( futureJob != null )
					{
						futureJob.cancel( true );
					}
					AELog.error( e );
				}
			}
		}
	}

}
