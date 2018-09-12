package thaumicenergistics.item;

import com.google.common.base.Preconditions;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import net.minecraftforge.client.model.ModelLoader;

import thaumicenergistics.client.render.IThEModel;
import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public class ItemMaterial extends ItemBase implements IThEModel {

    public ItemMaterial(String id) {
        super(id);
    }

    @Override
    public void initModel() {
        Preconditions.checkNotNull(this.getRegistryName());
        Preconditions.checkNotNull(this.getRegistryName().getResourcePath());

        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(ModGlobals.MOD_ID + ":material/" + this.getRegistryName().getResourcePath(), "inventory"));
    }
}
