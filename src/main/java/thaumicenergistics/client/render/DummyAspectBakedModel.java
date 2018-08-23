package thaumicenergistics.client.render;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ItemLayerModel;

import thaumcraft.api.aspects.Aspect;

import thaumicenergistics.item.ItemDummyAspect;

/**
 * @author BrockWS
 */
public class DummyAspectBakedModel implements IBakedModel {

    private IBakedModel base;
    private VertexFormat format;
    private Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;
    private ImmutableList<BakedQuad> quads;

    public DummyAspectBakedModel(IBakedModel base, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.base = base;
        this.format = format;
        this.bakedTextureGetter = bakedTextureGetter;
        this.quads = ImmutableList.of();
    }

    public DummyAspectBakedModel(ImmutableList<BakedQuad> quads) {
        this.quads = quads;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return this.quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        if (this.hasBase())
            return this.base.getParticleTexture();
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new ItemOverrideList(Collections.emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                if (!(stack.getItem() instanceof ItemDummyAspect))
                    return super.handleItemState(originalModel, stack, world, entity);

                ItemDummyAspect item = (ItemDummyAspect) stack.getItem();

                Aspect aspect = item.getAspect(stack);
                if (aspect == null)
                    return new DummyAspectBakedModel(ImmutableList.of());

                TextureAtlasSprite sprite = bakedTextureGetter.apply(getAspectImage(aspect));
                if (sprite == null || sprite.getIconName().equalsIgnoreCase("missingno"))
                    throw new NullPointerException("Unable to find texture for aspect " + aspect.getName());

                return new DummyAspectBakedModel(ItemLayerModel.getQuadsForSprite(0, sprite, format, Optional.empty()));
            }
        };
    }

    private boolean hasBase() {
        return this.base != null;
    }

    private ResourceLocation getAspectImage(Aspect aspect) {
        return new ResourceLocation(aspect.getImage().getResourceDomain(), aspect.getImage().getResourcePath()
                .replace("textures/", "")
                .replace(".png", ""));
    }
}
