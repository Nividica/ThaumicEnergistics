package thaumicenergistics.network;

import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.network.packet.ThEAreaPacket;
import thaumicenergistics.network.packet.ThEBasePacket;
import thaumicenergistics.network.packet.ThEClientPacket;
import thaumicenergistics.network.packet.ThEServerPacket;
import thaumicenergistics.network.packet.WrapperPacket;
import thaumicenergistics.network.packet.client.Packet_C_ArcaneCraftingTerminal;
import thaumicenergistics.network.packet.client.Packet_C_AspectSlot;
import thaumicenergistics.network.packet.client.Packet_C_EssentiaCellTerminal;
import thaumicenergistics.network.packet.client.Packet_C_EssentiaEmitter;
import thaumicenergistics.network.packet.client.Packet_C_EssentiaIOBus;
import thaumicenergistics.network.packet.client.Packet_C_EssentiaStorageBus;
import thaumicenergistics.network.packet.client.Packet_C_EssentiaVibrationChamber;
import thaumicenergistics.network.packet.client.Packet_C_KnowledgeInscriber;
import thaumicenergistics.network.packet.client.Packet_C_Priority;
import thaumicenergistics.network.packet.client.Packet_R_ParticleFX;
import thaumicenergistics.network.packet.client.WrapperPacket_C;
import thaumicenergistics.network.packet.server.Packet_S_ArcaneCraftingTerminal;
import thaumicenergistics.network.packet.server.Packet_S_AspectSlot;
import thaumicenergistics.network.packet.server.Packet_S_ChangeGui;
import thaumicenergistics.network.packet.server.Packet_S_ConfirmCraftingJob;
import thaumicenergistics.network.packet.server.Packet_S_EssentiaCellTerminal;
import thaumicenergistics.network.packet.server.Packet_S_EssentiaCellWorkbench;
import thaumicenergistics.network.packet.server.Packet_S_EssentiaEmitter;
import thaumicenergistics.network.packet.server.Packet_S_EssentiaIOBus;
import thaumicenergistics.network.packet.server.Packet_S_EssentiaStorageBus;
import thaumicenergistics.network.packet.server.Packet_S_KnowledgeInscriber;
import thaumicenergistics.network.packet.server.Packet_S_Priority;
import thaumicenergistics.network.packet.server.Packet_S_WrenchFocus;
import thaumicenergistics.network.packet.server.WrapperPacket_S;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NetworkHandler
{
	/**
	 * Channel used to send packets.
	 */
	private static SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel( ThaumicEnergistics.MOD_ID );

	/**
	 * Next packet ID.
	 */
	private static int nextID = 0;

	/**
	 * Maps a class to a unique id.
	 */
	private static HashMap<Class, Integer> ClassToID = new HashMap<Class, Integer>();

	/**
	 * Maps a unique id to a class.
	 */
	private static HashMap<Integer, Class> IDToClass = new HashMap<Integer, Class>();

	/**
	 * Registers a packet.
	 * 
	 * @param packet
	 */
	private static void registerPacket( final Class<? extends ThEBasePacket> packetClass )
	{
		NetworkHandler.ClassToID.put( packetClass, NetworkHandler.nextID );
		NetworkHandler.IDToClass.put( NetworkHandler.nextID, packetClass );
		++NetworkHandler.nextID;
	}

	/**
	 * Get's the class for the packet with the specified ID.
	 * 
	 * @param id
	 * @return
	 */
	public static Class getPacketClassFromID( final Integer id )
	{
		return NetworkHandler.IDToClass.getOrDefault( id, null );
	}

	/**
	 * Gets the ID for the specified packet.
	 * 
	 * @param packet
	 * @return
	 */
	public static int getPacketID( final ThEBasePacket packet )
	{
		return NetworkHandler.ClassToID.getOrDefault( packet.getClass(), -1 );
	}

	/**
	 * Registers all packets
	 */
	public static void registerPackets()
	{
		// Register channel client side handler
		NetworkHandler.channel.registerMessage( HandlerClient.class, WrapperPacket_C.class, 1, Side.CLIENT );

		// Register channel server side handler
		NetworkHandler.channel.registerMessage( HandlerServer.class, WrapperPacket_S.class, 2, Side.SERVER );

		// Aspect slot
		registerPacket( Packet_C_AspectSlot.class );
		registerPacket( Packet_S_AspectSlot.class );

		// Essentia import/export bus
		registerPacket( Packet_C_EssentiaIOBus.class );
		registerPacket( Packet_S_EssentiaIOBus.class );

		// Essentia storage bus
		registerPacket( Packet_C_EssentiaStorageBus.class );
		registerPacket( Packet_S_EssentiaStorageBus.class );

		// Essentia level emitter
		registerPacket( Packet_C_EssentiaEmitter.class );
		registerPacket( Packet_S_EssentiaEmitter.class );

		// Essentia terminal
		registerPacket( Packet_C_EssentiaCellTerminal.class );
		registerPacket( Packet_S_EssentiaCellTerminal.class );

		// Arcane crafting terminal
		registerPacket( Packet_C_ArcaneCraftingTerminal.class );
		registerPacket( Packet_S_ArcaneCraftingTerminal.class );

		// Change GUI
		registerPacket( Packet_S_ChangeGui.class );

		// Priority GUI
		registerPacket( Packet_C_Priority.class );
		registerPacket( Packet_S_Priority.class );

		// Essentia cell workbench
		registerPacket( Packet_S_EssentiaCellWorkbench.class );

		// Knowledge inscriber
		registerPacket( Packet_C_KnowledgeInscriber.class );
		registerPacket( Packet_S_KnowledgeInscriber.class );

		// Particle FX
		registerPacket( Packet_R_ParticleFX.class );

		// Wrench Focus
		registerPacket( Packet_S_WrenchFocus.class );

		// Essentia Vibration Chamber
		registerPacket( Packet_C_EssentiaVibrationChamber.class );

		// Confirm crafting
		registerPacket( Packet_S_ConfirmCraftingJob.class );
	}

	public static void sendAreaPacketToClients( final ThEAreaPacket areaPacket, final int range )
	{
		// Create the wrapper packet
		WrapperPacket wrapper = new WrapperPacket_C( areaPacket );

		// Create the target point
		TargetPoint targetPoint = new TargetPoint( areaPacket.getDimension(), areaPacket.getX(), areaPacket.getY(), areaPacket.getZ(), range );

		// Send the packet
		NetworkHandler.channel.sendToAllAround( wrapper, targetPoint );

	}

	public static void sendPacketToClient( final ThEClientPacket clientPacket )
	{
		// Create the wrapper packet
		WrapperPacket wrapper = new WrapperPacket_C( clientPacket );

		// Send the packet
		NetworkHandler.channel.sendTo( wrapper, (EntityPlayerMP)clientPacket.player );
	}

	public static void sendPacketToServer( final ThEServerPacket serverPacket )
	{
		// Create the wrapper packet
		WrapperPacket wrapper = new WrapperPacket_S( serverPacket );

		// Send the packet
		NetworkHandler.channel.sendToServer( wrapper );
	}
}
