package thaumicenergistics.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * @author BrockWS
 */
public class ForgeUtil {

    public static boolean isClient() {
        return ForgeUtil.getSide().isClient();
    }

    public static boolean isServer() {
        return ForgeUtil.getSide().isServer();
    }

    public static Side getSide() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }

    public static <K extends IForgeRegistryEntry<K>> IForgeRegistry<K> getRegistry(Class<K> reg) {
        return GameRegistry.findRegistry(reg);
    }

    public static <K extends IForgeRegistryEntry<K>> IForgeRegistryEntry getRegistryEntry(Class<K> reg, ResourceLocation resourceLocation) {
        return ForgeUtil.getRegistry(reg).getValue(resourceLocation);
    }

    public static ItemStack addStackToPlayerInventory(EntityPlayer player, ItemStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty())
            return ItemStack.EMPTY;

        IItemHandler handler = new PlayerMainInvWrapper(player.inventory);
        ItemStack left = stack.copy();

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack existing = handler.getStackInSlot(slot);
            if (ForgeUtil.areItemStacksEqual(existing, left))
                left = handler.insertItem(slot, left, simulate);
            if (left.isEmpty())
                return ItemStack.EMPTY;
        }

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            left = handler.insertItem(slot, left, simulate);
            if (left.isEmpty())
                return ItemStack.EMPTY;
        }
        return left;
    }

    public static boolean areItemStacksEqual(ItemStack a, ItemStack b) {
        return ItemStack.areItemsEqual(a, b) && ForgeUtil.areNBTTagsEqual(a.getTagCompound(), b.getTagCompound());
    }

    public static boolean areNBTTagsEqual(NBTBase a, NBTBase b) {
        return a == b || (a.hasNoTags() && b.hasNoTags()) || (a.hasNoTags() != b.hasNoTags()) || a.equals(b);
    }
}
