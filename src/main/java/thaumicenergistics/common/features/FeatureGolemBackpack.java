package thaumicenergistics.common.features;

import net.minecraft.item.ItemStack;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.items.ItemGolemWirelessBackpack;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import thaumicenergistics.fml.ThECore;

/**
 * {@link ItemGolemWirelessBackpack} feature.
 *
 * @author Nividica
 *
 */
public class FeatureGolemBackpack extends ThEThaumcraftResearchFeature {

    public FeatureGolemBackpack() {
        super(ResearchRegistry.ResearchTypes.GOLEM_BACKPACK.getKey());
    }

    @Override
    protected boolean checkConfigs(final IThEConfig theConfig) {
        // Did the core fail to transform the required classes?
        if (ThECore.golemHooksTransformFailed) {
            return false;
        }

        return theConfig.craftGolemWifiBackpack();
    }

    @Override
    protected Object[] getItemReqs(final CommonDependantItems cdi) {
        return new Object[] {cdi.MEInterfacePart, cdi.WirelessReceiver, cdi.FluixCrystal, cdi.MECharger};
    }

    @Override
    protected ThEThaumcraftResearchFeature getParentFeature() {
        return FeatureRegistry.instance().featureConversionCores;
    }

    @Override
    protected void registerCrafting(final CommonDependantItems cdi) {
        // Backpack item
        ItemStack backpack = ThEApi.instance().items().GolemWifiBackpack.getStack();

        // Aspects
        AspectList aspects = new AspectList();
        aspects.add(Aspect.AIR, 6);
        aspects.add(Aspect.FIRE, 4);
        aspects.add(Aspect.ORDER, 3);

        // Recipe
        Object[] recipe = new Object[] {
            "TIT",
            "NWN",
            "FCF",
            'T',
            cdi.ThaumiumIngot,
            'I',
            cdi.MEInterfacePart,
            'N',
            cdi.Nitor,
            'W',
            cdi.WirelessReceiver,
            'F',
            cdi.FluixCrystal,
            'C',
            cdi.MECharger
        };

        // Register
        RecipeRegistry.ITEM_GOLEM_WIFI_BACKPACK =
                ThaumcraftApi.addArcaneCraftingRecipe(this.researchKey, backpack, aspects, recipe);
    }

    @Override
    protected void registerPseudoParents() {
        PseudoResearchTypes.COREGATHER.registerPsudeoResearch();
    }

    @Override
    protected void registerResearch() {
        // Set the research aspects
        AspectList aspects = new AspectList();
        aspects.add(Aspect.ENERGY, 5);
        aspects.add(Aspect.AURA, 5);
        aspects.add(Aspect.MIND, 3);
        aspects.add(Aspect.ARMOR, 3);
        aspects.add(Aspect.MOTION, 1);
        aspects.add(Aspect.EXCHANGE, 1);

        // Set the icon
        ItemStack icon = ThEApi.instance().items().GolemWifiBackpack.getStack();

        // Set the pages
        ResearchPage[] pages = new ResearchPage[] {
            new ResearchPage(ResearchTypes.GOLEM_BACKPACK.getPageName(1)),
            new ResearchPage(RecipeRegistry.ITEM_GOLEM_WIFI_BACKPACK),
            new ResearchPage(ResearchTypes.GOLEM_BACKPACK.getPageName(2)),
            new ResearchPage(ResearchTypes.GOLEM_BACKPACK.getPageName(3)),
            new ResearchPage(ResearchTypes.GOLEM_BACKPACK.getPageName("COREGATHER")),
            new ResearchPage("COREFILL", ResearchTypes.GOLEM_BACKPACK.getPageName("COREFILL")),
            new ResearchPage("CORELIQUID", ResearchTypes.GOLEM_BACKPACK.getPageName("CORELIQUID")),
            new ResearchPage("COREALCHEMY", ResearchTypes.GOLEM_BACKPACK.getPageName("COREALCHEMY")),
            new ResearchPage("UPGRADEORDER", ResearchTypes.GOLEM_BACKPACK.getPageName("UPGRADEORDER")),
            new ResearchPage("ADVANCEDGOLEM", ResearchTypes.GOLEM_BACKPACK.getPageName("ADVANCEDGOLEM"))
        };

        // Create the research
        ResearchTypes.GOLEM_BACKPACK.createResearchItem(aspects, ResearchRegistry.COMPLEXITY_LARGE, icon, pages);

        // Set parents
        ResearchTypes.GOLEM_BACKPACK.researchItem.setParents(
                this.getFirstValidParentKey(false), "COREGATHER", PseudoResearchTypes.COREGATHER.getKey());

        // Hide until the parent has been researched
        ResearchTypes.GOLEM_BACKPACK.researchItem.setConcealed();

        // Register
        ResearchTypes.GOLEM_BACKPACK.researchItem.registerResearchItem();
    }
}
