package thaumicenergistics.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.api.items.ItemsTC;

/**
 * @author BrockWS
 */
public class TCUtil {

    public static void drainVis(World world, BlockPos pos, float vis, int radis) {
        if (world == null || pos == null || vis <= 0)
            return;
        if (radis == 0) { // We can only drain from one chunk
            AuraHelper.drainVis(world, pos, vis, false);
            return;
        }
        int i = (radis * 2) + 1;
        i = i * i;
        double toDrain = vis / i;
        float drained = 0f;
        for (int x = (-16 * radis); x <= 16 * radis; x += 16) {
            for (int z = (-16 * radis); z <= 16 * radis; z += 16) {
                //ThELog.info("Draining {} from chunk", toDrain);
                drained += AuraHelper.drainVis(world, pos.add(x, 0, z), (float) toDrain, false);
            }
        }
        if (drained < vis) { // We didn't drain enough, so loop through chunks to drain as much as possible
            //ThELog.info("Trying to completly drain each chunk");
            for (int x = (-16 * radis); x <= 16 * radis; x += 16) {
                for (int z = (-16 * radis); z <= 16 * radis; z += 16) {
                    //ThELog.info("Draining {} from chunk", vis - drained);
                    drained += AuraHelper.drainVis(world, pos.add(x, 0, z), vis - drained, false);
                }
            }
        }
        if (vis - drained > 0.1)
            ThELog.error("Failed to drain enough vis from nearby chunks. Drained {} of {}", drained, vis);
    }

    @Deprecated
    public static int getMaxStorable(IEssentiaContainerItem container) {
        switch (container.getClass().getSimpleName()) {
            case "ItemPhial":
                return 10;
        }
        return 0;
    }

    public static Aspect getCrystalAspect(ItemStack stack) {
        if (!(stack.getItem() instanceof IEssentiaContainerItem) || stack.getItem() != ItemsTC.crystalEssence)
            return null;
        return ((IEssentiaContainerItem) stack.getItem()).getAspects(stack).getAspects()[0];
    }

    public static AspectList getItemAspects(ItemStack stack) {
        return ThaumcraftApi.internalMethods.getObjectAspects(stack);
    }
}
