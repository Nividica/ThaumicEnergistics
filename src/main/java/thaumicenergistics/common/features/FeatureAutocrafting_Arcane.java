package thaumicenergistics.common.features;

import net.minecraft.item.ItemStack;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumicenergistics.api.IThEConfig;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.common.integration.tc.VisCraftingHelper;
import thaumicenergistics.common.registries.FeatureRegistry;
import thaumicenergistics.common.registries.RecipeRegistry;
import thaumicenergistics.common.registries.ResearchRegistry;
import thaumicenergistics.common.registries.ResearchRegistry.PseudoResearchTypes;
import thaumicenergistics.common.registries.ResearchRegistry.ResearchTypes;
import thaumicenergistics.common.tiles.TileArcaneAssembler;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

/**
 * {@link TileArcaneAssembler} feature.
 *
 * @author Nividica
 *
 */
public class FeatureAutocrafting_Arcane extends ThEThaumcraftResearchFeature {

    public FeatureAutocrafting_Arcane() {
        super(ResearchTypes.ARCANE_ASSEMBLER.getKey());
    }

    @Override
    protected boolean checkConfigs(final IThEConfig theConfig) {
        // Depends on crafting CPU's
        if (!AEConfig.instance.isFeatureEnabled(AEFeature.CraftingCPU)) {
            return false;
        }

        // Depends on assembler
        if (!AEConfig.instance.isFeatureEnabled(AEFeature.MolecularAssembler)) {
            return false;
        }

        return theConfig.craftArcaneAssembler();
    }

    @Override
    protected Object[] getItemReqs(final CommonDependantItems cdi) {
        return new Object[] { cdi.VibrantGlass, cdi.CalculationProcessor, cdi.LogicProcessor, cdi.MolecularAssembler };
    }

    @Override
    protected ThEThaumcraftResearchFeature getParentFeature() {
        return FeatureRegistry.instance().featureVRI;
    }

    @Override
    protected void registerCrafting(final CommonDependantItems cdi) {
        // Thaumcraft items
        ItemStack CraftingScepter = VisCraftingHelper.INSTANCE.getCraftingScepter();

        // My Items
        ItemStack KnowledgeCore = ThEApi.instance().items().KnowledgeCore.getStack();
        ItemStack KnowledgeInscriber = ThEApi.instance().blocks().KnowledgeInscriber.getStack();
        ItemStack ArcaneAssembler = ThEApi.instance().blocks().ArcaneAssembler.getStack();
        ArcaneAssembler.setTagCompound(TileArcaneAssembler.getCraftTag());

        // Knowledge Core
        AspectList kCoreAspects = new AspectList();

        // Set KC crafting aspects
        kCoreAspects.add(Aspect.WATER, 3);
        kCoreAspects.add(Aspect.ORDER, 3);
        kCoreAspects.add(Aspect.EARTH, 1);

        // Set KC recipe
        Object[] kCoreRecipe = new Object[] { "VLV", "LZL", "VCV", 'V', cdi.VibrantGlass, 'L', cdi.VanillaLapis, 'Z',
                cdi.ZombieBrain, 'C', cdi.CalculationProcessor };

        // Register KC
        RecipeRegistry.ITEM_KNOWLEDGE_CORE = ThaumcraftApi
                .addArcaneCraftingRecipe(this.researchKey, KnowledgeCore, kCoreAspects, kCoreRecipe);

        // Knowledge Inscriber
        AspectList kiAspects = new AspectList();

        // Set KI crafting aspects
        kiAspects.add(Aspect.WATER, 5);
        kiAspects.add(Aspect.ORDER, 5);
        kiAspects.add(Aspect.EARTH, 5);
        kiAspects.add(Aspect.ENTROPY, 5);
        kiAspects.add(Aspect.FIRE, 5);
        kiAspects.add(Aspect.AIR, 5);

        // Set KI recipe
        Object[] kiRecipe = new Object[] { "IPI", " T ", "ILI", 'I', cdi.IronIngot, 'P', cdi.IlluminatedPanel, 'T',
                cdi.Thaumonomicon, 'L', cdi.LogicProcessor };

        // Register KI
        RecipeRegistry.BLOCK_KNOWLEDGE_INSCRIBER = ThaumcraftApi
                .addArcaneCraftingRecipe(this.researchKey, KnowledgeInscriber, kiAspects, kiRecipe);

        // Arcane Assembler
        AspectList assemblerAspects = new AspectList();

        // Set Assembler crafting aspects
        assemblerAspects.add(Aspect.CRAFT, 64);
        assemblerAspects.add(Aspect.EXCHANGE, 32);
        assemblerAspects.add(Aspect.AURA, 16);
        assemblerAspects.add(Aspect.MAGIC, 16);
        assemblerAspects.add(Aspect.METAL, 8);
        assemblerAspects.add(Aspect.CRYSTAL, 8);

        // Set Assembler recipe
        ItemStack[] assemblerRecipe = { CraftingScepter, cdi.AirShard, cdi.EarthShard, cdi.WaterShard,
                cdi.BallanceShard, cdi.OrderShard, cdi.EntropyShard, cdi.FireShard };

        // Register Assembler
        RecipeRegistry.BLOCK_ARCANE_ASSEMBLER = ThaumcraftApi.addInfusionCraftingRecipe(
                this.researchKey,
                ArcaneAssembler,
                7,
                assemblerAspects,
                cdi.MolecularAssembler,
                assemblerRecipe);
    }

    @Override
    protected void registerPseudoParents() {
        PseudoResearchTypes.SCEPTRE.registerPsudeoResearch();
    }

    @Override
    protected void registerResearch() {
        // Set Assembler research aspects
        AspectList assemblerAspectList = new AspectList();
        assemblerAspectList.add(Aspect.CRAFT, 5);
        assemblerAspectList.add(Aspect.MECHANISM, 3);
        assemblerAspectList.add(Aspect.MIND, 3);
        assemblerAspectList.add(Aspect.EXCHANGE, 3);
        assemblerAspectList.add(Aspect.AURA, 3);
        assemblerAspectList.add(Aspect.GREED, 3);

        // Set the icon
        ItemStack assemblerIcon = ThEApi.instance().blocks().ArcaneAssembler.getStack();

        // Set the pages
        ResearchPage[] assemblerPages = new ResearchPage[] {
                new ResearchPage(ResearchTypes.ARCANE_ASSEMBLER.getPageName(1)),
                new ResearchPage(RecipeRegistry.BLOCK_ARCANE_ASSEMBLER),
                new ResearchPage(ResearchTypes.ARCANE_ASSEMBLER.getPageName(2)),
                new ResearchPage(ResearchTypes.ARCANE_ASSEMBLER.getPageName(3)) };

        // Create the assembler research
        ResearchTypes.ARCANE_ASSEMBLER.createResearchItem(
                assemblerAspectList,
                ResearchRegistry.COMPLEXITY_LARGE,
                assemblerIcon,
                assemblerPages);

        // Set the parents
        ResearchTypes.ARCANE_ASSEMBLER.researchItem
                .setParents(this.getFirstValidParentKey(false), PseudoResearchTypes.SCEPTRE.getKey());
        ResearchTypes.ARCANE_ASSEMBLER.researchItem
                .setParentsHidden(FeatureRegistry.instance().featureACT.getFirstValidParentKey(true));
        ResearchTypes.ARCANE_ASSEMBLER.researchItem.setConcealed();

        // Register the research
        ResearchTypes.ARCANE_ASSEMBLER.researchItem.registerResearchItem();

        // Set Knowledge Inscriber research aspects
        AspectList kiAspectList = new AspectList();
        kiAspectList.add(Aspect.MIND, 5);
        kiAspectList.add(Aspect.MECHANISM, 3);
        kiAspectList.add(Aspect.CRAFT, 3);
        kiAspectList.add(Aspect.EXCHANGE, 1);

        // Set the icon
        ItemStack kiIcon = ThEApi.instance().blocks().KnowledgeInscriber.getStack();

        // Set the pages
        ResearchPage[] kiPages = new ResearchPage[] {
                new ResearchPage(ResearchTypes.KNOWLEDGE_INSCRIBER.getPageName(1)),
                new ResearchPage(RecipeRegistry.ITEM_KNOWLEDGE_CORE),
                new ResearchPage(RecipeRegistry.BLOCK_KNOWLEDGE_INSCRIBER),
                new ResearchPage(ResearchTypes.KNOWLEDGE_INSCRIBER.getPageName(2)) };

        // Create the KI research
        ResearchTypes.KNOWLEDGE_INSCRIBER
                .createResearchItem(kiAspectList, ResearchRegistry.COMPLEXITY_SMALL, kiIcon, kiPages);
        ResearchTypes.KNOWLEDGE_INSCRIBER.researchItem.setSecondary();

        // Set the parent
        ResearchTypes.KNOWLEDGE_INSCRIBER.researchItem.setParents(ResearchTypes.ARCANE_ASSEMBLER.getKey());
        ResearchTypes.KNOWLEDGE_INSCRIBER.researchItem.setConcealed();

        // Register the research
        ResearchTypes.KNOWLEDGE_INSCRIBER.researchItem.registerResearchItem();
    }
}
