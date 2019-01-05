package thaumicenergistics.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Contains both Minecraft and Forge utility functions
 *
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
        return ItemHandlerUtil.insert(new PlayerMainInvWrapper(player.inventory), stack, simulate);
    }

    public static boolean areItemStacksEqual(ItemStack a, ItemStack b) {
        return a != null && b != null && ItemStack.areItemsEqual(a, b) && ForgeUtil.areNBTTagsEqual(a.getTagCompound(), b.getTagCompound());
    }

    public static boolean areNBTTagsEqual(NBTBase a, NBTBase b) {
        return a == b || (a.hasNoTags() && b.hasNoTags()) || (a.hasNoTags() != b.hasNoTags()) || a.equals(b);
    }

    /**
     * Merges list b into a
     *
     * @param a Merged into
     * @param b List to be merged
     * @return a with the contents of b
     */
    public static NBTTagList mergeTagLists(NBTTagList a, NBTTagList b) {
        b.forEach(a::appendTag);
        return a;
    }

    public static List<NBTBase> toArrayList(NBTTagList tags) {
        return ForgeUtil.toList(tags, new ArrayList<>());
    }

    public static List<NBTBase> toList(NBTTagList tags, List<NBTBase> list) {
        tags.forEach(list::add);
        return list;
    }
}
