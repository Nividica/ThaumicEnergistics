package thaumicenergistics.integration.theoneprobe;

import appeng.api.implementations.IPowerChannelState;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.tile.TileArcaneAssembler;
import thaumicenergistics.tile.TileBase;

/**
 * @author Alex811
 */
public class TileTOPDataProvider extends TOPDataProvider {
    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        TileEntity te = world.getTileEntity(data.getPos());
        if(!(te instanceof TileBase)) return;
        if(te instanceof IPowerChannelState){   // Add power-state info to our TileEntities
            IPowerChannelState pst = (IPowerChannelState) te;
            if(pst.isPowered()){
                if(pst.isActive())
                    probeInfo.text(ThEApi.instance().lang().deviceOnline().getLocalizedKey());
                else
                    probeInfo.text(ThEApi.instance().lang().deviceMissingChannel().getLocalizedKey());
            }else
                probeInfo.text(ThEApi.instance().lang().deviceOffline().getLocalizedKey());
        }
        if(te instanceof TileArcaneAssembler){
            TileArcaneAssembler taa = (TileArcaneAssembler) te;
            if(taa.isActive()){
                if(taa.hasJob()){
                    probeInfo.text("Status: busy");
                    probeInfo.text("Progress: " + taa.getProgress() + "%");
                }else{
                    probeInfo.text("Status: idle");
                    if(taa.isMissingAspect())
                        probeInfo.text("Error: out of aspect");
                    if(!taa.getHasEnoughVis())
                        probeInfo.text("Error: out of Vis");
                }
            }
        }
    }
}
