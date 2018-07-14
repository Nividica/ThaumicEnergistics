package thaumicenergistics.api.model;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author BrockWS
 */
public interface IThEModel {

    @SideOnly(Side.CLIENT)
    void initModel();
}
