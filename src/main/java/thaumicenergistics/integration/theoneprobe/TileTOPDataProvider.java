package thaumicenergistics.integration.theoneprobe;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumicenergistics.tile.TileArcaneAssembler;
import thaumicenergistics.tile.TileNetwork;

/**
 * @author Alex811
 */
public class TileTOPDataProvider extends TOPDataProvider {
    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());
        if(te instanceof TileNetwork){   // Add power-state info to our TileEntities
            ((TileNetwork) te).withPowerStateText(probeInfo::text, this::getLocalizedKey);
            if(te instanceof TileArcaneAssembler)
                ((TileArcaneAssembler) te).withInfoText(probeInfo::text, this::getLocalizedKey);
        }
    }
}
