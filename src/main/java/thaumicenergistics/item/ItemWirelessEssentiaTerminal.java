package thaumicenergistics.item;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.wireless.IEssentiaTermWirelessHandler;
import thaumicenergistics.api.wireless.IThEWirelessHandler;
import thaumicenergistics.api.wireless.IThEWirelessObject;
import thaumicenergistics.client.render.IThEModel;
import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public class ItemWirelessEssentiaTerminal extends ItemBase implements IThEWirelessObject, IThEModel {

    public ItemWirelessEssentiaTerminal(String id) {
        super(id);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ThEApi.instance().wireless().openGUI(player.getHeldItem(hand), player);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public boolean isHandledBy(IThEWirelessHandler handler, Object obj, EntityPlayer player) {
        return handler == ThEApi.instance().wireless().getWirelessHandler(IEssentiaTermWirelessHandler.class);
    }

    @Override
    public boolean hasPower(double amount) {
        return false;
    }

    @Override
    public boolean usePower(double amount) {
        return false;
    }

    @Override
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(ModGlobals.MOD_ID + ":wireless_essentia_terminal", "inventory"));
    }
}
