package thaumicenergistics.integration.thaumcraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.crafting.CraftingHelper;

import appeng.api.AEApi;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ScanItem;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.api.research.theorycraft.TheorycraftManager;

import thaumicenergistics.api.ThEApi;
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
        this.registerArcaneRecipes();
    }

    @Override
    public void postInit() {

    }

    private void registerArcaneRecipes() {
        ResourceLocation recipeGroup = new ResourceLocation("");
        ThEApi.instance().items().coalescenceCore().maybeStack(2).ifPresent(stack -> {
            List<ItemStack> quartz = new ArrayList<>(Arrays.asList(CraftingHelper.getIngredient("crystalCertusQuartz").getMatchingStacks()));
            quartz.add(AEApi.instance().definitions().materials().certusQuartzCrystalCharged().maybeStack(1).orElse(ItemStack.EMPTY));
            quartz.add(AEApi.instance().definitions().materials().purifiedCertusQuartzCrystal().maybeStack(1).orElse(ItemStack.EMPTY));

            ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "coalescence_core"), new ShapedArcaneRecipe(
                    recipeGroup,
                    "DIGISENTIA",
                    10,
                    new AspectList(),
                    ThEApi.instance().items().coalescenceCore().maybeStack(2).get(),
                    "SSS",
                    "QFL",
                    "SSS",
                    'S',
                    new ItemStack(ItemsTC.nuggets, 1, 5),
                    'Q',
                    Ingredient.fromStacks(quartz.toArray(new ItemStack[0])),
                    'F',
                    AEApi.instance().definitions().materials().fluixDust().maybeStack(1).orElse(ItemStack.EMPTY),
                    'L',
                    AEApi.instance().definitions().materials().logicProcessor().maybeStack(1).orElse(ItemStack.EMPTY)
            ));
        });
        ThEApi.instance().items().diffusionCore().maybeStack(2).ifPresent(stack -> {
            List<ItemStack> quartz = new ArrayList<>(Arrays.asList(CraftingHelper.getIngredient("gemQuartz").getMatchingStacks()));
            quartz.add(AEApi.instance().definitions().materials().purifiedNetherQuartzCrystal().maybeStack(1).orElse(ItemStack.EMPTY));

            ThaumcraftApi.addArcaneCraftingRecipe(new ResourceLocation(ModGlobals.MOD_ID, "diffusion_core"), new ShapedArcaneRecipe(
                    recipeGroup,
                    "DIGISENTIA",
                    10,
                    new AspectList(),
                    ThEApi.instance().items().diffusionCore().maybeStack(2).get(),
                    "SSS",
                    "QFL",
                    "SSS",
                    'S',
                    new ItemStack(ItemsTC.nuggets, 1, 5),
                    'Q',
                    Ingredient.fromStacks(quartz.toArray(new ItemStack[0])),
                    'F',
                    AEApi.instance().definitions().materials().fluixDust().maybeStack(1).orElse(ItemStack.EMPTY),
                    'L',
                    AEApi.instance().definitions().materials().logicProcessor().maybeStack(1).orElse(ItemStack.EMPTY)
            ));
        });
    }
}
