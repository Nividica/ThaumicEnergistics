package thaumicenergistics.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.parts.AEPartBase;
import appeng.api.parts.IPartHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.google.common.base.Charsets;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class AbstractPacket
	implements IMessage
{
	/**
	 * Starting size of buffers compressed buffers, 1 Megabyte
	 */
	private static final int COMPRESSED_BUFFER_SIZE = 1048576;

	/**
	 * Player entity
	 */
	public EntityPlayer player;

	/**
	 * Used by subclasses to distinguish what's in the stream.
	 */
	public byte mode;

	/**
	 * If true will compress subclass data streams.
	 */
	public boolean useCompression;

	/**
	 * Creates an empty packet (called from the netty core)
	 */
	public AbstractPacket()
	{
		this.player = null;
		this.mode = -1;
		this.useCompression = false;
	}

	/**
	 * Creates the packet with the specified player.
	 * 
	 * @param player
	 */
	public AbstractPacket( EntityPlayer player )
	{
		this.player = player;
		this.mode = -1;
		this.useCompression = false;
	}

	/**
	 * Gets the loaded world on the client side.
	 * 
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public static World getClientWorld()
	{
		return Minecraft.getMinecraft().theWorld;
	}

	/**
	 * Reads a Thaumcraft aspect from the stream.
	 * 
	 * @param stream
	 * @return
	 */
	public static Aspect readAspect( ByteBuf stream )
	{
		return Aspect.aspects.get( readString( stream ) );
	}

	/**
	 * Reads an AE part from the stream.
	 * 
	 * @param stream
	 * @return
	 */
	public static AEPartBase readPart( ByteBuf stream )
	{
		ForgeDirection side = ForgeDirection.getOrientation( stream.readInt() );

		IPartHost host = (IPartHost)AbstractPacket.readTileEntity( stream );

		return (AEPartBase)host.getPart( side );
	}

	/**
	 * Reads an AE itemstack from the stream.
	 * @param stream
	 * @return
	 */
	public static IAEItemStack readAEItemStack( ByteBuf stream )
	{
		IAEItemStack itemStack;
		try
		{
			itemStack = AEItemStack.loadItemStackFromPacket( stream );
			
			return itemStack;
		}
		catch( IOException e )
		{
		}
		
		return null;

	}

	/**
	 * Reads a player entity from the stream.
	 * 
	 * @param stream
	 * @return
	 */
	public static EntityPlayer readPlayer( ByteBuf stream )
	{
		EntityPlayer player = null;

		if( stream.readBoolean() )
		{
			World playerWorld = readWorld( stream );
			player = playerWorld.getPlayerEntityByName( readString( stream ) );
		}

		return player;
	}

	/**
	 * Reads a string from the stream.
	 * 
	 * @param stream
	 * @return
	 */
	public static String readString( ByteBuf stream )
	{
		byte[] stringBytes = new byte[stream.readInt()];

		stream.readBytes( stringBytes );

		return new String( stringBytes, Charsets.UTF_8 );
	}

	/**
	 * Reads a tile entity from the stream.
	 * 
	 * @param stream
	 * @return
	 */
	public static TileEntity readTileEntity( ByteBuf stream )
	{
		World world = AbstractPacket.readWorld( stream );

		return world.getTileEntity( stream.readInt(), stream.readInt(), stream.readInt() );
	}

	/**
	 * Reads a world from the stream.
	 * 
	 * @param stream
	 * @return
	 */
	public static World readWorld( ByteBuf stream )
	{
		World world = DimensionManager.getWorld( stream.readInt() );

		if( FMLCommonHandler.instance().getSide() == Side.CLIENT )
		{
			if( world == null )
			{
				world = getClientWorld();
			}
		}

		return world;
	}

	/**
	 * Writes a Thaumcraft aspect to the stream.
	 * 
	 * @param aspect
	 * @param stream
	 */
	public static void writeAspect( Aspect aspect, ByteBuf stream )
	{
		String aspectName = "";

		if( aspect != null )
		{
			aspectName = aspect.getTag();
		}

		writeString( aspectName, stream );
	}

	/**
	 * Writes an AE itemstack to the stream.
	 * @param itemStack
	 * @param stream
	 */
	public static void writeAEItemStack( IAEItemStack itemStack, ByteBuf stream )
	{
		// Do we have a valid stack?
		if( itemStack != null )
		{
			// Write into the stream
			try
			{
				itemStack.writeToPacket( stream );
			}
			catch( IOException e )
			{
			}
		}

	}

	/**
	 * Writes an AE part to the stream.
	 * 
	 * @param part
	 * @param stream
	 */
	public static void writePart( AEPartBase part, ByteBuf stream )
	{
		stream.writeInt( part.getSide().ordinal() );

		writeTileEntity( part.getHost().getTile(), stream );
	}

	/**
	 * Writes a player to the stream.
	 * 
	 * @param player
	 * @param stream
	 */
	@SuppressWarnings("null")
	public static void writePlayer( EntityPlayer player, ByteBuf stream )
	{
		boolean validPlayer = ( player != null );

		stream.writeBoolean( validPlayer );

		if( validPlayer )
		{
			writeWorld( player.worldObj, stream );
			writeString( player.getCommandSenderName(), stream );
		}
	}

	/**
	 * Writes a string to the stream.
	 * 
	 * @param string
	 * @param stream
	 */
	public static void writeString( String string, ByteBuf stream )
	{
		byte[] stringBytes = string.getBytes( Charsets.UTF_8 );

		stream.writeInt( stringBytes.length );

		stream.writeBytes( stringBytes );
	}

	/**
	 * Writes a tile entity to the stream.
	 * 
	 * @param entity
	 * @param stream
	 */
	public static void writeTileEntity( TileEntity entity, ByteBuf stream )
	{
		writeWorld( entity.getWorldObj(), stream );
		stream.writeInt( entity.xCoord );
		stream.writeInt( entity.yCoord );
		stream.writeInt( entity.zCoord );
	}

	/**
	 * Writes a world to the stream.
	 * 
	 * @param world
	 * @param stream
	 */
	public static void writeWorld( World world, ByteBuf stream )
	{
		stream.writeInt( world.provider.dimensionId );
	}

	private void fromCompressedBytes( final ByteBuf packetStream )
	{
		// Create a new data stream
		ByteBuf decompressedStream = Unpooled.buffer( AbstractPacket.COMPRESSED_BUFFER_SIZE );

		// Create the decompressor
		try( GZIPInputStream decompressor = new GZIPInputStream( new InputStream()
		{

			@Override
			public int read() throws IOException
			{
				// Is there anymore data to read from the packet stream?
				if( packetStream.readableBytes() <= 0 )
				{
					// Return end marker
					return -1;
				}

				// Return the byte
				return packetStream.readByte() & 0xFF;
			}
		} ) )
		{
			// Create a temporary holding array
			byte[] holding = new byte[512];

			// Decompress
			while( decompressor.available() != 0 )
			{
				// Read into the holding array
				int bytesRead = decompressor.read( holding );

				// Did we read any data?
				if( bytesRead > 0 )
				{
					// Write the holding array into the decompressed stream
					decompressedStream.writeBytes( holding, 0, bytesRead );
				}
			}

			// Close the decompressor
			decompressor.close();

			// Reset stream position
			decompressedStream.readerIndex( 0 );

			// Pass to subclass
			this.readData( decompressedStream );

		}
		catch( IOException e )
		{
			// Failed
		}
	}

	/**
	 * Creates a new stream, calls to the subclass to write
	 * into it, then compresses it into the packet stream.
	 * 
	 * @param packetStream
	 */
	private void toCompressedBytes( final ByteBuf packetStream )
	{
		// Create a new data stream
		ByteBuf streamToCompress = Unpooled.buffer( AbstractPacket.COMPRESSED_BUFFER_SIZE );

		// Pass to subclass
		this.writeData( streamToCompress );

		// Create the compressor
		try( GZIPOutputStream compressor = new GZIPOutputStream( new OutputStream()
		{

			@Override
			public void write( int byteToWrite ) throws IOException
			{
				// Write the byte to the packet stream
				packetStream.writeByte( byteToWrite & 0xFF );
			}
		} ); )
		{
			// Compress
			compressor.write( streamToCompress.array(), 0, streamToCompress.writerIndex() );

			// Close the compressor
			compressor.close();
		}
		catch( IOException e )
		{
			// Failed
		}
	}

	/**
	 * Packet has been read and action can now take place.
	 */
	public abstract void execute();

	/**
	 * Reads data from the packet stream.
	 */
	@Override
	public void fromBytes( ByteBuf stream )
	{
		this.mode = stream.readByte();
		this.player = AbstractPacket.readPlayer( stream );
		this.useCompression = stream.readBoolean();

		// Is there a compressed substream?
		if( this.useCompression )
		{
			this.fromCompressedBytes( stream );
		}
		else
		{
			// Pass stream directly to subclass
			this.readData( stream );
		}
	}

	/**
	 * Allows subclasses to read data from the specified stream.
	 * 
	 * @param stream
	 */
	public abstract void readData( ByteBuf stream );

	/**
	 * Send this packet to all players.
	 */
	public void sendPacketToAllPlayers()
	{
		ChannelHandler.sendPacketToAllPlayers( this );
	}

	/**
	 * Send this packet to the specified player.
	 * 
	 * @param player
	 */
	public void sendPacketToPlayer( EntityPlayer player )
	{
		ChannelHandler.sendPacketToPlayer( this, player );
	}

	/**
	 * Send this packet to the server.
	 */
	public void sendPacketToServer()
	{
		ChannelHandler.sendPacketToServer( this );
	}

	/**
	 * Writes data into the packet stream.
	 */
	@Override
	public void toBytes( ByteBuf stream )
	{
		// Write the mode
		stream.writeByte( this.mode );

		// Write the player
		AbstractPacket.writePlayer( this.player, stream );

		// Write if there is a compressed sub-stream.
		stream.writeBoolean( this.useCompression );

		// Is compression enabled?
		if( this.useCompression )
		{
			this.toCompressedBytes( stream );
		}
		else
		{
			// No compression, subclass writes directly into stream.
			this.writeData( stream );
		}
	}

	/**
	 * Allows subclasses to write data into the specified stream.
	 * 
	 * @param stream
	 */
	public abstract void writeData( ByteBuf stream );

}
