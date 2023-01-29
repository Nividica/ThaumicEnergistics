package thaumicenergistics.common.features;

import net.minecraft.item.ItemStack;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import thaumicenergistics.common.tiles.TileDistillationPatternEncoder;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

/**
 * {@link TileDistillationPatternEncoder} feature.
 *
 * @author Nividica
 *
 */
public class FeatureAutocrafting_Essentia extends ThEThaumcraftResearchFeature {

    public FeatureAutocrafting_Essentia() {
        super(ResearchTypes.DISTILLATION_PATTERN_ENCODER.getKey());
    }

    @Override
    protected boolean checkConfigs(final IThEConfig theConfig) {
        // Depends on crafting CPU's
        if (!AEConfig.instance.isFeatureEnabled(AEFeature.CraftingCPU)) {
            return false;
        }

        return theConfig.craftDistillationPatternEncoder();
    }

    @Override
    protected Object[] getItemReqs(final CommonDependantItems cdi) {
        return new Object[] { cdi.MEInterface, cdi.EngineeringProcessor };
    }

    @Override
    protected ThEThaumcraftResearchFeature getParentFeature() {
        return FeatureRegistry.instance().featureEssentiaIOBuses;
    }

    @Override
    protected void registerCrafting(final CommonDependantItems cdi) {
        // My items
        ItemStack dpeStack = ThEApi.instance().blocks().DistillationPatternEncoder.getStack();

        // Aspect list
        AspectList dpeAspects = new AspectList();
        dpeAspects.add(Aspect.ORDER, 5);
        dpeAspects.add(Aspect.ENTROPY, 5);
        dpeAspects.add(Aspect.FIRE, 3);

        // Recipe
        Object[] dpeRecipe = new Object[] { "IPI", " T ", "IEI", 'I', cdi.IronIngot, 'P', cdi.IlluminatedPanel, 'T',
                cdi.Thaumometer, 'E', cdi.EngineeringProcessor };

        // Register
        RecipeRegistry.BLOCK_DISTILLATION_PATTERN_ENCODER = ThaumcraftApi
                .addArcaneCraftingRecipe(this.researchKey, dpeStack, dpeAspects, dpeRecipe);
    }

    @Override
    protected void registerPseudoParents() {}

    @Override
    protected void registerResearch() {
        // Aspect list
        AspectList dpeAspects = new AspectList();
        dpeAspects.add(Aspect.CRAFT, 5);
        dpeAspects.add(Aspect.EXCHANGE, 5);
        dpeAspects.add(Aspect.ENTROPY, 5);
        dpeAspects.add(Aspect.MECHANISM, 3);
        dpeAspects.add(Aspect.GREED, 3);
        dpeAspects.add(Aspect.MIND, 1);

        // Icon
        ItemStack dpeIcon = ThEApi.instance().blocks().DistillationPatternEncoder.getStack();

        // Pages
        ResearchPage[] dpePages = new ResearchPage[] {
                new ResearchPage(ResearchTypes.DISTILLATION_PATTERN_ENCODER.getPageName(1)),
                new ResearchPage(RecipeRegistry.BLOCK_DISTILLATION_PATTERN_ENCODER) };

        // Create the research item
        ResearchTypes.DISTILLATION_PATTERN_ENCODER
                .createResearchItem(dpeAspects, ResearchRegistry.COMPLEXITY_LARGE, dpeIcon, dpePages);

        // Set parents
        ResearchTypes.DISTILLATION_PATTERN_ENCODER.researchItem.setParents(this.getFirstValidParentKey(false));
        ResearchTypes.DISTILLATION_PATTERN_ENCODER.researchItem
                .setParentsHidden(FeatureRegistry.instance().featureEssentiaMonitoring.getFirstValidParentKey(true));
        ResearchTypes.DISTILLATION_PATTERN_ENCODER.researchItem.setConcealed();

        // Register the research
        ResearchTypes.DISTILLATION_PATTERN_ENCODER.researchItem.registerResearchItem();
    }
}
