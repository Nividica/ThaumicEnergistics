package thaumicenergistics.integration.thaumcraft;

import net.minecraft.util.ResourceLocation;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;

import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.IThEIntegration;

/**
 * @author BrockWS
 */
public class ThEThaumcraft implements IThEIntegration {

    @Override
    public void preInit() {
    }

    @Override
    public void init() {

    }

    @Override
    public void postInit() {
        ResearchCategory category = ResearchCategories.registerCategory(
                ModGlobals.RESEARCH_CATEGORY,
                null,
                new AspectList().add(Aspect.FIRE, 1),
                new ResourceLocation(ModGlobals.MOD_ID, "textures/research/tab_icon.png"),
                ResearchCategories.getResearchCategory("BASICS").background,
                ResearchCategories.getResearchCategory("BASICS").background2);
        ThaumcraftApi.registerResearchLocation(new ResourceLocation(ModGlobals.MOD_ID, "research/" + ModGlobals.RESEARCH_CATEGORY));
    }
}
