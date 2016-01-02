package thaumicenergistics.common.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ThEAreaPacket
	extends ThEClientPacket
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

	@SideOnly(Side.CLIENT)
	public abstract void areaExecute();

	public int getDimension()
	{
		return this.targetWorld.provider.dimensionId;
	}

	public float getX()
	{
		return this.targetX;
	}

	public float getY()
	{
		return this.targetY;
	}

	public float getZ()
	{
		return this.targetZ;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public final void wrappedExecute()
	{
		this.setClientWorld();
		this.areaExecute();
	}
}
