package thaumicenergistics.common.network.packet.client;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.beams.FXBeam;
import thaumicenergistics.common.network.NetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

/**
 * Particle effect packet, clients-in-area-bound
 *
 * @author Nividica
 *
 */
public class Packet_R_ParticleFX extends ThEAreaPacket {

    private static final byte MODE_WRENCH_FX = 0;

    /**
     * Destination coords.
     */
    private float dX, dY, dZ;

    /**
     * Color of the FX.
     */
    private int aspectColor;

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
     * @param range
     */
    public static void createWrenchFX(final World world, final double sourceX, final double sourceY,
            final double sourceZ, final double destinationX, final double destinationY, final double destinationZ,
            final Aspect aspectColor, final float range) {
        Packet_R_ParticleFX packet = new Packet_R_ParticleFX();

        // Set the mode
        packet.mode = Packet_R_ParticleFX.MODE_WRENCH_FX;

        // Set the world
        packet.targetWorld = world;

        // Set the target coords
        packet.targetX = (float) sourceX;
        packet.targetY = (float) sourceY;
        packet.targetZ = (float) sourceZ;

        // Set the destination values
        packet.dX = (float) destinationX;
        packet.dY = (float) destinationY;
        packet.dZ = (float) destinationZ;

        // Set the aspect color
        packet.aspectColor = aspectColor.getColor();

        // Send it
        NetworkHandler.sendAreaPacketToClients(packet, range);
    }

    @SideOnly(Side.CLIENT)
    private void addWrenchFX() {
        // Get the color
        Color color = new Color(this.aspectColor);

        // Create the particle
        FXBeam beam = new FXBeam(
                this.targetWorld,
                this.targetX,
                this.targetY + 1.5D,
                this.targetZ,
                this.dX + 0.5D,
                this.dY + 0.5D,
                this.dZ + 0.5D,
                color.getRed() / 255.0F,
                color.getGreen() / 255.0F,
                color.getBlue() / 255.0F,
                6);

        // Set the width
        beam.width = 0.2F;

        // Add the particle to thaumcraft engine
        ParticleEngine.instance.addEffect(this.targetWorld, beam);
    }

    @Override
    protected void readData(final ByteBuf stream) {
        // Call super
        super.readData(stream);

        switch (this.mode) {
            case Packet_R_ParticleFX.MODE_WRENCH_FX:

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
    protected void writeData(final ByteBuf stream) {
        // Call super
        super.writeData(stream);

        switch (this.mode) {
            case Packet_R_ParticleFX.MODE_WRENCH_FX:
                // Write the destination
                stream.writeFloat(this.dX);
                stream.writeFloat(this.dY);
                stream.writeFloat(this.dZ);

                // Write the color
                stream.writeInt(this.aspectColor);
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void areaExecute() {
        // Ensure the world is valid
        if (this.targetWorld == null) {
            return;
        }

        // Has the player set particles to minimum?
        if (Minecraft.getMinecraft().gameSettings.particleSetting == 2) {
            return;
        }

        switch (this.mode) {
            case Packet_R_ParticleFX.MODE_WRENCH_FX:
                this.addWrenchFX();
                break;
        }
    }
}
