package thaumicenergistics.network.packet.client;

import io.netty.buffer.ByteBuf;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.beams.FXBeam;
import thaumicenergistics.network.packet.AbstractAreaPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketAreaParticleFX
	extends AbstractAreaPacket
{

	private static final byte MODE_WRENCH_FX = 0;

	/**
	 * Destination coords.
	 */
	private float dX, dY, dZ;

	/**
	 * Color of the FX.
	 */
	private int aspectColor;

	@SideOnly(Side.CLIENT)
	private void addWrenchFX()
	{
		// Get the color
		Color color = new Color( this.aspectColor );

		// Create the particle
		FXBeam beam = new FXBeam( this.targetWorld, this.targetX, this.targetY + 1.5D, this.targetZ, this.dX + 0.5D, this.dY + 0.5D, this.dZ + 0.5D,
						color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 6 );

		// Set the width
		beam.width = 0.2F;

		// Add the particle to thaumcraft engine
		ParticleEngine.instance.addEffect( this.targetWorld, beam );
	}

	@Override
	protected void readData( final ByteBuf stream )
	{
		// Call super
		super.readData( stream );

		switch ( this.mode )
		{
			case PacketAreaParticleFX.MODE_WRENCH_FX:

				// Read the destination
				this.dX = stream.readFloat();
				this.dY = stream.readFloat();
				this.dZ = stream.readFloat();

				// Read the color
				this.aspectColor = stream.readInt();
				break;
		}

	}

	@Override
	protected void writeData( final ByteBuf stream )
	{
		// Call super
		super.writeData( stream );

		switch ( this.mode )
		{
			case PacketAreaParticleFX.MODE_WRENCH_FX:
				// Write the destination
				stream.writeFloat( this.dX );
				stream.writeFloat( this.dY );
				stream.writeFloat( this.dZ );

				// Write the color
				stream.writeInt( this.aspectColor );
				break;
		}
	}

	/**
	 * Creates an FX packet.
	 * 
	 * @param world
	 * @param sourceX
	 * @param sourceY
	 * @param sourceZ
	 * @param destinationX
	 * @param destinationY
	 * @param destinationZ
	 * @param aspectColor
	 * @return
	 */
	public PacketAreaParticleFX createWrenchFX( final World world, final double sourceX, final double sourceY, final double sourceZ,
												final double destinationX, final double destinationY, final double destinationZ,
												final Aspect aspectColor )
	{
		// Set the mode
		this.mode = PacketAreaParticleFX.MODE_WRENCH_FX;

		// Set the world
		this.targetWorld = world;

		// Set the target coords
		this.targetX = (float)sourceX;
		this.targetY = (float)sourceY;
		this.targetZ = (float)sourceZ;

		// Set the destination values
		this.dX = (float)destinationX;
		this.dY = (float)destinationY;
		this.dZ = (float)destinationZ;

		// Set the aspect color
		this.aspectColor = aspectColor.getColor();

		return this;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void wrappedExecute()
	{
		// Ensure the world is valid
		if( this.targetWorld == null )
		{
			return;
		}

		// Has the player set particles to minimum?
		if( Minecraft.getMinecraft().gameSettings.particleSetting == 2 )
		{
			return;
		}

		switch ( this.mode )
		{
			case PacketAreaParticleFX.MODE_WRENCH_FX:
				this.addWrenchFX();
				break;
		}
	}
}
