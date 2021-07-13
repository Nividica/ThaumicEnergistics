package thaumicenergistics.util;

import appeng.api.AEApi;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.NotImplementedException;

/**
 * @author Alex811
 */
public interface IThEOwnable {
    void setOwner(EntityPlayer player);

    EntityPlayer getOwner();

    default void initGridNodeOwner(){
        if(!(this instanceof IThEGridNodeBlock))
            throw new NotImplementedException("Can't initialize the GridNode of an object that doesn't implement " + IThEGridNodeBlock.class.getSimpleName() + "!");
        if(this.getOwner() != null)
            ((IThEGridNodeBlock) this).getGridNode().setPlayerID(AEApi.instance().registries().players().getID(this.getOwner()));
    }
}
