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
import thaumcraft.api.research.theorycraft.TheorycraftManager;

import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.IThEIntegration;
import thaumicenergistics.integration.thaumcraft.research.CardTinkerAE;
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
        ThELog.info("Registering Research Category");
        ResearchCategory category = ResearchCategories.registerCategory(
                ModGlobals.RESEARCH_CATEGORY,
                "f_AECORE",
                new AspectList()
                        .add(Aspect.MECHANISM, 15)
                        .add(Aspect.CRAFT, 15)
                        .add(Aspect.ENERGY, 20)
                        .add(Aspect.EXCHANGE, 20)
                        .add(Aspect.MAGIC, 15)
                        .add(Aspect.METAL, 5),
                new ResourceLocation(ModGlobals.MOD_ID, "textures/research/tab_icon.png"),
                ResearchCategories.getResearchCategory("BASICS").background,
                ResearchCategories.getResearchCategory("BASICS").background2);
        ThELog.info("Registering Research");
        ThaumcraftApi.registerResearchLocation(new ResourceLocation(ModGlobals.MOD_ID, "research/" + ModGlobals.RESEARCH_CATEGORY));
        Optional<ItemStack> core;

        core = AEApi.instance().definitions().materials().annihilationCore().maybeStack(1);
        core.ifPresent(itemStack -> ScanningManager.addScannableThing(new ScanItem("f_AECORE", itemStack)));

        core = AEApi.instance().definitions().materials().formationCore().maybeStack(1);
        core.ifPresent(itemStack -> ScanningManager.addScannableThing(new ScanItem("f_AECORE", itemStack)));

        TheorycraftManager.registerCard(CardTinkerAE.class);
        //if (AEApi.instance().definitions().blocks().controller().maybeEntity().isPresent())
        //TheorycraftManager.registerAid(new AidMEController());
    }

    @Override
    public void postInit() {

    }
}
