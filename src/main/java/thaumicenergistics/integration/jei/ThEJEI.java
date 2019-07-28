package thaumicenergistics.integration.jei;

import thaumicenergistics.api.ThEApi;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

/**
 * @author BrockWS
 */
@JEIPlugin
public class ThEJEI implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        ThEApi.instance().items().arcaneTerminal().maybeStack(1).ifPresent(stack -> {
            IRecipeTransferHandler handler = new ACTRecipeTransferHandler(registry.getJeiHelpers().recipeTransferHandlerHelper());
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(handler, VanillaRecipeCategoryUid.CRAFTING);
            registry.getRecipeTransferRegistry().addRecipeTransferHandler(handler, "THAUMCRAFT_ARCANE_WORKBENCH");

            registry.addRecipeCatalyst(stack, "THAUMCRAFT_ARCANE_WORKBENCH");
        });

        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();

        ThEApi.instance().items().dummyAspect().maybeStack(1).ifPresent(blacklist::addIngredientToBlacklist);
    }
}
