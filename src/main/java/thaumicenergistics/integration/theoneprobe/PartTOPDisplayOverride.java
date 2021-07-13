package thaumicenergistics.integration.theoneprobe;

import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.part.PartBase;

/**
 * @author Alex811
 */
public class PartTOPDisplayOverride implements IBlockDisplayOverride {
    @Override
    public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        PartBase part = TOPPartAccessor.getPart(world.getTileEntity(data.getPos()), data);
        if(part != null){
            probeInfo.horizontal()
                    .item(part.getRepr())
                    .vertical()
                    .itemLabel(part.getRepr())
                    .text(TextStyleClass.MODNAME + ModGlobals.MOD_NAME);
            return true;
        }
        return false;
    }
}
