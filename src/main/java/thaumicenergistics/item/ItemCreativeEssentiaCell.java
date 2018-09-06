package thaumicenergistics.item;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.ICellWorkbenchItem;

import thaumicenergistics.client.render.IThEModel;
import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public class ItemCreativeEssentiaCell extends ItemBase implements ICellWorkbenchItem, IThEModel {

    public ItemCreativeEssentiaCell() {
        super("essentia_cell_creative");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Contains all types of essentia");
    }

    @Override
    public boolean isEditable(ItemStack itemStack) {
        return false;
    }

    @Override
    public IItemHandler getUpgradesInventory(ItemStack itemStack) {
        return null;
    }

    @Override
    public IItemHandler getConfigInventory(ItemStack itemStack) {
        return null;
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack itemStack) {
        return null;
    }

    @Override
    public void setFuzzyMode(ItemStack itemStack, FuzzyMode fuzzyMode) {

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(ModGlobals.MOD_ID + ":cell/essentia_cell_creative", "inventory"));
    }
}
