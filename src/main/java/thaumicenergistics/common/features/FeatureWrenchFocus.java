package thaumicenergistics.common.features;

import net.minecraft.item.ItemStack;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.items.ItemFocusAEWrench;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

/**
 * {@link ItemFocusAEWrench} featire.
 *
 * @author Nividica
 *
 */
public class FeatureWrenchFocus extends ThEThaumcraftResearchFeature {

    public FeatureWrenchFocus() {
        super(ResearchTypes.FOCUS_WRENCH.getKey());
    }

    @Override
    protected boolean checkConfigs(final IThEConfig theConfig) {
        // Depends on quartz tools
        if (!AEConfig.instance.isFeatureEnabled(AEFeature.CertusQuartzTools)) {
            return false;
        }

        // Depends on quartz wrench
        if (!AEConfig.instance.isFeatureEnabled(AEFeature.QuartzWrench)) {
            return false;
        }

        return theConfig.enableWrenchFocus();
    }

    @Override
    protected Object[] getItemReqs(final CommonDependantItems cdi) {
        return new Object[] { cdi.CertusWrench };
    }

    @Override
    protected ThEThaumcraftResearchFeature getParentFeature() {
        return null;
    }

    @Override
    protected void registerCrafting(final CommonDependantItems cdi) {
        // My items
        ItemStack WrenchFocus = ThEApi.instance().items().WandFocusAEWrench.getStack();

        // Wrench focus
        AspectList wrenchAspects = new AspectList();
        wrenchAspects.add(Aspect.AIR, 10);
        wrenchAspects.add(Aspect.FIRE, 10);
        RecipeRegistry.ITEM_WRENCH_FOCUS = ThaumcraftApi.addArcaneCraftingRecipe(
                this.researchKey,
                WrenchFocus,
                wrenchAspects,
                new Object[] { "ANF", "NWN", "FNA", 'A', cdi.AirShard, 'F', cdi.FireShard, 'N', cdi.NetherQuartz, 'W',
                        cdi.CertusWrench });
    }

    @Override
    protected void registerPseudoParents() {
        PseudoResearchTypes.FOCUSFIRE.registerPsudeoResearch();
    }

    @Override
    protected void registerResearch() {
        // Set the research aspects
        AspectList focusAspects = new AspectList();
        focusAspects.add(Aspect.MECHANISM, 6);
        focusAspects.add(Aspect.TOOL, 5);
        focusAspects.add(Aspect.MAGIC, 3);

        // Set the icon
        ItemStack focusIcon = ThEApi.instance().items().WandFocusAEWrench.getStack();

        // Set the pages
        ResearchPage[] focusPages = new ResearchPage[] { new ResearchPage(ResearchTypes.FOCUS_WRENCH.getPageName(1)),
                new ResearchPage(RecipeRegistry.ITEM_WRENCH_FOCUS) };

        // Create the research
        ResearchTypes.FOCUS_WRENCH
                .createResearchItem(focusAspects, ResearchRegistry.COMPLEXITY_SMALL, focusIcon, focusPages);
        ResearchTypes.FOCUS_WRENCH.researchItem.setParents(PseudoResearchTypes.FOCUSFIRE.getKey()).setSecondary();
        ResearchTypes.FOCUS_WRENCH.researchItem.registerResearchItem();
    }
}
