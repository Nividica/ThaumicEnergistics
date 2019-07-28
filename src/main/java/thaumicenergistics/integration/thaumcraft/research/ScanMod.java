package thaumicenergistics.integration.thaumcraft.research;

import net.minecraft.entity.player.EntityPlayer;

import thaumcraft.api.research.IScanThing;

import thaumicenergistics.util.AEUtil;

import com.google.common.base.Preconditions;

/**
 * @author BrockWS
 */
public class ScanMod implements IScanThing {

    private String research;
    private String modId;

    public ScanMod(String research, String modId) {
        Preconditions.checkNotNull(research);
        Preconditions.checkNotNull(modId);
        Preconditions.checkArgument(!research.isEmpty());
        Preconditions.checkArgument(!modId.isEmpty());
        this.research = research;
        this.modId = modId;
    }

    @Override
    public boolean checkThing(EntityPlayer entityPlayer, Object o) {
        if (o == null)
            return false;
        return AEUtil.getModID(o).equalsIgnoreCase(modId);
    }

    @Override
    public String getResearchKey(EntityPlayer entityPlayer, Object o) {
        return this.research;
    }
}
