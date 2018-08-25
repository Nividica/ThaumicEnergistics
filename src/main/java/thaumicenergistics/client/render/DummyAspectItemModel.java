package thaumicenergistics.client.render;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.init.ModGlobals;

/**
 * @author BrockWS
 */
public class DummyAspectItemModel implements IModel {

    private static ResourceLocation BASE = new ResourceLocation(ModGlobals.MOD_ID, "item/dummy_aspect_base");

    private IModel baseModel;

    public DummyAspectItemModel() {

    }

    private IModel getBaseModel() {
        if (this.baseModel == null) {
            try {
                this.baseModel = ModelLoaderRegistry.getModel(BASE);
            } catch (Exception e) {
                // Couldn't load the base model, something must be really wrong
                throw new RuntimeException(e);
            }
        }
        return this.baseModel;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        List<ResourceLocation> images = new ArrayList<>();
        Aspect.aspects.forEach((s, aspect) -> {
            ResourceLocation image = aspect.getImage();
            if (image.getResourcePath().contains("textures/"))
                image = new ResourceLocation(image.getResourceDomain(), image.getResourcePath().replace("textures/", "").replace(".png", ""));
            images.add(image);
        });
        return ImmutableList.copyOf(images);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IBakedModel base = this.getBaseModel().bake(state, format, bakedTextureGetter);

        return new DummyAspectBakedModel(base, format, bakedTextureGetter);
    }
}
