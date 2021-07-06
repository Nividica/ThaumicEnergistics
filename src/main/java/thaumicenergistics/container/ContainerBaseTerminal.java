package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.part.PartBase;

/**
 * @author Alex811
 */
public abstract class ContainerBaseTerminal extends ContainerBaseConfigurable{

    public ContainerBaseTerminal(EntityPlayer player, PartBase part) {
        super(player, part.getConfigManager());
    }

    abstract public PartBase getPart();
}
