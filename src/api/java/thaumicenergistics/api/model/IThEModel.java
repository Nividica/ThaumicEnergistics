package thaumicenergistics.api.model;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Implemented by Block/Item/Tile that needs to initialise a custom model
 *
 * @author BrockWS
 */
public interface IThEModel {

    /**
     * Called during {@link net.minecraftforge.client.event.ModelRegistryEvent ModelRegistryEvent}
     */
    @SideOnly(Side.CLIENT)
    void initModel();
}
