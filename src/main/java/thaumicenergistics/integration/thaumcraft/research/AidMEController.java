package thaumicenergistics.integration.thaumcraft.research;

import appeng.api.AEApi;

import thaumcraft.api.research.theorycraft.ITheorycraftAid;
import thaumcraft.api.research.theorycraft.TheorycraftCard;

/**
 * @author BrockWS
 */
public class AidMEController implements ITheorycraftAid {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public Object getAidObject() {
        return AEApi.instance().definitions().blocks().controller().maybeBlock().get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<TheorycraftCard>[] getCards() {
        return new Class[]{CardTinkerAE.class};
    }
}
