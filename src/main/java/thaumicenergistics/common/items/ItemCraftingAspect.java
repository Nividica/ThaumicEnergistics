package thaumicenergistics.common.items;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.integration.tc.AspectHooks;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item used to represent an {@link Aspect}
 *
 * @author Nividica
 *
 */
public class ItemCraftingAspect extends Item {

    private static final String NBTKEY_ASPECT = "Aspect";

    public ItemCraftingAspect() {
        this.setMaxStackSize(64);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(ThaumicEnergistics.ThETab);
        this.setUnlocalizedName("itemCraftingAspect");
    }

    /**
     * Returns true if the player has discovered the aspect.
     *
     * @param player
     * @param aspect
     * @return
     */
    public static boolean canPlayerSeeAspect(@Nonnull final EntityPlayer player, @Nonnull final Aspect aspect) {
        return Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(player.getCommandSenderName(), aspect);
    }

    /**
     * Creates a new crafting aspect stack with the specified amount.
     *
     * @param aspect
     * @param amount
     * @return
     */
    public static ItemStack createStackForAspect(final Aspect aspect, final int amount) {
        // Create the stack
        ItemStack stack = ItemEnum.CRAFTING_ASPECT.getStack(amount);

        // Set the aspect
        setAspect(stack, aspect);

        return stack;
    }

    /**
     * Gets the aspect associated with the item.
     *
     * @param itemStack
     * @return
     */
    public static Aspect getAspect(final ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        return getAspect(itemStack.getTagCompound());
    }

    /**
     * Gets the aspect stored in the crafting items tag.
     *
     * @param tag
     * @return
     */
    public static Aspect getAspect(final NBTTagCompound tag) {
        if ((tag != null) && tag.hasKey(NBTKEY_ASPECT)) {
            // Get the aspect
            return Aspect.getAspect(tag.getString(NBTKEY_ASPECT));
        }
        return null;
    }

    /**
     * Sets the aspect of the item stack.
     *
     * @param stack
     * @param aspect
     */
    public static void setAspect(final ItemStack stack, final Aspect aspect) {
        // Null check
        if (aspect == null) {
            // Clear the aspect
            stack.setTagCompound(null);
            return;
        }

        NBTTagCompound tag = null;

        // Is there a tag?
        if (!stack.hasTagCompound()) {
            // Create the tag
            tag = new NBTTagCompound();

            // Set the tag
            stack.setTagCompound(tag);
        } else {
            tag = stack.getTagCompound();
        }

        // Set the aspect
        tag.setString(NBTKEY_ASPECT, aspect.getTag());
    }

    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List list,
            final boolean advancedInfo) {
        // Call super
        super.addInformation(stack, player, list, advancedInfo);

        // Is there an aspect associated?
        Aspect aspect = ItemCraftingAspect.getAspect(stack);
        if (aspect != null) {
            // Has the player discovered this aspect?
            if (ItemCraftingAspect.canPlayerSeeAspect(player, aspect)) {
                // Add the aspect info
                list.add(aspect.getLocalizedDescription());

                ModContainer mod = AspectHooks.aspectToMod.getOrDefault(aspect, null);
                if (mod != null) {
                    list.add(
                            EnumChatFormatting.DARK_PURPLE.toString() + EnumChatFormatting.ITALIC.toString()
                                    + mod.getName());
                }
            } else {
                // Show unknown
                list.add(StatCollector.translateToLocal("tc.aspect.unknown"));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(final ItemStack stack, final int renderPass) {
        // Is there an aspect associated?
        Aspect aspect = ItemCraftingAspect.getAspect(stack);
        if (aspect != null) {
            // Return it's color
            return aspect.getColor();
        }

        // Pass to super
        return super.getColorFromItemStack(stack, renderPass);
    }

    @Override
    public String getItemStackDisplayName(final ItemStack stack) {
        return this.getUnlocalizedNameInefficiently(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(final Item item, final CreativeTabs par2CreativeTabs, final List itemList) {
        // Don't show these
        /*
         * for( Aspect aspect : Aspect.aspects.values() ) { ItemStack stack = new ItemStack( this, 1, 0 );
         * ItemCraftingAspect.setAspect( stack, aspect ); itemList.add( stack ); }
         */
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getUnlocalizedName(final ItemStack stack) {
        Aspect aspect = ItemCraftingAspect.getAspect(stack);

        // Null check
        if (aspect != null) {
            // Get the player
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            // Has the player discovered the aspect?
            if (ItemCraftingAspect.canPlayerSeeAspect(player, aspect)) {
                // Show name
                return aspect.getName();
            }
        }

        // Show unknown
        return "tc.aspect.unknown";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(final IIconRegister par1IconRegister) {
        // Handled by special renderer
    }
}
