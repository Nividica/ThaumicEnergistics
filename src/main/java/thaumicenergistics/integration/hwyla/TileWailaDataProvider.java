package thaumicenergistics.integration.hwyla;

import appeng.api.implementations.IPowerChannelState;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.tile.TileArcaneAssembler;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Alex811
 */
public class TileWailaDataProvider implements IWailaDataProvider {
    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity te = accessor.getTileEntity();
        if(te instanceof IPowerChannelState){
            IPowerChannelState pst = (IPowerChannelState) te;
            if(pst.isPowered()){
                if(pst.isActive())
                    tooltip.add(ThEApi.instance().lang().deviceOnline().getLocalizedKey());
                else
                    tooltip.add(ThEApi.instance().lang().deviceMissingChannel().getLocalizedKey());
            }else
                tooltip.add(ThEApi.instance().lang().deviceOffline().getLocalizedKey());
        }
        if(te instanceof TileArcaneAssembler){
            TileArcaneAssembler taa = (TileArcaneAssembler) te;
            if(taa.isActive()){
                if(taa.hasJob()){
                    tooltip.add("Status: busy");
                    tooltip.add("Progress: " + taa.getProgress() + "%");
                }else{
                    tooltip.add("Status: idle");
                    if(taa.isMissingAspect())
                        tooltip.add("Error: out of aspect");
                    if(!taa.getHasEnoughVis())
                        tooltip.add("Error: out of Vis");
                }
            }
        }
        return tooltip;
    }
}
