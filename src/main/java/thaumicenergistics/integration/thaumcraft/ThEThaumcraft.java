package thaumicenergistics.integration.thaumcraft;

import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import appeng.api.AEApi;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ScanItem;
import thaumcraft.api.research.ScanningManager;

import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.IThEIntegration;
import thaumicenergistics.util.ThELog;

/**
 * @author BrockWS
 */
public class ThEThaumcraft implements IThEIntegration {

    @Override
    public void preInit() {
    }

    @Override
    public void init() {
        ThELog.info("Registering Research");
        ThaumcraftApi.registerResearchLocation(new ResourceLocation(ModGlobals.MOD_ID, "research/" + ModGlobals.RESEARCH_CATEGORY));
        Optional<ItemStack> core;

        core = AEApi.instance().definitions().materials().annihilationCore().maybeStack(1);
        core.ifPresent(itemStack -> ScanningManager.addScannableThing(new ScanItem("AECORE", itemStack)));

        core = AEApi.instance().definitions().materials().formationCore().maybeStack(1);
        core.ifPresent(itemStack -> ScanningManager.addScannableThing(new ScanItem("AECORE", itemStack)));
    }

    @Override
    public void postInit() {
        ThELog.info("Registering Research Category");
        ResearchCategory category = ResearchCategories.registerCategory(
                ModGlobals.RESEARCH_CATEGORY,
                "AECORE",
                new AspectList().add(Aspect.FIRE, 1),
                new ResourceLocation(ModGlobals.MOD_ID, "textures/research/tab_icon.png"),
                ResearchCategories.getResearchCategory("BASICS").background,
                ResearchCategories.getResearchCategory("BASICS").background2);

    }
}
