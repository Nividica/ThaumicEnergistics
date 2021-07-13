package thaumicenergistics.util;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.helpers.PatternHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.util.inventory.ThEInternalInventory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Alex811
 */
public abstract class KnowledgeCoreUtil {
    private static final int SLOT_NUM = 9;

    public static void setRecipe(ItemStack knowledgeCoreStack, int slot, Recipe recipe){
        String slotKey = String.valueOf(slot);
        NBTTagCompound nbt = knowledgeCoreStack.getTagCompound();
        if(nbt == null) nbt = new NBTTagCompound();
        if(recipe == null){
            nbt.removeTag(slotKey);
            return;
        }
        NBTTagCompound nbtRecipe = new NBTTagCompound();
        nbtRecipe.setTag("ingredients", recipe.getIngredients().serializeNBT());
        nbtRecipe.setTag("result", recipe.getResult().serializeNBT());
        nbtRecipe.setFloat("visCost", recipe.getVisCost());
        nbt.setTag(slotKey, nbtRecipe);
        knowledgeCoreStack.setTagCompound(nbt);
    }

    /**
     * @param knowledgeCoreStack the Knowledge Core ItemStack
     * @param slot recipe index
     * @return Recipe or null if no recipe exists in the specified slot
     */
    public static Recipe getRecipe(ItemStack knowledgeCoreStack, int slot){
        NBTTagCompound nbtRecipe = getNBTRecipe(knowledgeCoreStack, slot);
        if(nbtRecipe == null) return null;
        ThEInternalInventory ingredients = new ThEInternalInventory("ingredients", 15, 64);
        ItemStack result = new ItemStack(nbtRecipe.getCompoundTag("result"));
        ingredients.deserializeNBT(nbtRecipe.getTagList("ingredients", 10));
        return new Recipe(ingredients, result, nbtRecipe.getFloat("visCost"));
    }

    public static HashMap<ItemStack, ThEInternalInventory> getRecipeMap(ItemStack knowledgeCoreStack){
        HashMap<ItemStack, ThEInternalInventory> recipeMap = new HashMap<>();
        for(int i = 0; i < SLOT_NUM; i++) {
            Recipe recipe = getRecipe(knowledgeCoreStack, i);
            if(recipe != null) recipeMap.put(recipe.result, recipe.ingredients);
        }
        return recipeMap;
    }

    @Nullable
    public static Recipe getRecipe(ItemStack knowledgeCoreStack, ItemStack result){
        for(int i = 0; i < SLOT_NUM; i++) {
            Recipe recipe = getRecipe(knowledgeCoreStack, i);
            if(recipe != null && recipe.getResult().getItem().equals(result.getItem()))
                return recipe;
        }
        return null;
    }

    /**
     * @param knowledgeCore the Knowledge Core ItemStack
     * @return A clean stream of all the available recipes in the Knowledge Core, no nulls
     */
    public static Stream<Recipe> recipeStreamOf(ItemStack knowledgeCore){
        final ArrayList<Recipe> recipeArrayList = new ArrayList<>();
        for(int i = 0; i < SLOT_NUM; i++)
            recipeArrayList.add(getRecipe(knowledgeCore, i));
        return recipeArrayList.stream().filter(Objects::nonNull);
    }

    public static boolean hasRecipe(ItemStack knowledgeCoreStack, Item result){
        return getRecipeMap(knowledgeCoreStack).keySet().stream()
                .map(ItemStack::getItem)
                .anyMatch(item -> item.equals(result));
    }

    public static boolean hasRecipe(ItemStack knowledgeCoreStack, int slot){
        return getNBTRecipe(knowledgeCoreStack, slot) != null;
    }

    /**
     * @param knowledgeCoreStack the Knowledge Core ItemStack
     * @return True if the Knowledge Core has no recipes stored.
     */
    public static boolean isEmpty(ItemStack knowledgeCoreStack){
        NBTTagCompound nbt = knowledgeCoreStack.getTagCompound();
        if(nbt == null) return true;
        return nbt.isEmpty();
    }

    /**
     *
     * @param knowledgeCoreStack The Knowledge Core ItemStack
     * @param slot The Knowledge Core's recipe slot index
     * @return NBTTagCompound that represents the recipe, or null if no recipe exists in the specified slot
     */
    private static NBTTagCompound getNBTRecipe(ItemStack knowledgeCoreStack, int slot){
        String slotKey = String.valueOf(slot);
        NBTTagCompound nbt = knowledgeCoreStack.getTagCompound();
        if(nbt == null || !nbt.hasKey(slotKey)) return null;
        return nbt.getCompoundTag(slotKey);
    }

    /**
     * Similar to {@link KnowledgeCoreUtil#getAEPattern(Recipe, World)},
     * for when you don't have the actual recipe yet
     * and you want to extract it from a Knowledge Core
     * @param knowledgeCore Knowledge Core to extract from
     * @param slot The Knowledge Core slot to read from
     * @param world The world we'll craft it in
     * @return ICraftingPatternDetails instance to send to AE2.
     */
    public static ICraftingPatternDetails getAEPattern(ItemStack knowledgeCore, int slot, World world){
        Recipe recipe = getRecipe(knowledgeCore, slot);
        return getAEPattern(recipe, world);
    }

    /**
     * Method to extract a ICraftingPatternDetails instance from a Knowledge Core recipe, to use with AE2
     * @param recipe Recipe to extract from
     * @param world The world we'll craft it in
     * @return ICraftingPatternDetails instance to send to AE2.
     * @throws IllegalArgumentException If the recipe slot is empty, it lets AE2 deal with it, which currently means you'll get an exception indirectly
     */
    public static ICraftingPatternDetails getAEPattern(Recipe recipe, World world){
        ItemStack AEPatternStack;
        if(recipe == null) AEPatternStack = ThEApi.instance().items().knowledgeCore().maybeStack(1).orElseThrow(RuntimeException::new);
        else AEPatternStack = recipe.toAEPatternStack();
        return new PatternHelper(AEPatternStack, world);
    }

    public static class Recipe {
        private final ThEInternalInventory ingredients;
        private final ItemStack result;
        private final float visCost;

        public Recipe(ThEInternalInventory ingredients, ItemStack result, float visCost){
            this.ingredients = ingredients;
            this.result = result.copy();
            this.visCost = visCost;
        }

        public ItemStack getResult() {
            return this.result;
        }

        /**
         * @return The recipe ingredients, aspect crystals included as the last 6 elements
         */
        public ThEInternalInventory getIngredients() {
            return this.ingredients;
        }

        /**
         * Get the part of the ingredients that excludes aspects, or the part that only has the aspects
         * @param aspect true to get the aspect part
         * @return the ingredients
         */
        public ThEInternalInventory getIngredientPart(boolean aspect) {
            ThEInternalInventory ingredients;
            ThEInternalInventory ingredientsWithAspect = getIngredients();
            if (aspect){
                ingredients = new ThEInternalInventory("ingredients", 6, 64);
                for (int i = 0; i < 6; i++)
                    ingredients.setInventorySlotContents(i, ingredientsWithAspect.getStackInSlot(i + 9));
            }else {
                ingredients = new ThEInternalInventory("ingredients", 9, 64);
                for (int i = 0; i < 9; i++)
                    ingredients.setInventorySlotContents(i, ingredientsWithAspect.getStackInSlot(i));
            }
            return ingredients;
        }

        public float getVisCost() {
            return this.visCost;
        }

        /**
         * Transforms the recipe to an ItemStack that includes tags like the ones a normal AE2 pattern would have.
         * Mainly for internal use, you're probably looking for {@link KnowledgeCoreUtil#getAEPattern(ItemStack, int, World)}
         * @return AE2 pattern ItemStack
         */
        public ItemStack toAEPatternStack(){
            ItemStack stack = ThEApi.instance().items().knowledgeCore().maybeStack(1).orElseThrow(RuntimeException::new);
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("in", this.getIngredientPart(false).serializeNBT(true));
            //nbt.setTag("aspects", this.getIngredientPart(true).serializeNBT(true));
            NBTTagList out = new NBTTagList();
            out.appendTag(this.getResult().serializeNBT());
            nbt.setTag("out", out);
            nbt.setBoolean("crafting", false);
            nbt.setBoolean("substitute", false);
            stack.setTagCompound(nbt);
            return stack;
        }
    }
}
