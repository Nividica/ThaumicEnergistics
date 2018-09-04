package thaumicenergistics.integration.thaumcraft.research;

import net.minecraft.entity.player.EntityPlayer;

import thaumcraft.api.research.theorycraft.ResearchTableData;
import thaumcraft.api.research.theorycraft.TheorycraftCard;

import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public class CardTinkerAE extends TheorycraftCard {

    @Override
    public int getInspirationCost() {
        return 1;
    }

    @Override
    public String getLocalizedName() {
        return "card.tinkerae.name";
    }

    @Override
    public String getLocalizedText() {
        return "Experiment with Energy/Matter Conversion";
    }

    @Override
    public boolean activate(EntityPlayer player, ResearchTableData data) {
        data.addTotal(ModGlobals.RESEARCH_CATEGORY, 15);
        return true;
    }
}
