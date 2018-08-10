package thaumicenergistics.integration.thaumcraft;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;

import thaumicenergistics.init.ModGlobals;
import thaumicenergistics.init.ModItems;
import thaumicenergistics.integration.IThEIntegration;

/**
 * @author BrockWS
 */
public class ThEThaumcraft implements IThEIntegration {

    @Override
    public void preInit() {
    }

    @Override
    public void init() {

    }

    @Override
    public void postInit() {
        ResearchCategory category = ResearchCategories.registerCategory(
                ModGlobals.RESEARCH_CATEGORY,
                null,
                new AspectList().add(Aspect.FIRE, 1),
                new ResourceLocation(ModGlobals.MOD_ID, "textures/research/icon.png"),
                new ResourceLocation(ModGlobals.MOD_ID, "textures/research/background.png"));
        ThaumcraftApi.registerResearchLocation(new ResourceLocation(ModGlobals.MOD_ID, "research/" + ModGlobals.RESEARCH_CATEGORY));
        /*ResearchEntry IO_BUS = new ResearchEntry();
        IO_BUS.setKey("IO_BUS");
        IO_BUS.setCategory(category.key);
        IO_BUS.setMeta(ResearchEntry.EnumResearchMeta.values());
        IO_BUS.setRewardItem(new ItemStack[]{new ItemStack(ModItems.itemEssentiaExportBus), new ItemStack(ModItems.itemEssentiaImportBus)});
        IO_BUS.setMeta(new ResearchEntry.EnumResearchMeta[]{ResearchEntry.EnumResearchMeta.ROUND});
        IO_BUS.setIcons(new Object[]{new ItemStack(ModItems.itemEssentiaImportBus)});
        IO_BUS.setName("Essenta IO Bus");
        category.research.put("IO_BUS", IO_BUS);*/
    }
}
