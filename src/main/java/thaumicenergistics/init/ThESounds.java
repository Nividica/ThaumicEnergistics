package thaumicenergistics.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import thaumicenergistics.api.IThESounds;

import java.util.HashMap;

/**
 * @author Alex811
 */
@Mod.EventBusSubscriber
public class ThESounds implements IThESounds {

    private static HashMap<String, ResourceLocation> SOUNDS = new HashMap<>();

    @SubscribeEvent
    public static void registerSoundEvents(RegistryEvent.Register<SoundEvent> event){
        IForgeRegistry<SoundEvent> registry = event.getRegistry();
        SOUNDS.forEach((name, loc) -> {
            SoundEvent soundEvent = new SoundEvent(loc);
            soundEvent.setRegistryName(name);
            registry.register(soundEvent);
        });
    }

    private static ResourceLocation addSound(String sound){
        ResourceLocation resourceLocation = new ResourceLocation(ModGlobals.MOD_ID, sound);
        SoundEvent soundEvent = new SoundEvent(resourceLocation);
        soundEvent.setRegistryName(sound);
        SOUNDS.put(sound, resourceLocation);
        return resourceLocation;
    }

    private final ResourceLocation soundKnowledgeCoreWrite;
    private final ResourceLocation soundKnowledgeCorePowerUp;
    private final ResourceLocation soundKnowledgeCorePowerDown;

    public ThESounds(){
        this.soundKnowledgeCoreWrite = ThESounds.addSound("knowledge_core_write");
        this.soundKnowledgeCorePowerUp = ThESounds.addSound("knowledge_core_power_up");
        this.soundKnowledgeCorePowerDown = ThESounds.addSound("knowledge_core_power_down");
    }

    @Override
    public ResourceLocation knowledgeCoreWrite(){
        return this.soundKnowledgeCoreWrite;
    }

    @Override
    public ResourceLocation knowledgeCorePowerUp(){
        return this.soundKnowledgeCorePowerUp;
    }

    @Override
    public ResourceLocation knowledgeCorePowerDown(){
        return this.soundKnowledgeCorePowerDown;
    }
}
