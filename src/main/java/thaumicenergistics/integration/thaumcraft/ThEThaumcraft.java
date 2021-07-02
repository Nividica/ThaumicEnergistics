package thaumicenergistics.integration.thaumcraft;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.IForgeRegistryEntry;

import appeng.api.AEApi;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.api.research.theorycraft.TheorycraftManager;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.IThEIntegration;
import thaumicenergistics.integration.thaumcraft.research.AidMEController;
import thaumicenergistics.integration.thaumcraft.research.AidMEDrive;
import thaumicenergistics.integration.thaumcraft.research.CardTinkerAE;
import thaumicenergistics.integration.thaumcraft.research.ScanMod;
import thaumicenergistics.util.ForgeUtil;
import thaumicenergistics.util.TCUtil;
import thaumicenergistics.util.ThELog;

/**
 * @author BrockWS
 * @author Alex811
 */
public class ThEThaumcraft implements IThEIntegration {

    @Override
    public void init() {
        ThELog.info("Registering Research Category");
        ResearchCategories.registerCategory(
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

        ScanningManager.addScannableThing(new ScanMod("f_AECORE", ModGlobals.MOD_ID_AE2));

        TheorycraftManager.registerCard(CardTinkerAE.class);
        if (AEApi.instance().definitions().blocks().controller().maybeBlock().isPresent())
            TheorycraftManager.registerAid(new AidMEController());
        else if (AEApi.instance().definitions().blocks().drive().maybeBlock().isPresent())
            TheorycraftManager.registerAid(new AidMEDrive());
        this.registerArcaneRecipes();
        this.registerInfusionRecipes();
    }

    @Override
    public String getModID() {
        return "thaumcraft";
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    private void registerArcaneRecipes() {
        ResourceLocation recipeGroup = new ResourceLocation("");

        List<ItemStack> certusQuartz = new ArrayList<>(Arrays.asList(CraftingHelper.getIngredient("crystalCertusQuartz").getMatchingStacks()));
        certusQuartz.add(AEApi.instance().definitions().materials().certusQuartzCrystalCharged().maybeStack(1).orElse(ItemStack.EMPTY));
        certusQuartz.add(AEApi.instance().definitions().materials().purifiedCertusQuartzCrystal().maybeStack(1).orElse(ItemStack.EMPTY));

        List<ItemStack> netherQuartz = new ArrayList<>(Arrays.asList(CraftingHelper.getIngredient("gemQuartz").getMatchingStacks()));
        netherQuartz.add(AEApi.instance().definitions().materials().purifiedNetherQuartzCrystal().maybeStack(1).orElse(ItemStack.EMPTY));

        ThEApi.instance().items().coalescenceCore().maybeStack(2).ifPresent(stack ->
                ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "coalescence_core"), new ShapedArcaneRecipe(
                        recipeGroup,
                        "DIGISENTIA@2",
                        10,
                        new AspectList(),
                        stack,
                        "SSS",
                        "QFL",
                        "SSS",
                        'S',
                        new ItemStack(ItemsTC.nuggets, 1, 5),
                        'Q',
                        Ingredient.fromStacks(certusQuartz.toArray(new ItemStack[0])),
                        'F',
                        AEApi.instance().definitions().materials().fluixDust().maybeStack(1).orElse(ItemStack.EMPTY),
                        'L',
                        AEApi.instance().definitions().materials().logicProcessor().maybeStack(1).orElse(ItemStack.EMPTY)
                )));
        ThEApi.instance().items().diffusionCore().maybeStack(2).ifPresent(stack ->
                ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "diffusion_core"), new ShapedArcaneRecipe(
                        recipeGroup,
                        "DIGISENTIA@2",
                        10,
                        new AspectList(),
                        stack,
                        "SSS",
                        "QFL",
                        "SSS",
                        'S',
                        new ItemStack(ItemsTC.nuggets, 1, 5),
                        'Q',
                        Ingredient.fromStacks(netherQuartz.toArray(new ItemStack[0])),
                        'F',
                        AEApi.instance().definitions().materials().fluixDust().maybeStack(1).orElse(ItemStack.EMPTY),
                        'L',
                        AEApi.instance().definitions().materials().logicProcessor().maybeStack(1).orElse(ItemStack.EMPTY)
                )));

        ThEApi.instance().items().essentiaComponent1k().maybeStack(1).ifPresent(stack -> {
            ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "essentia_component_1k"), new ShapedArcaneRecipe(
                    recipeGroup,
                    "ESSENTIASTORAGE1k@2",
                    10,
                    new AspectList(),
                    stack,
                    "SQS",
                    "QPQ",
                    "SQS",
                    'S',
                    ItemsTC.salisMundus,
                    'Q',
                    Ingredient.fromStacks(certusQuartz.toArray(new ItemStack[0])),
                    'P',
                    AEApi.instance().definitions().materials().logicProcessor().maybeStack(1).orElse(ItemStack.EMPTY)
            ));
            this.addFakeCrafting(new ResourceLocation(ModGlobals.MOD_ID, "cells/essentia_cell_1k"));
        });
        ThEApi.instance().items().essentiaComponent4k().maybeStack(1).ifPresent(stack -> {
            ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "essentia_component_4k"), new ShapedArcaneRecipe(
                    recipeGroup,
                    "ESSENTIASTORAGE4k@2",
                    10,
                    new AspectList(),
                    stack,
                    "SPS",
                    "CGC",
                    "SCS",
                    'S',
                    ItemsTC.salisMundus,
                    'C',
                    ThEApi.instance().items().essentiaComponent1k().maybeStack(1).orElse(ItemStack.EMPTY),
                    'P',
                    AEApi.instance().definitions().materials().calcProcessor().maybeStack(1).orElse(ItemStack.EMPTY),
                    'G',
                    AEApi.instance().definitions().blocks().quartzGlass().maybeBlock().orElse(Blocks.GLASS)
            ));
            this.addFakeCrafting(new ResourceLocation(ModGlobals.MOD_ID, "cells/essentia_cell_4k"));
        });
        ThEApi.instance().items().essentiaComponent16k().maybeStack(1).ifPresent(stack -> {
            ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "essentia_component_16k"), new ShapedArcaneRecipe(
                    recipeGroup,
                    "ESSENTIASTORAGE16k@2",
                    10,
                    new AspectList(),
                    stack,
                    "SPS",
                    "CGC",
                    "SCS",
                    'S',
                    ItemsTC.salisMundus,
                    'C',
                    ThEApi.instance().items().essentiaComponent4k().maybeStack(1).orElse(ItemStack.EMPTY),
                    'P',
                    AEApi.instance().definitions().materials().engProcessor().maybeStack(1).orElse(ItemStack.EMPTY),
                    'G',
                    AEApi.instance().definitions().blocks().quartzGlass().maybeBlock().orElse(Blocks.GLASS)
            ));
        });
        ThEApi.instance().items().essentiaComponent64k().maybeStack(1).ifPresent(stack -> {
            ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "essentia_component_64k"), new ShapedArcaneRecipe(
                    recipeGroup,
                    "ESSENTIASTORAGE64k@2",
                    10,
                    new AspectList(),
                    stack,
                    "SPS",
                    "CGC",
                    "SCS",
                    'S',
                    ItemsTC.salisMundus,
                    'C',
                    ThEApi.instance().items().essentiaComponent16k().maybeStack(1).orElse(ItemStack.EMPTY),
                    'P',
                    AEApi.instance().definitions().materials().engProcessor().maybeStack(1).orElse(ItemStack.EMPTY),
                    'G',
                    AEApi.instance().definitions().blocks().quartzGlass().maybeBlock().orElse(Blocks.GLASS)
            ));
            this.addFakeCrafting(new ResourceLocation(ModGlobals.MOD_ID, "cells/essentia_cell_64k"));
        });

        ThEApi.instance().items().essentiaExportBus().maybeStack(1).ifPresent(stack ->
                ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "essentia_export_bus"), new ShapedArcaneRecipe(
                        recipeGroup,
                        "ESSENTIABUSES@2",
                        20,
                        new AspectList(),
                        stack,
                        "ICI",
                        "STS",
                        'S',
                        ItemsTC.salisMundus,
                        'C',
                        ThEApi.instance().items().coalescenceCore().maybeStack(1).orElse(ItemStack.EMPTY),
                        'I',
                        "ingotIron",
                        'T',
                        BlocksTC.tube
                )));
        ThEApi.instance().items().essentiaImportBus().maybeStack(1).ifPresent(stack ->
                ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "essentia_import_bus"), new ShapedArcaneRecipe(
                        recipeGroup,
                        "ESSENTIABUSES@2",
                        20,
                        new AspectList(),
                        stack,
                        "SCS",
                        "ITI",
                        'S',
                        ItemsTC.salisMundus,
                        'C',
                        ThEApi.instance().items().diffusionCore().maybeStack(1).orElse(ItemStack.EMPTY),
                        'I',
                        "ingotIron",
                        'T',
                        BlocksTC.tube
                )));
        ThEApi.instance().items().essentiaStorageBus().maybeItem().ifPresent(item ->
                ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "essentia_storage_bus"), new ShapelessArcaneRecipe(
                        recipeGroup,
                        "ESSENTIABUSES@2",
                        20,
                        new AspectList(),
                        item,
                        Ingredient.fromStacks(
                                AEApi.instance().definitions().blocks().iface().maybeStack(1).orElse(ItemStack.EMPTY),
                                AEApi.instance().definitions().parts().iface().maybeStack(1).orElse(ItemStack.EMPTY)
                        ),
                        Blocks.PISTON,
                        Blocks.STICKY_PISTON,
                        ItemsTC.salisMundus
                )));
        ThEApi.instance().items().essentiaTerminal().maybeItem().ifPresent(essentia -> {
            ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "essentia_terminal"), new ShapelessArcaneRecipe(
                    recipeGroup,
                    "ESSENTIATERMINAL@2",
                    25,
                    new AspectList(),
                    essentia,
                    ItemsTC.salisMundus,
                    ThEApi.instance().items().diffusionCore().maybeStack(1).orElse(ItemStack.EMPTY),
                    ThEApi.instance().items().coalescenceCore().maybeStack(1).orElse(ItemStack.EMPTY),
                    AEApi.instance().definitions().materials().logicProcessor().maybeStack(1).orElse(ItemStack.EMPTY),
                    "itemIlluminatedPanel"
            ));
        });
        ThEApi.instance().items().arcaneTerminal().maybeItem().ifPresent(arcane ->
                ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "arcane_terminal"), new ShapelessArcaneRecipe(
                        recipeGroup,
                        "ARCANETERMINAL@2",
                        50,
                        new AspectList(),
                        arcane,
                        AEApi.instance().definitions().parts().terminal().maybeStack(1).orElse(ItemStack.EMPTY),
                        BlocksTC.arcaneWorkbench,
                        AEApi.instance().definitions().materials().calcProcessor().maybeStack(1).orElse(ItemStack.EMPTY)
                )));
        ThEApi.instance().items().arcaneInscriber().maybeItem().ifPresent(inscriber ->
                ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "arcane_inscriber"), new ShapelessArcaneRecipe(
                        recipeGroup,
                        "ARCANEINSCRIBER@2",
                        50,
                        new AspectList().add(Aspect.AIR, 1).add(Aspect.EARTH, 1).add(Aspect.FIRE, 1).add(Aspect.WATER, 1).add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1),
                        inscriber,
                        AEApi.instance().definitions().parts().patternTerminal().maybeStack(1).orElse(ItemStack.EMPTY),
                        BlocksTC.arcaneWorkbench,
                        AEApi.instance().definitions().materials().engProcessor().maybeStack(1).orElse(ItemStack.EMPTY)
                )));
        ThEApi.instance().items().upgradeArcane().maybeItem().ifPresent(upgrade -> {
            ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "upgrade_arcane"), new ShapelessArcaneRecipe(
                    recipeGroup,
                    "ARCANETERMINAL@2&&WORKBENCHCHARGER",
                    25,
                    new AspectList(),
                    upgrade,
                    AEApi.instance().definitions().materials().advCard().maybeStack(1).orElse(ItemStack.EMPTY),
                    BlocksTC.arcaneWorkbenchCharger
            ));
        });
        ThEApi.instance().items().blankKnowledgeCore().maybeItem().ifPresent(core ->
                ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "knowledge_core"), new ShapedArcaneRecipe(
                        recipeGroup,
                        "KNOWLEDGECORE@2",
                        100,
                        new AspectList().add(Aspect.EARTH, 1).add(Aspect.ORDER, 1).add(Aspect.WATER, 1),
                        core,
                        "GLG",
                        "LBL",
                        "GPG",
                        'G',
                        AEApi.instance().definitions().blocks().quartzVibrantGlass().maybeBlock().orElse(Blocks.GLASS),
                        'L',
                        "dyeBlue",
                        'B',
                        ItemsTC.brain,
                        'P',
                        AEApi.instance().definitions().materials().calcProcessor().maybeStack(1).orElse(ItemStack.EMPTY)
                )));
    }

    private void registerInfusionRecipes() {
        ThEApi.instance().blocks().infusionProvider().maybeStack(1).ifPresent(stack ->
                ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "infusion_provider"), new InfusionRecipe(
                        "INFUSIONPROVIDER@2",
                        stack,
                        2,
                        new AspectList().add(Aspect.MECHANISM, 25).add(Aspect.MAGIC, 25).add(Aspect.EXCHANGE, 20),
                        AEApi.instance().definitions().blocks().iface().maybeBlock().orElseThrow(() -> new NullPointerException("Missing interface block for recipe")),
                        ThEApi.instance().items().coalescenceCore().maybeStack(1).orElse(ItemStack.EMPTY),
                        ItemsTC.salisMundus,
                        ThEApi.instance().items().coalescenceCore().maybeStack(1).orElse(ItemStack.EMPTY),
                        ItemsTC.salisMundus
                )));
        ThEApi.instance().blocks().arcaneAssembler().maybeStack(1).ifPresent(stack ->
                ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "arcane_assembler"), new InfusionRecipe(
                        "ARCANEASSEMBLER@2",
                        stack,
                        6,
                        new AspectList().add(Aspect.CRAFT, 64).add(Aspect.EXCHANGE, 32).add(Aspect.AURA, 16).add(Aspect.MAGIC, 16).add(Aspect.METAL, 8).add(Aspect.CRYSTAL, 8),
                        AEApi.instance().definitions().blocks().molecularAssembler().maybeBlock().orElseThrow(() -> new NullPointerException("Missing molecular assembler block for recipe")),
                        ThEApi.instance().items().coalescenceCore().maybeStack(1).orElse(ItemStack.EMPTY),
                        TCUtil.getCrystalWithAspect(Aspect.AIR),
                        TCUtil.getCrystalWithAspect(Aspect.WATER),
                        ItemsTC.salisMundus,
                        TCUtil.getCrystalWithAspect(Aspect.ENTROPY),
                        ThEApi.instance().items().diffusionCore().maybeStack(1).orElse(ItemStack.EMPTY),
                        TCUtil.getCrystalWithAspect(Aspect.EARTH),
                        TCUtil.getCrystalWithAspect(Aspect.FIRE),
                        ItemsTC.salisMundus,
                        TCUtil.getCrystalWithAspect(Aspect.ORDER)
                )));
    }

    private void addFakeCrafting(ResourceLocation resourceLocation) {
        IForgeRegistryEntry entry = ForgeUtil.getRegistryEntry(IRecipe.class, resourceLocation);
        Preconditions.checkNotNull(entry);
        ThaumcraftApi.addFakeCraftingRecipe(entry.getRegistryName(), entry);
    }
}
