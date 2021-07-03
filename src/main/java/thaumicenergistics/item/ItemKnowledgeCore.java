package thaumicenergistics.item;

import thaumicenergistics.util.KnowledgeCoreUtil;

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
