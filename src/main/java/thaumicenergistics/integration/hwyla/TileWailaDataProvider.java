package thaumicenergistics.integration.hwyla;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import thaumicenergistics.tile.TileArcaneAssembler;
import thaumicenergistics.tile.TileNetwork;

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
        if(te instanceof TileNetwork){
            ((TileNetwork) te).withPowerStateText(tooltip::add);
            if(te instanceof TileArcaneAssembler)
                ((TileArcaneAssembler) te).withInfoText(tooltip::add);
        }
        return tooltip;
    }
}
