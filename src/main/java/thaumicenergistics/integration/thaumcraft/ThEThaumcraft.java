package thaumicenergistics.integration.thaumcraft;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import thaumcraft.api.research.ScanItem;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.api.research.theorycraft.TheorycraftManager;

import thaumicenergistics.api.ThEApi;
import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.integration.IThEIntegration;
import thaumicenergistics.integration.thaumcraft.research.CardTinkerAE;
import thaumicenergistics.util.ForgeUtil;
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
        Optional<ItemStack> core;

        core = AEApi.instance().definitions().materials().annihilationCore().maybeStack(1);
        core.ifPresent(itemStack -> ScanningManager.addScannableThing(new ScanItem("f_AECORE", itemStack)));

        core = AEApi.instance().definitions().materials().formationCore().maybeStack(1);
        core.ifPresent(itemStack -> ScanningManager.addScannableThing(new ScanItem("f_AECORE", itemStack)));

        TheorycraftManager.registerCard(CardTinkerAE.class);
        //if (AEApi.instance().definitions().blocks().controller().maybeEntity().isPresent())
        //TheorycraftManager.registerAid(new AidMEController());
        this.registerArcaneRecipes();
        this.registerInfusionRecipes();
    }

    @Override
    public void postInit() {
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
                        "DIGISENTIA",
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
                        "DIGISENTIA",
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
                    "ESSENTIASTORAGE1k",
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
                    "ESSENTIASTORAGE4k",
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
                    "ESSENTIASTORAGE16k",
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
                    "ESSENTIASTORAGE64k",
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
                        "ESSENTIABUSES",
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
                        "ESSENTIABUSES",
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
                        "ESSENTIABUSES",
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
                    "ESSENTIATERMINAL",
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
                        "ARCANETERMINAL",
                        50,
                        new AspectList(),
                        arcane,
                        AEApi.instance().definitions().parts().terminal().maybeStack(1).orElse(ItemStack.EMPTY),
                        BlocksTC.arcaneWorkbench,
                        AEApi.instance().definitions().materials().calcProcessor().maybeStack(1).orElse(ItemStack.EMPTY)
                )));
        ThEApi.instance().items().upgradeArcane().maybeItem().ifPresent(upgrade -> {
            ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "upgrade_arcane"), new ShapelessArcaneRecipe(
                    recipeGroup,
                    "WORKBENCHCHARGER",
                    25,
                    new AspectList(),
                    upgrade,
                    AEApi.instance().definitions().materials().advCard().maybeStack(1).orElse(ItemStack.EMPTY),
                    BlocksTC.arcaneWorkbenchCharger
            ));
        });
    }

    private void registerInfusionRecipes() {
        ThEApi.instance().blocks().infusionProvider().maybeStack(1).ifPresent(stack ->
                ThaumcraftApi.addInfusionCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "infusion_provider"), new InfusionRecipe(
                        "INFUSIONPROVIDER",
                        stack,
                        2,
                        new AspectList().add(Aspect.MECHANISM, 25).add(Aspect.MAGIC, 25).add(Aspect.EXCHANGE, 20),
                        AEApi.instance().definitions().blocks().iface().maybeBlock().orElseThrow(() -> new NullPointerException("Missing interface block for recipe")),
                        ThEApi.instance().items().coalescenceCore().maybeStack(1).orElse(ItemStack.EMPTY),
                        ItemsTC.salisMundus,
                        ThEApi.instance().items().coalescenceCore().maybeStack(1).orElse(ItemStack.EMPTY),
                        ItemsTC.salisMundus
                )));
    }

    private void addFakeCrafting(ResourceLocation resourceLocation) {
        IForgeRegistryEntry entry = ForgeUtil.getRegistryEntry(IRecipe.class, resourceLocation);
        Preconditions.checkNotNull(entry);
        ThaumcraftApi.addFakeCraftingRecipe(entry.getRegistryName(), entry);
    }
}
