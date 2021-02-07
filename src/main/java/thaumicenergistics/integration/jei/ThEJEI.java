package thaumicenergistics.integration.jei;

import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import thaumicenergistics.api.IThEItems;
import thaumicenergistics.api.ThEApi;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

/**
 * @author BrockWS
 * @author Alex811
 */
@JEIPlugin
public class ThEJEI implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        IRecipeTransferHandlerHelper rthh = registry.getJeiHelpers().recipeTransferHandlerHelper();
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        IThEItems items = ThEApi.instance().items();

        items.arcaneTerminal().maybeStack(1).ifPresent(stack -> registerWorkbenchCatalyst(registry, new ACTRecipeTransferHandler<>(rthh), stack));
        items.arcaneInscriber().maybeStack(1).ifPresent(stack -> registerWorkbenchCatalyst(registry, new ACIRecipeTransferHandler<>(rthh), stack));

        items.dummyAspect().maybeStack(1).ifPresent(blacklist::addIngredientToBlacklist);
    }

    public void registerWorkbenchCatalyst(IModRegistry registry, IRecipeTransferHandler<? extends Container> handler, ItemStack stack){
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(handler, VanillaRecipeCategoryUid.CRAFTING);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(handler, "THAUMCRAFT_ARCANE_WORKBENCH");
        registry.addRecipeCatalyst(stack, "THAUMCRAFT_ARCANE_WORKBENCH");
    }
}
