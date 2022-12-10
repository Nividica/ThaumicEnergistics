package thaumicenergistics.common.items;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.registries.ThEStrings;

/**
 * Holds arcane crafting patterns.
 *
 * @author Nividica
 *
 */
public class ItemKnowledgeCore extends Item {

    public ItemKnowledgeCore() {
        // Can not be damaged
        this.setMaxDamage(0);

        // Has no subtypes
        this.setHasSubtypes(false);

        // Can not stack
        this.setMaxStackSize(1);
    }

    @Override
    public void addInformation(
            final ItemStack kCore, final EntityPlayer player, final List tooltip, final boolean advancedItemTooltips) {}

    @Override
    public String getUnlocalizedName() {
        return ThEStrings.Item_KnowledgeCore.getUnlocalized();
    }

    @Override
    public String getUnlocalizedName(final ItemStack itemStack) {
        return ThEStrings.Item_KnowledgeCore.getUnlocalized();
    }

    /**
     * Registers and sets the core icon
     */
    @Override
    public void registerIcons(final IIconRegister iconRegister) {
        this.itemIcon = iconRegister.registerIcon(ThaumicEnergistics.MOD_ID + ":knowledge.core");
    }
}
