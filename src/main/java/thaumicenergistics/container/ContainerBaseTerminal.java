package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.part.PartBase;

/**
 * @author Alex811
 */
public abstract class ContainerBaseTerminal extends ContainerBase{

    public ContainerBaseTerminal(EntityPlayer player) {
        super(player);
    }

    abstract public PartBase getPart();
}
