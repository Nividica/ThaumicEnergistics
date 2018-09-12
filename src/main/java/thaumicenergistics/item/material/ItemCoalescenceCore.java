package thaumicenergistics.item.material;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import net.minecraftforge.client.model.ModelLoader;

import thaumicenergistics.client.render.IThEModel;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.item.ItemBase;

/**
 * @author BrockWS
 */
public class ItemCoalescenceCore extends ItemBase implements IThEModel {

    public ItemCoalescenceCore(String id) {
        super(id);
    }

    @Override
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(ModGlobals.MOD_ID + ":material/coalescence_core", "inventory"));
    }
}
