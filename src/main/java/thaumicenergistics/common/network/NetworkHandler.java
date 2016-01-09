package thaumicenergistics.common.network;

import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.network.packet.client.*;
import thaumicenergistics.common.network.packet.server.*;
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
	private static short nextID = 0;

	/**
	 * Maps a class to a unique id.
	 */
	private static HashMap<Class, Short> ClassToID = new HashMap<Class, Short>();

	/**
	 * Maps a unique id to a class.
	 */
	private static HashMap<Short, Class> IDToClass = new HashMap<Short, Class>();

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
	public static Class getPacketClassFromID( final Short id )
	{
		return NetworkHandler.IDToClass.getOrDefault( id, null );
	}

	/**
	 * Gets the ID for the specified packet.
	 * 
	 * @param packet
	 * @return
	 */
	public static short getPacketID( final ThEBasePacket packet )
	{
		return NetworkHandler.ClassToID.getOrDefault( packet.getClass(), (short) -1 );
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

		// Distillation encoder
		registerPacket( Packet_S_DistillationEncoder.class );

		// Sync packet
		registerPacket( Packet_C_Sync.class );
	}

	public static void sendAreaPacketToClients( final ThEAreaPacket areaPacket, final float range )
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
