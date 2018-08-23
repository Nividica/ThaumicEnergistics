package thaumicenergistics.util;

import thaumcraft.api.aspects.IEssentiaContainerItem;

/**
 * @author BrockWS
 */
public class TCUtil {

    public static int getMaxStorable(IEssentiaContainerItem container) {
        switch (container.getClass().getSimpleName()) {
            case "ItemPhial":
                return 10;
        }
        return 0;
    }
}
