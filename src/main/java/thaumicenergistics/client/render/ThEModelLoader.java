package thaumicenergistics.client.render;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.util.ThELog;

/**
 * @author BrockWS
 */
@SideOnly(Side.CLIENT)
public class ThEModelLoader implements ICustomModelLoader {

    private Map<String, IModel> models;

    public ThEModelLoader() {
        this.models = new HashMap<>();
    }

    public void addModel(String path, IModel model) {
        this.models.put(path, model);
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!modelLocation.getResourceDomain().equals(ModGlobals.MOD_ID))
            return false;
        return this.models.containsKey(modelLocation.getResourcePath());
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        return this.models.get(modelLocation.getResourcePath());
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }
}
