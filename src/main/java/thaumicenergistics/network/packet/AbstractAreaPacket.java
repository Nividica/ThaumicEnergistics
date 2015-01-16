package thaumicenergistics.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import thaumicenergistics.network.ChannelHandler;
import thaumicenergistics.util.EffectiveSide;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class AbstractAreaPacket
	extends AbstractPacket
{

	/**
	 * Target coords.
	 */
	protected float targetX, targetY, targetZ;

	/**
	 * Target world.
	 */
	protected World targetWorld;

	@SideOnly(Side.CLIENT)
	private void setClientWorld()
	{
		this.targetWorld = FMLClientHandler.instance().getClient().theWorld;
	}

	@Override
	protected void readData( final ByteBuf stream )
	{
		// Read target coords
		this.targetX = stream.readFloat();
		this.targetY = stream.readFloat();
		this.targetZ = stream.readFloat();
	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		// Write target coords
		stream.writeFloat( this.targetX );
		stream.writeFloat( this.targetY );
		stream.writeFloat( this.targetZ );
	}

	@Override
	public final void execute()
	{
		// Ensure this is client side
		if( EffectiveSide.isClientSide() )
		{
			this.setClientWorld();
			this.wrappedExecute();
		}
	}

	/**
	 * Sends the packet to all players within range
	 * 
	 * @param dimension
	 * @param x
	 * @param y
	 * @param z
	 * @param range
	 */
	public void sendToAllAround( final int range )
	{
		ChannelHandler.sendPacketToAllAround( this, this.targetWorld.provider.dimensionId, this.targetX, this.targetY, this.targetZ, range );
	}

	@SideOnly(Side.CLIENT)
	public abstract void wrappedExecute();
}
