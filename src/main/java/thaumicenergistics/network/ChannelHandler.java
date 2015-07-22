package thaumicenergistics.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.network.handlers.HandlerAreaParticleFX;
import thaumicenergistics.network.handlers.HandlerClientAspectSlot;
import thaumicenergistics.network.handlers.HandlerClientEssentiaCellTerminal;
import thaumicenergistics.network.handlers.HandlerClientEssentiaStorageBus;
import thaumicenergistics.network.handlers.HandlerClientEssentiaVibrationChamber;
import thaumicenergistics.network.handlers.HandlerClientKnowledgeInscriber;
import thaumicenergistics.network.handlers.HandlerClientPriority;
import thaumicenergistics.network.handlers.HandlerServerAspectSlot;
import thaumicenergistics.network.handlers.HandlerServerChangeGui;
import thaumicenergistics.network.handlers.HandlerServerEssentiaCellTerminal;
import thaumicenergistics.network.handlers.HandlerServerEssentiaCellWorkbench;
import thaumicenergistics.network.handlers.HandlerServerKnowledgeInscriber;
import thaumicenergistics.network.handlers.HandlerServerPriority;
import thaumicenergistics.network.handlers.HandlerServerWrenchFocus;
import thaumicenergistics.network.handlers.part.HandlerClientArcaneCraftingTerminal;
import thaumicenergistics.network.handlers.part.HandlerClientEssentiaIOBus;
import thaumicenergistics.network.handlers.part.HandlerClientEssentiaLevelEmitter;
import thaumicenergistics.network.handlers.part.HandlerServerArcaneCraftingTerminal;
import thaumicenergistics.network.handlers.part.HandlerServerEssentiaIOBus;
import thaumicenergistics.network.handlers.part.HandlerServerEssentiaLevelEmitter;
import thaumicenergistics.network.handlers.part.HandlerServerEssentiaStorageBus;
import thaumicenergistics.network.packet.AbstractPacket;
import thaumicenergistics.network.packet.client.PacketAreaParticleFX;
import thaumicenergistics.network.packet.client.PacketClientArcaneCraftingTerminal;
import thaumicenergistics.network.packet.client.PacketClientAspectSlot;
import thaumicenergistics.network.packet.client.PacketClientEssentiaCellTerminal;
import thaumicenergistics.network.packet.client.PacketClientEssentiaEmitter;
import thaumicenergistics.network.packet.client.PacketClientEssentiaIOBus;
import thaumicenergistics.network.packet.client.PacketClientEssentiaStorageBus;
import thaumicenergistics.network.packet.client.PacketClientEssentiaVibrationChamber;
import thaumicenergistics.network.packet.client.PacketClientKnowledgeInscriber;
import thaumicenergistics.network.packet.client.PacketClientPriority;
import thaumicenergistics.network.packet.server.PacketServerArcaneCraftingTerminal;
import thaumicenergistics.network.packet.server.PacketServerAspectSlot;
import thaumicenergistics.network.packet.server.PacketServerChangeGui;
import thaumicenergistics.network.packet.server.PacketServerEssentiaCellTerminal;
import thaumicenergistics.network.packet.server.PacketServerEssentiaCellWorkbench;
import thaumicenergistics.network.packet.server.PacketServerEssentiaEmitter;
import thaumicenergistics.network.packet.server.PacketServerEssentiaIOBus;
import thaumicenergistics.network.packet.server.PacketServerEssentiaStorageBus;
import thaumicenergistics.network.packet.server.PacketServerKnowledgeInscriber;
import thaumicenergistics.network.packet.server.PacketServerPriority;
import thaumicenergistics.network.packet.server.PacketServerWrenchFocus;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class ChannelHandler
{
	// private static EnumMap<Side, FMLEmbeddedChannel> channels;
	public static SimpleNetworkWrapper wrapper = NetworkRegistry.INSTANCE.newSimpleChannel( ThaumicEnergistics.MOD_ID );

	public static void registerMessages()
	{
		byte discriminator = 0;

		// Aspect slot
		wrapper.registerMessage( HandlerClientAspectSlot.class, PacketClientAspectSlot.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerAspectSlot.class, PacketServerAspectSlot.class, discriminator++ , Side.SERVER );

		// Essentia import/export bus
		wrapper.registerMessage( HandlerClientEssentiaIOBus.class, PacketClientEssentiaIOBus.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerEssentiaIOBus.class, PacketServerEssentiaIOBus.class, discriminator++ , Side.SERVER );

		// Essentia storage bus
		wrapper.registerMessage( HandlerClientEssentiaStorageBus.class, PacketClientEssentiaStorageBus.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerEssentiaStorageBus.class, PacketServerEssentiaStorageBus.class, discriminator++ , Side.SERVER );

		// Essentia level emitter
		wrapper.registerMessage( HandlerClientEssentiaLevelEmitter.class, PacketClientEssentiaEmitter.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerEssentiaLevelEmitter.class, PacketServerEssentiaEmitter.class, discriminator++ , Side.SERVER );

		// Essentia terminal
		wrapper.registerMessage( HandlerServerEssentiaCellTerminal.class, PacketServerEssentiaCellTerminal.class, discriminator++ , Side.SERVER );
		wrapper.registerMessage( HandlerClientEssentiaCellTerminal.class, PacketClientEssentiaCellTerminal.class, discriminator++ , Side.CLIENT );

		// Arcane crafting terminal
		wrapper.registerMessage( HandlerClientArcaneCraftingTerminal.class, PacketClientArcaneCraftingTerminal.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerArcaneCraftingTerminal.class, PacketServerArcaneCraftingTerminal.class, discriminator++ , Side.SERVER );

		// Change GUI
		wrapper.registerMessage( HandlerServerChangeGui.class, PacketServerChangeGui.class, discriminator++ , Side.SERVER );

		// Priority GUI
		wrapper.registerMessage( HandlerClientPriority.class, PacketClientPriority.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerPriority.class, PacketServerPriority.class, discriminator++ , Side.SERVER );

		// Essentia cell workbench
		wrapper.registerMessage( HandlerServerEssentiaCellWorkbench.class, PacketServerEssentiaCellWorkbench.class, discriminator++ , Side.SERVER );

		// Knowledge inscriber
		wrapper.registerMessage( HandlerClientKnowledgeInscriber.class, PacketClientKnowledgeInscriber.class, discriminator++ , Side.CLIENT );
		wrapper.registerMessage( HandlerServerKnowledgeInscriber.class, PacketServerKnowledgeInscriber.class, discriminator++ , Side.SERVER );

		// Particle FX
		wrapper.registerMessage( HandlerAreaParticleFX.class, PacketAreaParticleFX.class, discriminator++ , Side.CLIENT );

		// Wrench Focus
		wrapper.registerMessage( HandlerServerWrenchFocus.class, PacketServerWrenchFocus.class, discriminator++ , Side.SERVER );

		// Essentia Vibration Chamber
		wrapper.registerMessage( HandlerClientEssentiaVibrationChamber.class, PacketClientEssentiaVibrationChamber.class, discriminator++ ,
			Side.CLIENT );
	}

	public static void sendPacketToAllAround( final AbstractPacket packet, final int dimension, final double x, final double y, final double z,
												final int range )
	{
		TargetPoint p = new TargetPoint( dimension, x, y, z, range );
		wrapper.sendToAllAround( packet, p );
	}

	public static void sendPacketToPlayer( final AbstractPacket packet, final EntityPlayer player )
	{
		wrapper.sendTo( packet, (EntityPlayerMP)player );
	}

	public static void sendPacketToServer( final AbstractPacket packet )
	{
		wrapper.sendToServer( packet );
	}
}
