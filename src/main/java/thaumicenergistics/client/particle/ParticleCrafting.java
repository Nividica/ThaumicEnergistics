package thaumicenergistics.client.particle;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

/**
 * @author Alex811
 */
@SideOnly(Side.CLIENT)
public class ParticleCrafting extends ThEParticle {
    protected static final Color[] colors = { // (ﾉ´ヮ´)ﾉ
            new Color(112, 239, 253),
            new Color(126, 150, 248),
            new Color(234, 162, 110),
            new Color(224, 92, 105),
            new Color(234, 234, 100),
            new Color(101, 238, 151)
    };

    public ParticleCrafting(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
        super("crafting", worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        setMaxAge(20);
        multiplyVelocity(0.15F);
        setScale(0.14F, 0.08F, 0.2F);
        setAlpha(1.0F, 0.6F, 0.3F);
        Color color = colors[this.rand.nextInt(6)];
        setTint(color, color, 0.001F);
    }
}
