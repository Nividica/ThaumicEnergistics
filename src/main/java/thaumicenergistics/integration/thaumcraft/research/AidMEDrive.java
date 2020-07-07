package thaumicenergistics.integration.thaumcraft.research;

import appeng.api.AEApi;
import thaumcraft.api.research.theorycraft.ITheorycraftAid;
import thaumcraft.api.research.theorycraft.TheorycraftCard;

public class AidMEDrive implements ITheorycraftAid {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public Object getAidObject() {
        return AEApi.instance().definitions().blocks().drive().maybeBlock().get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<TheorycraftCard>[] getCards() {
        return new Class[]{CardTinkerAE.class};
    }
}
