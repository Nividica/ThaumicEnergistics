package thaumicenergistics.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import thaumicenergistics.init.ModGUIs;

/**
 * @author Alex811
 */
public interface IThEGuiTile {
    ModGUIs getGUI();

    void openGUI(EntityPlayer player);
}
