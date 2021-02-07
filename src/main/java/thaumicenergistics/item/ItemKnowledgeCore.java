package thaumicenergistics.item;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.util.IConfigManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumicenergistics.integration.appeng.util.ThEConfigManager;
import thaumicenergistics.util.KnowledgeCoreUtil;

import java.util.Objects;

/**
 * If you're looking for methods to operate on a
 * Knowledge Core ItemStack and its recipes, check out {@link KnowledgeCoreUtil}
 * @author Alex811
 */
public class ItemKnowledgeCore extends ItemMaterial {

    boolean isBlank;

    public ItemKnowledgeCore(String id, boolean isBlank) {
        super(id, 1);
        this.isBlank = isBlank;
    }

    public boolean isBlank() {
        return this.isBlank;
    }
}
