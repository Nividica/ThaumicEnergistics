package thaumicenergistics.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.world.World;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.network.handlers.HandlerClientAspectSlot;
import thaumicenergistics.network.handlers.HandlerClientEssentiaCell;
import thaumicenergistics.network.handlers.HandlerClientPriority;
import thaumicenergistics.network.handlers.HandlerServerAspectSlot;
import thaumicenergistics.network.handlers.HandlerServerChangeGui;
import thaumicenergistics.network.handlers.HandlerServerEssentiaCell;
import thaumicenergistics.network.handlers.HandlerServerPriority;
import thaumicenergistics.network.handlers.part.HandlerClientArcaneCraftingTerminal;
import thaumicenergistics.network.handlers.part.HandlerClientEssentiaIOBus;
import thaumicenergistics.network.handlers.part.HandlerClientEssentiaLevelEmitter;
import thaumicenergistics.network.handlers.part.HandlerClientEssentiaTerminal;
import thaumicenergistics.network.handlers.part.HandlerServerArcaneCraftingTerminal;
import thaumicenergistics.network.handlers.part.HandlerServerEssentiaIOBus;
import thaumicenergistics.network.handlers.part.HandlerServerEssentiaLevelEmitter;
import thaumicenergistics.network.handlers.part.HandlerServerEssentiaStorageBus;
import thaumicenergistics.network.handlers.part.HandlerServerEssentiaTerminal;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.client.PacketClientArcaneCraftingTerminal;
import thaumicenergistics.network.packet.client.PacketClientAspectSlot;
import thaumicenergistics.network.packet.client.PacketClientEssentiaCell;
import thaumicenergistics.network.packet.client.PacketClientEssentiaEmitter;
import thaumicenergistics.network.packet.client.PacketClientEssentiaIOBus;
import thaumicenergistics.network.packet.client.PacketClientEssentiaTerminal;
import thaumicenergistics.network.packet.client.PacketClientPriority;
import thaumicenergistics.network.packet.server.PacketServerArcaneCraftingTerminal;
import thaumicenergistics.network.packet.server.PacketServerAspectSlot;
import thaumicenergistics.network.packet.server.PacketServerChangeGui;
import thaumicenergistics.network.packet.server.PacketServerEssentiaCell;
import thaumicenergistics.network.packet.server.PacketServerEssentiaEmitter;
import thaumicenergistics.network.packet.server.PacketServerEssentiaIOBus;
import thaumicenergistics.network.packet.server.PacketServerEssentiaStorageBus;
import thaumicenergistics.network.packet.server.PacketServerEssentiaTerminal;
import thaumicenergistics.network.packet.server.PacketServerPriority;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class ChannelHandler
{
	// private static EnumMap<Side, FMLEmbeddedChannel> channels;
	public static SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel( ThaumicEnergistics.MOD_ID );

	public static void registerMessages()
	{
		byte discriminator = 0;

		wrapper.registerMessage( HandlerClientAspectSlot.class, PacketClientAspectSlot.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerAspectSlot.class, PacketServerAspectSlot.class, discriminator++ , Side.SERVER );

		wrapper.registerMessage( HandlerClientEssentiaIOBus.class, PacketClientEssentiaIOBus.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerEssentiaIOBus.class, PacketServerEssentiaIOBus.class, discriminator++ , Side.SERVER );

		//wrapper.registerMessage( HandlerClientEssentiaStorageBus.class, PacketClientEssentiaStorageBus.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerEssentiaStorageBus.class, PacketServerEssentiaStorageBus.class, discriminator++ , Side.SERVER );

		wrapper.registerMessage( HandlerClientEssentiaLevelEmitter.class, PacketClientEssentiaEmitter.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerEssentiaLevelEmitter.class, PacketServerEssentiaEmitter.class, discriminator++ , Side.SERVER );

		wrapper.registerMessage( HandlerClientEssentiaCell.class, PacketClientEssentiaCell.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerEssentiaCell.class, PacketServerEssentiaCell.class, discriminator++ , Side.SERVER );

		wrapper.registerMessage( HandlerClientEssentiaTerminal.class, PacketClientEssentiaTerminal.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerEssentiaTerminal.class, PacketServerEssentiaTerminal.class, discriminator++ , Side.SERVER );

		wrapper.registerMessage( HandlerClientArcaneCraftingTerminal.class, PacketClientArcaneCraftingTerminal.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerArcaneCraftingTerminal.class, PacketServerArcaneCraftingTerminal.class, discriminator++ , Side.SERVER );
		
		wrapper.registerMessage( HandlerServerChangeGui.class, PacketServerChangeGui.class, discriminator++, Side.SERVER );
		
		wrapper.registerMessage( HandlerClientPriority.class, PacketClientPriority.class, discriminator++, Side.CLIENT );
		wrapper.registerMessage( HandlerServerPriority.class, PacketServerPriority.class, discriminator++, Side.SERVER );

	}

	public static void sendPacketToAllPlayers( AbstractPacket packet )
	{
		wrapper.sendToAll( packet );
	}

	public static void sendPacketToAllPlayers( Packet packet, World world )
	{
		for( Object player : world.playerEntities )
		{
			if( player instanceof EntityPlayerMP )
			{
				( (EntityPlayerMP)player ).playerNetServerHandler.sendPacket( packet );
			}
		}
	}

	public static void sendPacketToPlayer( AbstractPacket packet, EntityPlayer player )
	{
		wrapper.sendTo( packet, (EntityPlayerMP)player );
	}

	public static void sendPacketToServer( AbstractPacket packet )
	{
		wrapper.sendToServer( packet );
	}
}
