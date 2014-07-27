package thaumicenergistics.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.world.World;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.network.handlers.HandlerAspectSlot;
import thaumicenergistics.network.handlers.HandlerClientEssentiaCell;
import thaumicenergistics.network.handlers.HandlerServerEssentiaCell;
import thaumicenergistics.network.handlers.part.HandlerClientArcaneCraftingTerminal;
import thaumicenergistics.network.handlers.part.HandlerClientEssentiaTerminal;
import thaumicenergistics.network.handlers.part.HandlerEssentiaIOBus;
import thaumicenergistics.network.handlers.part.HandlerEssentiaLevelEmitter;
import thaumicenergistics.network.handlers.part.HandlerEssentiaStorageBus;
import thaumicenergistics.network.handlers.part.HandlerServerArcaneCraftingTerminal;
import thaumicenergistics.network.handlers.part.HandlerServerEssentiaTerminal;
import thaumicenergistics.network.packet.PacketAspectSlot;
import thaumicenergistics.network.packet.PacketClientArcaneCraftingTerminal;
import thaumicenergistics.network.packet.PacketClientEssentiaCell;
import thaumicenergistics.network.packet.PacketClientEssentiaTerminal;
import thaumicenergistics.network.packet.PacketEssentiaEmitter;
import thaumicenergistics.network.packet.PacketEssentiaIOBus;
import thaumicenergistics.network.packet.PacketEssentiaStorageBus;
import thaumicenergistics.network.packet.PacketServerArcaneCraftingTerminal;
import thaumicenergistics.network.packet.PacketServerEssentiaCell;
import thaumicenergistics.network.packet.PacketServerEssentiaTerminal;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class ChannelHandler
{
	// private static EnumMap<Side, FMLEmbeddedChannel> channels;
	public static SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel( ThaumicEnergistics.MOD_ID );

	public static void registerMessages()
	{
		wrapper.registerMessage( HandlerAspectSlot.class, PacketAspectSlot.class, 0, Side.CLIENT );
		wrapper.registerMessage( HandlerAspectSlot.class, PacketAspectSlot.class, 0, Side.SERVER );

		wrapper.registerMessage( HandlerEssentiaIOBus.class, PacketEssentiaIOBus.class, 1, Side.CLIENT );
		wrapper.registerMessage( HandlerEssentiaIOBus.class, PacketEssentiaIOBus.class, 1, Side.SERVER );

		wrapper.registerMessage( HandlerEssentiaStorageBus.class, PacketEssentiaStorageBus.class, 2, Side.CLIENT );
		wrapper.registerMessage( HandlerEssentiaStorageBus.class, PacketEssentiaStorageBus.class, 2, Side.SERVER );

		wrapper.registerMessage( HandlerEssentiaLevelEmitter.class, PacketEssentiaEmitter.class, 3, Side.CLIENT );
		wrapper.registerMessage( HandlerEssentiaLevelEmitter.class, PacketEssentiaEmitter.class, 3, Side.SERVER );

		wrapper.registerMessage( HandlerClientEssentiaCell.class, PacketClientEssentiaCell.class, 4, Side.CLIENT );
		wrapper.registerMessage( HandlerServerEssentiaCell.class, PacketServerEssentiaCell.class, 5, Side.SERVER );

		wrapper.registerMessage( HandlerClientEssentiaTerminal.class, PacketClientEssentiaTerminal.class, 6, Side.CLIENT );
		wrapper.registerMessage( HandlerServerEssentiaTerminal.class, PacketServerEssentiaTerminal.class, 7, Side.SERVER );

		wrapper.registerMessage( HandlerClientArcaneCraftingTerminal.class, PacketClientArcaneCraftingTerminal.class, 8, Side.CLIENT );
		wrapper.registerMessage( HandlerServerArcaneCraftingTerminal.class, PacketServerArcaneCraftingTerminal.class, 9, Side.SERVER );

	}

	public static void sendPacketToAllPlayers( AbstractPacket packet )
	{
		wrapper.sendToAll( packet );
	}

	public static void sendPacketToAllPlayers( Packet packet, World world )
	{
		for( Object player : world.playerEntities )
		{
			if ( player instanceof EntityPlayerMP )
			{
				( (EntityPlayerMP) player ).playerNetServerHandler.sendPacket( packet );
			}
		}
	}

	public static void sendPacketToPlayer( AbstractPacket packet, EntityPlayer player )
	{
		wrapper.sendTo( packet, (EntityPlayerMP) player );
	}

	public static void sendPacketToServer( AbstractPacket packet )
	{
		wrapper.sendToServer( packet );
	}
}
