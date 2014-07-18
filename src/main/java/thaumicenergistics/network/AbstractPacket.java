package thaumicenergistics.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.parts.AEPartBase;
import appeng.api.parts.IPartHost;
import com.google.common.base.Charsets;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class AbstractPacket implements IMessage
{
	@SideOnly(Side.CLIENT)
	public static World getClientWorld()
	{
		return Minecraft.getMinecraft().theWorld;
	}

	public static Aspect readAspect( ByteBuf stream )
	{
		return Aspect.aspects.get( readString( stream ) );
	}

	public static AEPartBase readPart( ByteBuf stream )
	{
		ForgeDirection side = ForgeDirection.getOrientation( stream.readInt() );

		IPartHost host = (IPartHost) AbstractPacket.readTileEntity( stream );

		return (AEPartBase) host.getPart( side );
	}

	public static EntityPlayer readPlayer( ByteBuf stream )
	{
		EntityPlayer player = null;

		if ( stream.readBoolean() )
		{
			World playerWorld = readWorld( stream );
			player = playerWorld.getPlayerEntityByName( readString( stream ) );
		}

		return player;
	}

	public static String readString( ByteBuf stream )
	{
		byte[] stringBytes = new byte[stream.readInt()];

		stream.readBytes( stringBytes );

		return new String( stringBytes, Charsets.UTF_8 );
	}

	public static TileEntity readTileEntity( ByteBuf stream )
	{
		World world = AbstractPacket.readWorld( stream );

		return world.getTileEntity( stream.readInt(), stream.readInt(), stream.readInt() );
	}

	public static World readWorld( ByteBuf stream )
	{
		World world = DimensionManager.getWorld( stream.readInt() );

		if ( FMLCommonHandler.instance().getSide() == Side.CLIENT )
		{
			if ( world == null )
			{
				world = getClientWorld();
			}
		}

		return world;
	}

	public static void writeAspect( Aspect aspect, ByteBuf stream )
	{
		String aspectName = "";

		if ( aspect != null )
		{
			aspectName = aspect.getTag();
		}

		writeString( aspectName, stream );
	}

	public static void writePart( AEPartBase part, ByteBuf stream )
	{
		stream.writeInt( part.getSide().ordinal() );

		writeTileEntity( part.getHost().getTile(), stream );
	}

	@SuppressWarnings("null")
	public static void writePlayer( EntityPlayer player, ByteBuf stream )
	{
		boolean validPlayer = ( player != null );

		stream.writeBoolean( validPlayer );

		if ( validPlayer )
		{
			writeWorld( player.worldObj, stream );
			writeString( player.getCommandSenderName(), stream );
		}
	}

	public static void writeString( String string, ByteBuf stream )
	{
		byte[] stringBytes = string.getBytes( Charsets.UTF_8 );

		stream.writeInt( stringBytes.length );

		stream.writeBytes( stringBytes );
	}

	public static void writeTileEntity( TileEntity entity, ByteBuf stream )
	{
		writeWorld( entity.getWorldObj(), stream );
		stream.writeInt( entity.xCoord );
		stream.writeInt( entity.yCoord );
		stream.writeInt( entity.zCoord );
	}

	public static void writeWorld( World world, ByteBuf stream )
	{
		stream.writeInt( world.provider.dimensionId );
	}

	public EntityPlayer player;

	public byte mode;

	public AbstractPacket()
	{
		this.player = null;
	}

	public AbstractPacket(EntityPlayer player)
	{
		this.player = player;
	}

	public abstract void execute();

	@Override
	public void fromBytes( ByteBuf stream )
	{
		this.mode = stream.readByte();
		this.player = AbstractPacket.readPlayer( stream );
		this.readData( stream );
	}

	public abstract void readData( ByteBuf stream );

	public void sendPacketToAllPlayers()
	{
		ChannelHandler.sendPacketToAllPlayers( this );
	}

	public void sendPacketToPlayer( EntityPlayer player )
	{
		ChannelHandler.sendPacketToPlayer( this, player );
	}

	public void sendPacketToServer()
	{
		ChannelHandler.sendPacketToServer( this );
	}

	@Override
	public void toBytes( ByteBuf stream )
	{
		stream.writeByte( this.mode );
		AbstractPacket.writePlayer( this.player, stream );
		this.writeData( stream );
	}

	public abstract void writeData( ByteBuf stream );

}
