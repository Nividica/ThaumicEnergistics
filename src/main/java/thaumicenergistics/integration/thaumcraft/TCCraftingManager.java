package thaumicenergistics.integration.thaumcraft;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import net.minecraftforge.items.IItemHandler;

import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.IArcaneWorkbench;
import thaumcraft.api.items.IVisDiscountGear;
import thaumcraft.api.potions.PotionVisExhaust;
import thaumcraft.common.lib.potions.PotionInfectiousVisExhaust;

import thaumicenergistics.container.DummyContainer;

/**
 * @author BrockWS
 */
public class TCCraftingManager {

    public static IArcaneRecipe findArcaneRecipe(IItemHandler handler, EntityPlayer player) {
        ArcaneInventoryCrafting inventory = TCCraftingManager.getInvFromItemHandler(handler);
        IRecipe recipe = CraftingManager.findMatchingRecipe(inventory, player.world);
        return recipe instanceof IArcaneRecipe
                && ThaumcraftCapabilities.knowsResearch(player, ((IArcaneRecipe) recipe).getResearch()) ?
                (IArcaneRecipe) recipe :
                null;
    }

    public static ItemStack getCraftingResult(IItemHandler handler, IArcaneRecipe recipe) {
        return recipe.getCraftingResult(TCCraftingManager.getInvFromItemHandler(handler));
    }

    public static ArcaneInventoryCrafting getInvFromItemHandler(IItemHandler handler) {
        ArcaneInventoryCrafting inventory = new ArcaneInventoryCrafting();
        for (int i = 0; i < handler.getSlots(); i++) {
            inventory.setInventorySlotContents(i, handler.getStackInSlot(i).copy());
        }
        return inventory;
    }

    public static float getDiscount(EntityPlayer player) {
        if (player == null)
            return 0f;
        int discount = 0;

        for (int i = 0; i < 4; i++) {
            ItemStack stack = player.inventory.getStackInSlot(36 + i);
            if (stack.isEmpty() || !(stack.getItem() instanceof IVisDiscountGear))
                continue;
            IVisDiscountGear gear = (IVisDiscountGear) stack.getItem();
            discount += gear.getVisDiscount(stack, player);
        }

        // TODO: Baubles

        int level1 = 0;
        int level2 = 0;
        if (player.isPotionActive(PotionVisExhaust.instance)) {
            level1 = Objects.requireNonNull(player.getActivePotionEffect(PotionVisExhaust.instance)).getAmplifier();
        }
        if (player.isPotionActive(PotionInfectiousVisExhaust.instance)) {
            level2 = Objects.requireNonNull(player.getActivePotionEffect(PotionInfectiousVisExhaust.instance)).getAmplifier();
        }
        if (level1 > 0 || level2 > 0)
            discount -= (Math.max(level1, level2) + 1) * 10;
        return discount / 100f;
    }

    private static class ArcaneInventoryCrafting extends InventoryCrafting implements IArcaneWorkbench {
        public ArcaneInventoryCrafting() {
            super(new DummyContainer(), 5, 3);
        }
    }

}
