package thaumicenergistics.common.features;

import static thaumicenergistics.common.registries.ResearchRegistry.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;

public class FeatureCellMicroscope extends ThEThaumcraftResearchFeature {

    public FeatureCellMicroscope() {
        super(ResearchTypes.CELL_MICROSCOPE.getKey());
    }

    @Override
    protected boolean checkConfigs(@Nonnull IThEConfig theConfig) {
        return true;
    }

    @Nullable
    @Override
    protected Object[] getItemReqs(CommonDependantItems cdi) {
        return new Object[] {};
    }

    @Override
    protected ThEThaumcraftResearchFeature getParentFeature() {
        return FeatureRegistry.instance().featureThaumicEnergistics;
    }

    @Override
    protected void registerPseudoParents() {}

    @Override
    protected void registerCrafting(CommonDependantItems cdi) {
        ItemStack CellMicroscope = ThEApi.instance().items().CellMicroscope.getStack();
        AspectList microscopeAspects = new AspectList();
        microscopeAspects.add(Aspect.AIR, 300);
        microscopeAspects.add(Aspect.EARTH, 300);
        microscopeAspects.add(Aspect.ORDER, 300);
        RecipeRegistry.ITEM_CELL_MICROSCOPE = ThaumcraftApi.addArcaneCraftingRecipe(
                this.researchKey,
                CellMicroscope,
                microscopeAspects,
                new Object[] { "RTR", "SQS", "RPR", 'Q', cdi.QuantumLink, 'P', cdi.ItemP2P, 'S', cdi.Singularity, 'T',
                        cdi.Thaumometer, 'R', cdi.EssentiaMirror });
    }

    @Override
    protected void registerResearch() {
        AspectList msAspectList = new AspectList();
        msAspectList.add(Aspect.VOID, 5);
        msAspectList.add(Aspect.ENERGY, 5);
        msAspectList.add(Aspect.CRYSTAL, 3);
        msAspectList.add(Aspect.METAL, 3);
        ItemStack msIcon = ThEApi.instance().items().CellMicroscope.getStack();
        ResearchPage[] msPages = new ResearchPage[] { new ResearchPage(ResearchTypes.CELL_MICROSCOPE.getPageName(1)),
                new ResearchPage(RecipeRegistry.ITEM_CELL_MICROSCOPE) };

        ResearchTypes.CELL_MICROSCOPE.createResearchItem(msAspectList, COMPLEXITY_SMALL, msIcon, msPages);
        ResearchTypes.CELL_MICROSCOPE.researchItem.setParents(this.getFirstValidParentKey(false));
        ResearchTypes.CELL_MICROSCOPE.researchItem.registerResearchItem();
    }
}
