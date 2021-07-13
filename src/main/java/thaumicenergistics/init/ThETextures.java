package thaumicenergistics.init;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thaumicenergistics.api.IThETextures;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex811
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ThETextures implements IThETextures {

    private static List<ResourceLocation> TEXTURES = new ArrayList<>();

    @SubscribeEvent
    public static void textureStitch(TextureStitchEvent.Pre event){
        TEXTURES.forEach(event.getMap()::registerSprite);
    }

    private static ResourceLocation addTexture(String texture){
        ResourceLocation resourceLocation = new ResourceLocation(ModGlobals.MOD_ID, texture);
        TEXTURES.add(resourceLocation);
        return resourceLocation;
    }

    private final ResourceLocation textureKnowledgeCoreSlot;

    public ThETextures(){
        this.textureKnowledgeCoreSlot = ThETextures.addTexture("gui/slot/knowledge_core");
    }

    @Override
    public ResourceLocation knowledgeCoreSlot(){
        return this.textureKnowledgeCoreSlot;
    }
}
