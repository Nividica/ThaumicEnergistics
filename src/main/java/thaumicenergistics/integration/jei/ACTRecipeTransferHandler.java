package thaumicenergistics.integration.jei;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import javax.annotation.Nullable;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import thaumicenergistics.container.part.ContainerArcaneTerminal;
import thaumicenergistics.network.PacketHandler;
import thaumicenergistics.network.packets.PacketJEIRecipe;
import thaumicenergistics.util.ForgeUtil;

/**
 * @author BrockWS
 */
public class ACTRecipeTransferHandler implements IRecipeTransferHandler<ContainerArcaneTerminal> {

    private IRecipeTransferHandlerHelper recipeTransferHelper;

    public ACTRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.recipeTransferHelper = helper;
    }

    @Override
    public Class<ContainerArcaneTerminal> getContainerClass() {
        return ContainerArcaneTerminal.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ContainerArcaneTerminal container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList normalIngredients = new NBTTagList();
        NBTTagList crystalIngredients = new NBTTagList();
        ingredients.forEach((i, o) -> {
            if (i == 0)
                return;
            NBTTagList subTypes = new NBTTagList();
            ItemStack displayed = o.getDisplayedIngredient();
            if (displayed != null && !displayed.isEmpty()) // Try using the displayed one first
                subTypes.appendTag(o.getDisplayedIngredient().serializeNBT());
            o.getAllIngredients().forEach(stack -> {
                if (!ForgeUtil.areItemStacksEqual(stack, displayed)) // No point sending the same stack twice
                    subTypes.appendTag(stack.serializeNBT());
            });
            if (o.isInput())
                normalIngredients.appendTag(subTypes);
            else
                crystalIngredients.appendTag(subTypes);
        });
        tag.setTag("normal", normalIngredients);
        tag.setTag("crystal", crystalIngredients);
        // TODO: Actually check and report errors
        if (doTransfer)
            PacketHandler.sendToServer(new PacketJEIRecipe(tag));
        return null;
    }
}
