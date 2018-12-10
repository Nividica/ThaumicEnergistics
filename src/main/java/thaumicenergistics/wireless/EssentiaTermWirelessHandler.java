package thaumicenergistics.wireless;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import thaumicenergistics.api.wireless.IEssentiaTermWirelessHandler;
import thaumicenergistics.api.wireless.IThEWirelessObject;
import thaumicenergistics.util.ThELog;

/**
 * @author BrockWS
 */
public class EssentiaTermWirelessHandler implements IEssentiaTermWirelessHandler {

    private List<IThEWirelessObject> registeredObjects;

    public EssentiaTermWirelessHandler() {
        this.registeredObjects = new ArrayList<>();
    }

    @Override
    public void openGUI(Object obj, EntityPlayer player) {
        ThELog.info("Opening GUI");
    }

    @Override
    public List<IThEWirelessObject> getRegisteredObjects() {
        return this.registeredObjects;
    }
}
